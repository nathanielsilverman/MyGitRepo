package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.Utils;
import org.junit.Before;
import org.junit.Test;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class DocumentImplTest {

/////////Judah's Tests///////////////////////////////////////////

        private URI textUri;
        private String textString;
        private int textHashCode;

        private URI pdfUri;
        private String pdfString;
        private int pdfHashCode;
        private byte[] pdfData;

        @Before
        public void setUp() throws Exception {
            this.textUri = new URI("http://edu.yu.cs/com1320/txt");
            this.textString = "This is text content. Lots of it.";
            this.textHashCode = this.textString.hashCode();

            this.pdfUri = new URI("http://edu.yu.cs/com1320/pdf");
            this.pdfString = "This is a PDF, brought to you by Adobe.";
            this.pdfHashCode = this.pdfString.hashCode();
            this.pdfData = Utils.textToPdfData(this.pdfString);
        }
        @Test
        public void testGetTextDocumentAsTxt() {
            DocumentImpl textDocument = new DocumentImpl(this.textUri, this.textString, this.textHashCode);
            assertEquals(this.textString, textDocument.getDocumentAsTxt());
        }
        @Test
        public void testGetPdfDocumentAsTxt() {
            DocumentImpl pdfDocument = new DocumentImpl(this.pdfUri, this.pdfString, this.pdfHashCode, this.pdfData);
            assertEquals(this.pdfString, pdfDocument.getDocumentAsTxt());
        }
        @Test
        public void testGetTextDocumentAsPdf() {
            DocumentImpl textDocument = new DocumentImpl(this.textUri, this.textString, this.textHashCode);
            byte[] pdfBytes = textDocument.getDocumentAsPdf();
            String textAsPdfString = Utils.pdfDataToText(pdfBytes);
            assertEquals(this.textString, textAsPdfString);
        }
        @Test
        public void testGetPdfDocumentAsPdf() {
            DocumentImpl pdfDocument = new DocumentImpl(this.pdfUri, this.pdfString, this.pdfHashCode, this.pdfData);
            byte[] pdfBytes = pdfDocument.getDocumentAsPdf();
            String pdfAsPdfString = Utils.pdfDataToText(pdfBytes);
            assertEquals(this.pdfString, pdfAsPdfString);
        }
        @Test
        public void testGetTextDocumentTextHashCode() {
            DocumentImpl textDocument = new DocumentImpl(this.textUri, this.textString, this.textHashCode);
            assertEquals(this.textHashCode, textDocument.getDocumentTextHashCode());
        }
        @Test
        public void testGetPdfDocumentTextHashCode() {
            DocumentImpl pdfDocument = new DocumentImpl(this.pdfUri, this.pdfString, this.pdfHashCode, this.pdfData);
            assertEquals(this.pdfHashCode, pdfDocument.getDocumentTextHashCode());
            assertNotEquals(this.pdfHashCode, 25);
        }
        @Test
        public void testGetTextDocumentKey() {
            DocumentImpl textDocument = new DocumentImpl(this.textUri, this.textString, this.textHashCode);
            assertEquals(this.textUri, textDocument.getKey());
            URI fakeUri = null;
            try {
                fakeUri = new URI("http://wrong.com");
            }
            catch (URISyntaxException e) {
                e.printStackTrace();
            }
            assertNotEquals(this.textUri, fakeUri);
        }
        @Test
        public void testGetPdfDocumentKey() {
            DocumentImpl pdfDocument = new DocumentImpl(this.pdfUri, this.pdfString, this.pdfHashCode, this.pdfData);
            assertEquals(this.pdfUri, pdfDocument.getKey());
            URI fakeUri = null;
            try {
                fakeUri = new URI("http://wrong.com");
            }
            catch (URISyntaxException e) {
                e.printStackTrace();
            }
            assertNotEquals(this.pdfUri, fakeUri);
        }

//////////////////End of Judah's Tests//////////////////////////////////

/////////////////////My tests////////////////////////////////////
    @Test
    public void DocumentImplTest1() throws URISyntaxException {
        URI uri = new URI("URI/URI-Example");
        String docText = "Sample text.";
        int hashcode1 = docText.hashCode();
        DocumentImpl doc1 = new DocumentImpl(uri, docText, hashcode1);
        Assert.assertEquals(doc1.getDocumentAsTxt(), docText);
        Assert.assertEquals(doc1.getDocumentTextHashCode(), hashcode1);
    }

    @Test(expected = URISyntaxException.class)
    public void DocumentImplTest2() throws URISyntaxException {
        new URI("URI Example");
    }

    @Test
    public void DocumentImplTest3() throws URISyntaxException {
        URI uri = new URI("URI-uri/URI");
        String docText = "Sample text1";
        int hashcode1 = docText.hashCode();
        DocumentImpl doc1 = new DocumentImpl(uri, docText, hashcode1);
        Assert.assertEquals(doc1.getDocumentAsTxt(), docText);
        Assert.assertEquals(hashcode1, doc1.getDocumentTextHashCode());
        URI uri1 = new URI("URI1-uri/URI1");
        String docText1 = "Sample text1";
        int hashcode2 = docText.hashCode();
        DocumentImpl doc2 = new DocumentImpl(uri1, docText1, hashcode2);
        Assert.assertEquals(doc2.getDocumentAsTxt(), docText1);
        Assert.assertEquals(hashcode2, doc2.getDocumentTextHashCode());
        Assert.assertNotEquals(doc1, doc2);
    }

    @Test
    public void getDocumentAsPdf() throws URISyntaxException, IOException{
        String text = "Sample text.";
        byte[] bytes = txtToPDF(text);
        URI uri = new URI("https://PDF-example-test1");
        DocumentImpl pdf = new DocumentImpl(uri, text, text.hashCode(), bytes);
        Assert.assertEquals(bytes ,pdf.getDocumentAsPdf());
        Assert.assertEquals(text, pdf.getDocumentAsTxt());
        Assert.assertEquals(text.hashCode(), pdf.getDocumentTextHashCode());
        String text1 = "Sample text.";
        byte[] bytes1 = txtToPDF(text1);
        URI uri1 = new URI("https://PDF-example-test1");
        DocumentImpl pdf1 = new DocumentImpl(uri1, text1, text1.hashCode(), bytes1);
        Assert.assertEquals(bytes1 , pdf1.getDocumentAsPdf());
        Assert.assertEquals(text1, pdf1.getDocumentAsTxt());
        Assert.assertEquals(text1.hashCode(), pdf1.getDocumentTextHashCode());
        Assert.assertNotEquals(bytes, bytes1);
        Assert.assertEquals(pdf.getDocumentAsTxt().hashCode(), pdf1.getDocumentTextHashCode());
    }

    @Test
    public void getWordCount() throws URISyntaxException {
        String text = "Sample text is a 3 story of how some text became sample text 3.";
        URI uri = new URI("https://PDF-example-test1");
        DocumentImpl wordDoc = new DocumentImpl(uri, text, text.hashCode());
        Assert.assertEquals(text, wordDoc.getDocumentAsTxt());
        Assert.assertEquals(text.hashCode(), wordDoc.getDocumentTextHashCode());
        Assert.assertEquals(2, wordDoc.wordCount("sample"));
        Assert.assertEquals(3, wordDoc.wordCount("text"));
        Assert.assertEquals(2,  wordDoc.wordCount("3"));
        Assert.assertEquals(0, wordDoc.wordCount("dragon"));

    }

///////////////////End of My Tests ///////////////////////////////////////

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
            throw new IOException();
        }
    }
}
