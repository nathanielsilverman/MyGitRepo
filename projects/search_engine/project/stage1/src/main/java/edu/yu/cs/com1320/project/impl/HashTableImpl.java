package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

import java.lang.Object;
import java.util.Objects;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {

    private Object[] hashTable;

    public HashTableImpl() {
        int defaultSize = 5;
        this.hashTable = new EntryNode[defaultSize];
    }

    private class EntryNode<Key, Value> extends Object {

        private final Key key;
        private Value value;
        private EntryNode<Key, Value> next;

        private EntryNode(Key k, Value v) {
            try {
                if (k == null || v == null) {
                    throw new IllegalArgumentException("null key/value");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("illegalArgumentException");
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
        boolean hasKey = containsKey(k);
        if (hasKey) {
            return replaceValue(getEntryNodeWithKey(k), v);
        }
        if (v != null) {
            EntryNode<Key, Value> newNode = new EntryNode<>(k, v);
            int index = hashFunction(k);
            addToHashTable(newNode, index);
        }
        return null;
    }

    /**
     * @param k check if the key is contained in the hashTable
     * @return if the key is present in the hashTable, return true. If the hashTable doesn't contain the key, return false;
     */
    private boolean containsKey(Key k) {
        if (k != null) {
            int index = hashFunction(k);
            if (!(isIndexNull(index))){
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
     * @param k the key to calculate the hashing to determine what index in the hashTable to place key into.
     * @return an integer equal to the hashcode of the key modulo (%) the length of the hashTable array.
     */
    private int hashFunction(Key k) {
        if (k != null) {
            return ((k.hashCode() & 0x7fffffff) % hashTable.length);
        }
        return 1;
    }

    /**
     * @param k the key to retrieve the EntryNode with the matching key.
     * @return if the hashTable contains the EntryNode with matching key, return a reference to the EntryNode. Else, return null
     */
    private EntryNode<Key, Value> getEntryNodeWithKey(Key k) {
        if (k != null) {
            int index = hashFunction(k);
            if (!(isIndexNull(index))) {
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
     * @param index the index which to add the the newEntryNode
     */
    private void addToHashTable(EntryNode < Key, Value > newNode, int index){
        if (newNode != null) {
            if (!(isIndexNull(index))) {
                EntryNode<Key, Value> node = castToEntryNode(hashTable[index]);
                while (node != null) {
                    if (node.next == null) {
                        node.next = newNode;
                        return;
                    }
                    node = node.next;
                }
            }
            if (hashTable[index] == null) {
                hashTable[index] = newNode;
            }
        }
    }

    /**
     * @param i the index to determine if the index is null
     * @return if hashTable[i] is null, return true. If hashTable[i] != null, return false.
     */
    private boolean isIndexNull(int i){
        return hashTable[i] == null;
    }

    /**
     * @param k the key of the EntryNode to remove from the hashTable.
     */
    private void removeEntryNode(Key k){
        if (k != null) {
            int index = hashFunction(k);
            if (!(isIndexNull(index))) {
                EntryNode<Key, Value> node = castToEntryNode(hashTable[index]);
                if (node.key == k) {
                    hashTable[hashFunction(k)] = node.next;
                    return;
                }
                while (node.next != null) {
                    if (node.next.key == k) {
                        node.next = node.next.next;
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
}

