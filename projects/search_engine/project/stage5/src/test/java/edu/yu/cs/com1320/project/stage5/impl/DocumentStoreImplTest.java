package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.Utils;
import edu.yu.cs.com1320.project.stage5.*;

import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.junit.Assert.*;

public class DocumentStoreImplTest {

    DocumentStoreImpl store;

    //variables to hold possible values for doc1
    private URI uri1;
    private String txt1;
    private byte[] pdfData1;
    private String pdfTxt1;

    //variables to hold possible values for doc2
    private URI uri2;
    private String txt2;
    private byte[] pdfData2;
    private String pdfTxt2;

    //variables to hold possible values for doc3
    private URI uri3;
    private String txt3;
    private byte[] pdfData3;
    private String pdfTxt3;

    //variables to hold possible values for doc4
    private URI uri4;
    private String txt4;
    private byte[] pdfData4;
    private String pdfTxt4;

    private int bytes1;
    private int bytes2;
    private int bytes3;
    private int bytes4;

    @Before
    public void init() throws Exception {
        //init possible values for doc1
        this.uri1 = new URI("http://edu.yu.cs/com1320/project/doc1");
        this.txt1 = "This is the text of doc1, in plain text. No fancy file format - just plain old String. Computer. Headphones.";
        this.pdfTxt1 = "This is some PDF text for doc1, hat tip to Adobe.";
        this.pdfData1 = Utils.textToPdfData(this.pdfTxt1);

        //init possible values for doc2
        this.uri2 = new URI("http://edu.yu.cs/com1320/project/doc2");
        this.txt2 = "Text for doc2. A plain old String.";
        this.pdfTxt2 = "PDF content for doc2: PDF format was opened in 2008.";
        this.pdfData2 = Utils.textToPdfData(this.pdfTxt2);

        //init possible values for doc3
        this.uri3 = new URI("http://edu.yu.cs/com1320/project/doc3");
        this.txt3 = "This is the text of doc3";
        this.pdfTxt3 = "This is some PDF text for doc3, hat tip to Adobe.";
        this.pdfData3 = Utils.textToPdfData(this.pdfTxt3);

        //init possible values for doc4
        this.uri4 = new URI("http://edu.yu.cs/com1320/project/doc4");
        this.txt4 = "This is the text of doc4";
        this.pdfTxt4 = "This is some PDF text for doc4, which is open source.";
        this.pdfData4 = Utils.textToPdfData(this.pdfTxt4);

        this.bytes1 = this.pdfTxt1.getBytes().length + this.pdfData1.length;
        this.bytes2 = this.pdfTxt2.getBytes().length + this.pdfData2.length;
        this.bytes3 = this.pdfTxt3.getBytes().length + this.pdfData3.length;
        this.bytes4 = this.pdfTxt4.getBytes().length + this.pdfData4.length;
    }
    
    @After
    public void cleanUp() {
        if (this.store != null) {
            store.getStoreInformation();
            store.persistenceManagerCleanUp();
        }
    }

    @BeforeClass
    public static void before() {
        System.out.println(" ");
        System.out.println("Running: "  + DocumentStoreImplTest.class.getName());
        System.out.println(" ");
    }

    @AfterClass
    public static void after() {
        System.out.println(" ");
    }

    ////////// Stage 1 Tests ////////////////

    @Test
    public void stage1testPutPdfDocumentNoPreviousDocAtURI(){
        store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        assertTrue(returned == 0);
    }

