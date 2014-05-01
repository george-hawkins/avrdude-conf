package net.betaengine.util.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheFactory {
    private final static Logger log = LoggerFactory.getLogger(CacheFactory.class);

    public static Cache getCache(String uuid) {
        try {
            String name = System.getProperty("betaengine.cache", NoCache.class.getName());
            Cache cache = (Cache)Class.forName(name).newInstance();
            
            cache.setUuid(uuid);
            
            log.info("created cache {}", name);
            
            return cache;
        } catch (Exception e) {
            throw new CacheException(e);
        }
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
