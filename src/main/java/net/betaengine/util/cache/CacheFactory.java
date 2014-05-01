package net.betaengine.util.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheFactory {
    private final static Logger log = LoggerFactory.getLogger(CacheFactory.class);

    public static Cache getCache(String uuid) {
        try {
            String name = getCacheClassName();
            Cache cache = (Cache)Class.forName(name).newInstance();
            
            cache.setUuid(uuid);
            
            log.info("created cache {}", name);
            
            return cache;
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }
    
    private static String getCacheClassName() {
        // On Heroku it's more convenient to set environment variables that system properties.
        String name = System.getenv("BETAENGINE_CACHE");
        
        return name != null ? name : System.getProperty("betaengine.cache", NoCache.class.getName());
    }
    
    public static class NoCache implements Cache {
        @Override
        public void setUuid(String uuid) { }
        
        @Override
        public String get(String key, ValueCreator creator) {
            return creator.create();
        }
    }
}