    @Test
    public void stage1testPutTxtDocumentNoPreviousDocAtURI(){
        store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);
    }

    @Test
    public void stage1testPutDocumentWithNullArguments(){
        store = new DocumentStoreImpl();
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
    public void stage1testPutNewVersionOfDocumentPdf(){
        //put the first version
        store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        assertTrue(returned == 0);
        assertEquals("failed to return correct pdf text",this.pdfTxt1,Utils.pdfDataToText(store.getDocumentAsPdf(this.uri1)));

        //put the second version, testing both return value of put and see if it gets the correct text
        returned = store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri1, DocumentStore.DocumentFormat.PDF);
        assertTrue("should return hashcode of old text",this.pdfTxt1.hashCode() == returned);
        assertEquals("failed to return correct pdf text", this.pdfTxt2,Utils.pdfDataToText(store.getDocumentAsPdf(this.uri1)));
    }

    @Test
    public void stage1testPutNewVersionOfDocumentTxt(){
        //put the first version
        store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);
        assertEquals("failed to return correct text",this.txt1,store.getDocumentAsTxt(this.uri1));

        //put the second version, testing both return value of put and see if it gets the correct text
        returned = store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue("should return hashcode of old text",this.txt1.hashCode() == returned);
        assertEquals("failed to return correct text",this.txt2,store.getDocumentAsTxt(this.uri1));
    }

    @Test
    public void stage1testGetTxtDocAsPdf(){
        store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);
        assertEquals("failed to return correct pdf text",this.txt1,Utils.pdfDataToText(store.getDocumentAsPdf(this.uri1)));
    }

    @Test
    public void stage1testGetTxtDocAsTxt(){
        store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        assertTrue(returned == 0);
        assertEquals("failed to return correct text",this.txt1,store.getDocumentAsTxt(this.uri1));
    }

    @Test
    public void stage1testGetPdfDocAsPdf(){
        store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        assertTrue(returned == 0);
        assertEquals("failed to return correct pdf text",this.pdfTxt1,Utils.pdfDataToText(store.getDocumentAsPdf(this.uri1)));
    }

    @Test
    public void stage1testGetPdfDocAsTxt(){
        store = new DocumentStoreImpl();
        int returned = store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        assertTrue(returned == 0);
        assertEquals("failed to return correct text",this.pdfTxt1,store.getDocumentAsTxt(this.uri1));
    }

    @Test
    public void stage1testDeleteDoc(){
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        store.deleteDocument(this.uri1);
        assertEquals("calling get on URI from which doc was deleted should've returned null", null, store.getDocumentAsPdf(this.uri1));
    }

    @Test
    public void stage1testDeleteDocReturnValue(){
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        //should return true when deleting a document
        assertEquals("failed to return true when deleting a document",true,store.deleteDocument(this.uri1));
        //should return false if I try to delete the same doc again
        assertEquals("failed to return false when trying to delete that which was already deleted",false,store.deleteDocument(this.uri1));
        //should return false if I try to delete something that was never there to begin with
        assertEquals("failed to return false when trying to delete that which was never there to begin with",false,store.deleteDocument(this.uri2));
    }

    ////////// End of Stage 1 Tests ////////////////

    ////////// Stage 2 Tests ////////////////

    @Test
    public void stage2UndoAfterOnePut() throws IllegalStateException {
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
    public void stage2UndoWhenEmptyShouldThrow() throws IllegalStateException {
        DocumentStoreImpl dsi = createStoreAndPutOne();
        //undo after putting only one doc
        dsi.undo();
        dsi.undo();
    }

    @Test(expected=IllegalStateException.class)
    public void stage2UndoByURIWhenEmptyShouldThrow() throws IllegalStateException {
        DocumentStoreImpl dsi = createStoreAndPutOne();
        //undo after putting only one doc
        dsi.undo();
        dsi.undo(this.uri1);
    }

    @Test
    public void stage2undoAfterMultiplePuts() throws IllegalStateException {
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
    public void stage2undoNthPutByURI() throws IllegalStateException {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        //undo put 2 - test before and after
        Document returned = dsi.getDocument(this.uri2);
        assertEquals("should've returned doc with uri2",this.uri2,returned.getKey());
        dsi.undo(this.uri2);
        assertNull("should've returned null - put was undone",dsi.getDocument(this.uri2));
    }

    @Test
    public void stage2undoDelete() throws IllegalStateException {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        assertTrue("text was not as expected",dsi.getDocumentAsTxt(this.uri3).equals(this.txt3));
        dsi.deleteDocument(this.uri3);
        assertNull("doc should've been deleted",dsi.getDocument(this.uri3));
        dsi.undo(this.uri3);
        assertTrue("should return doc3",dsi.getDocument(this.uri3).getKey().equals(this.uri3));
    }

    @Test
    public void stage2UndoNthDeleteByURI() throws IllegalStateException {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        assertTrue("text was not as expected",dsi.getDocumentAsTxt(this.uri3).equals(this.txt3));
        dsi.deleteDocument(this.uri3);
        dsi.deleteDocument(this.uri2);
        assertNull("should've been null",dsi.getDocument(this.uri2));
        dsi.undo(this.uri2);
        assertTrue("should return doc2",dsi.getDocument(this.uri2).getKey().equals(this.uri2));
    }

    @Test
    public void stage2UndoOverwriteByURI() throws IllegalStateException {
        DocumentStoreImpl dsi = createStoreAndPutAll();
        String replacement = "this is a replacement for txt2";
        dsi.putDocument(new ByteArrayInputStream(replacement.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        assertTrue("should've returned replacement text",dsi.getDocument(this.uri2).getDocumentAsTxt().equals(replacement));
        dsi.undo(this.uri2);
        assertTrue("should've returned original text",dsi.getDocument(this.uri2).getDocumentAsTxt().equals(this.txt2));
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

    ////////// End of Stage 2 Tests ////////////////

    ////////// Stage 3 Tests ////////////////

    @Test
    public void stage3Search(){
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);

        List<String> results = store.search("plain");
        assertEquals("expected 2 matches, only received " + results.size(),2,results.size());
        results = store.search("missing");
        assertEquals("expected 0 matches, received " + results.size(),0,results.size());
    }

    @Test
    public void stage3SearchPDFs(){
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri2, DocumentStore.DocumentFormat.PDF);

        List<byte[]> results = store.searchPDFs("pdf");
        assertEquals("expected 2 matches, only received " + results.size(),2,results.size());
        results = store.searchPDFs("missing");
        assertEquals("expected 0 matches, received " + results.size(),0,results.size());
    }

    @Test
    public void stage3DeleteAll(){
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        //search, get results
        List<String> results = store.search("plain");
        assertEquals("expected 2 matches, only received " + results.size(),2,results.size());
        //delete all, get no matches
        store.deleteAll("plain");
        results = store.search("plain");
        assertEquals("expected 0 matches, received " + results.size(),0,results.size());
    }

    @Test
    public void stage3SearchByPrefix(){
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()),this.uri1, DocumentStore.DocumentFormat.TXT);
        store.putDocument(new ByteArrayInputStream(this.txt2.getBytes()),this.uri2, DocumentStore.DocumentFormat.TXT);
        //search, get results
        List<String> results = store.searchByPrefix("str");
        assertEquals("expected 2 matches, only received " + results.size(),2,results.size());
        results = store.searchByPrefix("comp");
        assertEquals("expected 1 match, only received " + results.size(),1,results.size());
        results = store.searchByPrefix("doc2");
        assertEquals("expected 1 match, only received " + results.size(),1,results.size());
        results = store.searchByPrefix("blah");
        assertEquals("expected 0 match, received " + results.size(),0,results.size());
    }

    @Test
    public void stage3SearchPDFsByPrefix(){
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri2, DocumentStore.DocumentFormat.PDF);
        //search, get results
        List<byte[]> results = store.searchPDFsByPrefix("pd");
        assertEquals("expected 2 matches, only received " + results.size(),2,results.size());
        results = store.searchPDFsByPrefix("ado");
        assertEquals("expected 1 match, only received " + results.size(),1,results.size());
        results = store.searchPDFsByPrefix("blah");
        assertEquals("expected 0 match, received " + results.size(),0,results.size());
    }

    @Test
    public void stage3DeleteAllWithPrefix(){
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1),this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2),this.uri2, DocumentStore.DocumentFormat.PDF);
        //search, get results
        List<byte[]> results = store.searchPDFsByPrefix("pd");
        assertEquals("expected 2 matches, only received " + results.size(),2,results.size());
        //delete all starting with pd
        store.deleteAllWithPrefix("pd");
        //search again, should be empty
        results = store.searchPDFsByPrefix("pd");
        assertEquals("expected 0 matches, received " + results.size(),0,results.size());
    }

    @Test
    public void stage3TestSearchByKeyword() {
        //put the four docs into the doc store
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
        //search by keyword
        List<String> results = store.search("adobe");
        assertEquals("search should've returned 2 results",2,results.size());
        //make sure we have the correct two documents
        boolean found1, found3;
        found1 = found3 = false;
        String lower1 = this.pdfTxt1.toLowerCase();
        String lower3 = this.pdfTxt3.toLowerCase();
        for(String txt:results){
            if(txt.toLowerCase().equals(lower1)) {
                found1 = true;
            }else if(txt.toLowerCase().equals(lower3)){
                found3 = true;
            }
        }
        assertTrue("should've found doc1 and doc3",found1 && found3);
    }

    @Test
    public void stage3TestSearchByPrefix() {
        //put the four docs into the doc store
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
        //search by prefix
        List<String> results = store.searchByPrefix("ha");
        assertEquals("search should've returned 2 results",2,results.size());
        //make sure we have the correct two documents
        boolean found1, found3;
        found1 = found3 = false;
        String lower1 = this.pdfTxt1.toLowerCase();
        String lower3 = this.pdfTxt3.toLowerCase();
        for(String txt:results){
            if(txt.toLowerCase().equals(lower1)) {
                found1 = true;
            }else if(txt.toLowerCase().equals(lower3)){
                found3 = true;
            }
        }
        assertTrue("should've found doc1 and doc3",found1 && found3);
    }

    @Test
    public void stage3TestDeleteAllByKeyword() {
        //put the four docs into the doc store
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
        //delete by keyword
        store.deleteAll("adobe");
        //search by keyword
        List<String> results = store.search("adobe");
        assertEquals("search should've returned 0 results",0,results.size());
        //make sure the correct two documents were deleted
        assertNull("doc1 should've been deleted",store.getDocument(this.uri1));
        assertNull("doc3 should've been deleted",store.getDocument(this.uri3));
        //make sure the other two documents were NOT deleted
        assertNotNull("doc2 should NOT been deleted",store.getDocument(this.uri2));
        assertNotNull("doc4 should NOT been deleted",store.getDocument(this.uri4));
    }

    @Test
    public void stage3TestDeleteAllByPrefix() {
        //put the four docs into the doc store
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
        String prefix = "ha";
        //delete by prefix
        store.deleteAllWithPrefix(prefix);
        //search by keyword
        List<String> results = store.searchByPrefix(prefix);
        assertEquals("search should've returned 0 results",0,results.size());
        //make sure the correct two documents were deleted
        assertNull("doc1 should've been deleted",store.getDocument(this.uri1));
        assertNull("doc3 should've been deleted",store.getDocument(this.uri3));
        //make sure the other two documents were NOT deleted
        assertNotNull("doc2 should NOT been deleted",store.getDocument(this.uri2));
        assertNotNull("doc4 should NOT been deleted",store.getDocument(this.uri4));
    }

    ////////// End of Stage 3 Tests ////////////////

    ////////// Stage 4 Tests ////////////////

    /*
Every time a document is used, its last use time should be updated to the relative JVM time, as measured in nanoseconds (see java.lang.System.nanoTime().)
A Document is considered to be “used” whenever it is accessed as a result of a call to any part of DocumentStore’s public API. In other words, if it is “put”,
or returned in any form as the result of any “get” or “search” request, or an action on it is undone via any call to either of the DocumentStore.undo methods.
     */

    @Test
    public void stage4TestNoUpdateDocLastUseTimeOnProtectedGet(){
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        Document doc = store.getDocument(this.uri1);
        long first = doc.getLastUseTime();
        doc = store.getDocument(this.uri1);
        long second = doc.getLastUseTime();
        //was last use time updated on the put?
        assertTrue("last use time should NOT be changed when the protected DocStore.getDoc method is called", first == second);
    }

    @Test
    public void stage4TestUpdateDocLastUseTimeOnPut(){
        store = new DocumentStoreImpl();
        long before = System.nanoTime();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        Document doc = store.getDocument(this.uri1);
        //was last use time updated on the put?
        assertTrue("last use time should be after the time at which the document was put", before < doc.getLastUseTime());
    }

    @Test
    public void stage4TestUpdateDocLastUseTimeOnOverwrite(){
        store = new DocumentStoreImpl();
        //was last use time updated on the put?
        long before = System.nanoTime();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        Document doc = store.getDocument(this.uri1);
        assertTrue("last use time should be after the time at which the document was put", before < doc.getLastUseTime());
        before = System.nanoTime();
        //was last use time updated on overwrite?
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri1, DocumentStore.DocumentFormat.PDF);
        Document doc2 = store.getDocument(this.uri1);
        assertTrue("last use time should be after the time at which the document was overwritten", before < doc2.getLastUseTime());
    }

    @Test
    public void stage4TestUpdateDocLastUseTimeOnSearch(){
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        long before = System.nanoTime();
        //this search should return the contents of the doc at uri1
        List<String> results = store.search("pdf");
        Document doc = store.getDocument(this.uri1);
        //was last use time updated on the search?
        assertTrue("last use time should be after the time at which the document was put", before < doc.getLastUseTime());
    }

    @Test
    public void stage4TestUpdateDocLastUseTimeOnSearchByPrefix(){
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        long before = System.nanoTime();
        //this search should return the contents of the doc at uri1
        List<String> results = store.searchByPrefix("pdf");
        Document doc = store.getDocument(this.uri1);
        //was last use time updated on the searchByPrefix?
        assertTrue("last use time should be after the time at which the document was put", before < doc.getLastUseTime());
    }

    @Test
    public void stage4TestUpdateDocLastUseTimeOnSearchPDFs(){
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        long before = System.nanoTime();
        //this search should return the contents of the doc at uri1
        List<byte[]> results = store.searchPDFs("pdf");
        Document doc = store.getDocument(this.uri1);
        //was last use time updated on the searchPDFs?
        assertTrue("last use time should be after the time at which the document was put", before < doc.getLastUseTime());
    }

    @Test
    public void stage4TestUpdateDocLastUseTimeOnSearchPDFsByPrefix(){
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        long before = System.nanoTime();
        //this search should return the contents of the doc at uri1
        List<byte[]> results = store.searchPDFsByPrefix("pdf");
        Document doc = store.getDocument(this.uri1);
        //was last use time updated on the searchPDFs?
        assertTrue("last use time should be after the time at which the document was put", before < doc.getLastUseTime());
    }

    /**
     * test max doc count via put
     */
    @Test
    public void stage4TestMaxDocCountViaPut() {
        store = new DocumentStoreImpl();
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
        //uri1 and uri2 should both be gone, having been pushed out by 3 and 4
        assertNull("uri1 should've been pushed out of memory when uri3 was inserted",store.getDocument(this.uri1));
        assertNull("uri2 should've been pushed out of memory when uri4 was inserted",store.getDocument(this.uri2));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
        assertNotNull("uri4 should still be in memory",store.getDocument(this.uri4));
    }

    /**
     * test max doc count via search
     */
    @Test
    public void stage4TestMaxDocCountViaSearch() {
        store = new DocumentStoreImpl();
        store.setMaxDocumentCount(3);
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        //all 3 should still be in memory
        assertNotNull("uri1 should still be in memory",store.getDocument(this.uri1));
        assertNotNull("uri2 should still be in memory",store.getDocument(this.uri2));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
        //"touch" uri1 via a search
        store.search("doc1");
        //add doc4, doc2 should be pushed out, not doc1
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
        assertNotNull("uri1 should still be in memory",store.getDocument(this.uri1));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
        assertNotNull("uri4 should still be in memory",store.getDocument(this.uri4));
        //uri2 should've been pushed out of memory
        assertNull("uri2 should still be in memory",store.getDocument(this.uri2));
    }

    /**
     * test undo after going over max doc count
     */
    @Test
    public void stage4TestUndoAfterMaxDocCount() {
        store = new DocumentStoreImpl();
        store.setMaxDocumentCount(3);
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        //all 3 should still be in memory
        assertNotNull("uri1 should still be in memory",store.getDocument(this.uri1));
        assertNotNull("uri2 should still be in memory",store.getDocument(this.uri2));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
        //add doc4, doc1 should be pushed out
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
        assertNotNull("uri2 should still be in memory",store.getDocument(this.uri2));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
        assertNotNull("uri4 should still be in memory",store.getDocument(this.uri4));
        //uri1 should've been pushed out of memory
        assertNull("uri1 should still be in memory",store.getDocument(this.uri1));
        //undo the put - should eliminate doc4, and only uri2 and uri3 should be left
        store.undo();
        assertNull("uri4 should be gone due to the undo",store.getDocument(this.uri4));
        assertNotNull("uri2 should still be in memory",store.getDocument(this.uri2));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
    }

    /**
     * test max doc bytes via put
     */
    @Test
    public void stage4TestMaxDocBytesViaPut() {
        store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(this.bytes1 + this.bytes2 + 20);
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
        //uri1 and uri2 should both be gone, having been pushed out by 3 and 4
        assertNull("uri1 should've been pushed out of memory when uri3 was inserted",store.getDocument(this.uri1));
        assertNull("uri2 should've been pushed out of memory when uri4 was inserted",store.getDocument(this.uri2));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
        assertNotNull("uri4 should still be in memory",store.getDocument(this.uri4));
    }

    /**
     * test max doc bytes via search
     */
    @Test
    public void stage4TestMaxDocBytesViaSearch() {
        store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(this.bytes1 + this.bytes2 + this.bytes3 + 20);
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        //all 3 should still be in memory
        assertNotNull("uri1 should still be in memory",store.getDocument(this.uri1));
        assertNotNull("uri2 should still be in memory",store.getDocument(this.uri2));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
        //"touch" uri1 via a search
        store.search("doc1");
        //add doc4, doc2 should be pushed out, not doc1
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
        assertNotNull("uri1 should still be in memory",store.getDocument(this.uri1));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
        assertNotNull("uri4 should still be in memory",store.getDocument(this.uri4));
        //uri2 should've been pushed out of memory
        assertNull("uri2 should still be in memory",store.getDocument(this.uri2));
    }

    /**
     * test undo after going over max doc count
     */
    @Test
    public void stage4TestUndoAfterMaxBytes() {
        store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(this.bytes1 + this.bytes2 + this.bytes3 + 20);
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        //all 3 should still be in memory
        assertNotNull("uri1 should still be in memory",store.getDocument(this.uri1));
        assertNotNull("uri2 should still be in memory",store.getDocument(this.uri2));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
        //add doc4, doc1 should be pushed out
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
        assertNotNull("uri2 should still be in memory",store.getDocument(this.uri2));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
        assertNotNull("uri4 should still be in memory",store.getDocument(this.uri4));
        //uri1 should've been pushed out of memory
        assertNull("uri1 should still be in memory",store.getDocument(this.uri1));
        //undo the put - should eliminate doc4, and only uri2 and uri3 should be left
        store.undo();
        assertNull("uri4 should be gone due to the undo",store.getDocument(this.uri4));
        assertNotNull("uri2 should still be in memory",store.getDocument(this.uri2));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
    }

    /**
     * test going over max docs only when both max docs and max bytes are set
     */
    @Test
    public void stage4TestMaxDocsWhenDoubleMaxViaPut() {
        store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(this.bytes1*10);
        store.setMaxDocumentCount(2);
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
        //uri1 and uri2 should both be gone, having been pushed out by 3 and 4
        assertNull("uri1 should've been pushed out of memory when uri3 was inserted",store.getDocument(this.uri1));
        assertNull("uri2 should've been pushed out of memory when uri4 was inserted",store.getDocument(this.uri2));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
        assertNotNull("uri4 should still be in memory",store.getDocument(this.uri4));
    }

    /**
     * test going over max bytes only when both max docs and max bytes are set
     */
    @Test
    public void stage4TestMaxBytesWhenDoubleMaxViaPut() {
        store = new DocumentStoreImpl();
        store.setMaxDocumentBytes(this.bytes1 + this.bytes2 + 20);
        store.setMaxDocumentCount(20);
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);
        //uri1 and uri2 should both be gone, having been pushed out by 3 and 4
        assertNull("uri1 should've been pushed out of memory when uri3 was inserted",store.getDocument(this.uri1));
        assertNull("uri2 should've been pushed out of memory when uri4 was inserted",store.getDocument(this.uri2));
        assertNotNull("uri3 should still be in memory",store.getDocument(this.uri3));
        assertNotNull("uri4 should still be in memory",store.getDocument(this.uri4));
    }

    ////////// End of Stage 4 Tests ////////////////

    ////////// Stage 5 Tests ////////////////

    @Test
    public void stage5TestDPM() {
        File file = new File(System.getProperty("user.home"));
        DocumentPersistenceManager dpm = new DocumentPersistenceManager(file);
        DocumentImpl document = new DocumentImpl(this.uri1, this.txt1, this.txt1.hashCode());
        try {
            dpm.serialize(this.uri1, document);
        } catch (IOException e) {
            fail("serialize didnt work");
        }
        try {
            Document document1 = dpm.deserialize(this.uri1);
            assertTrue(document.equals(document1));
        } catch (IOException e) {
            fail("deserialize didnt work");
        }
    }

    @Test
    public void stage5TestDPMNullBaseDir() {
        DocumentPersistenceManager dpm = new DocumentPersistenceManager(null);
        DocumentImpl document = new DocumentImpl(this.uri1, this.txt1, this.txt1.hashCode());
        try {
            dpm.serialize(this.uri1, document);
        } catch (IOException e) {
            fail("serialize didnt work");
        }
        try {
            Document document1 = dpm.deserialize(this.uri1);
            assertTrue(document.equals(document1));
        } catch (IOException e) {
            fail("deserialize didnt work");
        }
        assertTrue(dpm.getBaseDir().getAbsolutePath() == System.getProperty("user.dir"));
    }

    @Test
    public void stage5TestRegularPutDocumentWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));
    }

    @Test
    public void stage5TestGetDocumentAsTxtWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));

        assertEquals(this.pdfTxt1, store.getDocumentAsTxt(this.uri1));
        assertEquals(this.pdfTxt2, store.getDocumentAsTxt(this.uri2));
        assertEquals(this.pdfTxt3, store.getDocumentAsTxt(this.uri3));
        assertEquals(this.pdfTxt4, store.getDocumentAsTxt(this.uri4));
    }

    @Test
    public void stage5TestGetDocumentAsPdfWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));

        assertArrayEquals(this.pdfData1, store.getDocumentAsPdf(this.uri1));
        assertArrayEquals(this.pdfData2, store.getDocumentAsPdf(this.uri2));
        assertArrayEquals(this.pdfData3, store.getDocumentAsPdf(this.uri3));
        assertArrayEquals(this.pdfData4, store.getDocumentAsPdf(this.uri4));
    }

    @Test
    public void stage5TestDeleteDocumentWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        assertTrue(store.deleteDocument(this.uri1));

        assertNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestUndoAPutWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        store.undo();

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestUndoAPutWithURIWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        store.undo(this.uri2);

        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestUndoADeleteWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        assertTrue(store.deleteDocument(this.uri1));

        assertNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        store.undo();

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestUndoADeleteWithURIWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        assertTrue(store.deleteDocument(this.uri1));

        assertNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        store.undo(this.uri2);

        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        store.undo();

        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestOverwriteUndoWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        Document document4 = store.getDocument(this.uri4);

        assertEquals(this.pdfTxt4.hashCode(), store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT));

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        assertNotEquals(document4, store.getDocument(this.uri4));

        store.undo();

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        assertEquals(document4, store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestOverwriteUndoWithURIWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        Document document4 = store.getDocument(this.uri4);

        assertEquals(this.pdfTxt4.hashCode(), store.putDocument(new ByteArrayInputStream(this.txt4.getBytes()), this.uri4, DocumentStore.DocumentFormat.TXT));

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        assertNotEquals(document4, store.getDocument(this.uri4));

        store.undo(this.uri4);

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        assertEquals(document4, store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestUndoDoNothingActionWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(null, this.uri4, DocumentStore.DocumentFormat.PDF));

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));

        store.undo();

        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestSearchWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));

        List<String> searched = store.search("PDF");
        assertNotNull(searched);
        assertFalse(searched.isEmpty());

        assertTrue(searched.contains(this.pdfTxt1));
        assertTrue(searched.contains(this.pdfTxt2));
        assertTrue(searched.contains(this.pdfTxt3));
        assertTrue(searched.contains(this.pdfTxt4));
    }

    @Test
    public void stage5TestSearchPDFsWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));


        byte[] doc1pdf = store.getDocumentAsPdf(this.uri1);
        byte[] doc2pdf = store.getDocumentAsPdf(this.uri2);
        byte[] doc3pdf = store.getDocumentAsPdf(this.uri3);
        byte[] doc4pdf = store.getDocumentAsPdf(this.uri4);

        List<byte[]> searched = store.searchPDFs("PDF");
        assertNotNull(searched);

        assertFalse(searched.isEmpty());
        assertTrue(searched.contains(doc1pdf));
        assertTrue(searched.contains(doc2pdf));
        assertTrue(searched.contains(doc3pdf));
        assertTrue(searched.contains(doc4pdf));
    }

    @Test
    public void stage5TestSearchByPrefihWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));

        List<String> searchedByPrefix = store.searchByPrefix("PD");
        assertNotNull(searchedByPrefix);
        assertFalse(searchedByPrefix.isEmpty());

        assertTrue(searchedByPrefix.contains(this.pdfTxt1));
        assertTrue(searchedByPrefix.contains(this.pdfTxt2));
        assertTrue(searchedByPrefix.contains(this.pdfTxt3));
        assertTrue(searchedByPrefix.contains(this.pdfTxt4));
    }

    @Test
    public void stage5TestSearchPDFsByPrefixWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));


        byte[] doc1pdf = store.getDocumentAsPdf(this.uri1);
        byte[] doc2pdf = store.getDocumentAsPdf(this.uri2);
        byte[] doc3pdf = store.getDocumentAsPdf(this.uri3);
        byte[] doc4pdf = store.getDocumentAsPdf(this.uri4);

        List<byte[]> searched = store.searchPDFsByPrefix("PD");
        assertNotNull(searched);

        assertFalse(searched.isEmpty());
        assertTrue(searched.contains(doc1pdf));
        assertTrue(searched.contains(doc2pdf));
        assertTrue(searched.contains(doc3pdf));
        assertTrue(searched.contains(doc4pdf));
    }

    @Test
    public void stage5TestDeleteAllWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));

        Set<URI> deleted = store.deleteAll("PDF");

        assertNotNull(deleted);
        assertFalse(deleted.isEmpty());

        assertTrue(deleted.contains(this.uri1));
        assertTrue(deleted.contains(this.uri2));
        assertTrue(deleted.contains(this.uri3));
        assertTrue(deleted.contains(this.uri4));

        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestDeleteAllWithPrefixWorks() {
        store = new DocumentStoreImpl();
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF));
        assertEquals(0, store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF));

        Set<URI> deleted = store.deleteAllWithPrefix("PD");

        assertNotNull(deleted);
        assertFalse(deleted.isEmpty());

        assertTrue(deleted.contains(this.uri1));
        assertTrue(deleted.contains(this.uri2));
        assertTrue(deleted.contains(this.uri3));
        assertTrue(deleted.contains(this.uri4));

        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestDocumentMovedToDisk() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert that documents 1 and 2 still exists at least on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));
    }

    @Test
    public void stage5TestDocumentThatWasOutOfMemory() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert that documents 1 and 2 still exists at least on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // Brings document 1 back into Memory, moving document3 to disk
        store.getDocumentAsTxt(this.uri1);

        // Documents 1 & 4 should be in memory and documents 2 & 3 should be saved on the disk.
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestDocumentThatWasOutOfMemoryB() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert that documents 1 and 2 still exists at least on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // Brings document 1 back into Memory, moving document3 to disk
        store.getDocumentAsPdf(this.uri1);

        // Documents 1 & 4 should be in memory and documents 2 & 3 should be saved on the disk.
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestDocumentSearch() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert that documents 1 and 2 still exists at least on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // Brings document 1 back into Memory, moving document3 to disk
        store.getDocumentAsPdf(this.uri1);

        // Documents 1 & 4 should be in memory and documents 2 & 3 should be saved on the disk.
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Documents 1 & 3 should be in found in this search. Documents 2 & 4 should be moved to disk
        store.search("hat");
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestDocumentSearchByPDF() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert that documents 1 and 2 still exists at least on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // Brings document 1 back into Memory, moving document3 to disk
        store.getDocumentAsPdf(this.uri1);

        // Documents 1 & 4 should be in memory and documents 2 & 3 should be saved on the disk.
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Documents 1 & 3 should be in found in this search. Documents 2 & 4 should be moved to disk
        List<byte[]> pdfSearch = store.searchPDFs("hat");
        assertNotNull(pdfSearch);
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));

        assertTrue(pdfSearch.contains(store.getDocument(this.uri1).getDocumentAsPdf()));
        assertTrue(pdfSearch.contains(store.getDocument(this.uri3).getDocumentAsPdf()));
    }

    @Test
    public void stage5TestDocumentSearchByPrefix() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert that documents 1 and 2 still exists at least on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // Brings document 1 back into Memory, moving document3 to disk
        store.getDocumentAsPdf(this.uri3);

        // Documents 3 & 4 should be in memory and documents 1 & 2 should be saved on the disk.
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Documents 1 & 3 should be in found in this search. Documents 2 & 4 should be moved to disk
        List<String> prefixSearch = store.searchByPrefix("Ado");
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));

        assertTrue(prefixSearch.contains(store.getDocument(this.uri1).getDocumentAsTxt()));
        assertTrue(prefixSearch.contains(store.getDocument(this.uri3).getDocumentAsTxt()));
    }

    @Test
    public void stage5TestDocumentSearchPDFByPrefix() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert that documents 1 and 2 still exists at least on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // Brings document 1 back into Memory, moving document3 to disk
        store.getDocumentAsPdf(this.uri3);

        // Documents 3 & 4 should be in memory and documents 1 & 2 should be saved on the disk.
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Documents 1 & 3 should be in found in this search. Documents 2 & 4 should be moved to disk
        List<byte[]> prefixSearchPDF = store.searchPDFsByPrefix("Ado");
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));

        assertTrue(prefixSearchPDF.contains(store.getDocument(this.uri1).getDocumentAsPdf()));
        assertTrue(prefixSearchPDF.contains(store.getDocument(this.uri3).getDocumentAsPdf()));
    }

    @Test
    public void stage5TestDeleteDocumentThatWasOnDisk() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // delete Document 1 from disk
        assertTrue(store.deleteDocument(this.uri1));

        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        // Assert Documents 2 and 3 are on the disk
        assertNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));
    }

    @Test
    public void stage5TestDeleteDocumentThatWasOnDiskThenUndo() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));
        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));
        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // delete Document 1 from disk
        assertTrue(store.deleteDocument(this.uri1));

        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        // Assert Documents 2 and 3 are on the disk
        assertNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));

        // undo delete on document 1
        store.undo();

        // Asserts Document 1 is back in memory with document 4
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert Documents 2 and 3 are on the disk
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));
    }

    @Test
    public void stage5TestPutDocumentThatWasOnDiskThenUndo() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));
        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));
        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // overwrite document 1
        assertEquals(document1.getDocumentTextHashCode(), store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT));

        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        // Assert Documents 2 and 3 are on the disk
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));

        assertFalse(document1.equals(store.getDocument(this.uri1)));

        // undo put overriding a put on document 1
        store.undo();

        assertTrue(document1.equals(store.getDocument(this.uri1)));

        // Asserts Document 1 is back in memory with document 4
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert Documents 2 and 3 are on the disk
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));
    }

    @Test
    public void stage5TestPutDocumentThatWasOnDiskThenUndoThenDeleteDocumentOnDisk() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));
        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));
        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // overwrite document 1
        assertEquals(document1.getDocumentTextHashCode(), store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT));

        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        // Assert Documents 2 and 3 are on the disk
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));

        assertFalse(document1.equals(store.getDocument(this.uri1)));

        // undo put overriding a put on document 1
        store.undo();

        assertTrue(document1.equals(store.getDocument(this.uri1)));

        // Asserts Document 1 is back in memory with document 4
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert Documents 2 and 3 are on the disk
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));

        assertTrue(store.deleteDocument(this.uri3));

        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));

        assertNotNull(store.peekDocument(this.uri2));
        assertNull(store.peekDocument(this.uri3));
        assertNotNull(store.peekDocument(this.uri4));
    }


    @Test
    public void stage5TestDeleteAll() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));

        // Assert Documents 3 and 4 are in memory (BTree/Heap)
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));

        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));

        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // Delete all documents the contain the word "Adobe"
        Set<URI> allDeletedWithAdobe = store.deleteAll("Adobe");
        assertFalse(allDeletedWithAdobe.size() == 0);

        // Assert deleted document contain document's 1 and 3 uris
        assertTrue(allDeletedWithAdobe.contains(this.uri1));
        assertTrue(allDeletedWithAdobe.contains(this.uri3));

        // Assert document store doesn't contain documents 1 & 3 in memory or on disk.
        assertNull(store.peekDocument(this.uri1));
        assertNull(store.peekDocument(this.uri3));

        // And Asserts Documents 2 & 4 are still on disk
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri4));

        // Asserts documents 2 & 4 are still in the document store but are on the disk.
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri4));
    }

    @Test
    public void stage5TestDeleteAllThenUndo() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));

        // Assert Documents 3 and 4 are in memory (BTree/Heap)
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));

        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));

        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // Delete all documents the contain the word "Adobe"
        Set<URI> allDeletedWithAdobe = store.deleteAll("Adobe");
        assertFalse(allDeletedWithAdobe.size() == 0);

        // Assert deleted document contain document's 1 and 3 uris
        assertTrue(allDeletedWithAdobe.contains(this.uri1));
        assertTrue(allDeletedWithAdobe.contains(this.uri3));

        // Assert document store doesn't contain documents 1 & 3 in memory or on disk.
        assertNull(store.peekDocument(this.uri1));
        assertNull(store.peekDocument(this.uri3));


        // And Asserts Documents 2 & 4 are still on disk
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri4));

        // Asserts documents 2 & 4 are still in the document store but are on the disk.
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri4));

        // undo delete all with "Adobe"
        store.undo();

        // Assert document store doesn't contain documents 1 & 3 in memory or on disk.
        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri3));

        // And Asserts Documents 2 & 4 are still on disk
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri4));
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri4));
    }

    @Test
    public void stage5TestDeleteAllThenUndoOnSpecificURI() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));

        // Assert Documents 3 and 4 are in memory (BTree/Heap)
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));

        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));

        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // Delete all documents the contain the word "Adobe"
        Set<URI> allDeletedWithAdobe = store.deleteAll("Adobe");
        assertFalse(allDeletedWithAdobe.size() == 0);

        // Assert deleted document contain document's 1 and 3 uris
        assertTrue(allDeletedWithAdobe.contains(this.uri1));
        assertTrue(allDeletedWithAdobe.contains(this.uri3));

        // Assert document store doesn't contain documents 1 & 3 in memory or on disk.
        assertNull(store.peekDocument(this.uri1));
        assertNull(store.peekDocument(this.uri3));


        // And Asserts Documents 2 & 4 are still on disk
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri4));

        // Asserts documents 2 & 4 are still in the document store but are on the disk.
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri4));

        // undo delete all with "Adobe"
        store.undo(this.uri1);

        // Assert document store doesn't contain documents 1 & 3 in memory or on disk.
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri3));
        assertNull(store.peekDocument(this.uri3));


        // And Asserts Documents 2 & 4 are still on disk
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri4));
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri4));
    }

    @Test
    public void stage5TestDeleteAllWithPrefix() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));

        // Assert Documents 3 and 4 are in memory (BTree/Heap)
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));

        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));

        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // Delete all documents the contain the word "Adobe"
        Set<URI> allDeletedWithAdobe = store.deleteAllWithPrefix("Ado");
        assertFalse(allDeletedWithAdobe.size() == 0);

        // Assert deleted document contain document's 1 and 3 uris
        assertTrue(allDeletedWithAdobe.contains(this.uri1));
        assertTrue(allDeletedWithAdobe.contains(this.uri3));

        // Assert document store doesn't contain documents 1 & 3 in memory or on disk.
        assertNull(store.peekDocument(this.uri1));
        assertNull(store.peekDocument(this.uri3));

        // And Asserts Documents 2 & 4 are still on disk
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri4));

        // Asserts documents 2 & 4 are still in the document store but are on the disk.
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri4));
    }

    @Test
    public void stage5TestDeleteAllWithPrefixThenUndo() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));

        // Assert Documents 3 and 4 are in memory (BTree/Heap)
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));

        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));

        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // Delete all documents the contain the word "Adobe"
        Set<URI> allDeletedWithAdobe = store.deleteAllWithPrefix("Ado");
        assertFalse(allDeletedWithAdobe.size() == 0);

        // Assert deleted document contain document's 1 and 3 uris
        assertTrue(allDeletedWithAdobe.contains(this.uri1));
        assertTrue(allDeletedWithAdobe.contains(this.uri3));

        // Assert document store doesn't contain documents 1 & 3 in memory or on disk.
        assertNull(store.peekDocument(this.uri1));
        assertNull(store.peekDocument(this.uri3));

        // And Asserts Documents 2 & 4 are still on disk
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri4));

        // Asserts documents 2 & 4 are still in the document store but are on the disk.
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri4));

        // undo delete all with "Adobe"
        store.undo();

        // Assert document store doesn't contain documents 1 & 3 in memory or on disk.
        assertNotNull(store.getDocument(this.uri1));
        assertNotNull(store.getDocument(this.uri3));

        // And Asserts Documents 2 & 4 are still on disk
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri4));
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri4));
    }

    @Test
    public void stage5TestDeleteAllWithPrefixThenUndoOnSpecificURI() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));

        // Assert Documents 3 and 4 are in memory (BTree/Heap)
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));

        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));

        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // Delete all documents the contain the word "Adobe"
        Set<URI> allDeletedWithAdobe = store.deleteAllWithPrefix("Ado");
        assertFalse(allDeletedWithAdobe.size() == 0);

        // Assert deleted document contain document's 1 and 3 uris
        assertTrue(allDeletedWithAdobe.contains(this.uri1));
        assertTrue(allDeletedWithAdobe.contains(this.uri3));

        // Assert document store doesn't contain documents 1 & 3 in memory or on disk.
        assertNull(store.peekDocument(this.uri1));
        assertNull(store.peekDocument(this.uri3));


        // And Asserts Documents 2 & 4 are still on disk
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri4));

        // Asserts documents 2 & 4 are still in the document store but are on the disk.
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri4));

        // undo delete all with "Adobe"
        store.undo(this.uri1);

        // Assert document store doesn't contain documents 1 & 3 in memory or on disk.
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri3));
        assertNull(store.peekDocument(this.uri3));


        // And Asserts Documents 2 & 4 are still on disk
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri4));
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri4));
    }

    @Test
    public void stage5TestDeleteAllWithNone() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));

        // Assert Documents 3 and 4 are in memory (BTree/Heap)
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));

        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));

        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // Delete all documents the contain the word "Adobe"
        Set<URI> allDeletedWithAdobe = store.deleteAll("Computer");
        assertTrue(allDeletedWithAdobe.size() == 0);

        // Asserts documents 1 & 2 are still in the document store but are on the disk.
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        // Assert document store contains documents 1 & 2 on disk.
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // And Asserts Documents 3 & 4 are still on disk
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestDeleteAllWithPrefixNone() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));

        // Assert Documents 3 and 4 are in memory (BTree/Heap)
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));

        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));

        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // Delete all documents the contain the word "Adobe"
        Set<URI> allDeletedWithAdobe = store.deleteAllWithPrefix("Comp");
        assertTrue(allDeletedWithAdobe.size() == 0);

        // Asserts documents 1 & 2 are still in the document store but are on the disk.
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        // Assert document store contains documents 1 & 2 on disk.
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // And Asserts Documents 3 & 4 are still on disk
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestTestOverwriteDocumentThatWasOnDiskWithItself() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));
        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));
        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // overwrite document 1
        assertEquals(document1.getDocumentTextHashCode(), store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF));

        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        // Assert Documents 2 and 3 are on the disk
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));

        assertTrue(document1.equals(store.getDocument(this.uri1)));

        // undo put overriding a put on document 1
        store.undo();

        assertTrue(document1.equals(store.getDocument(this.uri1)));

        // Asserts Document 1 is back in memory with document 4
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert Documents 2 and 3 are on the disk
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));
    }

    @Test
    public void stage5TestDocumentMovedToDiskWithBytesLimit() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentBytes(this.bytes3 + this.bytes4);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert that documents 1 and 2 still exists at least on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));
    }

    @Test
    public void stage5TestDocumentThatWasOutOfMemoryWithBytesLimit() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentBytes(this.bytes3 + this.bytes4);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert that documents 1 and 2 still exists at least on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // Brings document 1 back into Memory, moving document3 to disk
        store.getDocumentAsTxt(this.uri1);

        // Documents 1 & 4 should be in memory and documents 2 & 3 should be saved on the disk.
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestDocumentThatWasOutOfMemoryBWithBytesLimit() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentBytes(this.bytes3 + this.bytes4);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert that documents 1 and 2 still exists at least on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // Brings document 1 back into Memory, moving document3 to disk
        store.getDocumentAsPdf(this.uri1);

        // Documents 1 & 4 should be in memory and documents 2 & 3 should be saved on the disk.
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestDocumentSearchWithBytesLimit() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentBytes(this.bytes3 + this.bytes4);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert that documents 1 and 2 still exists at least on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // Brings document 1 back into Memory, moving document3 to disk
        store.getDocumentAsPdf(this.uri1);

        // Documents 1 & 4 should be in memory and documents 2 & 3 should be saved on the disk.
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Documents 1 & 3 should be in found in this search. Documents 2 & 4 should be moved to disk
        store.search("hat");
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestDocumentSearchByPDFWithBytesLimit() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentBytes(this.bytes3 + this.bytes4);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert that documents 1 and 2 still exists at least on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // Brings document 1 back into Memory, moving document3 to disk
        store.getDocumentAsPdf(this.uri1);

        // Documents 1 & 4 should be in memory and documents 2 & 3 should be saved on the disk.
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Documents 1 & 3 should be in found in this search. Documents 2 & 4 should be moved to disk
        List<byte[]> pdfSearch = store.searchPDFs("hat");
        assertNotNull(pdfSearch);
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));

        assertTrue(pdfSearch.contains(store.getDocument(this.uri1).getDocumentAsPdf()));
        assertTrue(pdfSearch.contains(store.getDocument(this.uri3).getDocumentAsPdf()));
    }

    @Test
    public void stage5TestDocumentSearchByPrefixWithBytesLimit() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentBytes(this.bytes3 + this.bytes4);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert that documents 1 and 2 still exists at least on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // Brings document 1 back into Memory, moving document3 to disk
        store.getDocumentAsPdf(this.uri3);

        // Documents 3 & 4 should be in memory and documents 1 & 2 should be saved on the disk.
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Documents 1 & 3 should be in found in this search. Documents 2 & 4 should be moved to disk
        List<String> prefixSearch = store.searchByPrefix("Ado");
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));

        assertTrue(prefixSearch.contains(store.getDocument(this.uri1).getDocumentAsTxt()));
        assertTrue(prefixSearch.contains(store.getDocument(this.uri3).getDocumentAsTxt()));
    }

    @Test
    public void stage5TestDocumentSearchPDFByPrefixWithBytesLimit() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentBytes(this.bytes3 + this.bytes4);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert that documents 1 and 2 still exists at least on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // Brings document 1 back into Memory, moving document3 to disk
        store.getDocumentAsPdf(this.uri3);

        // Documents 3 & 4 should be in memory and documents 1 & 2 should be saved on the disk.
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Documents 1 & 3 should be in found in this search. Documents 2 & 4 should be moved to disk
        List<byte[]> prefixSearchPDF = store.searchPDFsByPrefix("Ado");
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));

        assertTrue(prefixSearchPDF.contains(store.getDocument(this.uri1).getDocumentAsPdf()));
        assertTrue(prefixSearchPDF.contains(store.getDocument(this.uri3).getDocumentAsPdf()));
    }

    @Test
    public void stage5TestDeleteDocumentThatWasOnDiskWithBytesLimit() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentBytes(this.bytes3 + this.bytes4);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));

        // delete Document 1 from disk
        assertTrue(store.deleteDocument(this.uri1));

        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        // Assert Documents 2 and 3 are on the disk
        assertNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));
    }

    @Test
    public void stage5TestDeleteDocumentThatWasOnDiskThenUndoWithBytesLimit() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentBytes(this.bytes3 + this.bytes4);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));
        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));
        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // delete Document 1 from disk
        assertTrue(store.deleteDocument(this.uri1));

        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
        // Assert Documents 2 and 3 are on the disk
        assertNull(store.peekDocument(this.uri1));
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));

        // undo delete on document 1
        store.undo();

        // Asserts Document 1 is back in memory with document 4
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));

        // Assert Documents 2 and 3 are on the disk
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));
    }

    @Test
    public void stage5TestPutDocumentThatWasOnDiskThenUndoWithBytesLimit() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        int maxBytes = this.bytes3 + this.bytes4;
        store.setMaxDocumentBytes(maxBytes);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));
        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));
        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // overwrite document 1. new document's bytes pushes documents 3 & 4 to the disk.
        assertEquals(document1.getDocumentTextHashCode(), store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT));

        Document document1A = store.getDocument(this.uri1);
        assertFalse(document1.equals(document1A));

        int bytesDocument1A = document1A.getDocumentAsPdf().length + document1A.getDocumentAsTxt().getBytes().length;

        assertTrue(bytesDocument1A > this.bytes4);

        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));

        // Assert Documents 2 and 3 are on the disk
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));
        assertNotNull(store.peekDocument(this.uri4));

        assertFalse(document1.equals(store.getDocument(this.uri1)));

        // undo put overriding a put on document 1
        store.undo();

        assertTrue(document1.equals(store.getDocument(this.uri1)));

        // Asserts Document 1 is back in memory with document 4
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));

        // Assert Documents 2 and 3 are on the disk
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));
        assertNotNull(store.peekDocument(this.uri4));

        // bring document 4 back into store.
        store.getDocumentAsTxt(this.uri4);

        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri4));
    }

    @Test
    public void stage5TestPutDocumentThatWasOnDiskThenUndoThenDeleteDocumentOnDiskWithBytesLimit() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentBytes(this.bytes3 + this.bytes4);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));
        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));
        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        // overwrite document 1. moves document 4 to disk.
        assertEquals(document1.getDocumentTextHashCode(), store.putDocument(new ByteArrayInputStream(this.txt1.getBytes()), this.uri1, DocumentStore.DocumentFormat.TXT));

        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));

        // Assert Documents 2 and 3 are on the disk
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));
        assertNotNull(store.peekDocument(this.uri4));

        assertFalse(document1.equals(store.getDocument(this.uri1)));

        // undo put overriding a put on document 1
        store.undo();

        assertTrue(document1.equals(store.getDocument(this.uri1)));

        // Asserts Document 1 is back in memory with document 4
        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));

        // Assert Documents 2 and 3 are on the disk
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri3));
        assertNotNull(store.peekDocument(this.uri4));

        assertTrue(store.deleteDocument(this.uri3));

        assertNotNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri3));
        assertNull(store.getDocument(this.uri4));

        assertNotNull(store.peekDocument(this.uri2));
        assertNull(store.peekDocument(this.uri3));
        assertNotNull(store.peekDocument(this.uri4));
    }

    @Test
    public void stage5TestTimes() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));
        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));
        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        long timeDoc1 = document1.getLastUseTime();
        long timeDoc2 = document2.getLastUseTime();
        long timeDoc3 = document3.getLastUseTime();
        long timeDoc4 = document4.getLastUseTime();

        assertTrue(timeDoc4 > timeDoc3);
        assertTrue(timeDoc4 > timeDoc2);
        assertTrue(timeDoc4 > timeDoc1);

        assertTrue(timeDoc3 > timeDoc2);
        assertTrue(timeDoc3 > timeDoc1);

        assertTrue(timeDoc2 > timeDoc1);
    }

    @Test
    public void stage5TestTimesAfterGetAsTXT() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));
        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));
        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        long timeDoc1 = document1.getLastUseTime();
        long timeDoc2 = document2.getLastUseTime();
        long timeDoc3 = document3.getLastUseTime();
        long timeDoc4 = document4.getLastUseTime();

        assertTrue(timeDoc4 > timeDoc3);
        assertTrue(timeDoc4 > timeDoc2);
        assertTrue(timeDoc4 > timeDoc1);

        assertTrue(timeDoc3 > timeDoc2);
        assertTrue(timeDoc3 > timeDoc1);

        assertTrue(timeDoc2 > timeDoc1);

        store.getDocumentAsTxt(this.uri1);
        assertNotNull(store.getDocument(this.uri1));
        document1 = store.getDocument(this.uri1);

        timeDoc1 = document1.getLastUseTime();
        timeDoc2 = document2.getLastUseTime();
        timeDoc3 = document3.getLastUseTime();
        timeDoc4 = document4.getLastUseTime();

        assertTrue(timeDoc1 > timeDoc4);
        assertTrue(timeDoc1 > timeDoc3);
        assertTrue(timeDoc1 > timeDoc2);

        assertTrue(timeDoc4 > timeDoc3);
        assertTrue(timeDoc4 > timeDoc2);
        assertTrue(timeDoc3 > timeDoc2);
    }

    @Test
    public void stage5TestUndoThenSearch() {
        store = new DocumentStoreImpl();
        store.putDocument(new ByteArrayInputStream(this.pdfData1), this.uri1, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData2), this.uri2, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData3), this.uri3, DocumentStore.DocumentFormat.PDF);
        store.putDocument(new ByteArrayInputStream(this.pdfData4), this.uri4, DocumentStore.DocumentFormat.PDF);

        Document document1 = store.peekDocument(this.uri1);
        Document document2 = store.peekDocument(this.uri2);
        Document document3 = store.peekDocument(this.uri3);
        Document document4 = store.peekDocument(this.uri4);

        // Moves documents 1 and 2 out of memory to the disk
        store.setMaxDocumentCount(2);

        // Assert Documents 1 and 2 are no longer in memory (BTree/Heap)
        assertNull(store.getDocument(this.uri1));
        assertNull(store.getDocument(this.uri2));

        // Assert Documents 3 and 4 are in memory (BTree/Heap)
        assertNotNull(store.getDocument(this.uri3));
        assertTrue(document3.equals(store.getDocument(this.uri3)));

        assertNotNull(store.getDocument(this.uri4));
        assertTrue(document4.equals(store.getDocument(this.uri4)));

        // Assert Documents 1 and 2 are on the disk
        assertNotNull(store.peekDocument(this.uri1));
        assertTrue(document1.equals(store.peekDocument(this.uri1)));
        assertNotNull(store.peekDocument(this.uri2));
        assertTrue(document2.equals(store.peekDocument(this.uri2)));

        Set<URI> allDeletedWithAdobe = store.deleteAllWithPrefix("Ado");
        assertFalse(allDeletedWithAdobe.size() == 0);

        // Assert deleted document contain document's 1 and 3 uris
        assertTrue(allDeletedWithAdobe.contains(this.uri1));
        assertTrue(allDeletedWithAdobe.contains(this.uri3));

        // Assert document store doesn't contain documents 1 & 3 in memory or on disk.
        assertNull(store.peekDocument(this.uri1));
        assertNull(store.peekDocument(this.uri3));
        
        // And Asserts Documents 2 & 4 are still on disk
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri4));

        // Asserts documents 2 & 4 are still in the document store but are on the disk.
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri4));

        List<String> searched = store.search("PDF");
        assertTrue(searched.contains(document2.getDocumentAsTxt()));
        assertTrue(searched.contains(document4.getDocumentAsTxt()));

        assertNotNull(store.getDocument(this.uri2));
        assertNotNull(store.getDocument(this.uri4));

        store.undo(this.uri1);
        assertNotNull(store.peekDocument(this.uri1));
        assertNull(store.peekDocument(this.uri3));

        // And Asserts Documents 2 or 4 were moved to disk
        assertTrue(store.getDocument(this.uri2) != null || store.getDocument(this.uri4) != null);
        // Asserts documents 2 & 4 are still in the document store but are on the disk.
        assertNotNull(store.peekDocument(this.uri2));
        assertNotNull(store.peekDocument(this.uri4));

        assertNotNull(store.getDocument(this.uri1));

        // undo on Delete Document 3
        store.undo();

        assertNotNull(store.getDocument(this.uri3));
        assertNotNull(store.getDocument(this.uri1));

        // And Asserts Documents 2 & 4 are still on disk
        assertNull(store.getDocument(this.uri2));
        assertNull(store.getDocument(this.uri4));
    }
}
