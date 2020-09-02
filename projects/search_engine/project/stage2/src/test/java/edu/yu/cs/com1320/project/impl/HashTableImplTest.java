package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HashTableImplTest {

 /////////////// Judah's Tests //////////////

    private HashTable<String,String> table;

    @Before
    public void initTable(){
        this.table = new HashTableImpl<>();
        this.table.put("Key1", "Value1");
        this.table.put("Key2","Value2");
        this.table.put("Key3","Value3");
        this.table.put("Key4","Value4");
        this.table.put("Key5","Value5");
        this.table.put("Key6","Value6");
    }
    @Test
    public void testGet() {
        assertEquals("Value1",this.table.get("Key1"));
        assertEquals("Value2",this.table.get("Key2"));
        assertEquals("Value3",this.table.get("Key3"));
        assertEquals("Value4",this.table.get("Key4"));
        assertEquals("Value5",this.table.get("Key5"));
    }
    @Test
    public void testGetChained() {
        //second node in chain
        assertEquals("Value6",this.table.get("Key6"));
        //second node in chain after being modified
        this.table.put("Key6","Value6+1");
        assertEquals("Value6+1",this.table.get("Key6"));
        //check that other values still come back correctly
        testGet();
    }
    @Test
    public void testGetMiss() {
        assertNull(this.table.get("Key20"));
    }
    @Test
    public void testPutReturnValue() {
        assertEquals("Value3",this.table.put("Key3","Value3+1"));
        assertEquals("Value6",this.table.put("Key6", "Value6+1"));
        assertNull(this.table.put("Key7", "Value7"));
    }
    @Test
    public void testGetChangedValue () {
        HashTableImpl<String, String> table = new HashTableImpl<>();
        String key1 = "hello";
        String value1 = "how are you today?";
        String value2 = "HI!!!";
        table.put(key1, value1);
        assertEquals(value1,table.get(key1));
        table.put(key1, value2);
        assertEquals(value2,table.get(key1));
    }
    @Test
    public void testDeleteViaPutNull() {
        HashTableImpl<String, String> table = new HashTableImpl<>();
        String key1 = "hello";
        String value1 = "how are you today?";
        table.put(key1, value1);
        table.put(key1, null);
        assertNull(table.get(key1));
    }
    @Test
    public void testSeparateChaining () {
        HashTableImpl<Integer, String> table = new HashTableImpl<>();
        for(int i = 0; i <= 23; i++) {
            table.put(i, "entry " + i);
        }
        assertEquals("entry 12",table.put(12, "entry 12+1"));
        assertEquals("entry 12+1",table.get(12));
        assertEquals("entry 23",table.get(23));
    }

/////////////// End of Judah's Tests /////////////

//////////////// My tests ////////////////////////
    HashTableImpl<String, String> hashTableImpl = new HashTableImpl<>();
    HashTableImpl<String, Integer> hashTableImpl1 = new HashTableImpl<>();
    HashTableImpl<String, Integer> hashTableImpl2 = new HashTableImpl<>();
    HashTableImpl<String, Integer> hashTableImplDoubled = new HashTableImpl<>();

    @Test
    public void putNodes() {
        Assert.assertNull(hashTableImpl.put("Key1", "Value1"));
        Assert.assertEquals("Value1",  hashTableImpl.get("Key1"));
    }

    @Test
    public void getNodes(){
        Assert.assertNull(hashTableImpl.put("Key2", "Value2"));
        Assert.assertNotEquals("Value1",  hashTableImpl.get("Key2"));
    }

    @Test
    public void addBunchOfNodes1() {
        hashTableImpl.put("Key1", "Value1");
        hashTableImpl.put("Key2", "Value2");
        hashTableImpl.put("Key3", "Value3");
        hashTableImpl.put("Key4", "Value4");
        hashTableImpl.put("Key5", "Value5");
        hashTableImpl.put("Key6", "Value6");
        hashTableImpl.put("Key7", "Value7");
        hashTableImpl.put("Key8", "Value8");
        hashTableImpl.put("Key9", "Value9");
        hashTableImpl.put("Key10", "Value10");
        hashTableImpl.put("Key11", "Value11");
        Assert.assertEquals("Value10", hashTableImpl.put("Key10", "Value100"));
        Assert.assertEquals("Get key didn't work", "Value100", hashTableImpl.get("Key10"));
        Assert.assertEquals("Putting null value didn't work", "Value9", hashTableImpl.put("Key9", null));
        Assert.assertNull(hashTableImpl.get("Key9"));
        hashTableImpl.put("Key12", "Value12");
        Assert.assertEquals("Get key didn't work", "Value5", hashTableImpl.get("Key5"));
        Assert.assertNotEquals("Get key didn't work", "Value1", hashTableImpl.get("Key4"));
        Assert.assertEquals("Put key with null value didn't work", "Value1", hashTableImpl.put("Key1", null));
        Assert.assertNull(hashTableImpl.get("Key1"));
        Assert.assertEquals("Put key with null value didn't work", "Value2", hashTableImpl.put("Key2", null));
        Assert.assertNull(hashTableImpl.put("Key1", "Value1"));
        Assert.assertNull(hashTableImpl.get("Key2"));
        Assert.assertNotNull(hashTableImpl.get("Key1"));
    }

    @Test
    public void testGetNonExistentNodes(){
        Assert.assertNull(hashTableImpl.get("Key1115"));
        Assert.assertNull(hashTableImpl.get("Key115"));
    }

    @Test
    public void testPutNodesStringInt(){
        hashTableImpl1.put("Key1", 1);
        hashTableImpl1.put("Key2", 2);
        hashTableImpl1.put("Key3", 3);
        hashTableImpl1.put("Key4", 4);
        hashTableImpl1.put("Key5", 5);
        hashTableImpl1.put("Key6", 6);
        hashTableImpl1.put("Key7", 7);
        hashTableImpl1.put("Key8", 8);
        hashTableImpl1.put("Key9", 9);
        hashTableImpl1.put("Key10", 10);
        hashTableImpl1.put("Key11", 11);
        Assert.assertEquals((Integer) 11, hashTableImpl1.get("Key11"));
        Assert.assertNotEquals((Integer) 12, hashTableImpl1.get("Key11"));
        Assert.assertEquals((Integer) 1, hashTableImpl1.get("Key1"));
        Assert.assertEquals((Integer) 10, hashTableImpl1.get("Key10"));
        Assert.assertEquals((Integer) 10 , hashTableImpl1.put("Key10", null));
        Assert.assertNotEquals((Integer) 10, hashTableImpl1.get("Key10"));
        Assert.assertNull(hashTableImpl1.put("Key10", 10000));
        Assert.assertEquals((Integer) 10000, hashTableImpl1.get("Key10"));
        Assert.assertEquals((Integer) 10000 , hashTableImpl1.put("Key10", null));
        Assert.assertNull(hashTableImpl1.get("Key10"));
        Assert.assertNull(hashTableImpl1.get("Key"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullKey(){
        hashTableImpl1.put(null, 1);
        hashTableImpl1.get(null);
    }

    @Test
    public void testArrayResized(){
        hashTableImpl2.put("Key1", 1);
        hashTableImpl2.put("Key2", 2);
        hashTableImpl2.put("Key3", 3);
        hashTableImpl2.put("Key4", 4);
        hashTableImpl2.put("Key5", 5);
        hashTableImpl2.put("Key6", 6);
        hashTableImplDoubled.put("Key7", 7);
        hashTableImplDoubled.put("Key8", 8);
        hashTableImplDoubled.put("Key9", 9);
        hashTableImplDoubled.put("Key10", 10);
        hashTableImplDoubled.put("Key11", 11);
        hashTableImplDoubled.put("Key12", 12);
        hashTableImplDoubled.put("Key13", 13);
        hashTableImplDoubled.put("Key14", 14);
        hashTableImplDoubled.put("Key15", 15);
        hashTableImplDoubled.put("Key16", 16);
        hashTableImplDoubled.put("Key17", 17);
        hashTableImplDoubled.put("Key18", 18);
        hashTableImplDoubled.put("Key19", 19);
        hashTableImplDoubled.put("Key20", 20);
        hashTableImplDoubled.put("Key21", 21);
        hashTableImplDoubled.put("Key22", 22);
        hashTableImplDoubled.put("Key23", 23);
        hashTableImplDoubled.put("Key24",24);
        hashTableImplDoubled.put("Key25", 25);
        hashTableImplDoubled.put("Key26", 26);
        hashTableImplDoubled.put("Key27", 27);
        hashTableImplDoubled.put("Key28", 28);
        hashTableImplDoubled.put("Key29", 29);
        hashTableImplDoubled.put("Key30", 30);
        hashTableImplDoubled.put("Key31", 31);
        hashTableImplDoubled.put("Key32", 32);
        hashTableImplDoubled.put("Key33", 33);
        hashTableImplDoubled.put("Key34", 34);
        hashTableImplDoubled.put("Key35", 35);
        hashTableImplDoubled.put("Key36", 36);
        hashTableImplDoubled.put("Key37", 37);
        hashTableImplDoubled.put("Key33", null);
        Assert.assertNotEquals(hashTableImpl2, hashTableImplDoubled);
    }

/////////////// End of my tests //////////////////////////////
}