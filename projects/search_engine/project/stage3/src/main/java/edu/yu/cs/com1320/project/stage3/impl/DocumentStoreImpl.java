package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage3.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class DocumentStoreImpl implements DocumentStore {

    private HashTableImpl<URI, Document> documentStore;
    private StackImpl<Undoable> commandStack;
    private TrieImpl<Document> trie;

    public DocumentStoreImpl() {
        this.documentStore = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
    }

    /**
     * @param input  the document being put
     * @param uri    unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the String version of the previous doc. If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * */
    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        if (uri == null || format == null) {
            throw new IllegalArgumentException("URI/Format is null");
        }
        if (input != null) {
            try {
                return putDocumentWithInput(input, uri, format);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            return putDocumentWithNullInput(uri);
        }
        return 0;
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document as a PDF, or null if no document exists with that URI
     */
    @Override
    public byte[] getDocumentAsPdf(URI uri) {
        if (uri == null){
            throw new IllegalArgumentException("URI is null");
        }
        if (containsURI(uri)){
            return getDocument(uri).getDocumentAsPdf();
        }
        return null;
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document as TXT, i.e. a String, or null if no document exists with that URI
     */
    @Override
    public String getDocumentAsTxt(URI uri) {
        if (uri == null){
            throw new IllegalArgumentException("URI is null");
        }
        if (containsURI(uri)){
            return getDocument(uri).getDocumentAsTxt();
        }
        return null;
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    @Override
    public boolean deleteDocument(URI uri) {
        if (uri == null){
            throw new IllegalArgumentException("URI is null");
        }
        if (getDocument(uri) != null) {
            Document document = getDocument(uri);
            addToCommandStack(uri, undoFunctionRevertToPreviousState(document));
            documentStore.put(uri, null);
            removeDocumentFromTrie(document);
            return true;
        }
        else {
            return false;
        }
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
            undone = command.undo();
        }
        if (!undone){
            throw new IllegalStateException();
        }
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     *
     * @param uri the URI of the command to undo
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
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


// Added methods for stage3
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE INSENSITIVE.
     * @param keyword .
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<String> search(String keyword) {
        if (keyword == null){
            return new ArrayList<>();
        }
        List<Document> documentList = searchDocuments(keyword);
        List<String> documentSearch = new ArrayList<>();
        if (documentList != null){
            for (Document document: documentList) {
                if (document != null){
                    documentSearch.add(document.getDocumentAsTxt());
                }
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
        if (keyword == null){
            return new ArrayList<>();
        }
        List<Document> documentList = searchDocuments(keyword.toUpperCase());
        List<byte[]> pdfSearch = new ArrayList<>();
        if (documentList != null){
            for (Document document: documentList) {
                if (document != null){
                    pdfSearch.add(document.getDocumentAsPdf());
                }
            }
        }
        return pdfSearch;
    }

    /**
     * Retrieve all documents whose text starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE INSENSITIVE.
     *
     * @param prefix .
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<String> searchByPrefix(String prefix) {
        if (prefix == null){
            return new ArrayList<>();
        }
        List<Document> documentList = documentSearchByPrefix(prefix.toUpperCase());
        List<String> prefixSearch = new ArrayList<>();
        if (documentList != null){
            for (Document document: documentList) {
                if (document != null){
                    String text = document.getDocumentAsTxt();
                    if (!prefixSearch.contains(text)){
                        prefixSearch.add(text);
                    }
                }
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
        if (prefix == null){
            return new ArrayList<>();
        }
        List<Document> documentList = documentSearchByPrefix(prefix.toUpperCase());
        List<byte[]> prefixSearch = new ArrayList<>();
        if (documentList != null){
            for (Document document: documentList) {
                if (document != null){
                    byte[] bytes = document.getDocumentAsPdf();
                    if (!prefixSearch.contains(bytes)){
                        prefixSearch.add(bytes);
                    }
                }
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
        if (key != null){
            return getUriSet(trie.deleteAll(key.toUpperCase()));
        }
        return new HashSet<>();
    }

    /**
     * Delete all matches that contain a String with the given prefix.
     * Search is CASE INSENSITIVE.
     *
     * @param prefix .
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefix(String prefix) {
        if (prefix != null){
            return getUriSet(trie.deleteAllWithPrefix(prefix.toUpperCase()));
        }
        return new HashSet<>();
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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

    private Set<URI> getUriSet(Set<Document> deletedDocuments){
        Set<URI> uriSet = new HashSet<>();
        if (!(deletedDocuments.isEmpty())){
            CommandSet<URI> commandSet = new CommandSet<>();
            for (Document doc: deletedDocuments) {
                if (doc != null) {
                    URI uri = doc.getKey();
                    GenericCommand<URI> genericCommand = new GenericCommand<>(uri, undoFunctionRevertToPreviousState(doc));
                    if (!commandSet.containsTarget(uri)) {
                        commandSet.addCommand(genericCommand);
                    }
                    documentStore.put(uri, null);
                    removeDocumentFromTrie(doc);
                    uriSet.add(uri);
                }
            }
            if (commandSet.isEmpty()){
                commandStack.push(new GenericCommand<>(null, undoFunctionDoNothing()));
            }
            commandStack.push(commandSet);
        }
        else{
            commandStack.push(new GenericCommand<>(null, undoFunctionDoNothing()));
        }
        return uriSet;
    }

    private List<Document> searchDocuments(String keyword){
        keyword = keyword.toUpperCase();
        return trie.getAllSorted(keyword, wordOccurrenceComparator(keyword));
    }

    private List<Document> documentSearchByPrefix(String prefix){
        prefix = prefix.toUpperCase();
        return trie.getAllWithPrefixSorted(prefix, prefixOccurrenceComparator(prefix));
    }

    private Comparator<Document> wordOccurrenceComparator(String word){
        String keyword = word.toUpperCase();
        return new Comparator<Document>() {
            @Override
            public int compare(Document o1, Document o2) {
                return o2.wordCount(keyword) - o1.wordCount(keyword);
            }
        };
    }

    private Comparator<Document> prefixOccurrenceComparator(String keyword){
        String prefix = keyword.toUpperCase();
        return new Comparator<Document>() {
            @Override
            public int compare(Document o1, Document o2) {
                return getPrefixCount(o2, prefix) - getPrefixCount(o1, prefix);
            }
        };
    }

    private int getPrefixCount(Document document, String prefix){
        if (document == null || document.getDocumentAsTxt() == null){
            throw new IllegalArgumentException();
        }
        prefix = prefix.toUpperCase();
        String[] documentWords = stringSplitter(document.getDocumentAsTxt());
        int count = 0;
        for (String documentWord : documentWords) {
            if (documentWord != null) {
                if (documentWord.startsWith(prefix)) {
                    count++;
                }
            }
        }
        return count;
    }

    private void addDocumentToTrie(Document doc){
        if (doc != null){
            String[] words = stringSplitter(doc.getDocumentAsTxt());
            Set<String> documentWords = new HashSet<>();
            for (String word: words) {
                if (word != null){
                    if (!(documentWords.contains(word))) {
                        documentWords.add(word);
                        trie.put(word, doc);
                    }
                }
            }
        }
    }

    private String[] stringSplitter(String text){
        text = text.toUpperCase();
        text = text.replaceAll("[^a-zA-Z0-9\\s]", "");
        return text.split("[\\s]");
    }

    private int putDocumentWithNullInput(URI uri){
        if (containsURI(uri)) {
            DocumentImpl document = (DocumentImpl) getDocument(uri);
            addToCommandStack(uri, undoFunctionRevertToPreviousState(document));
            removeDocumentFromTrie(document);
            return documentStore.put(uri, null).getDocumentTextHashCode();
        }
        addToCommandStack(uri, undoFunctionDoNothing());
        return 0;
    }

    private int putDocumentWithInput(InputStream input, URI uri, DocumentFormat format) throws IOException{
        try {
            DocumentImpl newDoc = convertToDocument(input, uri, format);
            if (containsURI(uri)){
                return overWriteDocument(uri, newDoc);
            }
            else {
                addToCommandStack(uri, undoFunctionDelete());
                return putDocumentInStore(uri, newDoc);
            }
        }catch (IOException e){
            throw new IOException();
        }
    }

    private int overWriteDocument(URI uri, DocumentImpl doc){
        if (doc.getDocumentAsTxt().equals(getDocument(uri).getDocumentAsTxt())){
            addToCommandStack(uri, undoFunctionDoNothing());
            return doc.getDocumentTextHashCode();
        }
        else{
            Document oldDocument = documentStore.get(uri);
            addToCommandStack(uri, undoFunctionRevertToPreviousState(oldDocument));
            removeDocumentFromTrie(oldDocument);
            putDocumentInStore(uri, doc);
            addDocumentToTrie(doc);
            return oldDocument.getDocumentTextHashCode();
        }
    }

    private void removeDocumentFromTrie(Document doc) {
        if (doc != null && doc.getDocumentAsTxt() != null){
            String[] words = stringSplitter(doc.getDocumentAsTxt());
            HashSet<String> totalWords = new HashSet<>(Arrays.asList(words));
            for (String word : totalWords) {
                trie.delete(word, doc);
            }
        }
    }

    private void addToCommandStack(URI uri, Function<URI, Boolean> function){
        if ((uri == null) || (function == null)){
            throw new IllegalArgumentException();
        }
        Undoable command = new GenericCommand<>(uri, function);
        commandStack.push(command);
    }

    private Function<URI, Boolean> undoFunctionDelete(){
        return uri -> {
            if (containsURI(uri)) {
                Document doc = getDocument(uri);
                documentStore.put(uri, null);
                removeDocumentFromTrie(doc);
                return true;
            }
            else {
                return false;
            }
        };
    }

    private Function<URI, Boolean> undoFunctionRevertToPreviousState(Document currentState) {
        return uri -> {
            if (currentState == null){
                return false;
            }
            removeDocumentFromTrie(getDocument(uri));
            documentStore.put(uri, currentState);
            addDocumentToTrie(currentState);
            return documentStore.get(uri) == currentState;
        };
    }

    private Function<URI, Boolean> undoFunctionDoNothing(){
        return uri -> true;
    }

    /**
     * @param uri check if the uri is contained in the hashTable
     * @return if the uri is present in the hashTable, return true. If the hashTable doesn't contain the given uri, return false;
     */
    private boolean containsURI(URI uri){
        if (uri != null){
            return documentStore.get(uri) != null;
        }
        return false;
    }

    private int putDocumentInStore(URI uri, DocumentImpl document) throws NullPointerException {
        try {
            if (document != null && uri != null){
                Document doc = documentStore.put(uri, document);
                addDocumentToTrie(document);
                if (doc != null){
                    return doc.getDocumentTextHashCode();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @ return the Document object stored at that URI, or null if there is no such Document
     */
    protected Document getDocument(URI uri){
        if (uri == null){
            throw new IllegalArgumentException();
        }
        if (containsURI(uri)){
            return documentStore.get(uri);
        }
        return null;
    }

    private DocumentImpl convertToDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
        byte[] bytes = inputStreamToByteArray(input);
        if (format == DocumentFormat.PDF) {
            return bytesToPDFDocument(bytes, uri);
        }
        if (format == DocumentFormat.TXT) {
            return bytesToTXTDocument(bytes, uri);
        }
        throw new IllegalArgumentException();
    }

    /**
     * @param inputStream the given inputStream to read all the bytes into a byteArrayOutputStream
     * @return byte array from the byteArrayOutputStream
     */
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

    /**
     * Load the byte array into a PDDocument (PDF document) and extract the text of the document using a PDFTextStripper. Constructs a new document object and puts it into the DocumentStore with the given uri.
     * @param bytes the bytes array loaded into a new PDDocument.
     * @param uri the uri of the document to be put into the DocumentStore.
     * @return the hashcode of the document text.
     */
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

    /**
     * converts the byte array into a String of the document's text. returns a constructed DocumentImpl object.
     * @param bytes the byte array to be converted into a new String.
     * @param uri the uri of the document.
     * @return the hashcode of the document text.
     */
    private DocumentImpl bytesToTXTDocument(byte[] bytes, URI uri) {
        String text = new String(bytes, StandardCharsets.UTF_8);
        return new DocumentImpl(uri, text, text.hashCode());
    }

    @SuppressWarnings("unchecked")
    private CommandSet<URI> castToCommandSet(Object o){
        return (CommandSet<URI>) o;
    }

    @SuppressWarnings("unchecked")
    private GenericCommand<URI> castToGenericCommand(Object o){
        return (GenericCommand<URI>) o;
    }
}