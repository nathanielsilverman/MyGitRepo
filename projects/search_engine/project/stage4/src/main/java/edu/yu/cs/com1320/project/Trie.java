package edu.yu.cs.com1320.project;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * FOR STAGE 3
 * @param <Value>
 */
public interface Trie<Value>
{
    /**
     * add the given value at the given key
     * @param key
     * @param val
     */
    void put(String key, Value val);

    /**
     * get all exact matches for the given key, sorted in descending order.
     * Search is CASE INSENSITIVE.
     * @param key
     * @return a List of matching Values, in descending order
     */
    List<Value> getAllSorted(String key, Comparator<Value> comparator);

    /**
     * get all matches which contain a String with the given prefix, sorted in descending order.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE INSENSITIVE.
     * @param prefix
     * @return a List of all matching Values containing the given prefix, in descending order
     */
    List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator);

    /**
     * Delete all matches that contain a String with the given prefix.
     * Search is CASE INSENSITIVE.
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    Set<Value> deleteAllWithPrefix(String prefix);

    /**
     * delete ALL exact matches for the given key
     * @param key
     * @return a Set of all Values that were deleted.
     */
    Set<Value> deleteAll(String key);

    /**
     * delete ONLY the given value from the given key. Leave all other values.
     * @param key
     * @param val
     * @return if there was a Value already at that key, return that previous Value. Otherwise, return null.
     */
    Value delete(String key, Value val);
}