package edu.yu.cs.com1320.project.stage4;

import edu.yu.cs.com1320.project.GenericCommand;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;

public class GenericCommandAPITest {

    @Test
    public void interfaceCount() {//tests that the class only implements one interface and its the correct one
        @SuppressWarnings("rawtypes")
        Class[] classes = GenericCommand.class.getInterfaces();
        assertEquals(1, classes.length);
        assertEquals("edu.yu.cs.com1320.project.Undoable", classes[0].getName());
    }

    @Test
    public void methodCount() {//need only test for non constructors
        Method[] methods = GenericCommand.class.getDeclaredMethods();
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
    public void fieldCount() {
        Field[] fields = GenericCommand.class.getFields();
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
        Class[] classes = GenericCommand.class.getClasses();
        assertEquals(0, classes.length);
    }

    @Test
    public void constructorExists() {
        try {
            new GenericCommand<>(null, null);
        } catch (RuntimeException e) {}
    }
}
