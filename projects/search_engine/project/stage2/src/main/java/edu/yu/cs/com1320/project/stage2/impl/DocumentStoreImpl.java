package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage2.*;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class DocumentStoreImpl implements DocumentStore {

    private HashTableImpl<URI, DocumentImpl> documentStore;
    private StackImpl<Command> commandStack;

    public DocumentStoreImpl() {
        this.documentStore = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
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
            addToCommandStack(uri, undoFunctionRevertToPreviousState((DocumentImpl) getDocument(uri)));
            documentStore.put(uri, null);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * undo the last put or delete command
     *
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    @Override
    public void undo() throws IllegalStateException {
        if (commandStack.size() == 0){
            throw new IllegalStateException();
        }
        if (commandStack.peek() != null){
            Command command = commandStack.pop();
            boolean undone = command.undo();
            if (!undone){
                throw new IllegalArgumentException();
            }
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
            Command command = getCommandWithURI(uri);
            boolean undone = command.undo();
            if (!undone){
                throw new IllegalArgumentException();
            }
        }catch (IllegalStateException e){
            throw new IllegalStateException();
        }
    }

    private int putDocumentWithNullInput(URI uri){
        if (containsURI(uri)) {
            DocumentImpl document = (DocumentImpl) getDocument(uri);
            addToCommandStack(uri, undoFunctionRevertToPreviousState(document));
            return documentStore.put(uri, null).getDocumentTextHashCode();
        }
        else{
            addToCommandStack(uri, undoFunctionDoNothing());
            documentStore.put(uri, null);
            return 0;
        }
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
            DocumentImpl oldDocument = documentStore.get(uri);
            addToCommandStack(uri, undoFunctionRevertToPreviousState(oldDocument));
            putDocumentInStore(uri, doc);
            return oldDocument.getDocumentTextHashCode();
        }
    }

    private void addToCommandStack(URI uri, Function<URI, Boolean> function){
        if ((uri == null) || (function == null)){
            throw new IllegalArgumentException();
        }
        Command command = new Command(uri, function);
        commandStack.push(command);
    }

    private Function<URI, Boolean> undoFunctionDelete(){
        return uri -> {
            if (containsURI(uri)) {
                documentStore.put(uri, null);
                return true;
            }
            else {
                return false;
            }
        };
    }

    private Function<URI, Boolean> undoFunctionRevertToPreviousState(DocumentImpl currentState) {
        return uri -> {
            if (currentState == null){
                return false;
            }
            documentStore.put(uri, currentState);
            return documentStore.get(uri) == currentState;
        };
    }

    private Function<URI, Boolean> undoFunctionDoNothing(){
        return uri -> true;
    }

    /**
     * Given a URI recursively search through the commandStack checking if the top Command.getURI() == the URI and return the Command with the matching URI
     * @param uri the URI if the requested command in the commandStack
     * @return the Command with the matching URI
     * @throws IllegalStateException if none of the commands in the stack match the given URI
     */
    private Command getCommandWithURI(URI uri) throws IllegalStateException{
        if (commandStack.peek() != null){
            Command command = commandStack.pop();
            if (command.getUri() == uri){
                return command;
            }
            Command c = getCommandWithURI(uri);
            commandStack.push(command);
            return c;
        }
        throw new IllegalStateException();
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

    private int putDocumentInStore(URI uri, DocumentImpl document) throws NullPointerException {
        try {
            if (document != null){
                DocumentImpl doc = documentStore.put(uri, document);
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
}