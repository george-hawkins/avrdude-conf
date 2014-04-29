package net.betaengine.avrdude.rest;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Provider
public class CustomObjectMapperProvider implements ContextResolver<ObjectMapper> {
    @Override
    public ObjectMapper getContext(Class<?> type) {
//        if (type == Issue2322Resource.JsonString1.class) {
//            ObjectMapper result = new ObjectMapper();
//            result.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
//            result.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
//            return result;
//        } else {
        // TODO: return member variable ala https://jersey.java.net/documentation/latest/media.html#d0e6881
            ObjectMapper mapper = new ObjectMapper();
            
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            
            return mapper;
//        }
    }
}