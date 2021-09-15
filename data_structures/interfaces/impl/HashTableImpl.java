package dataStrctImpl.myJava.dataStructures.interfaces.impl;

import edu.yu.cs.com1320.project.HashTable;
import java.lang.Object;
import java.util.Objects;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {

    private EntryNode<Key, Value>[] hashTable;
    private EntryNode<Key, Value>[] hashTableDoubled;
    private int count = 0;

    public HashTableImpl() {
        Object[] temp = new EntryNode[5];
        this.hashTable = castToEntryNodeArray(temp);
    }

    private class EntryNode<key, value> {
        private final key key;
        private value value;
        private EntryNode<key, value> next;

        private EntryNode(key k, value v) {
            if (k == null || v == null) {
                throw new IllegalArgumentException("null key/value");
            }
            this.key = k;
            this.value = v;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntryNode<?, ?> node = castToEntryNode(o);
            return key.equals(node.key) &&
                    value.equals(node.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key) & 0x7fffffff;
        }
    }

    /**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */
    public Value get(Key k) {
        if (k == null){
            throw new IllegalArgumentException("null key");
        }
        EntryNode<Key, Value> node = getEntryNodeWithKey(k);
        if (node != null) {
            return node.value;
        }
        return null;
    }

    /**
     * @param k the key at which to store the value
     * @param v the value to store
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */
    public Value put(Key k, Value v) {
        if (k == null){
            throw new IllegalArgumentException("null key");
        }
        if (getLoadFactor() >= 4) {
            resizeAndRehash();
        }
        boolean hasKey = containsKey(k);
        if (!hasKey) {
            if (v != null) {
                EntryNode<Key, Value> newNode = new EntryNode<>(k, v);
                addToHashTable(hashTable, newNode);
            }
        }
        else {
            return replaceValue(getEntryNodeWithKey(k), v);
        }
        return null;
    }

    private double getLoadFactor(){
        return (double) count/hashTable.length;
    }

    /**
     * @param k check if the key is contained in the hashTable
     * @return if the key is present in the hashTable, return true. If the hashTable doesn't contain the key, return false;
     */
    private boolean containsKey(Key k) {
        if (k != null) {
            int index = hashFunction(hashTable, k);
            if (hashTable[index] != null){
                EntryNode<Key, Value> node = castToEntryNode(hashTable[index]);
                while (node != null) {
                    if (node.key == k) {
                        return true;
                    }
                    node = node.next;
                }
            }
        }
        return false;
    }

    /**
     * @param k the key to retrieve the EntryNode with the matching key.
     * @return if the hashTable contains the EntryNode with matching key, return a reference to the EntryNode. Else, return null
     */
    private EntryNode<Key, Value> getEntryNodeWithKey(Key k) {
        if (k != null) {
            int index = hashFunction(hashTable, k);
            if (!(isIndexNull(hashTable, index))) {
                EntryNode<Key, Value> node = castToEntryNode(hashTable[index]);
                while (node != null) {
                    if (node.key == k) {
                        return node;
                    }
                    node = node.next;
                }
            }
        }
        return null;
    }

    /**
     * @param node the EntryNode with which you want to change the value
     * @param newValue the new value to be assigned to the EntryNode
     * @return the pre-existing value
     */
    private Value replaceValue (EntryNode < Key, Value > node, Value newValue){
        if (node != null) {
            if (newValue == null) {
                Value value = node.value;
                removeEntryNode(node.key);
                return value;
            }
            if (newValue.hashCode() != node.value.hashCode()) {
                Value oldValue = node.value;
                node.value = newValue;
                return oldValue;
            }
        }
        return null;
    }

    /**
     * @param newNode an EntryNode that does not yet exist in the table.
     */
    private void addToHashTable(EntryNode<Key, Value>[] entryNodeArray, EntryNode < Key, Value > newNode){
        if (newNode != null) {
            int index = hashFunction(entryNodeArray, newNode.key);
            if (!(isIndexNull(entryNodeArray, index))) {
                newNode.next = entryNodeArray[index];
            }
            entryNodeArray[index] = newNode;
            count++;
        }
    }

    /**
     * @param entryNodeArray given an EntryNode<Key, Value>[] compute use
     * @param k the key to calculate the hashing to determine what index in the entryNodeArray to place key into.
     * @return an integer equal to the hashcode of the key modulo (%) the length of the entryNodeArray.
     */
    private int hashFunction(EntryNode<Key, Value>[] entryNodeArray, Key k) {
        if (k == null || entryNodeArray.length == 0) {
            throw new IllegalArgumentException();
        }
        return ((k.hashCode() & 0x7fffffff) % entryNodeArray.length);
    }

    /**
     * @param i the index to determine if the index is null
     * @return if hashTable[i] is null, return true. If hashTable[i] != null, return false.
     */
    private boolean isIndexNull(EntryNode<Key, Value>[] entryNodeArray, int i){
        return entryNodeArray[i] == null;
    }

    /**
     * @param k the key of the EntryNode to remove from the hashTable.
     */
    private void removeEntryNode(Key k){
        if (k != null) {
            int index = hashFunction(hashTable, k);
            if (!(isIndexNull(hashTable, index))) {
                EntryNode<Key, Value> node = castToEntryNode(hashTable[index]);
                if (node.key == k) {
                    hashTable[hashFunction(hashTable, k)] = node.next;
                    this.count--;
                    return;
                }
                while (node.next != null) {
                    if (node.next.key == k) {
                        node.next = node.next.next;
                        this.count--;
                        break;
                    }
                    node = node.next;
                }
            }
        }
    }

    /**
     * @param o object to cast as an EntryNode. Suppresses un-checked exception warnings
     * @return the object "casted" as an EntryNode
     */
    @SuppressWarnings("unchecked")
    private EntryNode<Key, Value> castToEntryNode (Object o){
        return (EntryNode<Key, Value>) o;
    }

    /**
     * @param o object [] to cast as an EntryNode[]. Suppresses un-checked exception warnings
     * @return the object[] "casted" as an EntryNode[]
     */
    @SuppressWarnings("unchecked")
    private EntryNode<Key, Value>[] castToEntryNodeArray (Object[] o){
        return (EntryNode<Key, Value>[]) o;
    }

    private void resizeAndRehash() {
        this.hashTableDoubled = getDoubledArray();
        this.hashTable = rehashHashTable();
        this.hashTableDoubled = null;
    }

    private EntryNode<Key, Value>[] getDoubledArray(){
        Object[] arrayDoubled = new EntryNode[this.hashTable.length * 2];
        return castToEntryNodeArray(arrayDoubled);
    }

    private EntryNode<Key, Value>[] rehashHashTable(){
        for (EntryNode<Key, Value> node : castToEntryNodeArray(this.hashTable)) {
            if (node != null) {
                reHash(node);
            }
        }
        return castToEntryNodeArray(hashTableDoubled);
    }

    private void reHash(EntryNode<Key, Value> node) {
        rehashEntryNode(node);
        if (node.next != null){
            EntryNode<Key, Value> nextNode = node.next;
            node.next = null;
            reHash(nextNode);
        }
    }

    private void rehashEntryNode(EntryNode<Key, Value> nodeToRehash){
        Key key = nodeToRehash.key;
        int index = (key.hashCode() & 0x7fffffff) % this.hashTableDoubled.length;
        if (this.hashTableDoubled[index] != null){
            EntryNode<Key, Value> node = castToEntryNode(this.hashTableDoubled[index]);
            while (node!= null){
                if (node.next == null) {
                    node.next = nodeToRehash;
                    return;
                }
                node = node.next;
            }
        }
        else {
            this.hashTableDoubled[index] = nodeToRehash;
        }
    }
}

