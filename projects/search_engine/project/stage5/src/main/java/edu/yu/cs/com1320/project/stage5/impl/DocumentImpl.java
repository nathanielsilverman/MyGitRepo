package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class DocumentImpl implements Document {
    private URI uri;
    private String txt;
    private int txtHash;
    private byte[] pdfBytes;
    private Map<String, Integer> wordCounts;
    private long lastUseTime;

    public DocumentImpl(URI uri, String txt, int txtHash) {
        this.uri = uri;
        this.txt = txt;
        this.txtHash = txtHash;
        this.pdfBytes = txtToPDF(this.txt);
        this.wordCounts = new HashMap<>();
        this.addDocumentWordOccurrencesToHashMap();
        this.setLastUseTime(System.nanoTime());
    }

    public DocumentImpl(URI uri, String txt, int txtHash, byte[] pdfBytes) {
        this.uri = uri;
        this.txt = txt;
        this.txtHash = txtHash;
        this.pdfBytes = pdfBytes;
        this.wordCounts = new HashMap<>();
        this.addDocumentWordOccurrencesToHashMap();
        this.setLastUseTime(System.nanoTime());
    }

    public DocumentImpl(URI uri, String txt, int txtHash, Map<String, Integer> wordCounts){
        this.uri = uri;
        this.txt = txt;
        this.txtHash = txtHash;
        this.pdfBytes = txtToPDF(this.txt);
        this.setWordMap(wordCounts);
        this.setLastUseTime(System.nanoTime());
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
        if (this.pdfBytes == null) {
            this.pdfBytes = txtToPDF(this.txt);
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

    /**
     * return the last time this document was used, via put/get or via a search result
     * (for stage 4 of project)
     */
    @Override
    public long getLastUseTime(){
        return this.lastUseTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds){
        this.lastUseTime = timeInNanoseconds;
    }

    /**
     * @return a copy of the word to count map so it can be serialized
     */
    @Override
    public Map<String, Integer> getWordMap() {
        return this.wordCounts;
    }

    /**
     * This must set the word to count map during deserialization
     *
     * @param wordMap a HashMap that maps the words in a documents to the number of times it occurrences
     */
    @Override
    public void setWordMap(Map<String, Integer> wordMap) {
        this.wordCounts = wordMap;
    }

    @Override
    public int compareTo(Document document){
        long docLastUseTime = document.getLastUseTime();
        if (docLastUseTime == this.lastUseTime){
            return 0;
        }
        else if (this.lastUseTime > docLastUseTime){
            return 1;
        }
        return -1;
    }

    //converts the given text into a PDDocument and returns a PDDocument containing the string.
    private byte[] txtToPDF(String string) {
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
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    private void addDocumentWordOccurrencesToHashMap() {
        if (this.txt == null) { return; }
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