package edu.yu.cs.com1320.project.stage4;

import edu.yu.cs.com1320.project.impl.StackImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

public class StackAPITest {

    @Test
    public void interfaceCount() {//tests that the class only implements one interface and its the correct one
        @SuppressWarnings("rawtypes")
        Class[] classes = StackImpl.class.getInterfaces();
        assertEquals(1, classes.length);
        assertEquals("edu.yu.cs.com1320.project.Stack", classes[0].getName());
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
        assertEquals(4, publicMethodCount);
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
        assertEquals(0, publicFieldCount);
    }

    @Test
    public void subClassCount() {
        @SuppressWarnings("rawtypes")
        Class[] classes = StackImpl.class.getClasses();
        assertEquals(0, classes.length);
    }

    @Test
    public void constructorExists(){
        try {
            new StackImpl<>();
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
            assertNull(new StackImpl<String>().peek());
            StackImpl<String> stack = new StackImpl<>();
            stack.push("hi");
            assertEquals("hi", stack.peek());
            assertNotNull(stack.peek());
        } catch (RuntimeException e) {}
    }

    @Test
    public void popExists(){
        try {
            StackImpl<String> stack = new StackImpl<>();
            stack.push("hi");
            assertEquals("hi", stack.pop());
            assertNull(stack.peek());
        } catch (RuntimeException e) {}
    }

    @Test
    public void sizeExists(){
        try {
            assertEquals(0, new StackImpl<String>().size());
            StackImpl<String> stack = new StackImpl<>();
            stack.push("hi");
            assertEquals(1, stack.size());
        } catch (RuntimeException e) {}
    }

}

