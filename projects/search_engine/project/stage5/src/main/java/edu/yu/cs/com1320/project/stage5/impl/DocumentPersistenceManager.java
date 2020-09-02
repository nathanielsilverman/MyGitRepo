package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    private File baseDir;
    private JsonDeserializer<Document> documentJsonDeserializer;
    private JsonSerializer<Document> documentJsonSerializer;
    private Gson gson;

    public DocumentPersistenceManager(File baseDir) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.setPrettyPrinting().create();
        this.documentJsonSerializer = jsonSerializer();
        this.documentJsonDeserializer = jsonDeserializer();
        if (baseDir != null) {
            this.baseDir = baseDir;
            if (!this.baseDir.exists()) {
                if(!this.baseDir.mkdirs()) {
                    if (!this.baseDir.exists()) {
                        throw new IllegalArgumentException("Creating directory didn't work");
                    }
                }
            }
        }
        if (baseDir == null) {
            this.baseDir = new File(System.getProperty("user.dir"));
        }
    }
    
    @Override
    public void serialize(URI uri, Document document) throws IOException {
        if (uri == null || document == null) {
            throw new IllegalArgumentException("uri or document is null");
        }
        if (this.documentJsonSerializer != null) {
            JsonElement jsonDocument = this.documentJsonSerializer.serialize(document, DocumentImpl.class, null);
            try {
                boolean isSaved = saveFile(uri, jsonDocument);
                if (!isSaved) {
                    throw new IOException();
                }
            } catch (IOException e) {
                throw new IOException("Error occurred while trying to save document to disk");
            }
        }
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        if (this.documentJsonDeserializer != null) {
            Document document = readOffDisk(uri);
            try {
                boolean fileDeleted = deleteFromDisk(uri);
                if (!fileDeleted) {
                    throw new IOException();
                }
                document.setLastUseTime(System.nanoTime());
                document.getDocumentAsPdf();
                return document;
            } catch (IOException e) {
                throw new IOException();
            }
        }
        return null;
    }

    private JsonSerializer<Document> jsonSerializer() {
        return new JsonSerializer<Document>() {
            @Override
            public JsonElement serialize(Document document, Type type, JsonSerializationContext jsonSerializationContext) {
                if (document == null || type == null) {
                    throw new NullPointerException();
                }
                JsonObject jsonDocument = new JsonObject();
                jsonDocument.addProperty("txt", document.getDocumentAsTxt());
                jsonDocument.add("uri", gson.toJsonTree(document.getKey()));
                jsonDocument.addProperty("txtHash", document.getDocumentTextHashCode());
                jsonDocument.add("wordCounts", gson.toJsonTree(document.getWordMap()));
                return jsonDocument;
            }
        };
    }

    private JsonDeserializer<Document> jsonDeserializer() {
        return new JsonDeserializer<Document>() {
            @Override
            public Document deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                if (jsonElement != null) {
                    return gson.fromJson(jsonElement, DocumentImpl.class);
                }
                return null;
            }
        };
    }

    ///// URI to path methods ////////
    private File uriToFilePath(URI uri) {
        String uriPath =(uri.getHost()) + (uri.getPath()) + ".json";
        try {
            uriPath = uriPath.replaceAll("\\\\ | /", File.separator);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return new File(this.baseDir + slash() + uriPath);
    }

    private String slash() {
        return File.separator;
    }

    ////// serialization methods ///////////
    private boolean saveFile(URI uri, JsonElement json) throws IOException {
        if (uri == null || json == null) {
            return false;
        }
        File file = uriToFilePath(uri);
        if (!file.exists()) {
            makeFile(file);
        }
        try {
            if (file.createNewFile()) {
                writeToFile(file, json);
            }
        } catch (IOException e) {
            throw new IOException();
        }
        return (file.exists() && !file.isDirectory());
    }

    private void makeFile(File file) {
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                boolean dirsMade = parentFile.mkdirs();
            }
        }
    }

    private void writeToFile(File file, JsonElement json) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) {
            writer.print(json);
        } catch (IOException e) {
            throw new IOException();
        }
        System.out.println("File created: " + file.getAbsolutePath());
    }

    ////// deserialization methods ///////////

    protected Document readOffDisk(URI uri) throws IOException {
        Document document = gson.fromJson(readFromDisk(uri), DocumentImpl.class);
        if (document != null) {
            return document;
        }
        throw new IllegalArgumentException();
    }

    private String readFromDisk(URI uri) throws IOException {
        File file = uriToFilePath(uri);
        String jsonObject;
        boolean fileExists = file.exists();
        if(fileExists) {
            jsonObject = getFileContents(file);
            if (jsonObject != null) {
                return jsonObject;
            }
        }
        throw new IOException();
    }

    private String getFileContents(File file) throws IOException {
        if (file.exists()) {
            byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            return new String(bytes);
        }
        return null;
    }

    private boolean deleteFromDisk(URI uri) {
        File file = uriToFilePath(uri);
        if (!file.exists()) {
            throw new IllegalStateException("File doesn't exist on disk.");
        }
        if (file.isFile()) {
            fileDelete(file, uri);
            return !file.exists();
        }
        throw new IllegalArgumentException();
    }

    private void fileDelete(File file, URI uri) {
        if (baseDir == file) {
            return;
        }
        if (file.isDirectory()) {
            if (isDirEmpty(file)) {
                File parent = file.getParentFile();
                if (file.delete()) {
                    System.out.println("Deleted directory: " + file.getAbsolutePath());
                }
                fileDelete(parent, uri);
            }
        }
        else  {
            if (file.getAbsolutePath().equals(uriToFilePath(uri).getAbsolutePath())) {
                File parent = file.getParentFile();
                if (file.delete()) {
                    System.out.println("Deleted file: " + file.getAbsolutePath());
                }
                fileDelete(parent, uri);
            }
        }
    }

    protected File getBaseDir() {
        return this.baseDir;
    }

    private boolean isDirEmpty(File file) {
        if (file != null && file.isDirectory() && !file.getAbsolutePath().equals(this.baseDir.getAbsolutePath())) {
            String[] files = file.list();
            if (files != null) {
                return files.length == 0;
            }
        }
        return false;
    }

    protected void cleanUp() {
        System.out.println("CleaningUp...");
        cleaner(this.baseDir);
    }

    private void cleaner(File file) {
        if (file == null || (file.isFile() && !file.getName().endsWith(".json")) || (file.isDirectory() && file.getName().equals("src")) || (file.isDirectory() && file.getName().equals("target"))) {
            return;
        }
        if (file.exists()) {
            if (file.isDirectory()) {
                if (!isDirEmpty(file)) {
                    cleanNonEmptyDir(file);
                }
                if (isDirEmpty(file)) {
                    cleanEmptyDir(file);
                }
            }
        }
    }

    private void cleanEmptyDir(File file) {
        if (!file.getName().equals(baseDir.getName())) {
            File parent = file.getParentFile();
            boolean bool = file.delete();
            if (bool) {
                System.out.println("Deleting Directory: " + file.getAbsolutePath());
            }
            if (!parent.getName().equals(this.baseDir.getName())) {
                cleaner(parent);
            }
        }
    }

    private void cleanNonEmptyDir(File file) {
        File[] dirFiles = file.listFiles();
        if (dirFiles != null) {
            for (File files : dirFiles) {
                if (files.isFile()) {
                    if (files.getName().endsWith(".json")) {
                        boolean bool = files.delete();
                        if (bool) {
                            System.out.println("Deleting File: " + files.getAbsolutePath());
                        }
                    }
                }
                if (files.isDirectory()) {
                    cleaner(files);
                }
            }
        }
    }
}