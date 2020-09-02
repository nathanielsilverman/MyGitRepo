package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.Map;

public class BTreeImpl2Test {

    @Test
    public void testBTreeImpl() throws Exception {
        File file = new File("/Users/nachumsilverman/testStage5");
        DocumentPersistenceManager dpm = new DocumentPersistenceManager(file);
        BTreeImpl<URI, Document> bTree = new BTreeImpl<>();
        bTree.setPersistenceManager(dpm);

        URI uri1 = new URI("http://www.uri1/doc1");
        URI uri2 = new URI("http://www.uri2/doc2");
        URI uri3 = new URI("http://www.uri3/doc3");
        URI uri4 = new URI("http://www.uri4/doc4");
        String string1 = "String doc1 this, that, and the other shoe.";
        String string2 = "String doc2 , stringing string together forever, thanks";
        String string3 = "String doc3 short string, but still a string";
        String string4 = "doc4 wait-for-it";

        DocumentImpl doc1 = new DocumentImpl(uri1, string1, string1.hashCode());
        DocumentImpl doc2 = new DocumentImpl(uri2, string2, string2.hashCode());
        DocumentImpl doc3 = new DocumentImpl(uri3, string3, string3.hashCode());
        DocumentImpl doc4 = new DocumentImpl(uri4, string4, string4.hashCode());

        bTree.put(uri1, doc1);
        bTree.put(uri2, doc2);
        bTree.put(uri3, doc3);
        bTree.put(uri4, doc4);

        Assert.assertNotNull(bTree.get(uri1));
        bTree.moveToDisk(uri1);
        File path = new File(file.getAbsolutePath() + File.separator + uri1.getHost() + uri1.getPath() + ".json");
        Assert.assertTrue(path.exists());
        Assert.assertEquals(doc1, dpm.deserialize(uri1));
        Assert.assertFalse(path.exists());
    }

    @Test
    public void testBTreeImpl1() throws Exception {
        File file = new File("/Users/nachumsilverman/testStage5");
        DocumentPersistenceManager dpm = new DocumentPersistenceManager(file);
        BTreeImpl<URI, Document> bTree = new BTreeImpl<>();
        bTree.setPersistenceManager(dpm);

        URI uri1 = new URI("http://www.yu.edu/uri1/doc1");
        URI uri2 = new URI("http://www.yu.edu/uri2/doc2");
        URI uri3 = new URI("http://www.yu.edu/uri3/doc3");
        URI uri4 = new URI("http://www.yu.edu/uri4/doc4");
        String string1 = "String doc1 this, that, and the other shoe.";
        String string2 = "String doc2 , stringing string together forever, thanks";
        String string3 = "String doc3 short string, but still a string";
        String string4 = "doc4 wait-for-it";

        DocumentImpl doc1 = new DocumentImpl(uri1, string1, string1.hashCode());
        DocumentImpl doc2 = new DocumentImpl(uri2, string2, string2.hashCode());
        DocumentImpl doc3 = new DocumentImpl(uri3, string3, string3.hashCode());
        DocumentImpl doc4 = new DocumentImpl(uri4, string4, string4.hashCode());

        bTree.put(uri1, doc1);
        bTree.put(uri2, doc2);
        bTree.put(uri3, doc3);
        bTree.put(uri4, doc4);

        Assert.assertNotNull(bTree.get(uri1));
        bTree.moveToDisk(uri1);
        bTree.moveToDisk(uri2);
        System.out.println(" ");

        Document document = dpm.deserialize(uri1);
        URI newURI = document.getKey();
        String text = document.getDocumentAsTxt();
        int hash = document.getDocumentTextHashCode();
        Map<String, Integer> wordCounts = document.getWordMap();

        Assert.assertEquals(newURI, uri1);
        Assert.assertEquals(text, string1);
        Assert.assertEquals(hash, string1.hashCode());
        Assert.assertEquals(wordCounts, doc1.getWordMap());

        Document document2 = dpm.deserialize(uri2);
        URI newURI2 = document2.getKey();
        String text2 = document2.getDocumentAsTxt();
        int hash2 = document2.getDocumentTextHashCode();
        Map<String, Integer> wordCounts2 = document2.getWordMap();

        Assert.assertEquals(newURI2, uri2);
        Assert.assertEquals(text2, string2);
        Assert.assertEquals(hash2, string2.hashCode());
        Assert.assertEquals(wordCounts2, doc2.getWordMap());

    }

    @Test
    public void testBtreeStringInt() {
        BTreeImpl<String, Integer> bTree = new BTreeImpl<>();
        bTree.put("A", 1);
        bTree.put("B" ,2);
        bTree.put("C" ,3);
        bTree.put("D", 4);
        bTree.put("F", 5);
        bTree.put("G", 6);
        bTree.put("H", 7);
        bTree.put("I", 9);
    }
}