package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.Utils;
import edu.yu.cs.com1320.project.stage3.*;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;

import static edu.yu.cs.com1320.project.Utils.toByteArray;
import static org.junit.Assert.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.net.URISyntaxException;
import java.util.*;

public class MyDocumentStoreImplTest {

/////////Judah's Tests///////////////////////////////////////////

//    //variables to hold possible values for doc1
    private URI uri1;
    private String txt1;
    private String pdfTxt1;
    private byte[] pdfData1;

    //variables to hold possible values for doc2
    private URI uri2;
    private String txt2;
    private String pdfTxt2;
    private byte[] pdfData2;

    //variables to hold possible values for doc3
    private URI uri3;
    private String txt3;
    private String pdfTxt3;
    private byte[] pdfData3;

    //variables to hold possible values for doc4
    private URI uri4;
    private String txt4;
    private String pdfTxt4;
    private byte[] pdfData4;


    @Before
        public void init() throws Exception {
            //init possible values for doc1
            this.uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
            this.txt1 = "This is the text of doc1, in plain text. No fancy file format - just plain old String, Hippo, Zebra";
            this.pdfTxt1 = "This is some PDF t%ext text texting for doc1, hat tip to Adobe. Hippo";
            this.pdfData1 = Utils.textToPdfData(this.pdfTxt1);

            //init possible values for doc2
            this.uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
            this.txt2 = "Text for doc2. Plain old text for testing. zelda";
            this.pdfTxt2 = "PDF content for doc2: PDF format was opened in 2008. zelda";
            this.pdfData2 = Utils.textToPdfData(this.pdfTxt2);

            //init possible values for doc3
            this.uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
            this.txt3 = "This is the text of doc3 written as plain text. The Stock Market is dead. Hippo Hippo Hippo";
            this.pdfTxt3 = "This is some PDF text, COVID-19 is destroying the world. Hippo Hippo Hippo";
            this.pdfData3 = Utils.textToPdfData(this.pdfTxt3);

            //init possible values for doc4
            this.uri4 = new URI("http://edu.yu.cs/com1320/project/doc4");
            this.txt4 = "Text for doc4. Document four to put into Document Store. Some more text. Hippo Hippo Hippo Hippo";
            this.pdfTxt4 = "PDF content for doc4: This PDF was made in 2020. Hippo Hippo Hippo Hippo. Content";
            this.pdfData4 = Utils.textToPdfData(this.pdfTxt4);
    }

        @Test
        public void testSearchPdf(){
            DocumentStoreImpl store = new DocumentStoreImpl();
            store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
            Assert.assertNotNull(store.getDocumentAsTxt(this.uri1));
            Assert.assertNotNull(store.getDocumentAsTxt(this.uri2));
            Document doc1 = store.getDocument(this.uri1);
            Document doc2 = store.getDocument(this.uri2);
            Assert.assertNotNull(doc1);
            Assert.assertNotNull(doc2);
            List<byte[]> list1 = store.searchPDFs("PDF");
            Assert.assertNotNull(list1);
            List<byte[]> list2 = new ArrayList<>();
            list2.add(doc2.getDocumentAsPdf());
            list2.add(doc1.getDocumentAsPdf());
            Assert.assertEquals(list1, list2);
        }

        @Test
        public void testSearchPdfPrefix(){
            DocumentStoreImpl store = new DocumentStoreImpl();
            store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
            Assert.assertNotNull(store.getDocumentAsTxt(this.uri1));
            Assert.assertNotNull(store.getDocumentAsTxt(this.uri2));
            Document doc1 = store.getDocument(this.uri1);
            Document doc2 = store.getDocument(this.uri2);
            Assert.assertNotNull(doc1);
            Assert.assertNotNull(doc2);
            List<byte[]> list1 = store.searchPDFsByPrefix("text");
            Assert.assertNotNull(list1);
            List<byte[]> list2 = new ArrayList<>();
            list2.add(doc1.getDocumentAsPdf());
            Assert.assertEquals(list1, list2);
        }

        @Test
        public void testSplitter(){
            String s1 = "This is some PDF text";
            String s2 = "This is some PDF t%ext";
            s2 = s2.replaceAll("[^a-zA-Z0-9\\s]", "");
            Assert.assertEquals(s1, s2);
        }

        @Test
        public void testGetAllSorted(){
            DocumentStoreImpl store = new DocumentStoreImpl();
            store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
            Assert.assertNotNull(store.getDocumentAsTxt(this.uri1));
            Assert.assertNotNull(store.getDocumentAsTxt(this.uri2));
            Document doc1 = store.getDocument(this.uri1);
            Document doc2 = store.getDocument(this.uri2);
            Assert.assertNotNull(doc1);
            Assert.assertNotNull(doc2);
            Assert.assertEquals(2, doc1.wordCount("text"));
            Assert.assertEquals(2, doc2.wordCount("text"));
            Assert.assertEquals(new ArrayList<>(Arrays.asList(doc2.getDocumentAsTxt(), doc1.getDocumentAsTxt())), store.search("for"));
            Assert.assertEquals(new ArrayList<>(Arrays.asList(doc1.getDocumentAsTxt(), doc2.getDocumentAsTxt())), store.searchByPrefix("text"));
            Assert.assertFalse(store.search("pizza").contains(doc1.getDocumentAsTxt()));
            Assert.assertFalse(store.search("chicken").contains(doc2.getDocumentAsTxt()));
        }

        @Test
        public void testGetPrefixOrdered(){
            DocumentStoreImpl store = new DocumentStoreImpl();
            store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
            store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
            Assert.assertNotNull(store.getDocumentAsTxt(this.uri1));
            Assert.assertNotNull(store.getDocumentAsTxt(this.uri2));
            Document doc1 = store.getDocument(this.uri1);
            Document doc2 = store.getDocument(this.uri2);
            Assert.assertNotNull(doc1);
            Assert.assertNotNull(doc2);
            Assert.assertEquals(new ArrayList<>(Arrays.asList(doc2.getDocumentAsTxt(), doc1.getDocumentAsTxt())), store.searchByPrefix("fo"));
        }

