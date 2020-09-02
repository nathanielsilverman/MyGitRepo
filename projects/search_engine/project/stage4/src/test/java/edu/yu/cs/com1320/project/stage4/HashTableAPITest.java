package edu.yu.cs.com1320.project.stage4;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import edu.yu.cs.com1320.project.impl.HashTableImpl;
import org.junit.Test;

public class HashTableAPITest {

    @Test
    public void interfaceCount() {//tests that the class only implements one interface and its the correct one
        @SuppressWarnings("rawtypes")
        Class[] classes = HashTableImpl.class.getInterfaces();
        assertEquals(1, classes.length);
        assertEquals("edu.yu.cs.com1320.project.HashTable", classes[0].getName());
    }

    @Test
    public void methodCount() {//tests for public and protected methods. the expected number should match the number of methods explicitly tested below save for the constructor
        Method[] methods = HashTableImpl.class.getDeclaredMethods();
        int publicMethodCount = 0;
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                if(!method.getName().equals("equals") && !method.getName().equals("hashCode")) {
                    publicMethodCount++;
                }
            }
        }
        assertEquals(2, publicMethodCount);
    }

    @Test
    public void fieldCount() {//tests for public or protected fields
        Field[] fields = HashTableImpl.class.getFields();
        int publicProtectedFieldCount = 0;
        for (Field field : fields) {
            if (Modifier.isPublic(field.getModifiers()) || Modifier.isProtected(field.getModifiers())) {
                publicProtectedFieldCount++;
            }
        }
        assertEquals(0, publicProtectedFieldCount);
    }

    @Test
    public void subClassCount() {//tests if any subclasses are public/protected
        @SuppressWarnings("rawtypes")
        Class[] classes = HashTableImpl.class.getClasses();
        assertEquals(0, classes.length);
    }

    @Test
    public void constructorExists() {
        new HashTableImpl<String,String>();
    }

    @Test
    public void putExists() {
        try {
            new HashTableImpl<String,String>().put("hello", "there");
        } catch (RuntimeException e) {}//catch any run time error this input might cause. This is meant to test method existence, not correctness
    }

    @Test
    public void getExists() {
        try {
            new HashTableImpl<String,String>().get("hello");
        } catch (RuntimeException e) {}//catch any run time error this input might cause. This is meant to test method existence, not correctness
    }

}
