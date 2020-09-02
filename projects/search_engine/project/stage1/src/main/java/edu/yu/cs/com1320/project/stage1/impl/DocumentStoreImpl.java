package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.DocumentStore;
import edu.yu.cs.com1320.project.impl.HashTableImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class DocumentStoreImpl implements DocumentStore {

    private HashTableImpl<URI, DocumentImpl> documentStore;

    public DocumentStoreImpl() {
        this.documentStore = new HashTableImpl<>();
    }

    /**
     * @param input  the document being put
     * @param uri    unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return the hashcode of the String version of the document
     */
    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) {
        if (uri == null || format == null) {
            throw new IllegalArgumentException("URI/Format is null");
        }
        if (input != null) {
            try {
                if (documentStoreHasDocument(uri)){
                    return replaceDocument(input, uri, format);
                }
                putDocumentInStore(input, uri, format);
                return 0;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        if (documentStoreHasDocument(uri)) {
            DocumentImpl document = getDocumentFromStore(uri);
            documentStore.put(uri, null);
            return document.getDocumentTextHashCode();
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
        if (documentStoreHasDocument(uri)){
            return getDocumentFromStore(uri).getDocumentAsPdf();
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
        if (documentStoreHasDocument(uri)){
            return getDocumentFromStore(uri).getDocumentAsTxt();
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
        if (getDocumentAsTxt(uri) == null) {
            return false;
        }
        else {
            documentStore.put(uri, null);
            return true;
        }
    }

    /**
     * @param uri the key used to retrieve the document from the DocumentStore
     * @return if a document with the given uri exists in the DocumentStore, return the document with the given uri. If the no documents exist in the DocumentStore with the given uri, return null.
     */
    private DocumentImpl getDocumentFromStore(URI uri){
        if (uri != null){
            if (documentStoreHasDocument(uri)) {
                return documentStore.get(uri);
            }
        }
        return null;
    }

    /**
     * @param uri heck if the uri is contained in the hashTable
     * @return if the uri is present in the hashTable, return true. If the hashTable doesn't contain the given uri, return false;
     */
    private boolean documentStoreHasDocument(URI uri){
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
    private int bytesToPDF(byte[] bytes, URI uri) throws IOException{
        try {
            InputStream inputStream = new ByteArrayInputStream(bytes);
            PDDocument doc = PDDocument.load(inputStream);
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            String text = pdfTextStripper.getText(doc);
            String newText = text.trim();
            doc.close();
            int hashCodeText = newText.hashCode();
            if(hasSameURIAndHashCode(uri, hashCodeText)){
                return 0;
            }
            DocumentImpl document = new DocumentImpl(uri, newText, hashCodeText, bytes);
            DocumentImpl document1 = documentStore.put(uri, document);
            if (document1 != null) {
                return document.getDocumentTextHashCode();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * converts the byte array into a String of the document's text. Constructs a new document object and puts it into the DocumentStore with the given uri.
     * @param bytes the byte array to be converted into a new String.
     * @param uri the uri of the document to be put into the DocumentStore.
     * @return the hashcode of the document text.
     */
    private int bytesToTXT(byte[] bytes, URI uri){
        String text = new String(bytes, StandardCharsets.UTF_8);
        int hashCodeText = text.hashCode();
        if ((getDocumentAsTxt(uri) != null) && (getDocumentAsTxt(uri).hashCode() == hashCodeText)){
            return 0;
        }
        DocumentImpl document = new DocumentImpl(uri, text, hashCodeText);
        documentStore.put(uri, document);
        return document.getDocumentTextHashCode();
    }

    /**
     * @param uri the uri to get a document's hashcode from the DocumentStore
     * @param hashCode the hashcode to compare to the document's hashcode
     * @return If the uri retrieves a document from the DocumentStore and has the document's hashcode matches with the (param) hashCode return true. Meaning that another document in the DocumentStore exists with the same hashCode. Otherwise, return false.
     */
    private boolean hasSameURIAndHashCode(URI uri, int hashCode){
        return (getDocumentFromStore(uri) != null) && (getDocumentFromStore(uri).getDocumentTextHashCode() == hashCode);
    }

    /**
     * @param inputStream the given inputStream to read all the bytes into a byteArrayOutputStream
     * @return byte array from the byteArrayOutputStream
     */
    private byte[] getByteArrayFromInputStream(InputStream inputStream) throws IOException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] inputStreamBytes = new byte[inputStream.available()];
            for (int i = inputStream.read(inputStreamBytes); i != -1; i = inputStream.read(inputStreamBytes)) {
                byteArrayOutputStream.write(inputStreamBytes, 0, i);
            }
            return byteArrayOutputStream.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private int replaceDocument(InputStream input, URI uri, DocumentFormat format) throws IOException{
        DocumentImpl document = getDocumentFromStore(uri);
        int hashCode = document.getDocumentTextHashCode();
        try {
            putDocumentInStore(input, uri, format);
            return hashCode;
        }catch (IOException e){
            e.printStackTrace();
        }
        return 0;
    }

    private int putDocumentInStore(InputStream input, URI uri, DocumentFormat format) throws IOException {
        try {
            byte[] bytes = getByteArrayFromInputStream(input);
            if (format == DocumentFormat.PDF) {
                return bytesToPDF(bytes, uri);
            }
            if (format == DocumentFormat.TXT) {
                return bytesToTXT(bytes, uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}