package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;
import org.junit.Assert;
import org.junit.Test;
import edu.yu.cs.com1320.project.stage4.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

public class MinHeapImplTest {

    @Test
    public void constructorExists() {
        try{
            MinHeapImpl<String> minHeap = new MinHeapImpl<>();
        }catch (Exception e){}
    }

    @Test
    public void testInsert() {
        try{
            MinHeapImpl<String> minHeap = new MinHeapImpl<>();
            minHeap.insert("A");
        }catch (Exception e){}
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertNull() {
        MinHeapImpl<String> minHeap = new MinHeapImpl<>();
        minHeap.insert(null);
    }

    @Test
    public void testRemoveMin(){
        MinHeapImpl<String> minHeap = new MinHeapImpl<>();
        String s1 = "A";
        String s2 = "Z";
        minHeap.insert(s2);
        minHeap.insert(s1);
        Assert.assertEquals(s1, minHeap.removeMin());
    }

    @Test(expected = NoSuchElementException.class)
    public void testRemoveEmpty(){
        MinHeapImpl<String> minHeap = new MinHeapImpl<>();
        minHeap.removeMin();
    }

    @Test(expected = NoSuchElementException.class)
    public void testReheapifyError(){
        MinHeapImpl<String> minHeap = new MinHeapImpl<>();
        minHeap.reHeapify("S");
    }

    @Test(expected = NoSuchElementException.class)
    public void testReheapifyNull(){
        MinHeapImpl<String> minHeap = new MinHeapImpl<>();
        minHeap.reHeapify(null);
    }

    @Test
    public void testReheapify(){
        MinHeapImpl<String> minHeap = new MinHeapImpl<>();
        minHeap.insert("A");
        minHeap.reHeapify("A");
        Assert.assertEquals("A", minHeap.removeMin());
    }

    @Test
    public void test1(){
        MinHeapImpl<String> testHeap = new MinHeapImpl<>();

        testHeap.insert("T");
        testHeap.insert("R");
        testHeap.insert("S");
        testHeap.insert("P");
        testHeap.insert("N");
        testHeap.insert("O");
        testHeap.insert("A");
        testHeap.insert("E");
        testHeap.insert("I");

        Assert.assertEquals("A", testHeap.removeMin());
    }

    @Test
    public void test2() throws URISyntaxException {
        MinHeapImpl<Document> testDocumentHeap = new MinHeapImpl<>();

        URI uri1 = new URI("http://doc1");
        String txt1 = "doc1";
        URI uri2 = new URI("http://doc2");
        String txt2 = "doc2";

        Document doc1 = new DocumentImpl(uri1, txt1, txt1.hashCode());
        Document doc2 = new DocumentImpl(uri2, txt2, txt2.hashCode());
        doc1.setLastUseTime(System.nanoTime());
        doc2.setLastUseTime(System.nanoTime());
        testDocumentHeap.insert(doc1);
        testDocumentHeap.insert(doc2);

        doc1.setLastUseTime(0);
        testDocumentHeap.reHeapify(doc1);

        Assert.assertEquals(doc1, testDocumentHeap.removeMin());
    }
}