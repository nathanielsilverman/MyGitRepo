package edu.yu.cs.com1320.project.stage4;

import edu.yu.cs.com1320.project.impl.TrieImpl;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class TrieAPITest {

    @Test
    public void interfaceCount() {//tests that the class only implements one interface and its the correct one
        @SuppressWarnings("rawtypes")
        Class[] classes = TrieImpl.class.getInterfaces();
        assertEquals(1, classes.length);
        assertEquals("edu.yu.cs.com1320.project.Trie", classes[0].getName());
    }

    @Test
    public void methodCount() {//need only test for non constructors
        Method[] methods = TrieImpl.class.getDeclaredMethods();
        int publicMethodCount = 0;
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                if(!method.getName().equals("equals") && !method.getName().equals("hashCode")) {
                    publicMethodCount++;
                }
            }
        }
        assertEquals(6, publicMethodCount);
    }

    @Test
    public void fieldCount() {
        Field[] fields = TrieImpl.class.getFields();
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
        Class[] classes = TrieImpl.class.getClasses();
        assertEquals(0, classes.length);
    }

    @Test
    public void constructorExists() throws URISyntaxException {
        try {
            new TrieImpl();
        } catch (RuntimeException e) {}
    }
}