package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class DocumentImpl implements Document {
    private URI uri;
    private String txt;
    private int txtHash;
    private byte[] pdfBytes;

    public DocumentImpl(URI uri, String txt, int txtHash) {
        this.uri = uri;
        this.txt = txt;
        this.txtHash = txtHash;
    }

    public DocumentImpl(URI uri, String txt, int txtHash, byte[] pdfBytes) {
        this.uri = uri;
        this.txt = txt;
        this.txtHash = txtHash;
        this.pdfBytes = pdfBytes;
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

    //converts the given text into a PDDocument and returns a PDDocument containing the string.
    private byte[] txtToPDF(String string) throws IOException{
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.TIMES_ROMAN, 12);
            contentStream.showText(string);
            contentStream.endText();
            contentStream.close();
            ByteArrayOutputStream byteArrayOutputStream= new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            document.close();
            return bytes;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}