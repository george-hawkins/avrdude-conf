package net.betaengine.util.cache;

public interface Cache {
    public interface ValueCreator {
        String create();
    }
    
    void setUuid(String uuid);

    /** Cache entries never expire so don't use it for resources that change over time. */
    String get(String key, ValueCreator creator);
}
