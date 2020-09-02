package edu.yu.cs.com1320.project.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class TrieImplTest {

    TrieImpl<String> trie = new TrieImpl<>();
    private Comparator<String> stringComparator = String::compareTo;

    @Before
    public void init(){
        trie.put("bat", "bat");
        trie.put("ball", "ball");
        trie.put("ball", "basketball");
        trie.put("bait", "bait");
        trie.put("ape", "ape");
        trie.put("apple", "apple");
        trie.put("a", "a");
        trie.put("at", "at");
        trie.put("elephant", "elephant");
        trie.put("eel", "eel");
        trie.put("314", "314");
        trie.put("814", "814");
        trie.put("420", "420");
    }

    @Test
    public void testGetAllSorted(){
        Assert.assertEquals(new ArrayList<>(Arrays.asList("ball","basketball")), trie.getAllSorted("ball", this.stringComparator));
    }

    @Test
    public void testGetAllWithPrefixSorted(){
        Assert.assertEquals(new ArrayList<>(Arrays.asList("a","ape", "apple","at")),trie.getAllWithPrefixSorted("a", this.stringComparator));
        Assert.assertEquals(new ArrayList<>(Arrays.asList("bait","ball","basketball", "bat")), trie.getAllWithPrefixSorted("ba", this.stringComparator));
    }

    @Test
    public void testDelete(){
        Assert.assertEquals("basketball", trie.delete("ball", "basketball"));
        Assert.assertEquals(new ArrayList<>(Arrays.asList("bait","ball", "bat")), trie.getAllWithPrefixSorted("ba", this.stringComparator));

    }

    @Test
    public void testDeleteAll(){
        Assert.assertEquals(new HashSet<>(new ArrayList<>(Arrays.asList("ball", "basketball"))), trie.deleteAll("ball"));
    }

    @Test
    public void testDeleteAllWithPrefix() {
        Assert.assertEquals(new ArrayList<>(Arrays.asList("bait", "ball", "basketball", "bat")), trie.getAllWithPrefixSorted("ba", this.stringComparator));
        Assert.assertEquals(new HashSet<>(new ArrayList<>(Arrays.asList("ape", "apple"))), trie.deleteAllWithPrefix("ap"));
    }

    @Test
    public void testAddToTrie1(){
        trie.put("boat", "boat");
        trie.put("boar", "boar");
        Assert.assertEquals(new ArrayList<>(Arrays.asList("ball","basketball")), trie.getAllSorted("ball", this.stringComparator));
        Assert.assertEquals(new ArrayList<>(Arrays.asList("a","ape", "apple","at")),trie.getAllWithPrefixSorted("a", this.stringComparator));
        Assert.assertEquals(new ArrayList<>(Arrays.asList("bait","ball","basketball", "bat", "boar", "boat")), trie.getAllWithPrefixSorted("b", this.stringComparator));
        Assert.assertEquals("ball", trie.delete("ball", "ball"));
        Assert.assertEquals(new HashSet<>(new ArrayList<>(Collections.singletonList("basketball"))), trie.deleteAll("ball"));
        Assert.assertEquals(new HashSet<>(new ArrayList<>(Arrays.asList("bait", "bat"))),trie.deleteAllWithPrefix("ba"));
        Assert.assertEquals(new HashSet<>(new ArrayList<>(Arrays.asList("boar", "boat"))),trie.deleteAllWithPrefix("b"));
        Assert.assertEquals(new ArrayList<>(Collections.emptyList()), trie.getAllWithPrefixSorted("b", this.stringComparator));
    }
}