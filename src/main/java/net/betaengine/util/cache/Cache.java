package net.betaengine.util.cache;

public interface Cache {
    public interface ValueCreator {
        String create();
    }
    
    void setUuid(String uuid);
    String get(String key, ValueCreator creator);
}
