package edu.yu.cs.com1320.project.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;

public class BTreeImplTest {

 /////////////// Judah's Tests //////////////

    private BTreeImpl<String,String> bTree;

    @Before
    public void initTable(){
        this.bTree = new BTreeImpl<>();
        this.bTree.put("Key1", "Value1");
        this.bTree.put("Key2","Value2");
        this.bTree.put("Key3","Value3");
        this.bTree.put("Key4","Value4");
        this.bTree.put("Key5","Value5");
        this.bTree.put("Key6","Value6");
    }

    @Test
    public void testGet() {
        assertEquals("Value1",this.bTree.get("Key1"));
        assertEquals("Value2",this.bTree.get("Key2"));
        assertEquals("Value3",this.bTree.get("Key3"));
        assertEquals("Value4",this.bTree.get("Key4"));
        assertEquals("Value5",this.bTree.get("Key5"));
    }

    @Test
    public void testGetChained() {
        //second node in chain
        assertEquals("Value6",this.bTree.get("Key6"));
        //second node in chain after being modified
        this.bTree.put("Key6","Value6+1");
        assertEquals("Value6+1",this.bTree.get("Key6"));
        //check that other values still come back correctly
        testGet();
    }

    @Test
    public void testGetMiss() {
        assertNull(this.bTree.get("Key20"));
    }

    @Test
    public void testPutReturnValue() {
        assertEquals("Value3",this.bTree.put("Key3","Value3+1"));
        assertEquals("Value6",this.bTree.put("Key6", "Value6+1"));
        assertNull(this.bTree.put("Key7", "Value7"));
    }

    @Test
    public void testGetChangedValue () {
        BTreeImpl<String, String> bTree = new BTreeImpl<>();
        String key1 = "hello";
        String value1 = "how are you today?";
        String value2 = "HI!!!";
        bTree.put(key1, value1);
        assertEquals(value1,bTree.get(key1));
        bTree.put(key1, value2);
        assertEquals(value2,bTree.get(key1));
    }

    @Test
    public void testDeleteViaPutNull() {
        BTreeImpl<String, String> bTree = new BTreeImpl<>();
        String key1 = "hello";
        String value1 = "how are you today?";
        bTree.put(key1, value1);
        assertEquals(bTree.put(key1, null), value1);
        assertNull(bTree.get(key1));
    }

/////////////// End of Judah's Tests /////////////

//////////////// My tests ////////////////////////

    BTreeImpl<String, String> bTreeImpl = new BTreeImpl<>();
    BTreeImpl<String, Integer> bTreeImpl1 = new BTreeImpl<>();

    @Test
    public void putNodes() {
        Assert.assertNull(bTreeImpl.put("Key1", "Value1"));
        Assert.assertEquals("Value1",  bTreeImpl.get("Key1"));
    }

    @Test
    public void getNodes(){
        Assert.assertNull(bTreeImpl.put("Key2", "Value2"));
        Assert.assertNotEquals("Value1",  bTreeImpl.get("Key2"));
    }

    @Test
    public void addBunchOfNodes1() {
        bTreeImpl.put("Key1", "Value1");
        bTreeImpl.put("Key2", "Value2");
        bTreeImpl.put("Key3", "Value3");
        bTreeImpl.put("Key4", "Value4");
        bTreeImpl.put("Key5", "Value5");
        bTreeImpl.put("Key6", "Value6");
        bTreeImpl.put("Key7", "Value7");
        bTreeImpl.put("Key8", "Value8");
        bTreeImpl.put("Key9", "Value9");
        bTreeImpl.put("Key10", "Value10");
        bTreeImpl.put("Key11", "Value11");
        Assert.assertEquals("Value10", bTreeImpl.put("Key10", "Value100"));
        Assert.assertEquals("Get key didn't work", "Value100", bTreeImpl.get("Key10"));
        Assert.assertEquals("Putting null value didn't work", "Value9", bTreeImpl.put("Key9", null));
        Assert.assertNull(bTreeImpl.get("Key9"));
        bTreeImpl.put("Key12", "Value12");
        Assert.assertEquals("Get key didn't work", "Value5", bTreeImpl.get("Key5"));
        Assert.assertNotEquals("Get key didn't work", "Value1", bTreeImpl.get("Key4"));
        Assert.assertEquals("Put key with null value didn't work", "Value1", bTreeImpl.put("Key1", null));
        Assert.assertNull(bTreeImpl.get("Key1"));
        Assert.assertEquals("Put key with null value didn't work", "Value2", bTreeImpl.put("Key2", null));
        Assert.assertNull(bTreeImpl.put("Key1", "Value1"));
        Assert.assertNull(bTreeImpl.get("Key2"));
        Assert.assertNotNull(bTreeImpl.get("Key1"));
    }

    @Test
    public void testGetNonExistentNodes(){
        Assert.assertNull(bTreeImpl.get("Key1115"));
        Assert.assertNull(bTreeImpl.get("Key115"));
    }

    @Test
    public void testPutNodesStringInt(){
        bTreeImpl1.put("Key1", 1);
        bTreeImpl1.put("Key2", 2);
        bTreeImpl1.put("Key3", 3);
        bTreeImpl1.put("Key4", 4);
        bTreeImpl1.put("Key5", 5);
        bTreeImpl1.put("Key6", 6);
        bTreeImpl1.put("Key7", 7);
        bTreeImpl1.put("Key8", 8);
        bTreeImpl1.put("Key9", 9);
        bTreeImpl1.put("Key10", 10);
        bTreeImpl1.put("Key11", 11);
        Assert.assertEquals((Integer) 11, bTreeImpl1.get("Key11"));
        Assert.assertNotEquals((Integer) 12, bTreeImpl1.get("Key11"));
        Assert.assertEquals((Integer) 1, bTreeImpl1.get("Key1"));
        Assert.assertEquals((Integer) 10, bTreeImpl1.get("Key10"));
        Assert.assertEquals((Integer) 10 , bTreeImpl1.put("Key10", null));
        Assert.assertNotEquals((Integer) 10, bTreeImpl1.get("Key10"));
        Assert.assertNull(bTreeImpl1.put("Key10", 10000));
        Assert.assertEquals((Integer) 10000, bTreeImpl1.get("Key10"));
        Assert.assertEquals((Integer) 10000 , bTreeImpl1.put("Key10", null));
        Assert.assertNull(bTreeImpl1.get("Key10"));
        Assert.assertNull(bTreeImpl1.get("Key"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullKey(){
        bTreeImpl1.put(null, 1);
        bTreeImpl1.get(null);
    }
/////////////// End of my tests //////////////////////////////
}