package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {

    private Node<Value> root; // root of trie

    public TrieImpl() {
        this.root = new Node<>();
        this.root.values = null;
    }

    private class Node<value> {
        private List<value> values;
        private Node<Value>[] links;

        public Node() {
            this.values = new ArrayList<>();
            this.links = castToNodeArray(new Node[36]);
        }

        private boolean isNull() {
            return hasNoLinks() && hasNoValues();
        }

        private boolean hasNoLinks() {
            boolean hasLinks = false;
            if (this.links == null) {
                return true;
            }
            for (Node<Value> link : this.links) {
                if (link != null) {
                    if (!link.isNull())
                        hasLinks = true;
                    break;
                }
            }
            return hasLinks;
        }

        private boolean hasNoValues() {
            if (this.values == null) {
                return true;
            }
            return this.values.isEmpty();
        }

        @SuppressWarnings("unchecked")
        private Node<Value>[] castToNodeArray(Object[] nodes) {
            return (Node<Value>[]) nodes;
        }
    }

    /**
     * add the given value at the given key
     * @param key the key to put in the trie
     * @param val the value to be associated with the given key
     */
    @Override
    public void put(String key, Value val) {
        if (key == null || val == null) {
            return;
        }
        this.root = put(this.root, key.toUpperCase(), val, 0);
    }

    /**
     * get all exact matches for the given key, sorted in descending order.
     * Search is CASE INSENSITIVE.
     * @param key the key which to get all the corresponding values of.
     * @param comparator used to sort  values
     * @return a List of matching Values, in descending order
     */
    @Override
    public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
        if (key == null) {
            return new ArrayList<>();
        }
        Node<Value> node = this.get(this.root, key.toUpperCase(), 0);
        if (node == null) {
            return new ArrayList<>();
        }
        node.values.sort(comparator);
        return node.values;
    }

    /**
     * get all matches which contain a String with the given prefix, sorted in descending order.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE INSENSITIVE.
     * @param prefix the prefix of the words that the requested Values contain.
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order
     */
    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        if (prefix == null) {
            return new ArrayList<>();
        }
        List<Value> valuesWithPrefix = getAllWithPrefix(prefix.toUpperCase());
        List<Value> listOfValues = new ArrayList<>();
        if (valuesWithPrefix != null) {
            for (Value value : valuesWithPrefix) {
                if (value != null) {
                    if (!listOfValues.contains(value)) {
                        listOfValues.add(value);
                    }
                }
            }
            listOfValues.sort(comparator);
        }
        return listOfValues;
    }

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE INSENSITIVE.
     * @param prefix the prefix of the words that the requested Values contain.
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {
        if (prefix == null) {
            return new HashSet<>();
        }
        prefix = prefix.toUpperCase();
        List<Value> allWithPrefix = getAllWithPrefix(prefix);
        Set<Value> valueSet = new HashSet<>();
        if (allWithPrefix != null) {
            for (Value val : allWithPrefix) {
                if (val != null) {
                    valueSet.add(val);
                }
            }
        }
        removeSubtree(prefix);
        return valueSet;
    }

    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     * @param key the key of the associated values to delete
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAll(String key) {
        if (key == null) {
            return new HashSet<>();
        }
        key = key.toUpperCase();
        Node<Value> node = get(this.root, key, 0);
        Set<Value> setOfDeletedValues = new HashSet<>();
        if (node != null) {
            setOfDeletedValues.addAll(node.values);
            node.values.clear();
            if (node.isNull()) {
                removeSubtree(key);
            }
        }
        return setOfDeletedValues;
    }

    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     * @param key the key of the value to delete.
     * @param val the value associated with the key to delete.
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */
    @Override
    public Value delete(String key, Value val) {
        if (key == null || val == null) {
            return null;
        }
        key = key.toUpperCase();
        Node<Value> node = get(this.root, key, 0);
        if (node != null && node.values != null) {
            if ((node.values.contains(val))) {
                int i = node.values.indexOf(val);
                Value removedValue = node.values.remove(i);
                if (node.isNull()) {
                    removeSubtree(key);
                }
                return removedValue;
            }
        }
        return null;
    }

    private void removeSubtree(String keyword) {
        keyword = keyword.toUpperCase();
        char char1 = keyword.charAt(keyword.length()-1);
        int i = charToInt(char1);
        String keyWord = keyword.substring(0, keyword.length()-1);
        Node<Value> node = this.get(this.root, keyWord, 0);
        if (node != null && node.links != null) {
            node.links[i] = null;
        }
    }

    private Node<Value> put(Node<Value> node, String key, Value val, int d) {
        if (node == null) {
            node = new Node<>();
        }
        if (d == key.length()) {
            if (node.values != null) {
                if (!node.values.contains(val)) {
                    node.values.add(val);
                }
            }
            return node;

        }
        char c = key.charAt(d);
        int i = charToInt(c);
        node.links[i] = this.put(node.links[i], key, val, d + 1);
        return node;
    }

    private Node<Value> get(Node<Value> node, String key, int d) {
        if (node == null) {
            return null;
        }
        if (d == key.length()) {
            return node;
        }
        char c = key.charAt(d);
        int i = charToInt(c);
        return this.get(node.links[i], key, d + 1);
    }

    private List<Value> getAllWithPrefix(String prefix) {
        prefix = prefix.toUpperCase();
        Node<Value> node = get(this.root, prefix, 0);
        List<Value> valuesList = new ArrayList<>();
        if (node != null) {
            Queue<Value> results = new ArrayDeque<>();
            this.collect(node, new StringBuilder(prefix), results);
            while (!results.isEmpty()) {
                Value value = results.remove();
                if (!valuesList.contains(value)) {
                    valuesList.add(value);
                }
            }
            return valuesList;
        }
        return null;
    }

    private void collect(Node<Value> node, StringBuilder prefix, Queue<Value> results) {
        if (node == null) {
            return;
        }
        if (node.values != null) {
            results.addAll(node.values);
        }
        if (node.links != null) {
            for (int c = 0; c < node.links.length; c++) {
                if (node.links[c] != null) {
                    prefix.append(intToChar(c));
                    this.collect(node.links[c], prefix, results);
                    prefix.deleteCharAt(prefix.length() - 1);
                }
            }
        }
    }

    private int charToInt(char c) {
        if (c >= 48 && c <= 57) {
            return (c - 48);
        }
        if (c >= 65 && c <= 90) {
            return  (c - 55);
        }
        if (c >= 97 && c <= 122) {
            return (c - 87);
        }
        throw new IllegalArgumentException();
    }

    private char intToChar(int i) {
        if (i <= 9 && i >= 0) {
            return (char) (i + 48);
        }
        if (i <= 35 && i >= 10) {
            return (char) (i + 55);
        }
        throw new IllegalArgumentException();
    }
}
