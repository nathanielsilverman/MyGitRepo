package edu.yu.cs.com1320.project.impl;

import org.junit.Assert;
import org.junit.Test;

public class StackImplTest {

    StackImpl<String> stack = new StackImpl<>();
    StackImpl<String> stack1 = new StackImpl<>();
    StackImpl<String> stack2 = new StackImpl<>();

    @Test
    public void testStack(){
        stack1.push("peter");
        stack1.push("tom");
        stack1.push("sam");
        Assert.assertEquals("sam", stack1.pop());
        Assert.assertEquals("tom", stack1.peek());
        Assert.assertNotEquals("peter", stack1.pop());
        Assert.assertEquals("peter", stack1.pop());
        Assert.assertNull(stack1.pop());
    }

    @Test
    public void testStack1(){
        stack.push("a");
        stack.push("b");
        stack.push("c");
        stack.push("d");
        stack.push("e");
        stack.push("f");
        stack.push("g");
        stack.push("h");
        stack.push("i");
        stack.push("j");
        stack.push("k");
        Assert.assertEquals("k", stack.pop());
        Assert.assertEquals(10, stack.size());
    }

    @Test
    public void testNullStackPeek(){
        Assert.assertNull(stack2.peek());
    }

    @Test
    public void testNullStackPop(){
        Assert.assertNull(stack2.pop());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullStackPush(){
        stack2.push(null);
    }

}