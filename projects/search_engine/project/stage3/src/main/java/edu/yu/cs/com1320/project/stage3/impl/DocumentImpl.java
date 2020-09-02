package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.stage3.Document;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class DocumentImpl implements Document {
    private URI uri;
    private String txt;
    private int txtHash;
    private byte[] pdfBytes;
    private Map<String, Integer> wordCounts;

    public DocumentImpl(URI uri, String txt, int txtHash) {
        this.uri = uri;
        this.txt = txt;
        this.txtHash = txtHash;
        this.wordCounts = new HashMap<>();
        this.addDocumentWordOccurrencesToHashMap();
    }

    public DocumentImpl(URI uri, String txt, int txtHash, byte[] pdfBytes) {
        this.uri = uri;
        this.txt = txt;
        this.txtHash = txtHash;
        this.pdfBytes = pdfBytes;
        this.wordCounts = new HashMap<>();
        this.addDocumentWordOccurrencesToHashMap();
    }

    @Override
    public boolean equals(Object docImpl) {
        if (this == docImpl) return true;
        if (docImpl == null || getClass() != docImpl.getClass()) return false;
        DocumentImpl doc = (DocumentImpl) docImpl;
        return txtHash == doc.txtHash && uri.equals(doc.uri) && txt.equals(doc.txt);
    }

    @Override
    public int hashCode() {
        final int p = 67;
        int hash = 1;
        hash = p * hash + (this.uri.hashCode());
        hash = p * hash + (this.txtHash);
        return hash;
    }

    /**
     * @return the document as a PDF
     */
    @Override
    public byte[] getDocumentAsPdf() {
        try{
            if (this.pdfBytes == null){
                this.pdfBytes = txtToPDF(this.txt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.pdfBytes;
    }

    /**
     * @return the document as a Plain String
     */
    @Override
    public String getDocumentAsTxt() {
        return this.txt;
    }

    /**
     * @return hash code of the plain text version of the document
     */
    @Override
    public int getDocumentTextHashCode() {
        return this.txtHash;
    }

    /**
     * @return URI which uniquely identifies this document
     */
    @Override
    public URI getKey() {
        return this.uri;
    }

    /**
     * how many times does the given word appear in the document?
     * @param word the word to get the number of word occurrences in this.txt
     * @return the number of times the given words appears in the document
     */
    @Override
    public int wordCount(String word) {
        word = word.toUpperCase();
        if (wordCounts.containsKey(word)) {
            return wordCounts.get(word);
        }
        return 0;
    }

    //converts the given text into a PDDocument and returns a PDDocument containing the string.
    private byte[] txtToPDF(String string) throws IOException{
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.showText(string);
            contentStream.endText();
            contentStream.close();
            ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            document.close();
            return byteArrayOutputStream.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
            throw new IOException();
        }
    }

    private void addDocumentWordOccurrencesToHashMap(){
        if (this.txt == null){
            return;
        }
        String text = this.txt.toUpperCase();
        text = text.trim();
        text = text.replaceAll("[^a-zA-Z0-9\\s]", "");
        String[] words = text.split("\\s");
        for (String word: words){
            word = word.toUpperCase();
            addWordToHashMap(word);
        }
    }

    private void addWordToHashMap(String word){
        word = word.toUpperCase();
        if (wordCounts.containsKey(word)){
            wordCounts.put(word, wordCounts.get(word) + 1);
        }
        else {
            wordCounts.put(word, 1);
        }
    }
}