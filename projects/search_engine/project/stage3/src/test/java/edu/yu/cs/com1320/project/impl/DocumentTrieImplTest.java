package edu.yu.cs.com1320.project.impl;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;

import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class DocumentTrieImplTest {

    TrieImpl<Document> trie = new TrieImpl<>();

    private Document doc1;
    private Document doc2;
    private Document doc3;
    private Document doc4;
    private Document doc4a;

    @Before
    public void init(){
        try {
            doc1 = new DocumentImpl(new URI("uri1/www"), ("String doc1 this, that, and the other shoe."), ("String doc1 this that the other thing").hashCode());
            doc2 = new DocumentImpl(new URI("uri2/www"), ("String doc2 , stringing string together forever, thanks"), ("String doc2 , stringing strings together forever, thanks").hashCode());
            doc3 = new DocumentImpl(new URI("uri3/www"), ("String doc3 short string, but still a string"), ("String doc3 short string, but still a string").hashCode());
            doc4 = new DocumentImpl(new URI("uri4/www.yu.edu"), ("doc4 wait-for-it"), ("doc4 wait-for-it").hashCode());
            doc4a = new DocumentImpl(new URI("uri4/www.yu.edu"), ("doc4 waitforit"), ("doc4 waitforit").hashCode());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        addDocumentToTrie(doc1);
        addDocumentToTrie(doc2);
        addDocumentToTrie(doc3);
        addDocumentToTrie(doc4);
    }

    @Test
    public void testGetAllSorted(){
        Assert.assertEquals(new ArrayList<>(Arrays.asList(doc3, doc2, doc1)), trie.getAllSorted("String", wordOccurrenceComparator("String")));
    }

    @Test
    public void testGetAllSorted2(){
        Assert.assertEquals(new ArrayList<>(Arrays.asList(doc4)), trie.getAllSorted("waitforit", wordOccurrenceComparator("waitforit")));
    }

    @Test
    public void testStringSplitter1(){
        String doc4txt = doc4.getDocumentAsTxt();
        String doc4atxt = doc4a.getDocumentAsTxt();
        String[] arr1 = stringSplitter(doc4txt);
        String[] arr2 = stringSplitter(doc4atxt);
        Assert.assertArrayEquals(arr1, arr2);
    }

    @Test
    public void testGetAllWithPrefixSorted(){
        Assert.assertEquals(new ArrayList<>(Arrays.asList(doc1, doc2)), trie.getAllWithPrefixSorted("th", prefixOccurrenceComparator("th")));
        Assert.assertFalse(trie.getAllWithPrefixSorted("th", prefixOccurrenceComparator("th")).contains(doc3));
    }


    @Test
    public void testDelete(){
        Assert.assertEquals(doc1, trie.delete("String", doc1));
        Assert.assertFalse(trie.getAllSorted("String",  wordOccurrenceComparator("String")).contains(doc1));
        Assert.assertTrue(trie.getAllSorted("String",  wordOccurrenceComparator("String")).contains(doc2));
        Assert.assertTrue(trie.getAllSorted("String",  wordOccurrenceComparator("String")).contains(doc3));
    }

    @Test
    public void testDeleteAll(){
        Assert.assertEquals(new HashSet<>(new ArrayList<>(Arrays.asList(doc3, doc2, doc1))), trie.deleteAll("String"));
    }

    @Test
    public void testDeleteAllWithPrefix(){
        Assert.assertEquals(new HashSet<>(new ArrayList<>(Arrays.asList(doc3, doc1))), trie.deleteAllWithPrefix("sh"));
    }

    private void addDocumentToTrie(Document doc){
        if (doc != null){
            String[] words = stringSplitter(doc.getDocumentAsTxt());
            Set<String> documentWords = new HashSet<>();
            for (String word: words) {
                if (word != null){
                    if (!(documentWords.contains(word))) {
                        documentWords.add(word);
                        trie.put(word, doc);
                    }
                }
            }
        }
    }

    private String[] stringSplitter(String text){
        text = text.toUpperCase();
        text = text.replaceAll("[^a-zA-Z0-9\\s]", "");
        return text.split("[\\s]");
    }

    private Comparator<Document> wordOccurrenceComparator(String word){
        String keyword = word.toUpperCase();
        return new Comparator<Document>() {
            @Override
            public int compare(Document o1, Document o2) {
                return o2.wordCount(keyword) - o1.wordCount(keyword);
            }
        };
    }

    private Comparator<Document> prefixOccurrenceComparator(String keyword){
        String prefix = keyword.toUpperCase();
        return new Comparator<Document>() {
            @Override
            public int compare(Document o1, Document o2) {
                return getPrefixCount(o2, prefix) - getPrefixCount(o1, prefix);
            }
        };
    }

    private int getPrefixCount(Document document, String prefix){
        if (document == null){
            throw new IllegalArgumentException();
        }
        prefix = prefix.toUpperCase();
        String[] documentWords = stringSplitter(document.getDocumentAsTxt());
        int count = 0;
        for (String documentWord : documentWords) {
            if (documentWord != null) {
                if (documentWord.startsWith(prefix)) {
                    count++;
                }
            }
        }
        return count;
    }

}