package edu.yu.cs.com1320.project.stage5;

import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;

public class MinHeapImplAPITest {

    @Test
    public void interfaceCount() {//tests that the class only implements one interface and its the correct one
        @SuppressWarnings("rawtypes")
        Class[] classes = MinHeapImpl.class.getInterfaces();
        assertEquals(0, classes.length);
    }

    @Test
    public void methodCount() {//need only test for non constructors
        Method[] methods = MinHeapImpl.class.getDeclaredMethods();
        int publicMethodCount = 0;
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                if(!method.getName().equals("equals") && !method.getName().equals("hashCode")) {
                    publicMethodCount++;
                }
            }
        }
        assertEquals(3, publicMethodCount);
    }

    @Test
    public void fieldCount() {
        Field[] fields = MinHeapImpl.class.getFields();
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
        Class[] classes = MinHeapImpl.class.getClasses();
        assertEquals(0, classes.length);
    }

    @Test
    public void constructorExists() {
        try {
            new MinHeapImpl<String>();
        } catch (RuntimeException e) { }
    }

}