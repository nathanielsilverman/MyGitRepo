package edu.yu.cs.com1320.project.stage2;

import edu.yu.cs.com1320.project.impl.StackImpl;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class StackAPITest {

    @Test
    public void interfaceCount() {//tests that the class only implements one interface and its the correct one
        @SuppressWarnings("rawtypes")
        Class[] classes = StackImpl.class.getInterfaces();
        Assert.assertTrue(classes.length == 1);
        Assert.assertTrue(classes[0].getName().equals("edu.yu.cs.com1320.project.Stack"));
    }

    @Test
    public void methodCount() {//need only test for non constructors
        Method[] methods = StackImpl.class.getDeclaredMethods();
        int publicMethodCount = 0;
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                if(!method.getName().equals("equals") && !method.getName().equals("hashCode")) {
                    publicMethodCount++;
                }
            }
        }
        Assert.assertTrue(publicMethodCount == 4);
    }

    @Test
    public void fieldCount() {
        Field[] fields = StackImpl.class.getFields();
        int publicFieldCount = 0;
        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers())) {
                publicFieldCount++;
            }
        }
        Assert.assertTrue(publicFieldCount == 0);
    }

    @Test
    public void subClassCount() {
        @SuppressWarnings("rawtypes")
        Class[] classes = StackImpl.class.getClasses();
        Assert.assertTrue(classes.length == 0);
    }

    @Test
    public void noArgsConstructorExists(){
        try {
            new StackImpl();
        } catch (RuntimeException e) {}
    }

    @Test
    public void pushExists(){
        try {
           new StackImpl<String>().push("hi");
        } catch (RuntimeException e) {}
    }

    @Test
    public void peekExists(){
        try {
            Assert.assertNull(new StackImpl<String>().peek());
            StackImpl<String> stack = new StackImpl<>();
            stack.push("hi");
            Assert.assertEquals("hi", stack.peek());
            Assert.assertNotNull(stack.peek());
        } catch (RuntimeException e) {}
    }

    @Test
    public void popExists(){
        try {
            StackImpl<String> stack = new StackImpl<>();
            stack.push("hi");
            Assert.assertEquals("hi", stack.pop());
            Assert.assertNull(stack.peek());
        } catch (RuntimeException e) {}
    }

    @Test
    public void sizeExists(){
        try {
            Assert.assertEquals(0, new StackImpl<String>().size());
            StackImpl<String> stack = new StackImpl<>();
            stack.push("hi");
            Assert.assertEquals(1, stack.size());
        } catch (RuntimeException e) {}
    }
}