package edu.yu.cs.com1320.project.stage5;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;

import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import org.junit.Assert;
import org.junit.Test;

public class DocumentAPITest {

    @Test
    public void interfaceCount() {//tests that the class only implements one interface and its the correct one
        @SuppressWarnings("rawtypes")
        Class[] classes = DocumentImpl.class.getInterfaces();
        assertEquals(1, classes.length);
        assertEquals("edu.yu.cs.com1320.project.stage5.Document", classes[0].getName());
    }

    @Test
    public void methodCount() {//need only test for non constructors
        Method[] methods = DocumentImpl.class.getDeclaredMethods();
        int publicMethodCount = 0;
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                if(!method.getName().equals("equals") && !method.getName().equals("hashCode")  && !method.getName().equals("compareTo")) {
                    publicMethodCount++;
                }
            }
        }
        assertEquals(9, publicMethodCount);
    }

    @Test
    public void fieldCount() {
        Field[] fields = DocumentImpl.class.getFields();
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
        Class[] classes = DocumentImpl.class.getClasses();
        assertEquals(0, classes.length);
    }

    @Test
    public void constructor1Exists() throws URISyntaxException {
        URI uri = new URI("https://this.com");
        try {
            new DocumentImpl(uri, "hi", 1);
        } catch (RuntimeException e) {}
    }

    @Test
    public void constructor2Exists() throws URISyntaxException {
        URI uri = new URI("https://this.com");
        byte[] ary = {0,0,0};
        try {
            new DocumentImpl(uri, "hi", 1, ary );
        } catch (RuntimeException e) {}
    }

    @Test
    public void getDocumentTextHashCodeExists() throws URISyntaxException{
        URI uri = new URI("https://this.com");
        try {
            int i = new DocumentImpl(uri, "hi", 1).getDocumentTextHashCode();
            Assert.assertEquals(1, i);
        } catch (RuntimeException e) {}
    }

    @Test
    public void getDocumentAsPdfExists() throws URISyntaxException{
        URI uri = new URI("https://this.com");
        try {
            byte[] bytes = new DocumentImpl(uri, "hi", 1).getDocumentAsPdf();
            Assert.assertNotNull(bytes);
        } catch (RuntimeException e) {}
    }

    @Test
    public void getDocumentAsTxtExists() throws URISyntaxException{
        URI uri = new URI("https://this.com");
        try {
            String txt = new DocumentImpl(uri, "hi", 1).getDocumentAsTxt();
            assertEquals("hi", txt);
        } catch (RuntimeException e) {}
    }

    @Test
    public void getKeyExists() throws URISyntaxException {
        URI uri = new URI("https://this.com");
        try {
            URI uri1 = new DocumentImpl(uri, "hi", 1).getKey();
            Assert.assertEquals(uri, uri1);
        } catch (RuntimeException e) {}
    }

    @Test
    public void wordCountExists() throws URISyntaxException {
        URI uri = new URI("https://this.com");
        try {
            Document document = new DocumentImpl(uri, "hi", 1);
            int i = document.wordCount("hi");
            Assert.assertEquals(1, i);
        } catch (RuntimeException e) {}
    }

}