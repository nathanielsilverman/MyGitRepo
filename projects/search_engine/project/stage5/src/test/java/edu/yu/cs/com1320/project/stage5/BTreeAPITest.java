package edu.yu.cs.com1320.project.stage5;

import edu.yu.cs.com1320.project.impl.BTreeImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;

public class BTreeAPITest {

    @Test
    public void interfaceCount() {//tests that the class only implements one interface and its the correct one
        @SuppressWarnings("rawtypes")
        Class[] classes = BTreeImpl.class.getInterfaces();
        assertEquals(1, classes.length);
        assertEquals("edu.yu.cs.com1320.project.BTree", classes[0].getName());
    }

    @Test
    public void methodCount() {//tests for public and protected methods. the expected number should match the number of methods explicitly tested below save for the constructor
        Method[] methods = BTreeImpl.class.getDeclaredMethods();
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
    public void fieldCount() {//tests for public or protected fields
        Field[] fields = BTreeImpl.class.getFields();
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
        Class[] classes = BTreeImpl.class.getClasses();
        assertEquals(0, classes.length);
    }

    @Test
    public void noArgsConstructorExists() {
        new BTreeImpl<String,String>();
    }

    @Test
    public void getExists() {
        try {
            new BTreeImpl<String,String>().get("hello");
        } catch (RuntimeException e) {}//catch any run time error this input might cause. This is meant to test method existence, not correctness
    }

    @Test
    public void putExists() {
        try {
            new BTreeImpl<String,String>().put("hello", "there");
        } catch (RuntimeException e) {}//catch any run time error this input might cause. This is meant to test method existence, not correctness
    }

    @Test
    public void moveToDiskExists(){
        try {
            new BTreeImpl<String,String>().moveToDisk("hi");
        } catch (Exception e) {}//catch any run time error this input might cause. This is meant to test method existence, not correctness
    }

    @Test
    public void setPersistenceManagerExists() {
        try{
            new BTreeImpl<String,String>().setPersistenceManager(null);
        } catch (RuntimeException e) {}//catch any run time error this input might cause. This is meant to test method existence, not correctness
    }
}