        @Test
        public void testPutPdfDocumentNoPreviousDocAtURI(){
            DocumentStore store = new DocumentStoreImpl();
            int returned = store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
            assertEquals(0, returned);
        }

        @Test
        public void testPutTxtDocumentNoPreviousDocAtURI(){
            DocumentStore store = new DocumentStoreImpl();
            int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
            assertEquals(0, returned);
        }

        @Test
        public void testPutDocumentWithNullArguments(){
            DocumentStore store = new DocumentStoreImpl();
            try {
                store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), null, DocumentStore.DocumentFormat.TXT);
                fail("null URI should've thrown IllegalArgumentException");
            }catch(IllegalArgumentException e){}

            try {
                store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, null);
                fail("null format should've thrown IllegalArgumentException");
            }catch(IllegalArgumentException e){}
        }

        @Test
        public void testPutNewVersionOfDocumentPdf(){
            //put the first version
            DocumentStore store = new DocumentStoreImpl();
            int returned = store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
            assertEquals(0, returned);
            assertEquals("failed to return correct pdf text",this.pdfTxt1,Utils.pdfDataToText(store.getDocumentAsPdf(this.uri1)));
            //put the second version, testing both return value of put and see if it gets the correct text
            returned = store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri1, DocumentStore.DocumentFormat.PDF);
            assertEquals("should return hashcode of old text", this.pdfTxt1.hashCode(), returned);
            assertEquals("failed to return correct pdf text", this.pdfTxt2, Utils.pdfDataToText(store.getDocumentAsPdf(this.uri1)));
        }

        @Test
        public void testPutNewVersionOfDocumentTxt(){
            //put the first version
            DocumentStore store = new DocumentStoreImpl();
            int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
            assertEquals(0, returned);
            assertEquals("failed to return correct text",this.txt1,store.getDocumentAsTxt(this.uri1));
            //put the second version, testing both return value of put and see if it gets the correct text
            returned = store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
            assertEquals("should return hashcode of old text", this.txt1.hashCode(), returned);
            assertEquals("failed to return correct text",this.txt2,store.getDocumentAsTxt(this.uri1));
        }

        @Test
        public void testGetTxtDocAsPdf(){
            DocumentStore store = new DocumentStoreImpl();
            int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
            assertEquals(0, returned);
            assertEquals("failed to return correct pdf text",this.txt1,Utils.pdfDataToText(store.getDocumentAsPdf(this.uri1)));
        }

        @Test
        public void testGetTxtDocAsTxt(){
            DocumentStore store = new DocumentStoreImpl();
            int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
            assertEquals(0, returned);
            assertEquals("failed to return correct text",this.txt1,store.getDocumentAsTxt(this.uri1));
        }

        @Test
        public void testToByteArray(){
            String s = "hi";
            byte[] bytes = toByteArray(new ByteArrayInputStream(s.getBytes()));
            Assert.assertNotNull(bytes);
            Assert.assertEquals("hi", new String(bytes));
        }

        @Test
        public void testGetPdfDocAsPdf(){
            DocumentStore store = new DocumentStoreImpl();
            int returned = store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
            assertEquals(0, returned);
            assertEquals("failed to return correct pdf text",this.pdfTxt1,Utils.pdfDataToText(store.getDocumentAsPdf(this.uri1)));
        }

        @Test
        public void testGetPdfDocAsTxt(){
            DocumentStore store = new DocumentStoreImpl();
            int returned = store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
            assertEquals(0, returned);
            assertEquals("failed to return correct text",this.pdfTxt1,store.getDocumentAsTxt(this.uri1));
        }

        @Test
        public void testDeleteDoc(){
            DocumentStore store = new DocumentStoreImpl();
            store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
            store.deleteDocument(this.uri1);
            assertNull("calling get on URI from which doc was deleted should've returned null", store.getDocumentAsPdf(this.uri1));
        }

        @Test
        public void testDeleteDocReturnValue(){
            DocumentStore store = new DocumentStoreImpl();
            store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
            //should return true when deleting a document
            assertTrue("failed to return true when deleting a document", store.deleteDocument(this.uri1));
            //should return false if I try to delete the same doc again
            assertFalse("failed to return false when trying to delete that which was already deleted", store.deleteDocument(this.uri1));
            //should return false if I try to delete something that was never there to begin with
            assertFalse("failed to return false when trying to delete that which was never there to begin with", store.deleteDocument(this.uri2));
        }

//////////////////End of Judah's Tests//////////////////////////////////

