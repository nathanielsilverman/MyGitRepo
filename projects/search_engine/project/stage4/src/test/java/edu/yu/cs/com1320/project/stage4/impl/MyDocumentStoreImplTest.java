package edu.yu.cs.com1320.project.stage4.impl;

import edu.yu.cs.com1320.project.Utils;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.stage4.DocumentStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.junit.Assert.*;

public class MyDocumentStoreImplTest {

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
        this.uri1 = new URI("http://doc1");
        this.txt1 = "Document One. Document 1 text.";
        this.pdfTxt1 = "This is some PDF text for document 1.";
        this.pdfData1 = Utils.textToPdfData(this.pdfTxt1);

        //init possible values for doc2
        this.uri2 = new URI("http://doc2");
        this.txt2 = "Text for document 2. Plain old text for testing.";
        this.pdfTxt2 = "PDF document content for document 2:";
        this.pdfData2 = Utils.textToPdfData(this.pdfTxt2);

        //init possible values for doc3
        this.uri3 = new URI("http://doc3");
        this.txt3 = "This is the text of doc 3 written as plain text.";
        this.pdfTxt3 = "This is some PDF text for PDF Doc 3.";
        this.pdfData3 = Utils.textToPdfData(this.pdfTxt3);

        //init possible values for doc4
        this.uri4 = new URI("http://doc4");
        this.txt4 = "Text for doc4.";
        this.pdfTxt4 = "PDF content for doc 4:";
        this.pdfData4 = Utils.textToPdfData(this.pdfTxt4);
    }

    /////////// Judah's Tests //////////////////
    @Test
    public void testPutPdfDocumentNoPreviousDocAtURI(){
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        assertTrue(returned == 0);
    }

    @Test
    public void testPutTxtDocumentNoPreviousDocAtURI(){
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0 );
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
        assertTrue(returned == 0);
        assertEquals("failed to return correct pdf text",this.pdfTxt1,Utils.pdfDataToText(store.getDocumentAsPdf(this.uri1)));

        //put the second version, testing both return value of put and see if it gets the correct text
        returned = store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri1, DocumentStore.DocumentFormat.PDF);
        assertTrue("should return hashcode of old text",this.pdfTxt1.hashCode() == returned);
        assertEquals("failed to return correct pdf text", this.pdfTxt2,Utils.pdfDataToText(store.getDocumentAsPdf(this.uri1)));
    }

    @Test
    public void testPutNewVersionOfDocumentTxt(){
        //put the first version
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);
        assertEquals("failed to return correct text",this.txt1,store.getDocumentAsTxt(this.uri1));

        //put the second version, testing both return value of put and see if it gets the correct text
        returned = store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue("should return hashcode of old text",this.txt1.hashCode() == returned);
        assertEquals("failed to return correct text",this.txt2,store.getDocumentAsTxt(this.uri1));
    }

    @Test
    public void testGetTxtDocAsPdf(){
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);
        assertEquals("failed to return correct pdf text",this.txt1,Utils.pdfDataToText(store.getDocumentAsPdf(this.uri1)));
    }

    @Test
    public void testGetTxtDocAsTxt(){
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);
        assertEquals("failed to return correct text",this.txt1,store.getDocumentAsTxt(this.uri1));
    }

    @Test
    public void testGetPdfDocAsPdf(){
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        assertTrue(returned == 0);
        assertEquals("failed to return correct pdf text",this.pdfTxt1,Utils.pdfDataToText(store.getDocumentAsPdf(this.uri1)));
    }

    @Test
    public void testGetPdfDocAsTxt(){
        DocumentStore store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        assertTrue(returned == 0);
        assertEquals("failed to return correct text",this.pdfTxt1,store.getDocumentAsTxt(this.uri1));
    }

    @Test
    public void testDeleteDoc(){
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        store.deleteDocument(this.uri1);
        assertEquals("calling get on URI from which doc was deleted should've returned null", null, store.getDocumentAsPdf(this.uri1));
    }

    @Test
    public void testDeleteDocReturnValue(){
        DocumentStore store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        //should return true when deleting a document
        assertEquals("failed to return true when deleting a document",true,store.deleteDocument(this.uri1));
        //should return false if I try to delete the same doc again
        assertEquals("failed to return false when trying to delete that which was already deleted",false,store.deleteDocument(this.uri1));
        //should return false if I try to delete something that was never there to begin with
        assertEquals("failed to return false when trying to delete that which was never there to begin with",false,store.deleteDocument(this.uri2));
    }

    @Test
    public void undoAfterOnePut() throws Exception {
        DocumentStoreImpl dsi = createStoreAndPutOne();
        //undo after putting only one doc
        Document doc1 = new DocumentImpl(this.uri1, this.txt1, this.txt1.hashCode());
        Document returned1 = dsi.getDocument(this.uri1);
        assertNotNull("Did not get a document back after putting it in",returned1);
        assertEquals("Did not get doc1 back",doc1.getKey(),returned1.getKey());
        dsi.undo();
        returned1 = dsi.getDocument(this.uri1);
        assertNull("Put was undone - should have been null",returned1);
        try {
            dsi.undo();
            fail("no documents - should've thrown IllegalStateException");
        }catch(IllegalStateException e){}
    }

    @Test(expected=IllegalStateException.class)
    public void undoWhenEmptyShouldThrow() throws Exception {
        DocumentStoreImpl dsi = createStoreAndPutOne();
        //undo after putting only one doc
        dsi.undo();
        dsi.undo();
    }

    @Test(expected=IllegalStateException.class)
    public void undoByURIWhenEmptyShouldThrow() throws Exception {
        DocumentStoreImpl dsi = createStoreAndPutOne();
        //undo after putting only one doc
        dsi.undo();
        dsi.undo(this.uri1);
    }

    @Test
    public void undoAfterMultiplePuts() throws Exception {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        //undo put 4 - test before and after
        Document returned = dsi.getDocument(this.uri4);
        assertEquals("should've returned doc with uri4",this.uri4,returned.getKey());
        dsi.undo();
        assertNull("should've been null - put doc4 was undone",dsi.getDocument(this.uri4));

        //undo put 3 - test before and after
        returned = dsi.getDocument(this.uri3);
        assertEquals("should've returned doc with uri3",this.uri3,returned.getKey());
        dsi.undo();
        assertNull("should've been null - put doc3 was undone",dsi.getDocument(this.uri3));

        //undo put 2 - test before and after
        returned = dsi.getDocument(this.uri2);
        assertEquals("should've returned doc with uri3",this.uri2,returned.getKey());
        dsi.undo();
        assertNull("should've been null - put doc2 was undone",dsi.getDocument(this.uri2));

        //undo put 1 - test before and after
        returned = dsi.getDocument(this.uri1);
        assertEquals("should've returned doc with uri1",this.uri1,returned.getKey());
        dsi.undo();
        assertNull("should've been null - put doc1 was undone",dsi.getDocument(this.uri1));
        try {
            dsi.undo();
            fail("no documents - should've thrown IllegalStateException");
        }catch(IllegalStateException e){}
    }

    @Test
    public void undoNthPutByURI() throws Exception {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        //undo put 2 - test before and after
        Document returned = dsi.getDocument(this.uri2);
        assertEquals("should've returned doc with uri2",this.uri2,returned.getKey());
        dsi.undo(this.uri2);
        assertNull("should've returned null - put was undone",dsi.getDocument(this.uri2));
    }

    @Test
    public void undoDelete() throws Exception {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        assertTrue("text was not as expected",dsi.getDocumentAsTxt(this.uri3).equals(this.txt3));
        dsi.deleteDocument(this.uri3);
        assertNull("doc should've been deleted",dsi.getDocument(this.uri3));
        dsi.undo(this.uri3);
        assertTrue("should return doc3",dsi.getDocument(this.uri3).getKey().equals(this.uri3));
    }

    @Test
    public void undoNthDeleteByURI() throws Exception {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        assertTrue("text was not as expected",dsi.getDocumentAsTxt(this.uri3).equals(this.txt3));
        dsi.deleteDocument(this.uri3);
        dsi.deleteDocument(this.uri2);
        assertNull("should've been null",dsi.getDocument(this.uri2));
        dsi.undo(this.uri2);
        assertTrue("should return doc2",dsi.getDocument(this.uri2).getKey().equals(this.uri2));
    }

    @Test
    public void undoOverwriteByURI() throws Exception {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        String replacement = "this is a replacement for txt2";
        dsi.putDocument(new ByteArrayInputStream(replacement.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        assertTrue("should've returned replacement text",dsi.getDocument(this.uri2).getDocumentAsTxt().equals(replacement));
        dsi.undo(this.uri2);
        assertTrue("should've returned original text",dsi.getDocument(this.uri2).getDocumentAsTxt().equals(this.txt2));
    }

    ///////// My Tests /////////

    @Test
    public void putDocument() {
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        int returned1 = dsi.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        int returned2 = dsi.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        int returned3 = dsi.putDocument(new ByteArrayInputStream(this.txt3.getBytes()),this.uri3, DocumentStore.DocumentFormat.TXT);
        int returned4 = dsi.putDocument(new ByteArrayInputStream(this.pdfData4),this.uri4, DocumentStore.DocumentFormat.PDF);
        Assert.assertEquals(returned1, 0);
        Assert.assertEquals(returned2, 0);
        Assert.assertEquals(returned3, 0);
        Assert.assertEquals(returned4, 0);
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void putDocumentOverwrite(){
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        int returned = dsi.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        Assert.assertEquals(returned, 0);
        int returned1 = dsi.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        Assert.assertEquals(this.pdfTxt1.hashCode(), returned1);
    }

    @Test
    public void putDocumentWithNullInputAsDelete(){
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        int returned = dsi.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        Assert.assertEquals(returned, 0);
        int returned1 = dsi.putDocument(null, this.uri1, DocumentStore.DocumentFormat.PDF);
        Assert.assertEquals(this.pdfTxt1.hashCode(), returned1);
        Assert.assertNull(dsi.getDocument(this.uri1));
    }

    @Test
    public void putDocumentWithNullInputWithEmptyDSI() throws URISyntaxException{
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        Assert.assertEquals(0, dsi.putDocument(null, new URI("uri1"), DocumentStore.DocumentFormat.TXT));
    }

    @Test(expected = URISyntaxException.class)
    public void testPutWithURIException() throws URISyntaxException{
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        Assert.assertEquals(0, dsi.putDocument(null, new URI("%"), DocumentStore.DocumentFormat.TXT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutWithNullURI() {
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        Assert.assertEquals(0, dsi.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), null, DocumentStore.DocumentFormat.TXT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutWithNullFormat() {
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        Assert.assertEquals(0, dsi.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, null));
    }

    @Test
    public void getDocumentAsPdf() {
        DocumentStoreImpl dsi = createStoreAndPutAllPDF();
        Assert.assertEquals(this.pdfTxt1, Utils.pdfDataToText(dsi.getDocumentAsPdf(this.uri1)));
        Assert.assertEquals(this.pdfTxt2, Utils.pdfDataToText(dsi.getDocumentAsPdf(this.uri2)));
        Assert.assertEquals(this.pdfTxt3, Utils.pdfDataToText(dsi.getDocumentAsPdf(this.uri3)));
        Assert.assertEquals(this.pdfTxt4, Utils.pdfDataToText(dsi.getDocumentAsPdf(this.uri4)));
    }

    @Test
    public void getDocumentAsPdf1() {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(this.txt1, Utils.pdfDataToText(dsi.getDocumentAsPdf(this.uri1)));
        Assert.assertEquals(this.txt2, Utils.pdfDataToText(dsi.getDocumentAsPdf(this.uri2)));
        Assert.assertEquals(this.txt3, Utils.pdfDataToText(dsi.getDocumentAsPdf(this.uri3)));
        Assert.assertEquals(this.txt4, Utils.pdfDataToText(dsi.getDocumentAsPdf(this.uri4)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDocumentAsPdfWithNullURI(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(this.pdfData1, dsi.getDocumentAsPdf(null));
    }

    @Test
    public void getDocumentAsPdfWithNoDocuments(){
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        Assert.assertNull(dsi.getDocumentAsPdf(this.uri1));
    }

    @Test
    public void getDocumentAsTxt() {
        DocumentStoreImpl dsi = createStoreAndPutAllPDF();
        Assert.assertEquals(this.pdfTxt1, dsi.getDocumentAsTxt(this.uri1));
        Assert.assertEquals(this.pdfTxt2, dsi.getDocumentAsTxt(this.uri2));
        Assert.assertEquals(this.pdfTxt3, dsi.getDocumentAsTxt(this.uri3));
        Assert.assertEquals(this.pdfTxt4, dsi.getDocumentAsTxt(this.uri4));
    }

    @Test
    public void getDocumentAsTxt1() {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(this.txt1, dsi.getDocumentAsTxt(this.uri1));
        Assert.assertEquals(this.txt2, dsi.getDocumentAsTxt(this.uri2));
        Assert.assertEquals(this.txt3, dsi.getDocumentAsTxt(this.uri3));
        Assert.assertEquals(this.txt4, dsi.getDocumentAsTxt(this.uri4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDocumentAsTxtWithNullURI(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(this.txt1, dsi.getDocumentAsTxt(null));
    }

    @Test
    public void getDocumentAsTxtWithNoDocuments(){
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        Assert.assertNull(dsi.getDocumentAsTxt(this.uri1));
    }

    @Test
    public void deleteDocuments() {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertTrue(dsi.deleteDocument(this.uri1));
        Assert.assertTrue(dsi.deleteDocument(this.uri2));
        Assert.assertTrue(dsi.deleteDocument(this.uri3));
        Assert.assertTrue(dsi.deleteDocument(this.uri4));
        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void undoPuts() {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        dsi.undo();
        Assert.assertNull(dsi.getDocument(this.uri4));
        dsi.undo();
        Assert.assertNull(dsi.getDocument(this.uri3));
        dsi.undo();
        Assert.assertNull(dsi.getDocument(this.uri2));
        dsi.undo();
        Assert.assertNull(dsi.getDocument(this.uri1));
    }

    @Test
    public void undoDeletes(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        //delete all documents
        Assert.assertTrue(dsi.deleteDocument(this.uri1));
        Assert.assertTrue(dsi.deleteDocument(this.uri2));
        Assert.assertTrue(dsi.deleteDocument(this.uri3));
        Assert.assertTrue(dsi.deleteDocument(this.uri4));

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));

        //Undo deletes
        dsi.undo();
        Assert.assertNotNull(dsi.getDocument(this.uri4));
        dsi.undo();
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        dsi.undo();
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        dsi.undo();
        Assert.assertNotNull(dsi.getDocument(this.uri1));

        //Undo puts
        dsi.undo();
        Assert.assertNull(dsi.getDocument(this.uri4));
        dsi.undo();
        Assert.assertNull(dsi.getDocument(this.uri3));
        dsi.undo();
        Assert.assertNull(dsi.getDocument(this.uri2));
        dsi.undo();
        Assert.assertNull(dsi.getDocument(this.uri1));
    }

    @Test
    public void testUndoWithURI() {
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Assert.assertTrue(dsi.deleteDocument(this.uri1));
        Assert.assertTrue(dsi.deleteDocument(this.uri2));
        Assert.assertTrue(dsi.deleteDocument(this.uri3));
        Assert.assertTrue(dsi.deleteDocument(this.uri4));

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));

        dsi.undo(this.uri1);
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));
        dsi.undo(this.uri2);
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));
        dsi.undo(this.uri3);
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));
        dsi.undo(this.uri4);
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void search() {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(new ArrayList<String>(Arrays.asList(this.txt1, this.txt2)), dsi.search("document"));
    }

    @Test
    public void searchWordDoesntExist(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(new ArrayList<String>(), dsi.search("documentary"));
    }

    @Test
    public void searchAfterWasDeleted(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);
        Assert.assertEquals(uriSet, dsi.deleteAll("document"));
        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        Assert.assertEquals(new ArrayList<String>(), dsi.search("document"));
    }

    @Test
    public void searchAfterWasDeletedThenUndo(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);
        Assert.assertEquals(uriSet, dsi.deleteAll("document"));
        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        Assert.assertEquals(new ArrayList<String>(), dsi.search("document"));

        dsi.undo(this.uri2);
        Assert.assertEquals(new ArrayList<String>(Arrays.asList(this.txt2)), dsi.search("document"));
        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.undo();

        Assert.assertEquals(new ArrayList<String>(Arrays.asList(this.txt1, this.txt2)), dsi.search("document"));
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void searchNullInput(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(new ArrayList<>(), dsi.search(null));
    }

    @Test
    public void searchByPrefix() {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(new ArrayList<String>(Arrays.asList(this.txt1, this.txt2)), dsi.searchByPrefix("docu"));
    }

    @Test
    public void searchByPrefixDoesntExist(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(new ArrayList<String>(), dsi.searchByPrefix("je"));
    }

    @Test
    public void searchByPrefixNullInput(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(new ArrayList<>(), dsi.searchByPrefix(null));
    }

    @Test
    public void searchPDFs() {
        DocumentStoreImpl dsi = createStoreAndPutAllPDF();
        Assert.assertEquals(new ArrayList<byte[]>(Arrays.asList(dsi.getDocumentAsPdf(this.uri2), dsi.getDocumentAsPdf(this.uri1))), dsi.searchPDFs("document"));
    }

    @Test
    public void searchPDFsDoesntExist(){
        DocumentStoreImpl dsi = createStoreAndPutAllPDF();
        Assert.assertEquals(new ArrayList<byte[]>(), dsi.searchPDFs("documentary"));
    }

    @Test
    public void searchPDFsNullInput(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(new ArrayList<>(), dsi.searchPDFs(null));
    }

    @Test
    public void searchPDFsByPrefix() {
        DocumentStoreImpl dsi = createStoreAndPutAllPDF();
        Assert.assertEquals(new ArrayList<byte[]>(Arrays.asList(dsi.getDocumentAsPdf(this.uri2), dsi.getDocumentAsPdf(this.uri1))), dsi.searchPDFsByPrefix("docu"));
    }

    @Test
    public void searchPDFsByPrefixDoesntExist(){
        DocumentStoreImpl dsi = createStoreAndPutAllPDF();
        Assert.assertEquals(new ArrayList<byte[]>(), dsi.searchPDFsByPrefix("app"));
    }

    @Test
    public void searchPDFsByPrefixNullInput(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(new ArrayList<>(), dsi.searchPDFsByPrefix(null));
    }

    @Test
    public void deleteAll() {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);
        Assert.assertEquals(uriSet, dsi.deleteAll("document"));
        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void deleteAllDoesntExist(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(new HashSet<>(), dsi.deleteAll("documentary"));
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void deleteAllNullInput(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(new HashSet<>(), dsi.deleteAll(null));
    }

    @Test
    public void undoDeleteAll(){
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);

        Assert.assertEquals(uriSet, dsi.deleteAll("document"));

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
        //undo deleteAll
        dsi.undo();
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void undoDeleteAll1(){
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);

        Assert.assertEquals(uriSet, dsi.deleteAll("document"));

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
        //undo deleteAll
        dsi.undo(this.uri3);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.undo();
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void undoWithURIDeleteAll(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);
        Assert.assertEquals(uriSet, dsi.deleteAll("document"));
        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        //undo deleteAll only on uri2
        dsi.undo(this.uri2);
        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        //undo deleteAll on uri1
        dsi.undo();
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test (expected = IllegalStateException.class)
    public void undoDeleteAllWithURIThatDoesntExist() throws URISyntaxException {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);
        Assert.assertEquals(uriSet, dsi.deleteAll("document"));
        dsi.undo(new URI("www.fakeURI.org"));
    }

    @Test
    public void undoDeleteAllDoesntExist(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(new HashSet<URI>(), dsi.deleteAll("foo"));

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        // undo should undo nothing
        dsi.undo();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void undoDeleteAllOnOneDocument(){
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);

        Assert.assertEquals(uriSet, dsi.deleteAll("1"));

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.undo(this.uri1);

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.undo();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void deleteAllWithPrefix() {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);
        Assert.assertEquals(uriSet, dsi.deleteAllWithPrefix("docum"));
        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void deleteAllWithPrefixDoesntExist(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(new HashSet<>(), dsi.deleteAllWithPrefix("foo"));
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void deleteAllWithPrefixNullInput(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertEquals(new HashSet<>(), dsi.deleteAllWithPrefix(null));
    }

    @Test
    public void undoDeleteAllWithPrefix(){
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);

        Assert.assertEquals(uriSet, dsi.deleteAllWithPrefix("docum"));

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        // undo delete all with prefix
        dsi.undo();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void undoDeleteAllWithPrefix1(){
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);

        Assert.assertEquals(uriSet, dsi.deleteAllWithPrefix("docum"));

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        // undo put on uri3
        dsi.undo(this.uri3);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        // undo deleteAllWithPrefix
        dsi.undo();
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void undoWithURIDeleteAllWithPrefix(){
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);

        Assert.assertEquals(uriSet, dsi.deleteAllWithPrefix("docum"));

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        // undo deleteAllWithPrefix only on uri1
        dsi.undo(this.uri1);

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        // undo deleteAllWithPrefix on uri2
        dsi.undo();
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test(expected = IllegalStateException.class)
    public void undoDeleteAllWithPrefixWithURIThatDoesntExist() throws URISyntaxException{
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);

        Assert.assertEquals(uriSet, dsi.deleteAllWithPrefix("docum"));

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.undo(new URI("www.fakeNews.org"));
    }

    @Test
    public void undoDeleteAllWithPrefixDoesntExist(){
        DocumentStoreImpl dsi = createStoreAndPutAllPDF();

        Assert.assertEquals(new HashSet<>(), dsi.deleteAllWithPrefix("foo"));

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.undo();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

    }

    @Test
    public void undoDeleteAllWithPrefixOnOneDocument(){
        DocumentStoreImpl dsi = createStoreAndPutAllPDF();

        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);

        Assert.assertEquals(uriSet, dsi.deleteAllWithPrefix("1"));

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.undo(this.uri1);

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.undo();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));

    }

    @Test
    public void setMaxDocumentCount() {
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.setMaxDocumentCount(3);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void setMaxDocumentCount1(){
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.setMaxDocumentCount(1);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void setMaxDocumentCountAfterAddingOne(){
        DocumentStoreImpl dsi = createStoreAndPutOne();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));

        dsi.setMaxDocumentCount(3);

        putAll(dsi);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void setMaxDocumentCount0(){
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        dsi.setMaxDocumentCount(0);
        putAll(dsi);
        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void setMaxDocumentCountAfterDeleteDocument(){
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.deleteDocument(this.uri1);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.setMaxDocumentCount(3);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.undo();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void setMaxDocumentCountAfterUndoDeleteAll(){
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.deleteAll("document");

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.setMaxDocumentCount(2);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        // undo deleteAll
        dsi.undo();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void setMaxDocumentCountAfterUndoDeleteAllDoesntExist(){
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.deleteAll("foo");
        dsi.undo();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.setMaxDocumentCount(3);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.undo(this.uri2);
        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.undo();
        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void setMaxDocumentBytes() {
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        int doc1Bytes = getDocumentBytes(dsi, this.uri1);
        int doc2Bytes = getDocumentBytes(dsi, this.uri2);
        int doc3Bytes = getDocumentBytes(dsi, this.uri3);
        int doc4Bytes = getDocumentBytes(dsi, this.uri4);

        int totalBytes = doc1Bytes + doc2Bytes + doc3Bytes + doc4Bytes;

        dsi.setMaxDocumentBytes(totalBytes - doc2Bytes);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void setMaxDocumentBytesAfterAddingOnlyOne(){
        DocumentStoreImpl dsi = createStoreAndPutOne();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));

        int doc1Bytes = getDocumentBytes(dsi, this.uri1);
        dsi.setMaxDocumentBytes(doc1Bytes * 2);

        putAll(dsi);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void setMaxDocumentBytes0(){
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        dsi.setMaxDocumentBytes(0);
        putAll(dsi);
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void setBothLimits(){
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        dsi.setMaxDocumentCount(10);
        dsi.setMaxDocumentBytes(1000);

        putAll(dsi);

        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void setBothLimitsWithUndo(){
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.deleteDocument(this.uri4);

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));

        dsi.setMaxDocumentCount(10);
        dsi.setMaxDocumentBytes(1000);

        dsi.undo();

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void getDocument() {
        DocumentStoreImpl dsi = createStoreAndPutOne();
        Assert.assertNotNull(dsi.getDocument(uri1));
        Assert.assertEquals(this.txt1, dsi.getDocumentAsTxt(this.uri1));
        Assert.assertEquals(this.txt1.hashCode(), dsi.getDocument(this.uri1).getDocumentTextHashCode());
        Assert.assertArrayEquals(this.txt1.getBytes(), dsi.getDocumentAsTxt(this.uri1).getBytes());
    }

    @Test
    public void setBothLimitsReplacingDocuments(){
        DocumentStoreImpl dsi = createStoreAndPutAllPDF();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        Document document1 = dsi.getDocument(this.uri1);
        Document document2 = dsi.getDocument(this.uri2);
        Document document3 = dsi.getDocument(this.uri3);
        Document document4 = dsi.getDocument(this.uri4);

        putAll(dsi);

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        Assert.assertEquals(document1, dsi.getDocument(this.uri1));
        Assert.assertNotEquals(document2, dsi.getDocument(this.uri2));
        Assert.assertNotEquals(document3, dsi.getDocument(this.uri3));
        Assert.assertNotEquals(document4, dsi.getDocument(this.uri4));

        Document document1a = dsi.getDocument(this.uri1);
        Document document2a = dsi.getDocument(this.uri2);
        Document document3a = dsi.getDocument(this.uri3);
        Document document4a = dsi.getDocument(this.uri4);

        Assert.assertEquals(document1, document1a);
        Assert.assertNotEquals(document2, document2a);
        Assert.assertNotEquals(document3, document3a);
        Assert.assertNotEquals(document4, document4a);

        dsi.setMaxDocumentCount(4);
        dsi.setMaxDocumentBytes(2653);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test
    public void setBothLimitsUndoDeleteAll(){
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);
        Assert.assertEquals(uriSet, dsi.deleteAll("document"));

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.setMaxDocumentCount(2);
        dsi.setMaxDocumentBytes(1748);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.undo(this.uri1);

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
    }

    @Test(expected = IllegalStateException.class)
    public void testSetLimitUndo(){
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.setMaxDocumentCount(0);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));

        dsi.undo();
    }

    @Test(expected = IllegalStateException.class)
    public void testSetLimitUndoURI(){
        DocumentStoreImpl dsi = createStoreAndPutAll();

        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));

        dsi.setMaxDocumentCount(0);

        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNull(dsi.getDocument(this.uri3));
        Assert.assertNull(dsi.getDocument(this.uri4));

        dsi.undo(this.uri1);
    }

    @Test
    public void testGetLastUseTime(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Assert.assertTrue(dsi.getDocument(this.uri1).getLastUseTime() < dsi.getDocument(this.uri2).getLastUseTime());
    }

    @Test
    public void testLastUseTime(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);
        Assert.assertEquals(uriSet, dsi.deleteAll("document"));
        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
        dsi.undo();
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
        Assert.assertTrue(dsi.getDocument(this.uri1).getLastUseTime() == dsi.getDocument(this.uri2).getLastUseTime());
    }

    @Test
    public void testLastUseTime1(){
        DocumentStoreImpl dsi = createStoreAndPutAll();
        Set<URI> uriSet = new HashSet<>();
        uriSet.add(this.uri1);
        uriSet.add(this.uri2);
        Assert.assertEquals(uriSet, dsi.deleteAll("document"));
        Assert.assertNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
        dsi.undo(this.uri1);
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
        dsi.undo();
        Assert.assertNotNull(dsi.getDocument(this.uri1));
        Assert.assertNotNull(dsi.getDocument(this.uri2));
        Assert.assertNotNull(dsi.getDocument(this.uri3));
        Assert.assertNotNull(dsi.getDocument(this.uri4));
        Assert.assertTrue(dsi.getDocument(this.uri1).getLastUseTime() < dsi.getDocument(this.uri2).getLastUseTime());
    }

    private DocumentStoreImpl createStoreAndPutOne(){
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        ByteArrayInputStream bas1 = new ByteArrayInputStream(this.txt1.getBytes());
        dsi.putDocument(bas1,this.uri1, DocumentStore.DocumentFormat.TXT);
        return dsi;
    }

    private DocumentStoreImpl createStoreAndPutAll(){
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        //doc1
        ByteArrayInputStream bas = new ByteArrayInputStream(this.txt1.getBytes());
        dsi.putDocument(bas,this.uri1, DocumentStore.DocumentFormat.TXT);
        //doc2
        bas = new ByteArrayInputStream(this.txt2.getBytes());
        dsi.putDocument(bas,this.uri2, DocumentStore.DocumentFormat.TXT);
        //doc3
        bas = new ByteArrayInputStream(this.txt3.getBytes());
        dsi.putDocument(bas,this.uri3, DocumentStore.DocumentFormat.TXT);
        //doc4
        bas = new ByteArrayInputStream(this.txt4.getBytes());
        dsi.putDocument(bas,this.uri4, DocumentStore.DocumentFormat.TXT);
        return dsi;
    }

    private DocumentStoreImpl createStoreAndPutAllPDF(){
        DocumentStoreImpl dsi = new DocumentStoreImpl();
        //doc1
        ByteArrayInputStream bas = new ByteArrayInputStream(this.pdfData1);
        dsi.putDocument(bas,this.uri1, DocumentStore.DocumentFormat.PDF);
        //doc2
        bas = new ByteArrayInputStream(this.pdfData2);
        dsi.putDocument(bas,this.uri2, DocumentStore.DocumentFormat.PDF);
        //doc3
        bas = new ByteArrayInputStream(this.pdfData3);
        dsi.putDocument(bas,this.uri3, DocumentStore.DocumentFormat.PDF);
        //doc4
        bas = new ByteArrayInputStream(this.pdfData4);
        dsi.putDocument(bas,this.uri4, DocumentStore.DocumentFormat.PDF);
        return dsi;
    }

    private void putAll(DocumentStore dsi){
        ByteArrayInputStream bas;
        //doc2
        bas = new ByteArrayInputStream(this.txt2.getBytes());
        dsi.putDocument(bas,this.uri2, DocumentStore.DocumentFormat.TXT);
        //doc3
        bas = new ByteArrayInputStream(this.txt3.getBytes());
        dsi.putDocument(bas,this.uri3, DocumentStore.DocumentFormat.TXT);
        //doc4
        bas = new ByteArrayInputStream(this.txt4.getBytes());
        dsi.putDocument(bas,this.uri4, DocumentStore.DocumentFormat.TXT);
    }

    private int getDocumentBytes(DocumentStoreImpl dsi, URI uri){
        if (dsi.getDocument(uri) != null){
            Document document = dsi.getDocument(uri);
            return document.getDocumentAsTxt().getBytes().length + document.getDocumentAsPdf().length;
        }
        return -1;
    }
}