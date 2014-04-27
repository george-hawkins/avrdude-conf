package net.betaengine.avrdude;

import java.util.LinkedHashMap;
import java.util.Map;

public class Part {
    private final Map<String, Value<?>> properties = new LinkedHashMap<>();
    private final Map<String, Map<String, Value<?>>> memories = new LinkedHashMap<>();
    
    public Part() { }
    
    public Part(Part original) {
        properties.putAll(original.properties);
        memories.putAll(original.memories);
    }
    
    public Map<String, Value<?>> getProperties() { return properties; }
    
    public void addMemory(String qualifier, Map<String, Value<?>> memory) {
        memories.put(qualifier, memory);
    }
    
    public Map<String, Map<String, Value<?>>> getMemories() { return memories; }
}