package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage5.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class DocumentStoreImpl implements DocumentStore {

    private BTreeImpl<URI, Document> bTree;
    private StackImpl<Undoable> commandStack;
    private TrieImpl<URI> trie;
    private MinHeapImpl<LastUsedTime> minHeap;
    private DocumentPersistenceManager documentPersistenceManager;
    private HashMap<URI, LastUsedTime> lastUsedTimes = new HashMap<>();
    private int maxDocumentCount = -1;
    private int maxDocumentBytes = -1;
    private int documentCount = 0;
    private int totalBytes = 0;
    private long setTimeInCommandSet;

    private Set<URI> docsInTrie = new HashSet<>();
    private Set<URI> docsInHeap = new HashSet<>();
    private Set<URI> docsOnDisk = new HashSet<>();


    public DocumentStoreImpl() {
        this.documentPersistenceManager = new DocumentPersistenceManager(null);
        this.bTree = new BTreeImpl<>();
        this.bTree.setPersistenceManager(this.documentPersistenceManager);
        this.commandStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.minHeap = new MinHeapImpl<>();
        setSentinel();
    }

    public DocumentStoreImpl(File baseDir) {
        this.documentPersistenceManager = new DocumentPersistenceManager(baseDir);
        this.bTree = new BTreeImpl<>();
        this.bTree.setPersistenceManager(this.documentPersistenceManager);
        this.commandStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.minHeap = new MinHeapImpl<>();
        setSentinel();
    }

    ////// DocumentStore Public Methods ///////////

    /**
     * @param input  the document being put
     * @param uri    unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return the hashcode of the previous replaced document or 0.
     */
    @Override
    public int putDocument(InputStream input, URI uri, DocumentStore.DocumentFormat format) {
        if (uri == null || format == null) {
            throw new IllegalArgumentException();
        }
        boolean containsDocumentWithURI = containsDocument(uri);
        Document document = documentConversion(input, uri, format);
        if (containsDocumentWithURI) {
            return putHasURI(uri, document, input);
        }
        else {
            putNewURI(uri, document);
        }
        return 0;
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return returns the pdf version of the document with the given uri
     */
    @Override
    public byte[] getDocumentAsPdf(URI uri) {
        isURINull(uri);
        Document document = retrieveDocument(uri);
        if (document != null) {
            return document.getDocumentAsPdf();
        }
        return null;
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return returns the text of the document with the given uri
     */
    @Override
    public String getDocumentAsTxt(URI uri) {
        isURINull(uri);
        Document document = retrieveDocument(uri);
        if (document != null) {
            return document.getDocumentAsTxt();
        }
        return null;
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document with the given uri was removed, else return false
     */
    @Override
    public boolean deleteDocument(URI uri) {
        isURINull(uri);
        Document document = retrieveDocument(uri);
        if (document != null) {
            documentDelete(uri);
            return peekDocument(uri) == null;
        }
        commandStack.push(new GenericCommand<>(null, undoFunctionDoNothing()));
        return false;
    }

    /**
     * undo the last put or delete command
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    @Override
    public void undo() throws IllegalStateException {
        if (commandStack.size() == 0 || commandStack.peek() == null) {
            throw new IllegalStateException();
        }
        boolean undone = false;
        if (commandStack.peek() != null) {
            Undoable command = commandStack.pop();
            if (command instanceof CommandSet) {
                undone = undoCommandSet(castToCommandSet(command));
            }
            else {
                undone = command.undo();
            }
        }
        if (!undone) {
            throw new IllegalStateException();
        }
    }

    /**
     * @param uri the uri of the given command to undo
     * @throws IllegalStateException if commandStack is empty or if none of the commands have the given uri
     */
    @Override
    public void undo(URI uri) throws IllegalStateException {
        if (commandStack.size() == 0 || uri == null) {
            throw new IllegalStateException();
        }
        try {
            Undoable command = getCommandWithURI(uri);
            boolean bool =  undoCommand(command, uri);
            if (!bool) {
                throw new IllegalStateException();
            }
        } catch (IllegalStateException e) {
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
        List<URI> documentList = searchDocuments(keyword);
        if (documentList == null || documentList.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> documentSearch = new ArrayList<>();
        long time = System.nanoTime();
        for (URI uri: documentList) {
            Document document = retrieveDocument(uri);
            if (document != null) {
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
        List<URI> documentList = searchDocuments(keyword);
        if (documentList == null || documentList.isEmpty()) {
            return new ArrayList<>();
        }
        List<byte[]> pdfSearch = new ArrayList<>();
        long time = System.nanoTime();
        for (URI uri: documentList) {
            Document document = retrieveDocument(uri);
            if (document != null) {
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
        List<URI> documentList = documentSearchByPrefix(prefix);
        if (documentList == null || documentList.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> prefixSearch = new ArrayList<>();
        long time = System.nanoTime();
        for (URI uri: documentList) {
            Document document = retrieveDocument(uri);
            if (document != null) {
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
        List<URI> documentList = documentSearchByPrefix(prefix);
        if (documentList == null || documentList.isEmpty()) {
            return new ArrayList<>();
        }
        List<byte[]> prefixSearch = new ArrayList<>();
        long time = System.nanoTime();
        for (URI uri: documentList) {
            Document document = retrieveDocument(uri);
            if (document != null) {
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
        if (limit < 0) {
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
        if (limit < 0) {
            throw new IllegalArgumentException();
        }
        this.maxDocumentBytes = limit;
        manageMemory();
    }

    //////// End of Document Store Public Methods ///////////

    //////// Methods that given a uri locate a document whether in memory or on the disk ///////

    private void isURINull(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("IllegalArgument uri is null");
        }
    }

    /**
     * Returns the document with the given URI if the document is in memory i.e it is in the BTree/Heap
     * @ return the Document object stored at that URI in the BTree, or null if there is no such Document in BTree. This method indicates if the document is currently in memory,
     */
    protected Document getDocument(URI uri) {
        isURINull(uri);
        return bTree.get(uri);
    }

    /**
     * @ return the Document object stored at that URI either in the bTree or on the disk without updating its last used time, or null if there is no such Document.
     * If document is saved to disk, this method deserializes it without removing it from the disk or modifying any of the other data structures. otherwise it will return null.
     */
    protected Document peekDocument(URI uri) {
        isURINull(uri);
        Document document = getDocument(uri);
        if (document != null) {
            return document;
        }
        try {
            document = documentPersistenceManager.readOffDisk(uri);
            addDocumentToTrie(document);
            return document;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * @ return the Document object stored at that URI.
     * If it is in memory i.e. it is in the BTree: update its last used time and manage memory
     * If it is in not in memory i.e. saved on the disk: bring it back into memory update its last used time and manage memory.
     */
    private Document retrieveDocument(URI uri) {
        isURINull(uri);
        Document document = getDocument(uri);
        if (document != null) {
            setLastUseTimeAndReHeapify(document, System.nanoTime());
        }
        else {
            try {
                document = documentPersistenceManager.deserialize(uri);
                if (document != null) {
                    this.docsOnDisk.remove(uri);

                    bTree.put(uri, document);
                    this.documentCount++;
                    this.totalBytes += getDocumentBytes(document);
                    addDocumentToTrieAndHeap(document);
                    setLastUseTimeAndReHeapify(document, System.nanoTime());
                }
            } catch (IOException e) {
                return null;
            }
        }
        manageMemory();
        return document;
    }

    private boolean containsDocument(URI uri) {
        isURINull(uri);
        if (getDocument(uri) != null) {
            return true;
        }
        else {
            try {
                return documentPersistenceManager.readOffDisk(uri) != null;
            } catch (IOException e) {
                return false;
            }
        }
    }

    /////// Methods that add/replace/remove documents from BTree /////////

    private Document documentConversion(InputStream input, URI uri, DocumentStore.DocumentFormat format) {
        if (input != null) {
            try {
                return convertToDocument(input, uri, format);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    private void putNewURI(URI uri, Document document) {
        // putting a new URI with not null value
        if (document != null) {
            documentAdd(uri, document);
            return;
        }
        // putting a new URI with null value does nothing
        commandStack.push(new GenericCommand<>(null, undoFunctionDoNothing()));
    }

    private int putHasURI(URI uri, Document document, InputStream input) {
        Document document1 = retrieveDocument(uri);
        if (document1 != null) {
            int hash = document1.getDocumentTextHashCode();
            if (input == null) {
                deleteDocument(uri);
            }
            else if (document != null) {
                replaceDocument(uri, document);
            }
            return hash;
        }
        return 0;
    }

    private void documentAdd(URI uri, Document document) {
        addDocument(document);
        addToCommandStack(uri, undoPutFunction(document));
    }

    private void documentDelete(URI uri) {
        Document document = retrieveDocument(uri);
        if (document != null) {
            removeDocument(document);
            addToCommandStack(uri, undoDeleteFunction(document));
        }
    }

    private void addDocument(Document document) {
        URI uri = document.getKey();
        this.documentCount++;
        this.totalBytes += getDocumentBytes(document);
        bTree.put(uri, document);
        addDocumentToTrieAndHeap(document);
        if (hasMemoryLimit()) {
            manageMemory();
        }
    }

    private void removeDocument(Document document) {
        if (document != null) {
            URI uri = document.getKey();
            if (retrieveDocument(uri) != null) {
                bTree.put(uri, null);
                this.documentCount--;
                this.totalBytes -= getDocumentBytes(document);
                removeDocumentFromTrieAndHeap(document);
            }
            else if (document.equals(removeFromDisk(uri))) {
                this.docsOnDisk.remove(uri);
            }
        }
    }

    private Document removeFromDisk(URI uri) {
        isURINull(uri);
        try {
            return documentPersistenceManager.deserialize(uri);
        } catch (IOException e) {}
        return null;
    }

    private void replaceDocument(URI uri, Document document) {
        isURINull(uri);
        if (document != null) {
            Document storedDocument = retrieveDocument(uri);
            if (storedDocument != null) {
                if (document.equals(storedDocument)) {
                    addToCommandStack(uri, undoFunctionDoNothing());
                    setLastUseTimeAndReHeapify(document, System.nanoTime());
                }
                else {
                    removeDocumentFromTrie(storedDocument);
                    this.totalBytes -= getDocumentBytes(storedDocument);
                    bTree.put(uri, document);
                    this.totalBytes += getDocumentBytes(document);
                    addDocumentToTrie(document);
                    setLastUseTimeAndReHeapify(document, System.nanoTime());
                    addToCommandStack(uri, undoReplaceFunction(storedDocument));
                    if (hasMemoryLimit()) {
                        manageMemory();
                    }
                }
            }
        }
    }

    // When a document is added to the DocumentStore add to Trie and Heap
    private void addDocumentToTrieAndHeap(Document document) {
        addDocumentToTrie(document);
        addDocumentToHeap(document);
    }

    private void addDocumentToTrie(Document document) {
        if (document != null) {
            URI uri = document.getKey();
            if (containsDocument(uri)) {

                this.docsInTrie.add(uri);

                Set<String> documentWords = new HashSet<>(Arrays.asList(stringSplitter(document.getDocumentAsTxt())));
                for (String word : documentWords) {
                    trie.put(word, uri);
                }
            }
        }
    }

    private void addDocumentToHeap(Document document) {
        if (document != null) {
            URI uri = document.getKey();
            long time = System.nanoTime();
            if (!this.lastUsedTimes.containsKey(uri)) {
                this.lastUsedTimes.put(uri, new LastUsedTime(uri, time));
            }
            LastUsedTime lastUsedTime = this.lastUsedTimes.get(uri);
            document.setLastUseTime(time);
            lastUsedTime.setTime(time);

            this.docsInHeap.add(uri);

            minHeap.insert(lastUsedTime);
            manageMemory();
        }
    }

    // When the document is being deleted
    private void removeDocumentFromTrieAndHeap(Document document) {
        removeDocumentFromTrie(document);
        removeDocumentFromHeap(document);
    }

    // When the document is being overwritten
    private void removeDocumentFromTrie(Document document) {
        if (document != null) {
            URI uri = document.getKey();

            this.docsInTrie.remove(uri);

            Set<String> documentWords = new HashSet<>(Arrays.asList(stringSplitter(document.getDocumentAsTxt())));
            for (String word: documentWords) {
                trie.delete(word, uri);
            }
        }
    }

    // When a document is deleted or moved to disk
    private void removeDocumentFromHeap(Document document) {
        if (document != null) {
            URI uri = document.getKey();
            this.docsInHeap.remove(uri);
            if (bTree.get(uri) != null) {
                long time = Long.MIN_VALUE;
                LastUsedTime lastUsedTime = this.lastUsedTimes.get(uri);
                document.setLastUseTime(time);
                lastUsedTime.setTime(time);
                minHeap.reHeapify(lastUsedTime);
                LastUsedTime lut = minHeap.removeMin();
                if (lut.getURI() != uri) {
                    throw new IllegalStateException();
                }
            }
        }
    }

    // Updates the document's last used time and
    private void setLastUseTimeAndReHeapify(Document document, long time) {
        if (document != null && containsDocument(document.getKey())) {
            URI uri = document.getKey();
            if (!this.lastUsedTimes.containsKey(uri)) {
                this.lastUsedTimes.put(uri, new LastUsedTime(uri, time));
            }
            LastUsedTime lastUsedTime = this.lastUsedTimes.get(uri);
            document.setLastUseTime(time);
            lastUsedTime.setTime(time);
            minHeap.reHeapify(lastUsedTime);
            if (hasMemoryLimit()) {
                manageMemory();
            }
        }
    }

    ////////// Trie Methods /////////////////

    private List<URI> searchDocuments(String keyword) {
        if (keyword == null) {
            return new ArrayList<>();
        }
        return trie.getAllSorted(keyword.toUpperCase(), wordOccurrenceComparator(keyword.toUpperCase()));
    }

    private List<URI> documentSearchByPrefix(String prefix) {
        if (prefix == null) {
            return new ArrayList<>();
        }
        return trie.getAllWithPrefixSorted(prefix.toUpperCase(), prefixOccurrenceComparator(prefix.toUpperCase()));
    }

    private Comparator<URI> wordOccurrenceComparator(String word) {
        return new Comparator<URI>() {
            @Override
            public int compare(URI uri1, URI uri2) {
                Document document1 = retrieveDocument(uri1);
                Document document2 = retrieveDocument(uri2);
                if (document1 != null && document2 != null) {
                    return document2.wordCount(word.toUpperCase()) - document1.wordCount(word.toUpperCase());
                }
                throw new IllegalArgumentException();
            }
        };
    }

    private Comparator<URI> prefixOccurrenceComparator(String prefix) {
        return new Comparator<URI>() {
            @Override
            public int compare(URI uri1, URI uri2) {
                return getPrefixCount(uri2, prefix.toUpperCase()) - getPrefixCount(uri1, prefix.toUpperCase());
            }
        };
    }

    private int getPrefixCount(URI uri, String prefix) {
        isURINull(uri);
        Document document = retrieveDocument(uri);
        if (document == null) {
            throw new IllegalArgumentException();
        }
        String[] documentWords = stringSplitter(document.getDocumentAsTxt());
        int count = 0;
        for (String word : documentWords) {
            if (word != null) {
                if (word.startsWith(prefix.toUpperCase())) {
                    count++;
                }
            }
        }
        return count;
    }

    private String[] stringSplitter(String text) {
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
        while (iterator.hasNext()) {
            GenericCommand<URI> genericCommand = iterator.next();
            uris.add(genericCommand.getTarget());
        }
        boolean undone = command.undo();
        if (undone) {
            for (URI uri: uris) {
                Document document = retrieveDocument(uri);
                if (document != null) {
                    setLastUseTimeAndReHeapify(document, time);
                }
            }
        }
        return undone;
    }

    private Undoable getCommandWithURI(URI uri) throws IllegalStateException {
        if (commandStack.peek() != null) {
            Undoable command = commandStack.peek();
            if (commandHasURI(uri, command)) {
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

    private Undoable returnCommand(Undoable command) {
        if (command instanceof GenericCommand) {
            if (command == commandStack.peek()) {
                return commandStack.pop();
            }
        }
        if (command instanceof CommandSet) {
            CommandSet<URI> command1 = castToCommandSet(command);
            if (command1 == commandStack.peek()) {
                if (command1.size() == 1) {
                    return commandStack.pop();
                }
            }
            return command;
        }
        throw new IllegalStateException();
    }

    private boolean undoCommand(Undoable command, URI uri) throws IllegalStateException {
        if (command == null) {
            throw new IllegalStateException();
        }
        if (command instanceof CommandSet) {
            CommandSet<URI> commandSet = castToCommandSet(command);
            return commandSet.undo(uri);
        }
        else if (command instanceof GenericCommand) {
            GenericCommand<URI> genericCommand = castToGenericCommand(command);
            return genericCommand.undo();
        }
        else {
            throw new IllegalStateException();
        }
    }

    @SuppressWarnings("unchecked")
    private CommandSet<URI> castToCommandSet(Object o) {
        return (CommandSet<URI>) o;
    }

    @SuppressWarnings("unchecked")
    private GenericCommand<URI> castToGenericCommand(Object o) {
        return (GenericCommand<URI>) o;
    }

    private Set<URI> getUriSet(Set<URI> deletedDocuments) {
        if (deletedDocuments == null || deletedDocuments.isEmpty()) {
            commandStack.push(new GenericCommand<>(null, undoFunctionDoNothing()));
            return new HashSet<>();
        }
        CommandSet<URI> commandSet = new CommandSet<>();
        this.setTimeInCommandSet = System.nanoTime();
        touchDocuments(deletedDocuments);
        for (URI uri : deletedDocuments) {
            Document document = retrieveDocument(uri);
            if (document != null) {
                removeDocument(document);
                commandSet.addCommand(new GenericCommand<>(uri, undoCommandsFunction(document)));
            }
        }
        if (commandSet.isEmpty()) {
            commandSet.addCommand(new GenericCommand<>(null, undoFunctionDoNothing()));
        }
        commandStack.push(commandSet);
        this.setTimeInCommandSet = 0;
        return deletedDocuments;
    }

    private void touchDocuments(Set<URI> deletedDocuments) {
        for (URI uri : deletedDocuments) {
            retrieveDocument(uri);
        }
    }

    private void addToCommandStack(URI uri, Function<URI, Boolean> function) {
        if ((uri == null) || (function == null)) {
            throw new IllegalArgumentException();
        }
        Undoable command = new GenericCommand<>(uri, function);
        commandStack.push(command);
    }

    private Function<URI, Boolean> undoPutFunction(Document document) {
        return uri -> {
            removeDocument(document);
            return peekDocument(uri) == null;
        };
    }

    private Function<URI, Boolean> undoDeleteFunction(Document savedState) {
        return uri -> {
            if (savedState == null) {
                return false;
            }
            addDocument(savedState);
            return getDocument(uri) == savedState;
        };
    }

    private Function<URI, Boolean> undoReplaceFunction(Document currentState) {
        return uri -> {
            if (currentState == null) {
                return false;
            }
            replaceDocument(uri, currentState);
            return bTree.get(uri) == currentState;
        };
    }

    private Function<URI, Boolean> undoCommandsFunction(Document document) {
        return uri -> {
            if (document == null) {
                return false;
            }
            addDocument(document);
            setLastUseTimeAndReHeapify(document, System.nanoTime());
            return peekDocument(uri) == document;
        };
    }

    private Function<URI, Boolean> undoFunctionDoNothing() {
        return uri -> true;
    }

    /////////// End of CommandStack and UndoFunction Methods /////////

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

    private byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            for (int i = inputStream.read(); i != -1; i = inputStream.read()) {
                byteArrayOutputStream.write(i);
            }
            byte[] bytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return bytes;
        }catch (IOException e) {
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

    /////////// MemoryManaging and MinHeap Methods  ///////////////

    private void setSentinel() {
        try{
            URI sentinel = new URI("");
            bTree.put(sentinel, null);
        }catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private class LastUsedTime implements Comparable<LastUsedTime> {
        private URI uri;
        private long time;

        private LastUsedTime(URI uri, long time) {
            this.uri = uri;
            this.time = time;
        }

        private URI getURI() {
            return this.uri;
        }

        private long getTime() {
            return time;
        }

        private void setTime(long time) {
            this.time = time;
            if (containsDocument(this.uri)) {
                bTree.get(uri).setLastUseTime(time);
            }
        }

        @Override
        public int compareTo(LastUsedTime uriLastUsedTime1) {
            long time1 = this.getTime();
            long time2 = uriLastUsedTime1.getTime();
            if (time1 == time2) {
                return 0;
            }
            if (time1 > time2) {
                return 1;
            }
            else{
                return -1;
            }
        }
    }

    private boolean hasDocumentLimit() {
        return this.maxDocumentCount >= 0;
    }

    private boolean hasBytesLimit() {
        return this.maxDocumentBytes >= 0;
    }

    private boolean hasMemoryLimit() {
        return hasDocumentLimit() || hasBytesLimit();
    }

    private int getDocumentBytes(Document document) {
        if (document != null) {
            int textBytes = document.getDocumentAsTxt().getBytes().length;
            int pdfBytes = document.getDocumentAsPdf().length;
            return textBytes + pdfBytes;
        }
        return 0;
    }

    private void manageMemory() {
        if (!hasMemoryLimit()) {
            return;
        }
        if (hasBytesLimit() && hasDocumentLimit()) {
            manageMemoryLimit();

        }
        else {
            if (hasDocumentLimit()) {
                manageDocumentLimit();
            }
            else if (hasBytesLimit()) {
                manageBytesLimit();
            }
        }
    }

    private void manageMemoryLimit() {
        while ((documentCount > maxDocumentCount) || (totalBytes > maxDocumentBytes)) {
            moveToDisk();
        }
    }

    private void manageDocumentLimit() {
        while (documentCount > maxDocumentCount) {
            moveToDisk();
        }
    }

    private void manageBytesLimit() {
        while (totalBytes > maxDocumentBytes) {
            moveToDisk();
        }
    }

    private void moveToDisk() {
        LastUsedTime uto = minHeap.removeMin();
        if (uto != null) {
            URI uri = uto.getURI();

            this.docsInHeap.remove(uri);

            Document document = getDocument(uri);
            if (document != null) {
                try {
                    this.documentCount--;
                    this.totalBytes -= getDocumentBytes(document);
                    bTree.moveToDisk(uri);

                    this.docsOnDisk.add(uri);

                } catch (Exception e) {
                    throw new IllegalArgumentException("error moving to disk");
                }
            }
        }
    }

    protected void persistenceManagerCleanUp() {
        if (this.documentPersistenceManager != null) {
            if (hasMemoryLimit()) {
                this.documentPersistenceManager.cleanUp();
            }
        }
    }

    protected void getStoreInformation() {
        String documentLimit = Integer.toString(this.maxDocumentCount);
        if (this.maxDocumentCount < 0) {
            documentLimit = "No Limit";
        }
        String bytesLimit = Integer.toString(this.maxDocumentBytes);
        if (this.maxDocumentBytes < 0) {
            bytesLimit = "No Limit";
        }
        System.out.println(" ");
        System.out.println("Document Store Info: ");
        System.out.println(" ");
        System.out.println("All the Documents In Document Store: " + this.docsInTrie);
        System.out.println("Limit of how many Documents are allowed in memory: " + documentLimit);
        System.out.println("# of Documents in Memory: " + this.documentCount);
        System.out.println("Limit of how many Bytes are allowed in memory: " + bytesLimit);
        System.out.println("# of Total Bytes in memory: " + this.totalBytes);
        System.out.println("Documents in Memory: " + this.docsInHeap);
        System.out.println("Docs on Disk: " + this.docsOnDisk);
        System.out.println(" ");

    }

    /////////// Ends of MinHeap Methods ///////////////
}