/////////////////////My tests////////////////////////////////////

    private DocumentStoreImpl documentStore = new DocumentStoreImpl();
    private DocumentStore.DocumentFormat txt = DocumentStore.DocumentFormat.TXT;
    private DocumentStore.DocumentFormat pdf = DocumentStore.DocumentFormat.PDF;

    @Test(expected = IllegalArgumentException.class)
    public void putDocumentsWithNullKeyTXT() {
        documentStore.putDocument(null, null, txt);
    }

    @Test(expected = IllegalArgumentException.class)
    public void putDocumentsWithNullKeyPDF(){
        documentStore.putDocument(null, null, pdf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void putDocumentWithNullFormat() throws URISyntaxException {
        URI uri = new URI("uri");
        documentStore.putDocument(null, uri, null);
    }

    @Test
    public void testPutWithInputStream() throws URISyntaxException {
        String text = "This-is-document-1";
        URI uri = new URI("randomURI/Key");
        Assert.assertEquals(0, documentStore.putDocument(txtToInputStream(text), uri, txt));
        Assert.assertNotNull(documentStore.getDocumentAsPdf(uri));
        Assert.assertNotNull(documentStore.getDocumentAsTxt(uri));
        int txtHashCode = getDoc(uri).getDocumentTextHashCode();
        Assert.assertNotEquals(0, txtHashCode);
        Assert.assertNotEquals(0, getDoc(uri).getDocumentTextHashCode());
        int i = documentStore.putDocument(txtToInputStream(text), uri, txt);
        Assert.assertEquals(txtHashCode, i);
        Assert.assertNotNull(documentStore.getDocumentAsPdf(uri));
        Assert.assertNotNull(documentStore.getDocumentAsTxt(uri));
        int textHashCode1 = text.hashCode();
        Assert.assertEquals(textHashCode1, txtHashCode);
        Assert.assertEquals(txtHashCode, i);
        Assert.assertEquals(txtHashCode, documentStore.getDocumentAsTxt(uri).hashCode());
        String textFromBytes = new String(text.getBytes(), StandardCharsets.UTF_8);
        Assert.assertEquals(text, textFromBytes);
        Assert.assertEquals(textFromBytes.hashCode(), text.hashCode());
        Assert.assertEquals(textFromBytes.hashCode(), txtHashCode);
        Assert.assertEquals(text.hashCode(), txtHashCode);
        Assert.assertEquals(text, textFromBytes);
        Assert.assertEquals(text, documentStore.getDocumentAsTxt(uri));
        Assert.assertNotNull(documentStore.getDocumentAsPdf(uri));
        Assert.assertEquals(text.hashCode(), documentStore.putDocument(null, uri, txt));
        Assert.assertNull(documentStore.getDocumentAsTxt(uri));
        Assert.assertEquals(0, documentStore.putDocument(txtToInputStream(text), uri, txt));
        Assert.assertNotNull(documentStore.getDocumentAsTxt(uri));
        Assert.assertNotNull(documentStore.getDocumentAsPdf(uri));
        Assert.assertTrue(documentStore.deleteDocument(uri));
        Assert.assertNull(documentStore.getDocumentAsPdf(uri));
    }

    @Test
    public void test2()throws URISyntaxException{
        URI uri = new URI("uri.SampleURI/uriExample/uri");
        String text = "Sample text for test 2";
        putDocumentTXTInStore(this.documentStore, uri, text);
        Assert.assertNotNull(documentStore.getDocumentAsPdf(uri));
        URI uri1 = new URI("URI1.uri/sampleURI-URI1");
        Assert.assertNotEquals(uri1,uri);
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        String txtHashCode = documentStore.getDocumentAsTxt(uri);
        InputStream inputStream1 = new ByteArrayInputStream(bytes);
        int textHashCode1 = text.hashCode();
        int textHashCode2 = txtHashCode.hashCode();
        Assert.assertEquals(textHashCode1, textHashCode2);
        Assert.assertEquals(textHashCode2, documentStore.putDocument(inputStream1, uri, txt));
        Assert.assertEquals(txtHashCode.hashCode() , documentStore.getDocumentAsTxt(uri).hashCode());
        String textFromBytes = new String(bytes, StandardCharsets.UTF_8);
        Assert.assertEquals(text, textFromBytes);
        Assert.assertEquals(textFromBytes.hashCode(), text.hashCode());
        Assert.assertEquals(textFromBytes.hashCode(), txtHashCode.hashCode());
        Assert.assertEquals(text.hashCode(), txtHashCode.hashCode());
        Assert.assertEquals(text, textFromBytes);
        Assert.assertEquals(text, documentStore.getDocumentAsTxt(uri));
        Assert.assertNotNull(documentStore.getDocumentAsPdf(uri));
    }

    @Test
    public void undoCommands() throws URISyntaxException, IOException {
        URI uriDocumentA = new URI("https://URI-Document1/DOC_ONE");
        URI uriDocumentB = new URI("https://URI-Document2/DOC_TWO");
        URI uriDocumentC = new URI("https://URI-Document3/DOC_THREE");
        URI uriDocumentD = new URI("https://URI-Document4/DOC_FOUR");
        URI uriDocumentE = new URI("https://URI-Document5/DOC_FIVE");

        String text1 = "Sample text (Text 1) Document A. I'm a text file. docTXT1.";
        String text2 = "Sample text (Text 2) Document B. I'm a text file. docTXT2.";
        String text3 = "Sample text (Text 3) Document A. I'm a text file. docTXT3. Text 3 Overwriting Text 1 (Document A): " + text1;
        String text4 = "Sample text (Text 4) Document C. I'm a PDF. docPDF4.";
        String text5 = "Sample text (Text 5) Document D. I'm a text file. docTXT5.";
        String text6 = "Sample text (Text 6) Document E. I'm a PDF also. docPDF6.";
        String text7 = "Sample text (Text 7) Document C. I'm a text file as well. docTXT7. Text 7 Overwriting Text 4 (Document C): " + text4;
        String text8 = "Sample text (Text 8) Document E. I'm a PDF file. docPDF8. Text 8 Overwriting Text 6 (Document E): " + text6;
        String text9 = "Sample text (Text 9) Document E. I'm a text file. docTXT9. Text 9 Overwriting Text 8 (Document E): " + text8;

        DocumentImpl docTXT1 = createDocument(uriDocumentA, text1, txt);
        DocumentImpl docTXT2 = createDocument(uriDocumentB, text2, txt);
        // Document overwrites Document A (docTXT1 --> docTXT3)
        DocumentImpl docTXT3 = createDocument(uriDocumentA, text3, txt);
        DocumentImpl docPDF4 = createDocument(uriDocumentC, text4, pdf);
        DocumentImpl docTXT5 = createDocument(uriDocumentD, text5, txt);
        DocumentImpl docPDF6 = createDocument(uriDocumentE, text6, pdf);
        // Document overwrites Document C (docPDF4 --> docTXT7)
        DocumentImpl docTXT7 = createDocument(uriDocumentC, text7, txt);
        // Document overwrites Document E (docPDF6 --> docPDF8)
        DocumentImpl docPDF8 = createDocument(uriDocumentE, text8, pdf);
        // Document overwrites Document E (docPDF8 --> docTXT9)
        DocumentImpl docTXT9 = createDocument(uriDocumentE, text9, txt);
        // Document overwrites Document C with same text  (no change)
        DocumentImpl docTXT10 = createDocument(uriDocumentC, text7, txt);

        Assert.assertNotNull(docTXT1);
        Assert.assertNotNull(docTXT2);
        Assert.assertNotNull(docTXT3);
        Assert.assertNotNull(docPDF4);
        Assert.assertNotNull(docTXT5);
        Assert.assertNotNull(docPDF6);
        Assert.assertNotNull(docTXT7);
        Assert.assertNotNull(docPDF8);
        Assert.assertNotNull(docTXT9);
        Assert.assertNotNull(docTXT10);

        // Put docTXT1 and docTXT2 in documentStore
        documentStore.putDocument(txtToInputStream(text1), uriDocumentA, txt);
        documentStore.putDocument(txtToInputStream(text2), uriDocumentB, txt);
        Assert.assertEquals(docTXT1, getDoc(uriDocumentA));
        Assert.assertEquals(docTXT2, getDoc(uriDocumentB));

        // Put docTXT3 in documentStore
        documentStore.putDocument(txtToInputStream(text3), uriDocumentA, txt);
        Assert.assertNotEquals(docTXT1, getDoc(uriDocumentA));
        Assert.assertEquals(docTXT3, getDoc(uriDocumentA));
        Assert.assertEquals(docTXT3.getDocumentAsTxt(), getDoc(uriDocumentA).getDocumentAsTxt());
        Assert.assertEquals(docTXT3.getDocumentTextHashCode(), getDoc(uriDocumentA).getDocumentTextHashCode());

        // Undo last action (Undo overwriting docTXT1)
        documentStore.undo();
        Assert.assertEquals(docTXT1, getDoc(uriDocumentA));
        Assert.assertEquals(docTXT1.getDocumentTextHashCode(), getDoc(uriDocumentA).getDocumentTextHashCode());
        Assert.assertNotEquals(docTXT3.getDocumentTextHashCode(), getDoc(uriDocumentA).getDocumentTextHashCode());
        Assert.assertNotEquals(docTXT3, getDoc(uriDocumentA));

        // Undo last action on uriDocumentA (remove docTXT1 from documentStore)
        documentStore.undo(uriDocumentA);
        Assert.assertNotEquals(docTXT1, getDoc(uriDocumentA));
        Assert.assertNull(getDoc(uriDocumentA));

        // Undo last action (remove docTXT2 from documentStore)
        documentStore.undo();
        Assert.assertNotEquals(docTXT2, getDoc(uriDocumentB));
        Assert.assertNull(getDoc(uriDocumentB));

        // Add docPDF4 to documentStore
        documentStore.putDocument(bytesToInputStream(txtToPDF(text4)), uriDocumentC, pdf);
        Assert.assertNotNull(getDoc(uriDocumentC));
        Assert.assertEquals(docPDF4.getDocumentAsTxt(), getDoc(uriDocumentC).getDocumentAsTxt());
        Assert.assertEquals(docPDF4.getDocumentTextHashCode(), getDoc(uriDocumentC).getDocumentTextHashCode());

        // Remove docPDF4 from documentStore
        Assert.assertTrue(documentStore.deleteDocument(uriDocumentC));
        Assert.assertNull(getDoc(uriDocumentC));

        // Undo delete
        documentStore.undo();
        Assert.assertNotNull(getDoc(uriDocumentC));
        Assert.assertEquals(docPDF4, getDoc(uriDocumentC));

        // Remove docPDF from documentStore with null input
        Assert.assertNotNull(getDoc(uriDocumentC));
        documentStore.putDocument(null, uriDocumentC, txt);
        Assert.assertNull(getDoc(uriDocumentC));

        // Add docTXT5 to documentStore
        documentStore.putDocument(txtToInputStream(text5), uriDocumentD, txt);
        Assert.assertNotNull(getDoc(uriDocumentD));
        Assert.assertEquals(docTXT5, getDoc(uriDocumentD));

        // Add docPDF6 to documentStore
        Assert.assertNull(getDoc(uriDocumentE));
        documentStore.putDocument(bytesToInputStream(txtToPDF(text6)), uriDocumentE, pdf);
        Assert.assertNotNull(getDoc(uriDocumentE));
        Assert.assertEquals(docPDF6, getDoc(uriDocumentE));

        Assert.assertNull(getDoc(uriDocumentC));
        Assert.assertNotEquals(docTXT7, getDoc(uriDocumentC));
        // Undo last action on uriDocumentC (add back docPDF4)
        documentStore.undo(uriDocumentC);
        Assert.assertNotNull(getDoc(uriDocumentC));
        Assert.assertNotEquals(docTXT7, getDoc(uriDocumentC));
        Assert.assertEquals(docPDF4, getDoc(uriDocumentC));
        // Undo last action on uriDocumentC (unadd docPDF4)
        documentStore.undo(uriDocumentC);
        Assert.assertNull(getDoc(uriDocumentC));
        // Add docPDF4
        addDoc(docPDF4, pdf);
        Assert.assertNotNull(getDoc(uriDocumentC));

        // Add docTXT7 to documentStore (overwrites docPDF4)
        addDoc(docTXT7, txt);
        Assert.assertNotNull(getDoc(uriDocumentC));
        Assert.assertEquals(docTXT7, getDoc(uriDocumentC));
        Assert.assertNotEquals(docPDF4, getDoc(uriDocumentC));

        Assert.assertNotNull(getDoc(uriDocumentC));
        Assert.assertNotNull(getDoc(uriDocumentD));
        Assert.assertNotNull(getDoc(uriDocumentE));

        Assert.assertEquals(docTXT5, getDoc(uriDocumentD));
        Assert.assertEquals(docTXT7, getDoc(uriDocumentC));
        Assert.assertEquals(docPDF6, getDoc(uriDocumentE));

        Assert.assertNull(getDoc(uriDocumentB));
        Assert.assertNull(getDoc(uriDocumentA));

        // Add docPDF8 to documentStore (overwrites docPDF6) URI = uriDocumentE
        Assert.assertNotNull(getDoc(uriDocumentE));
        Assert.assertEquals(docPDF6, getDoc(uriDocumentE));
        addDoc(docPDF8, pdf);
        Assert.assertNotEquals(docPDF6, getDoc(uriDocumentE));
        Assert.assertEquals(docPDF8, getDoc(uriDocumentE));

        // Add docTXT9 to documentStore (overwrites docPDF8) URI = uriDocumentE
        Assert.assertNotNull(getDoc(uriDocumentE));
        Assert.assertEquals(docPDF8, getDoc(uriDocumentE));
        addDoc(docTXT9, txt);
        Assert.assertNotEquals(docPDF6, getDoc(uriDocumentE));
        Assert.assertNotEquals(docPDF8, getDoc(uriDocumentE));
        Assert.assertEquals(docTXT9, getDoc(uriDocumentE));

        // Add docTXT10 to documentStore (overwrites docTXT7 with same text i.e. no change) URI = uriDocumentC
        Assert.assertNotNull(getDoc(uriDocumentC));
        Assert.assertEquals(docTXT7, getDoc(uriDocumentC));
        addDoc(docTXT10, txt);
        Assert.assertEquals(docTXT10.equals(docTXT7), docTXT7.equals(docTXT10));
        Assert.assertEquals(docTXT10.getDocumentAsTxt(), docTXT7.getDocumentAsTxt());
        Assert.assertEquals(docTXT10.getDocumentTextHashCode(), docTXT7.getDocumentTextHashCode());
        Assert.assertEquals(docTXT7.getDocumentTextHashCode(), getDoc(uriDocumentC).getDocumentTextHashCode());
        Assert.assertEquals(docTXT7.getDocumentAsTxt(), getDoc(uriDocumentC).getDocumentAsTxt());
        Assert.assertEquals(docTXT10, getDoc(uriDocumentC));
        Assert.assertEquals(docTXT7, getDoc(uriDocumentC));

        // Undo overwriting docTXT7 with the same text
        documentStore.undo();
        Assert.assertEquals(docTXT7.getDocumentTextHashCode(), getDoc(uriDocumentC).getDocumentTextHashCode());
        Assert.assertEquals(docTXT7.getDocumentAsTxt(), getDoc(uriDocumentC).getDocumentAsTxt());
        Assert.assertEquals(docTXT10, getDoc(uriDocumentC));
        Assert.assertEquals(docTXT7, getDoc(uriDocumentC));
        // Undo last action on uriDocumentC (Undo overwriting docPDF4)
        Assert.assertNotNull(getDoc(uriDocumentC));
        Assert.assertEquals(docTXT10, getDoc(uriDocumentC));
        Assert.assertEquals(docTXT7, getDoc(uriDocumentC));
        documentStore.undo(uriDocumentC);
        Assert.assertNotNull(getDoc(uriDocumentC));
        Assert.assertNotEquals(docTXT10, getDoc(uriDocumentC));
        Assert.assertNotEquals(docTXT7, getDoc(uriDocumentC));
        Assert.assertEquals(docPDF4, getDoc(uriDocumentC));
        // Undo last action on uriDocumentC (remove docPDF4)
        documentStore.undo(uriDocumentC);
        Assert.assertNull(getDoc(uriDocumentC));
        Assert.assertNotEquals(docTXT10, getDoc(uriDocumentC));
        Assert.assertNotEquals(docTXT7, getDoc(uriDocumentC));
        Assert.assertNotEquals(docPDF4, getDoc(uriDocumentC));

        //
        Assert.assertNotNull(getDoc(uriDocumentD));
        Assert.assertNotNull(getDoc(uriDocumentE));

        Assert.assertEquals(docTXT5, getDoc(uriDocumentD));
        Assert.assertEquals(docTXT9, getDoc(uriDocumentE));

        Assert.assertNull(getDoc(uriDocumentC));
        Assert.assertNull(getDoc(uriDocumentB));
        Assert.assertNull(getDoc(uriDocumentA));

        // Delete docTXT9
        documentStore.putDocument(null, uriDocumentE, pdf);
        Assert.assertNull(getDoc(uriDocumentE));

        // Delete docTXT5
        Assert.assertTrue(documentStore.deleteDocument(uriDocumentD));
        Assert.assertNull(getDoc(uriDocumentD));

        //Undo last action (add back docTXT5)
        documentStore.undo();
        Assert.assertNotNull(getDoc(uriDocumentD));
        Assert.assertEquals(docTXT5, getDoc(uriDocumentD));

        // Undo last action (add back docTXT9)
        documentStore.undo();
        Assert.assertNotNull(getDoc(uriDocumentE));
        Assert.assertEquals(docTXT9, getDoc(uriDocumentE));

        // Undo last action on uriDocumentE (undo overwritng docPDF8)
        documentStore.undo(uriDocumentE);
        Assert.assertNotNull(getDoc(uriDocumentE));
        Assert.assertEquals(docPDF8, getDoc(uriDocumentE));

        // Undo last action (undo overwriting docPDF6)
        documentStore.undo();
        Assert.assertNotNull(getDoc(uriDocumentE));
        Assert.assertEquals(docPDF6, getDoc(uriDocumentE));

        // Undo last action on uriDocumentD (unadd docTXt5)
        Assert.assertNotNull(getDoc(uriDocumentD));
        Assert.assertEquals(docTXT5, getDoc(uriDocumentD));
        documentStore.undo(uriDocumentD);
        Assert.assertNull(getDoc(uriDocumentD));
        Assert.assertNotEquals(docTXT5, getDoc(uriDocumentD));

        // Undo last action (unadd docPDF6)
        documentStore.undo();
        Assert.assertNull(getDoc(uriDocumentE));
        Assert.assertNotEquals(docPDF6, getDoc(uriDocumentE));
    }

    @Test
    public void undoOverwriteDocument() throws URISyntaxException, IOException {
        URI uri = new URI("this-is-a-URI-for-Testing/Purposes.");
        DocumentImpl doc1 = createDocument(uri, "sample-text-for-document-1.", txt);
        Assert.assertNotNull(doc1);
        addDoc(doc1, txt);
        Assert.assertNotNull(getDoc(uri));
        Assert.assertEquals(doc1, getDoc(uri));
        // Add TXT2, overwrites TXT1
        DocumentImpl doc2 = createDocument(uri, "sample-text-for-document-2. OVERWRITE document 1.", pdf);
        Assert.assertNotNull(doc2);
        addDoc(doc2, pdf);

        Assert.assertNotNull(getDoc(uri));
        Assert.assertNotEquals(doc1, getDoc(uri));
        Assert.assertEquals(doc2, getDoc(uri));
        Assert.assertNotEquals(doc1, getDoc(uri));
        // Undo overwrite TXT1
        Assert.assertEquals(doc2.getDocumentAsTxt(), getDoc(uri).getDocumentAsTxt());
        documentStore.undo();
        Assert.assertNotNull(getDoc(uri));
        Assert.assertEquals(doc1.getDocumentAsTxt(), getDoc(uri).getDocumentAsTxt());
        Assert.assertNotEquals(doc2, getDoc(uri));
        Assert.assertEquals(doc1, getDoc(uri));
        documentStore.undo();
        Assert.assertNull(getDoc(uri));
    }

    @Test
    public void testPutDocumentAsBothTXTAndPDF() throws URISyntaxException, IOException{
        URI uri = new URI("https://URI-uri/uri");
        String docText = "Consider all the possible ways to improve yourself and the world";
        byte[] pDFBytes = txtToPDF(docText);
        Assert.assertEquals(0, documentStore.putDocument(txtToInputStream(docText), uri, txt));
        int i = documentStore.putDocument(bytesToInputStream(pDFBytes), uri, pdf);
        Assert.assertEquals(getDoc(uri).getDocumentTextHashCode(), i);
    }

    @Test
    public void testPutDocumentAsBothTXT() throws URISyntaxException {
        URI uri = new URI("lvD9dklsjlkdsjlkdsxjOTNV/k3oXsDfadenCIpcqcV0i/GqRWulqB6xA0yEQINIZykjdksljf/5Kjgk.4Bop50BkFe53nJPvpZA4");
        String docText = "I am from atlanta georgia. i love chinese food.";
        Assert.assertEquals(0, documentStore.putDocument(txtToInputStream(docText), uri, txt));
        Assert.assertEquals(docText.hashCode(), documentStore.putDocument(txtToInputStream(docText), uri, txt));
        Assert.assertEquals(docText.hashCode(), documentStore.getDocument(uri).getDocumentTextHashCode());
    }

    @Test
    public void testDeleteNonExistingDocument() throws URISyntaxException{
        URI uri = new URI("hKSPPPoMAl/QNbDTzyzHy/JcB1bGppMAAqNy/iG5mOHpbpB/sdlkadssd/dfewfcs/d.fds/dfLj");
        Assert.assertFalse(documentStore.deleteDocument(uri));
    }

    @Test
    public void testDeleteDocument() throws URISyntaxException{
        URI uri = new URI("http://WulqB6xA0yEQIN/dEzLJHJKNSJ/sdalNMA4/xls2myJsVoxhXK08hoMO/lo3TNOeo6JUCoIiMx8LPZV3ssM2hPuvtCMpI/RP9lPWZwGugdb/3Ntg8POdAwgBPKqaxxb/Cq6oa1fWYKH93/guaMMQLwO3A9T4xurHX1q/01xQDWfB8sz88BdHhwZW");
        String docText = "Sometimes it is better to just walk when you’re in a better frame of mind.";
        Assert.assertNull(documentStore.getDocumentAsPdf(uri));
        Assert.assertEquals(0, documentStore.putDocument(txtToInputStream(docText), uri, txt));
        Assert.assertNotNull(documentStore.getDocumentAsTxt(uri));
        Assert.assertTrue(documentStore.deleteDocument(uri));
        Assert.assertNull(documentStore.getDocumentAsTxt(uri));
    }

    @Test(expected = URISyntaxException.class)
    public void checkURISyntax() throws URISyntaxException, IOException {
            URI uri = new URI("&&^*");
            String docText = "Check for URISyntax";
            Assert.assertEquals(documentStore.putDocument(txtToInputStream(docText), uri, txt), documentStore.putDocument(bytesToInputStream(txtToPDF(docText)), uri, pdf));
    }

    @Test
    public void testUndo() throws URISyntaxException{
        String text = "test doc";
        URI uri =  new URI("https://www.coronavirus.com/edu/yu/cs");
        putDocumentTXTInStore(this.documentStore, uri, text);
        documentStore.undo();
        Assert.assertNull(getDoc(uri));
        putDocumentTXTInStore(this.documentStore, uri, text);
        documentStore.deleteDocument(uri);
        Assert.assertNull(getDoc(uri));
        documentStore.undo();
        Assert.assertNotNull(getDoc(uri));
    }

    @Test
    public  void testUndo1() {
        try {
            URI uri1 = add1();
            URI uri2 = add2();
            URI uri3 = add3();
            documentStore.undo(uri1);
            Assert.assertNull(getDoc(uri1));
            documentStore.undo(uri3);
            Assert.assertNull(getDoc(uri3));
            documentStore.undo(uri2);
            Assert.assertNull(getDoc(uri2));
            URI uri3a = add3();
            Assert.assertNotNull(getDoc(uri3a));
            DocumentImpl doc = getDoc(uri3a);
            documentStore.deleteDocument(uri3a);
            Assert.assertNull(getDoc(uri3a));
            documentStore.undo(uri3a);
            Assert.assertNotNull(getDoc(uri3a));
            Assert.assertEquals(doc, getDoc(uri3a));
            documentStore.undo();
            Assert.assertNull(documentStore.getDocumentAsTxt(uri3a));
        } catch (URISyntaxException e){
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testUndoError(){
        documentStore.undo();
    }

    @Test(expected = IllegalStateException.class)
    public void testUndoURIError() throws URISyntaxException{
        URI uri = new URI("https//fakeURI");
        documentStore.undo(uri);
    }

    @Test
    public void testDocumentGetAllSortedWithUndo(){
        DocumentStoreImpl documentStore1 = new DocumentStoreImpl();
        initializeStore(documentStore1);
        Document doc1 = documentStore1.getDocument(this.uri1);
        Document doc2 = documentStore1.getDocument(this.uri2);
        Document doc3 = documentStore1.getDocument(this.uri3);
        Document doc4 = documentStore1.getDocument(this.uri4);
        Assert.assertNotNull(doc1);
        Assert.assertNotNull(doc2);
        Assert.assertNotNull(doc3);
        Assert.assertNotNull(doc4);

        Assert.assertEquals(doc2.getDocumentTextHashCode(), documentStore1.putDocument(new ByteArrayInputStream(this.txt2.getBytes()), this.uri2, DocumentStore.DocumentFormat.TXT));
        Assert.assertNotEquals(doc2, documentStore1.getDocument(uri2));
        documentStore1.undo(uri2);
        Assert.assertEquals(doc2, documentStore1.getDocument(uri2));


        Assert.assertTrue(documentStore1.deleteDocument(uri1));
        Assert.assertNull(documentStore1.getDocument(uri1));
        Assert.assertEquals(new ArrayList<>(), documentStore1.search("doc1"));
        documentStore1.undo();
        Assert.assertNotNull(documentStore1.getDocument(uri1));
        Assert.assertEquals(doc1, documentStore1.getDocument(uri1));

        Assert.assertEquals(doc4.getDocumentTextHashCode(), documentStore1.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));
        Assert.assertNotEquals(doc4, documentStore1.getDocument(uri4));
        Document doc4a = documentStore1.getDocument(uri4);
        Assert.assertNotEquals(doc4, doc4a);
        Assert.assertNotEquals(doc4.getDocumentAsTxt(), doc4a.getDocumentAsTxt());


        documentStore1.undo();


        Assert.assertNotEquals(doc4a, documentStore1.getDocument(uri4));
        Assert.assertEquals(doc4, documentStore1.getDocument(uri4));
        Assert.assertEquals(this.txt4, documentStore1.getDocument(uri4).getDocumentAsTxt());
        Assert.assertEquals(this.txt4, doc4.getDocumentAsTxt());


        Assert.assertNotNull(documentStore1.getDocument(this.uri1));
        Assert.assertNotNull(documentStore1.getDocument(this.uri2));
        Assert.assertNotNull(documentStore1.getDocument(this.uri3));
        Assert.assertNotNull(documentStore1.getDocument(this.uri4));


        Assert.assertEquals(new ArrayList<>(Arrays.asList(doc4.getDocumentAsTxt(), doc3.getDocumentAsTxt(), doc1.getDocumentAsTxt())), documentStore1.search("Hippo"));

        Assert.assertEquals(new HashSet<>(new ArrayList<>(Arrays.asList(this.uri1,this.uri3,this.uri4))), documentStore1.deleteAll("hippo"));
        Assert.assertEquals(new ArrayList<>(), documentStore1.search("hippo"));
        Assert.assertNull(documentStore1.getDocument(uri1));
        Assert.assertNull(documentStore1.getDocument(uri3));
        Assert.assertNull(documentStore1.getDocument(uri4));
        Assert.assertNotNull(documentStore1.getDocument(uri2));
        Assert.assertFalse(documentStore1.deleteDocument(uri1));

        Assert.assertNull(documentStore1.getDocument(uri1));
        documentStore1.undo(uri3);

        Assert.assertNull(documentStore1.getDocument(uri1));
        Assert.assertNull(documentStore1.getDocument(uri4));
        Assert.assertNotNull(documentStore1.getDocument(uri3));

        Assert.assertEquals(doc3, documentStore1.getDocument(uri3));
        Assert.assertNotEquals(doc1, documentStore1.getDocument(uri1));
        Assert.assertNotEquals(doc4, documentStore1.getDocument(uri4));

        Assert.assertEquals(new ArrayList<>(Collections.singletonList(doc3.getDocumentAsTxt())), documentStore1.search("hippo"));
    }

    @Test
    public void testLastUndoOnDeleteAll(){
        DocumentStoreImpl docStore = new DocumentStoreImpl();
        initializeStore(docStore);
        Set<URI> uriSet = docStore.deleteAll("text");
        for (URI uri: uriSet) {
            Assert.assertNull(docStore.getDocument(uri));
        }
        docStore.undo();
        for (URI uri: uriSet) {
            Assert.assertNotNull(docStore.getDocument(uri));
        }

    }

    @Test
    public void testLastUndoOnDeleteAllWithPrefix(){
        DocumentStoreImpl docStore = new DocumentStoreImpl();
        initializeStore(docStore);
        Set<URI> uriSet = docStore.deleteAllWithPrefix("some");
        for (URI uri: uriSet) {
            Assert.assertNull(docStore.getDocument(uri));
        }
        docStore.undo();
        for (URI uri: uriSet) {
            Assert.assertNotNull(docStore.getDocument(uri));
        }

    }

    @Test(expected = IllegalStateException.class)
    public void testCommandStackWithNonexistentURI() throws URISyntaxException {
        DocumentStoreImpl documentStore1 = new DocumentStoreImpl();
        putDocumentTXTInStore(documentStore1, new URI("www.newDoc1"), "doc1");
        putDocumentTXTInStore(documentStore1, new URI("www.newDoc2"), "doc2");
        URI uriNew = new URI("www.whoops.com");
        documentStore1.undo(uriNew);

    }

    @Test(expected = IllegalStateException.class)
    public void testCommandStackExceptionHandling(){
        DocumentStoreImpl documentStore1 = new DocumentStoreImpl();
        documentStore1.undo();
    }

    @Test
    public void testCommandStack() throws URISyntaxException {
        DocumentStoreImpl documentStore1 = new DocumentStoreImpl();
        URI uriNew = new URI("www.whoops.com");
        putDocumentTXTInStore(documentStore1, uriNew, "hi");
        initializeStore(documentStore1);
        documentStore1.undo(uriNew);
    }

    @Test
    public void testDeleteOnlyOneInCommandSet(){
        DocumentStoreImpl docStore = new DocumentStoreImpl();
        initializeStore(docStore);
        Document doc1 = docStore.getDocument(this.uri1);
        Document doc2 = docStore.getDocument(this.uri2);
        Document doc3 = docStore.getDocument(this.uri3);
        Document doc4 = docStore.getDocument(this.uri4);
        Assert.assertNotNull(doc1);
        Assert.assertNotNull(doc2);
        Assert.assertNotNull(doc3);
        Assert.assertNotNull(doc4);
        Assert.assertNotNull(docStore.getDocument(this.uri1));
        Assert.assertNotNull(docStore.getDocument(this.uri2));
        Assert.assertNotNull(docStore.getDocument(this.uri3));
        Assert.assertNotNull(docStore.getDocument(this.uri4));


        docStore.deleteAllWithPrefix("ze");
        Assert.assertNull(docStore.getDocument(this.uri1));
        Assert.assertNull(docStore.getDocument(this.uri2));
        Assert.assertNotNull(docStore.getDocument(this.uri3));
        Assert.assertNotNull(docStore.getDocument(this.uri4));
        Assert.assertTrue(docStore.deleteDocument(this.uri4));
        Assert.assertTrue(docStore.deleteDocument(this.uri3));
        Assert.assertNull(docStore.getDocument(this.uri1));
        Assert.assertNull(docStore.getDocument(this.uri2));
        Assert.assertNull(docStore.getDocument(this.uri3));
        Assert.assertNull(docStore.getDocument(this.uri4));
        docStore.undo(this.uri2);
        Assert.assertNotNull(docStore.getDocument(this.uri2));
        docStore.undo();
        Assert.assertNotNull(docStore.getDocument(this.uri3));
        docStore.undo(this.uri1);
        docStore.undo(this.uri1);
        docStore.undo(this.uri4);
        docStore.undo();
        Assert.assertNull(docStore.getDocument(this.uri1));
        Assert.assertNotNull(docStore.getDocument(this.uri2));
        Assert.assertNotNull(docStore.getDocument(this.uri3));
        Assert.assertNull(docStore.getDocument(this.uri4));
        docStore.undo();
        docStore.undo();
        Assert.assertNull(docStore.getDocument(this.uri1));
        Assert.assertNull(docStore.getDocument(this.uri2));
        Assert.assertNull(docStore.getDocument(this.uri3));
        Assert.assertNull(docStore.getDocument(this.uri4));
    }

    @Test(expected = IllegalStateException.class)
    public void testErrorUndo(){
        DocumentStore documentStore = new DocumentStoreImpl();
        documentStore.undo();
    }

    @Test(expected = IllegalStateException.class)
    public void testErrorUndo1() throws URISyntaxException {
        DocumentStore documentStore = new DocumentStoreImpl();
        documentStore.undo(new URI("www.yu.edu/cs"));
    }

    @Test
    public void testBadSearchInput(){
        DocumentStore documentStore = new DocumentStoreImpl();
        initializeStore(documentStore);
        Assert.assertEquals(new ArrayList<>(), documentStore.search(null));
    }

///////////////////End of My Tests ///////////////////////////////////////

    private void putDocumentTXTInStore(DocumentStore docStore, URI uri, String text){
        docStore.putDocument(txtToInputStream(text), uri, DocumentStore.DocumentFormat.TXT);
    }

    private void initializeStore(DocumentStore docStore){
        Assert.assertNotNull(docStore);
        Assert.assertEquals(0, docStore.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT));
        Assert.assertEquals(0, docStore.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        Assert.assertEquals(0,  docStore.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        Assert.assertEquals(0, docStore.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT));
    }

    private DocumentImpl getDoc(URI uri){
        return (DocumentImpl) documentStore.getDocument(uri);
    }

    private void addDoc(DocumentImpl doc, DocumentStore.DocumentFormat format){
        if (doc == null){
            throw new IllegalArgumentException();
        }
        if (doc.getKey() == null){
            throw new IllegalArgumentException();
        }
        if (format == txt){
            documentStore.putDocument(txtToInputStream(doc.getDocumentAsTxt()), doc.getKey(), txt);
            return;
        }
        if (format == pdf){
            documentStore.putDocument(bytesToInputStream(doc.getDocumentAsPdf()), doc.getKey(), pdf);
            return;
        }
        throw new IllegalArgumentException();
    }

    private DocumentImpl createDocument(URI uri, String text, DocumentStore.DocumentFormat format) throws IOException {
        if (uri == null || format == null){
            throw new IllegalArgumentException();
        }
        if (format == txt){
            return new DocumentImpl(uri, text, text.hashCode());
        }
        if (format == pdf){
            return new DocumentImpl(uri, text, text.hashCode(), txtToPDF(text));
        }
        throw new IllegalArgumentException();
    }

    private URI add1() throws URISyntaxException{
        URI uri = new URI("http://URI-Add1/Method");
        String docText = "Add1 method sample text.";
        Assert.assertEquals(0, documentStore.putDocument(txtToInputStream(docText), uri, txt));
        Assert.assertNotNull(documentStore.getDocumentAsTxt(uri));
        URI uri1 = new URI("https://www.google.com");
        Assert.assertNotEquals(uri1,uri);
        return uri;
    }

    private URI add2() throws URISyntaxException{
        URI uri = new URI("http://URI-Add3/Method");
        String docText = "Add2 method sample text.";
        Assert.assertEquals(0, documentStore.putDocument(txtToInputStream(docText), uri, txt));
        Assert.assertNotNull(documentStore.getDocumentAsPdf(uri));
        URI uri1 = new URI("https://www.google.com");
        Assert.assertNotEquals(uri1,uri);
        return uri;
    }

    private URI add3() throws URISyntaxException{
        URI uri = new URI("http://URI-Add3/Method");
        String docText = "Add3 method sample text.";
        Assert.assertEquals(0, documentStore.putDocument(txtToInputStream(docText), uri, txt));
        Assert.assertEquals(documentStore.getDocumentAsTxt(uri).hashCode(), docText.hashCode());
        URI uri1 = new URI("https://www.google.com");
        Assert.assertNotEquals(uri1,uri);
        return uri;
    }

    private InputStream txtToInputStream(String textString){
        byte[] bytes = textString.getBytes(StandardCharsets.UTF_8);
        return new ByteArrayInputStream(bytes);
    }

    private byte[] txtToPDF(String string) throws IOException {
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
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            document.close();
            return out.toByteArray();
        } catch (IOException e){
            throw new IOException();
        }
    }

    private InputStream bytesToInputStream(byte[] bytes){
        return new ByteArrayInputStream(bytes);
    }

}