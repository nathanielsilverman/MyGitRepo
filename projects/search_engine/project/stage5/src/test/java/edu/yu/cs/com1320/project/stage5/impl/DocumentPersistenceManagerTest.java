package edu.yu.cs.com1320.project.stage5.impl;

import org.junit.Assert;
import org.junit.Test;
import com.google.gson.*;
import edu.yu.cs.com1320.project.stage5.Document;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class DocumentPersistenceManagerTest {

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private DocumentPersistenceManager dpm = new DocumentPersistenceManager(null);

    @Test
    public void testSerialize() throws URISyntaxException, IOException{
        URI uri = new URI("http://doc1");
        String txt = "Document One. Document 1 text.";
        int txtHashCode = txt.hashCode();
        Document document = new DocumentImpl(uri, txt, txtHashCode);
        dpm.serialize(uri, document);
        dpm.cleanUp();
    }

    @Test
    public void testSerialize1() throws URISyntaxException, IOException{
        URI uri = new URI("http://www.yu.edu/uri1/doc1");
        String txt = "Document One. Document 1 text.";
        int txtHashCode = txt.hashCode();
        Document document = new DocumentImpl(uri, txt, txtHashCode);
        dpm.serialize(uri, document);
        Document doc = dpm.deserialize(uri);
        Assert.assertTrue(doc.equals(document));
    }

    @Test
    public void testDeserialize() throws URISyntaxException, IOException{
        DocumentPersistenceManager dpm2 = new DocumentPersistenceManager(new File(System.getProperty("user.home") + File.separator + "stage5Tests"));
        URI uri = new URI("http://abc/123/YouAndMe/doc1/document1Test");
        String txt = "Document One. Document 1 text.";
        int txtHashCode = txt.hashCode();
        Document document = new DocumentImpl(uri, txt, txtHashCode);
        dpm2.serialize(uri, document);
        Document document1 = dpm2.deserialize(uri);
        Assert.assertTrue(document.equals(document1));
        dpm2.cleanUp();
    }

    @Test
    public void testSerializeFromRootDirectory() throws URISyntaxException, IOException {
        File baseDir = new File(System.getProperty("user.home"));
        DocumentPersistenceManager documentPersistenceManager = new DocumentPersistenceManager(baseDir);
        URI uri = new URI("http://doc1/testDPM/doc1");
        String txt = "Document One. Document 1 text.";
        int txtHashCode = txt.hashCode();
        Document document = new DocumentImpl(uri, txt, txtHashCode);
        documentPersistenceManager.serialize(uri, document);
    }

    @Test
    public void testDPMWithNullInputEqualsWorkingDir() {
        String baseDir = this.dpm.getBaseDir().getAbsolutePath();
        String workingDir = System.getProperty("user.dir");
        Assert.assertTrue(workingDir.equals(baseDir));
    }

    @Test
    public void testFileWorksWithBothOS() throws URISyntaxException {
        URI uri = new URI("https://www.yu.edu/uri1/document1");
        String uriPath = uriToFile(uri);
        File userHome = new File(System.getProperty("user.home"));
        File uriFile = new File(userHome.getAbsolutePath() + File.separator + uriPath);
        System.out.println(userHome.getAbsolutePath());
        System.out.println(uriFile.getAbsolutePath());
    }

    private String uriToFile(URI uri) {
        if (uri != null) {
            String uriPath =(uri.getHost()) + File.separator + (uri.getPath()) + ".json";
            try {
                uriPath = uriPath.replaceAll("\\\\ | /", File.separator);
            } catch (IllegalArgumentException e){}
            return uriPath;
        }
        return null;
    }
}