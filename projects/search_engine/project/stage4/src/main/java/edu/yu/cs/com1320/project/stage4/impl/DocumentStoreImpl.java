package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage4.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class DocumentStoreImpl implements DocumentStore{

    private HashTableImpl<URI, Document> hashTable;
    private StackImpl<Undoable> commandStack;
    private TrieImpl<Document> trie;
    private MinHeapImpl<Document> minHeap;
    private int maxDocumentCount = -1;
    private int maxDocumentBytes = -1;
    private long setTimeInCommandSet = 0;
    private int documentCount = 0;
    private int totalBytes = 0;

    public DocumentStoreImpl() {
        this.hashTable = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.minHeap = new MinHeapImpl<>();
    }

    /**
     * @param input  the document being put
     * @param uri    unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return the hashcode of the previous replaced document or 0.
     */
    @Override
    public int putDocument(InputStream input, URI uri, DocumentStore.DocumentFormat format){
        if (uri == null || format == null){
            throw new IllegalArgumentException();
        }
        boolean containsURI = containsURI(uri);
        Document document = null;
        if (input != null) {
            try {
                document = convertToDocument(input, uri, format);
            } catch (IOException e) {}
        }
        if (containsURI){
            int hash = getDocument(uri).getDocumentTextHashCode();
            if (input== null){
                deleteDocument(uri);
            }
            if (document != null){
                replaceDocument(uri, document);
            }
            return hash;
        }
        else {
            if (document != null){
                documentAdd(uri, document);
            }
            else {
                commandStack.push(new GenericCommand<>(null, undoFunctionDoNothing()));
            }
        }
        return 0;
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return returns the pdf version of the document with the given uri
     */
    @Override
    public byte[] getDocumentAsPdf(URI uri) {
        if (containsURI(uri)){
            return getDocument(uri).getDocumentAsPdf();
        }
        return null;
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return returns the text of the document with the given uri
     */
    @Override
    public String getDocumentAsTxt(URI uri) {
        if (containsURI(uri)){
            return getDocument(uri).getDocumentAsTxt();
        }
        return null;
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document with the given uri was removed, else return false
     */
    @Override
    public boolean deleteDocument(URI uri) {
        if (containsURI(uri)){
            documentDelete(uri);
            return getDocument(uri) == null;
        }
        return false;
    }

    /**
     * undo the last put or delete command
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    @Override
    public void undo() throws IllegalStateException {
        if (commandStack.size() == 0 || commandStack.peek() == null){
            throw new IllegalStateException();
        }
        boolean undone = false;
        if (commandStack.peek() != null){
            Undoable command = commandStack.pop();
            if (command instanceof CommandSet){
                undone = undoCommandSet(castToCommandSet(command));
            }
            else {
                undone = command.undo();
            }
        }
        if (!undone){
            throw new IllegalStateException();
        }
    }

    /**
     * @param uri the uri of the given command to undo
     * @throws IllegalStateException if commandStack is empty or if none of the commands have the given uri
     */
    @Override
    public void undo(URI uri) throws IllegalStateException {
        if (commandStack.size() == 0 || uri == null){
            throw new IllegalStateException();
        }
        try {
            Undoable command = getCommandWithURI(uri);
            boolean undone = undoCommand(command, uri);
            if (!undone){
                throw new IllegalStateException();
            }
        }catch (IllegalStateException e){
            throw new IllegalStateException();
        }
    }

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE INSENSITIVE.
     * @param keyword .
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<String> search(String keyword) {
        List<Document> documentList = searchDocuments(keyword);
        if (documentList == null || documentList.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> documentSearch = new ArrayList<>();
        long time = System.nanoTime();
        for (Document document: documentList) {
            if (document != null){
                documentSearch.add(document.getDocumentAsTxt());
                setLastUseTimeAndReHeapify(document, time);
            }
        }
        return documentSearch;
    }

    /**
     * same logic as search, but returns the docs as PDFs instead of as Strings
     * @param keyword .
     */
    @Override
    public List<byte[]> searchPDFs(String keyword) {
        List<Document> documentList = searchDocuments(keyword);
        if (documentList == null || documentList.isEmpty()){
            return new ArrayList<>();
        }
        List<byte[]> pdfSearch = new ArrayList<>();
        long time = System.nanoTime();
        for (Document document: documentList) {
            if (document != null){
                pdfSearch.add(document.getDocumentAsPdf());
                setLastUseTimeAndReHeapify(document, time);
            }
        }
        return pdfSearch;
    }

    /**
     * Retrieve all documents whose text starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE INSENSITIVE.
     * @param prefix .
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<String> searchByPrefix(String prefix) {
        List<Document> documentList = documentSearchByPrefix(prefix);
        if (documentList == null || documentList.isEmpty()){
            return new ArrayList<>();
        }
        List<String> prefixSearch = new ArrayList<>();
        long time = System.nanoTime();
        for (Document document: documentList) {
            if (document != null){
                prefixSearch.add(document.getDocumentAsTxt());
                setLastUseTimeAndReHeapify(document, time);
            }
        }
        return prefixSearch;
    }

    /**
     * same logic as searchByPrefix, but returns the docs as PDFs instead of as Strings
     * @param prefix .
     */
    @Override
    public List<byte[]> searchPDFsByPrefix(String prefix) {
        List<Document> documentList = documentSearchByPrefix(prefix);
        if (documentList == null || documentList.isEmpty()) {
            return new ArrayList<>();
        }
        List<byte[]> prefixSearch = new ArrayList<>();
        long time = System.nanoTime();
        for (Document document: documentList) {
            if (document != null){
                prefixSearch.add(document.getDocumentAsPdf());
                setLastUseTimeAndReHeapify(document, time);
            }
        }
        return prefixSearch;
    }

    /**
     * delete ALL exact matches for the given key
     * @param key .
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAll(String key) {
        return getUriSet(trie.deleteAll(key));
    }

    /**
     * Delete all matches that contain a String with the given prefix.
     * Search is CASE INSENSITIVE.
     * @param prefix .
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefix(String prefix) {
        return getUriSet(trie.deleteAllWithPrefix(prefix));
    }

    /**
     * @param limit the maximum number of documents allowed in the documentStore's memory
     */
    @Override
    public void setMaxDocumentCount(int limit) {
        if (limit < 0){
            throw new IllegalArgumentException();
        }
        this.maxDocumentCount = limit;
        manageMemory();
    }

    /**
     * @param limit the maximum number of bytes allowed in the documentStore's memory
     */
    @Override
    public void setMaxDocumentBytes(int limit) {
        if (limit < 0){
            throw new IllegalArgumentException();
        }
        this.maxDocumentBytes = limit;
        manageMemory();
    }

    /**
     * @ return the Document object stored at that URI, or null if there is no such Document
     */
    protected Document getDocument(URI uri){
        if (uri == null){
            throw new IllegalArgumentException();
        }
        if (containsURI(uri)){
            return hashTable.get(uri);
        }
        return null;
    }

    /////// Methods that add/replace/remove documents from HashTable /////////

    private boolean containsURI(URI uri){
        if (uri == null){
            throw new IllegalArgumentException("URI is null");
        }
        return hashTable.get(uri) != null;
    }

    private void documentAdd(URI uri, Document document){
        addDocument(document);
        addToCommandStack(uri, undoPutFunction());
    }

    private void documentDelete(URI uri){
        Document document = getDocument(uri);
        removeDocument(uri);
        addToCommandStack(uri, undoDeleteFunction(document));
    }

    private void addDocument(Document document) {
        URI uri = document.getKey();
        hashTable.put(uri, document);
        this.documentCount++;
        this.totalBytes += getDocumentBytes(document);
        addDocumentToTrieAndHeap(document);
        setLastUseTimeAndReHeapify(document, System.nanoTime());
        if (hasMemoryLimit()){
            manageMemory();
        }
    }

    private void removeDocument(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        Document document = getDocument(uri);
        if (document == null){
            throw new IllegalArgumentException();
        }
        hashTable.put(uri, null);
        this.documentCount--;
        this.totalBytes -= getDocumentBytes(document);
        removeDocumentFromTrieAndHeap(document);
    }

    private void removeDocument(Document document){
        if (document != null) {
            URI uri = document.getKey();
            hashTable.put(uri, null);
            this.documentCount--;
            this.totalBytes -= getDocumentBytes(document);
            removeDocumentFromTrieAndHeap(document);
        }
    }

    private void replaceDocument(URI uri, Document document) {
        if (document != null && uri != null){
            Document storedDocument = getDocument(uri);
            if (document.equals(storedDocument)){
                addToCommandStack(uri, undoFunctionDoNothing());
                setLastUseTimeAndReHeapify(document, System.nanoTime());
            }
            else {
                replaceDocument(uri, storedDocument, document);
                addToCommandStack(uri, undoReplaceFunction(storedDocument));
                if (hasMemoryLimit()){
                    manageMemory();
                }
            }
        }
    }

    private void replaceDocument(URI uri, Document oldDocument, Document newDocument){
        hashTable.put(uri, newDocument);
        removeDocumentFromTrieAndHeap(oldDocument);
        this.totalBytes -= getDocumentBytes(oldDocument);
        this.totalBytes += getDocumentBytes(newDocument);
        addDocumentToTrieAndHeap(newDocument);
    }

    private void addDocumentToTrieAndHeap(Document document){
        addDocumentToTrie(document);
        addDocumentToHeap(document);
    }

    ////////// Trie Methods /////////////////

    private void addDocumentToTrie(Document document){
        if (document != null && document.getDocumentAsTxt() != null){
            Set<String> documentWords = new HashSet<>(Arrays.asList(stringSplitter(document.getDocumentAsTxt())));
            for (String word: documentWords) {
                trie.put(word, document);
            }
        }
    }
    private void removeDocumentFromTrie(Document document) {
        if (document != null && document.getDocumentAsTxt() != null) {
            Set<String> totalWords = new HashSet<>(Arrays.asList(stringSplitter(document.getDocumentAsTxt())));
            for (String word : totalWords) {
                trie.delete(word, document);
            }
        }
    }

    private List<Document> searchDocuments(String keyword){
        if (keyword == null){
            return new ArrayList<>();
        }
        return trie.getAllSorted(keyword.toUpperCase(), wordOccurrenceComparator(keyword.toUpperCase()));
    }

    private List<Document> documentSearchByPrefix(String prefix){
        if (prefix == null){
            return new ArrayList<>();
        }
        return trie.getAllWithPrefixSorted(prefix.toUpperCase(), prefixOccurrenceComparator(prefix.toUpperCase()));
    }

    private Comparator<Document> wordOccurrenceComparator(String word){
        return new Comparator<Document>() {
            @Override
            public int compare(Document o1, Document o2) {
                return o2.wordCount(word.toUpperCase()) - o1.wordCount(word.toUpperCase());
            }
        };
    }

    private Comparator<Document> prefixOccurrenceComparator(String prefix){
        return new Comparator<Document>() {
            @Override
            public int compare(Document o1, Document o2) {
                return getPrefixCount(o2, prefix.toUpperCase()) - getPrefixCount(o1, prefix.toUpperCase());
            }
        };
    }

    private int getPrefixCount(Document document, String prefix){
        if (document == null || document.getDocumentAsTxt() == null){
            throw new IllegalArgumentException();
        }
        String[] documentWords = stringSplitter(document.getDocumentAsTxt());
        int count = 0;
        for (String documentWord : documentWords) {
            if (documentWord != null) {
                if (documentWord.startsWith(prefix.toUpperCase())) {
                    count++;
                }
            }
        }
        return count;
    }

    private String[] stringSplitter(String text){
        text = text.toUpperCase();
        text = text.replaceAll("[^a-zA-Z0-9\\s]", "");
        return text.split("[\\s]");
    }

    ///////// Ends of Trie Methods ////////////

    ///////// CommandStack and Undoable Methods //////////////

    private boolean undoCommandSet(CommandSet<URI> command) {
        HashSet<URI> uris = new HashSet<>();
        long time = System.nanoTime();
        Iterator<GenericCommand<URI>> iterator = castToCommandSet(command).iterator();
        while (iterator.hasNext()){
            GenericCommand<URI> genericCommand = iterator.next();
            uris.add(genericCommand.getTarget());
        }
        boolean undone = command.undo();
        if (undone){
            for (URI uri: uris) {
                if (containsURI(uri)){
                    Document document = getDocument(uri);
                    setLastUseTimeAndReHeapify(document, time);
                }
            }
        }
        return undone;
    }

    private Undoable getCommandWithURI(URI uri) throws IllegalStateException{
        if (commandStack.peek() != null){
            Undoable command = commandStack.peek();
            if (commandHasURI(uri, command)){
                return returnCommand(command);
            }
            command = commandStack.pop();
            Undoable c = getCommandWithURI(uri);
            commandStack.push(command);
            return c;

        }
        throw new IllegalStateException();
    }

    private boolean commandHasURI(URI uri, Undoable command) {
        if (command instanceof GenericCommand) {
            GenericCommand<URI> command1 = castToGenericCommand(command);
            return command1.getTarget() == uri;
        }
        if (command instanceof CommandSet) {
            CommandSet<URI> command1 = castToCommandSet(command);
            return command1.containsTarget(uri);
        }
        return false;
    }

    private Undoable returnCommand(Undoable command){
        if (command instanceof GenericCommand) {
            if (command == commandStack.peek()) {
                return commandStack.pop();
            }
        }
        if (command instanceof CommandSet) {
            CommandSet<URI> command1 = castToCommandSet(command);
            if (command1 == commandStack.peek()){
                if (command1.size() == 1){
                    return commandStack.pop();
                }
            }
            return command;
        }
        throw new IllegalStateException();
    }

    private boolean undoCommand(Undoable command, URI uri) throws IllegalStateException{
        if (command == null){
            throw new IllegalStateException();
        }
        if (command instanceof CommandSet){
            CommandSet<URI> commandSet = castToCommandSet(command);
            return commandSet.undo(uri);
        }
        if (command instanceof GenericCommand){
            GenericCommand<URI> genericCommand = castToGenericCommand(command);
            return genericCommand.undo();
        }
        throw new IllegalStateException();
    }

    private Undoable removeCommandsWithURI(URI uri){
        if (commandStack.peek() != null){
            Undoable command = commandStack.peek();
            if (commandHasURI(uri, command)){
                if (command instanceof GenericCommand){
                    commandStack.pop();
                    return null;
                }
                if (command instanceof CommandSet){
                    removeCommandFromCommandSet(uri, castToCommandSet(command));
                    if (castToCommandSet(command).containsTarget(uri)){
                        throw new IllegalStateException();
                    }
                }
            }
            command = commandStack.pop();
            Undoable c = getCommandWithURI(uri);
            commandStack.push(command);
            return c;

        }
        throw new IllegalStateException();
    }

    private void removeCommandFromCommandSet(URI target, CommandSet<URI> commandSet){
        if (commandSet.containsTarget(target)) {
            Iterator<GenericCommand<URI>> iterator = commandSet.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getTarget() == target) {
                    iterator.remove();
                    break;
                }
            }
        }
        if (commandSet.containsTarget(target)) {
            throw new IllegalStateException();
        }
    }

    @SuppressWarnings("unchecked")
    private CommandSet<URI> castToCommandSet(Object o){
        return (CommandSet<URI>) o;
    }

    @SuppressWarnings("unchecked")
    private GenericCommand<URI> castToGenericCommand(Object o){
        return (GenericCommand<URI>) o;
    }

    private Set<URI> getUriSet(Set<Document> deletedDocuments){
        if (deletedDocuments == null || deletedDocuments.isEmpty()){
            commandStack.push(new GenericCommand<>(null, undoFunctionDoNothing()));
            return new HashSet<>();
        }
        Set<URI> uriSet = new HashSet<>();
        CommandSet<URI> commandSet = new CommandSet<>();
        this.setTimeInCommandSet = System.nanoTime();
        for (Document document: deletedDocuments) {
            if (document != null) {
                URI uri = document.getKey();
                commandSet.addCommand(new GenericCommand<>(uri, undoCommandsFunction(document)));
                removeDocument(document);
                uriSet.add(uri);
            }
        }
        this.setTimeInCommandSet = 0;
        if (commandSet.isEmpty()){
            commandSet.addCommand(new GenericCommand<>(null, undoFunctionDoNothing()));
        }
        commandStack.push(commandSet);
        return uriSet;
    }

    private void addToCommandStack(URI uri, Function<URI, Boolean> function){
        if ((uri == null) || (function == null)){
            throw new IllegalArgumentException();
        }
        Undoable command = new GenericCommand<>(uri, function);
        commandStack.push(command);
    }

    private Function<URI, Boolean> undoPutFunction(){
        return uri -> {
            if (containsURI(uri)) {
                removeDocument(uri);
                return getDocument(uri) == null;
            }
            return false;
        };
    }

    private Function<URI, Boolean> undoDeleteFunction(Document savedState) {
        return uri -> {
            if (savedState == null){
                return false;
            }
            addDocument(savedState);
            return hashTable.get(uri) == savedState;
        };
    }

    private Function<URI, Boolean> undoReplaceFunction(Document currentState) {
        return uri -> {
            if (currentState == null){
                return false;
            }
            replaceDocument(uri, getDocument(uri), currentState);
            return hashTable.get(uri) == currentState;
        };
    }

    private Function<URI, Boolean> undoCommandsFunction(Document document){
        return uri -> {
            if (document == null){
                return false;
            }
            addDocument(document);
            document.setLastUseTime(this.setTimeInCommandSet);
            return hashTable.get(uri) == document;
        };
    }

    private Function<URI, Boolean> undoFunctionDoNothing(){
        return uri -> true;
    }

    /////////// End of CommandStack and UndoFunction Methods /////////

    /////////// MemoryManaging and MinHeap Methods  ///////////////

    private void addDocumentToHeap(Document document){
        if (document != null) {
            document.setLastUseTime(System.nanoTime());
            minHeap.insert(document);
        }
    }

    private void removeDocumentFromHeap(Document document) throws IllegalArgumentException{
        document.setLastUseTime(Long.MIN_VALUE);
        try {
            minHeap.reHeapify(document);
        } catch (NoSuchElementException e){
            throw new IllegalArgumentException();
        }
        Document doc1 = (Document) minHeap.removeMin();
        if((document.getKey() != doc1.getKey())){
            throw new IllegalArgumentException();
        }
    }

    private void setLastUseTimeAndReHeapify(Document document, long time){
        document.setLastUseTime(time);
        minHeap.reHeapify(document);
    }

    private boolean hasDocumentLimit(){
        return this.maxDocumentCount >= 0;
    }

    private boolean hasBytesLimit(){
        return this.maxDocumentBytes >= 0;
    }

    private boolean hasMemoryLimit(){
        return hasDocumentLimit() || hasBytesLimit();
    }

    private int getDocumentBytes(Document document) {
        if (document != null) {
            return document.getDocumentAsTxt().getBytes().length + document.getDocumentAsPdf().length;
        }
        return 0;
    }

    private void manageMemory() {
        if (!hasMemoryLimit()){
            return;
        }
        if (hasBytesLimit() && hasDocumentLimit()) {
            manageMemoryLimit();
        }
        else {
            if (hasDocumentLimit()) {
                manageDocumentLimit();
            }

            if (hasBytesLimit()){
                manageBytesLimit();
            }
        }
    }

    private void manageMemoryLimit() {
        while ((documentCount > maxDocumentCount) || (totalBytes > maxDocumentBytes)){
            Document document = (Document) minHeap.removeMin();
            if (document == null){
                throw new IllegalArgumentException();
            }
            eraseExistenceOfDocument(document.getKey());
        }
    }

    private void manageDocumentLimit() {
        while (documentCount > maxDocumentCount){
            Document document = (Document) minHeap.removeMin();
            eraseExistenceOfDocument(document.getKey());
        }
    }

    private void manageBytesLimit() {
        while (totalBytes > maxDocumentBytes){
            Document document = (Document) minHeap.removeMin();
            eraseExistenceOfDocument(document.getKey());
        }
    }

    private void eraseExistenceOfDocument(URI uri) {
        try {
            removeDocument(uri);
            removeCommandsWithURI(uri);
        } catch (NoSuchElementException e){
            e.printStackTrace();
        } catch (Exception e){

        }
    }

    private void removeDocumentFromTrieAndHeap(Document document){
        removeDocumentFromTrie(document);
        removeDocumentFromHeap(document);
    }

    /////////// Ends of MinHeap Methods ///////////////

    ////////// Document Utility Methods //////////////

    private Document convertToDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
        byte[] bytes = inputStreamToByteArray(input);

        if (format == DocumentFormat.TXT) {
            return bytesToTXTDocument(bytes, uri);
        }
        if (format == DocumentFormat.PDF) {
            return bytesToPDFDocument(bytes, uri);
        }
        throw new IllegalArgumentException();
    }

    private byte[] inputStreamToByteArray(InputStream inputStream) throws IOException{
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            for (int i = inputStream.read(); i != -1; i = inputStream.read()) {
                byteArrayOutputStream.write(i);
            }
            byte[] bytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return bytes;
        }catch (IOException e){
            throw new IOException();
        }
    }

    private DocumentImpl bytesToTXTDocument(byte[] bytes, URI uri) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        return new DocumentImpl(uri, text, text.hashCode());
    }

    private DocumentImpl bytesToPDFDocument(byte[] bytes, URI uri) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(bytes);
        try (PDDocument pdf = PDDocument.load(inputStream)) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            String text = pdfTextStripper.getText(pdf);
            String newText = text.trim();
            int hashCodeText = newText.hashCode();
            return new DocumentImpl(uri, newText, hashCodeText, bytes);
        } catch (IOException e) {
            throw new IOException();
        }
    }

    /////////// End of Document Utility Methods///////////////
}