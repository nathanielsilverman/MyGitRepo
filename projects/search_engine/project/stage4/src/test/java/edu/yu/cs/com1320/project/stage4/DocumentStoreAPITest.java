package edu.yu.cs.com1320.project.stage4;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;

import edu.yu.cs.com1320.project.stage4.impl.DocumentStoreImpl;
import org.junit.Test;

import edu.yu.cs.com1320.project.stage4.DocumentStore.DocumentFormat;

public class DocumentStoreAPITest {

    @Test
    public void interfaceCount() {//tests that the class only implements one interface and its the correct one
        @SuppressWarnings("rawtypes")
        Class[] classes = DocumentStoreImpl.class.getInterfaces();
        assertEquals(1, classes.length);
        assertEquals("edu.yu.cs.com1320.project.stage4.DocumentStore", classes[0].getName());
    }

    @Test
    public void methodCount() {
        Method[] methods = DocumentStoreImpl.class.getDeclaredMethods();
        int publicMethodCount = 0;
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                publicMethodCount++;
            }
        }
        assertEquals(14, publicMethodCount);
    }

    @Test
    public void fieldCount() {
        Field[] fields = DocumentStoreImpl.class.getFields();
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
        Class[] classes = DocumentStoreImpl.class.getClasses();
        assertEquals(0, classes.length);
    }

    @Test
    public void constructorExists() {
        try {
            new DocumentStoreImpl();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void putDocumentExists() {
        try {
            new DocumentStoreImpl().putDocument(null, new URI("hi"), DocumentFormat.PDF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getDocumentAsPdfExists() {
        try {
            new DocumentStoreImpl().getDocumentAsPdf(new URI("hi"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getDocumentAsTxtExists() {
        try {
            new DocumentStoreImpl().getDocumentAsTxt(new URI("hi"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteDocumentExists()  {
        try {
            new DocumentStoreImpl().deleteDocument(new URI("hi"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
