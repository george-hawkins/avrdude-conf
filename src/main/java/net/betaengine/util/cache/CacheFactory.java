package net.betaengine.util.cache;

public class CacheFactory {
    public static Cache getCache(String uuid) {
        try {
            String name = System.getProperty("betaengine.cache", NoCache.class.getName());
            Cache cache = (Cache)Class.forName(name).newInstance();
            
            cache.setUuid(uuid);
            
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
