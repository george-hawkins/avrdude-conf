package net.betaengine.util.cache;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.StandardSystemProperty;
import com.google.common.io.Files;

public class FileCache implements Cache {
    private final static Logger log = LoggerFactory.getLogger(FileCache.class);
    
    private final Map<String, File> index = new HashMap<>();
    private File root;
    
    @Override
    public void setUuid(String uuid) {
        root = new File(StandardSystemProperty.JAVA_IO_TMPDIR.value(), uuid);
        
        if (root.exists()) {
            for (File dir : root.listFiles()) {
                String key = getContent(getKeyFile(dir));
                
                if (key != null) {
                    index.put(key, getValueFile(dir));
                }
            }
        }
    }
    
    private String getContent(File file) {
        try {
            return Files.toString(file, Charsets.UTF_8);
        } catch (IOException e) {
            log.warn("failed to read contents of {}", file, e);
            return null;
        }
    }
    
    private File getKeyFile(File dir) { return new File(dir, "key"); }
    
    private File getValueFile(File dir) { return new File(dir, "value"); }
    
    @Override
    public String get(String key, ValueCreator creator) {
        File valueFile = index.get(key);
        
        if (valueFile != null) {
            String value = getContent(valueFile);
            
            if (value != null) {
                return value;
            }
        }
        
        String value = creator.create();
        
        store(key, value);
        
        return value;
    }
    
    private void store(String key, String value) {
        try {
            String uuid = UUID.randomUUID().toString();
            File dir = new File(root, uuid);
            
            if (dir.mkdirs()) {
                File tmpKey = new File(dir, "tmp");
                File valueFile = getValueFile(dir);
                Files.write(value, valueFile, Charsets.UTF_8);
                Files.write(key, tmpKey, Charsets.UTF_8);
                
                // Only make the key "visible" if everything went well.
                if (tmpKey.renameTo(getKeyFile(dir))) {
                    index.put(key, valueFile);
                }
            }
        } catch (IOException e) {
            log.warn("failed to store key/value pair", e);
        }
    }
}
