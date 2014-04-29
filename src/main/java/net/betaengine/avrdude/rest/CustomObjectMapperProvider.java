package net.betaengine.avrdude.rest;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
public class CustomObjectMapperProvider implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper = new CustomObjectMapperBuilder().build();
    
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}