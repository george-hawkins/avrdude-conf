package net.betaengine.avrdude.rest;

import java.io.IOException;
import java.util.Map;

import net.betaengine.avrdude.Part;
import net.betaengine.avrdude.Value;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;

public class CustomObjectMapperBuilder {
    private final ObjectMapper mapper = new ObjectMapper();
    private final SimpleModule module = new SimpleModule();
    
    
    public CustomObjectMapperBuilder() {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // Cast away generics related issues.
        @SuppressWarnings({ "rawtypes", "unchecked" })
        JsonSerializer<Value> valueSerializer =
            (JsonSerializer<Value>)((JsonSerializer<?>)new ValueSerializer());
        
        addSerializer(Value.class, valueSerializer);
        addSerializer(Part.class, new PartSerializer());
    }
    
    public ObjectMapper build() {
        mapper.registerModule(module);
        
        return mapper;
    }
    
    public <T> CustomObjectMapperBuilder addSerializer(Class<? extends T> type,
            JsonSerializer<T> serializer) {
        module.addSerializer(type, serializer);
        
        return this;
    }
    
    private static class ValueSerializer extends JsonSerializer<Value<?>> {
        @Override
        public void serialize(Value<?> value, JsonGenerator jgen,
                SerializerProvider provider) throws IOException,
                JsonProcessingException {
            jgen.writeObject(value.getValue());
        }
    }
    
    private static class PartSerializer extends JsonSerializer<Part> {
        @Override
        public void serialize(Part part, JsonGenerator jgen,
                SerializerProvider provider) throws IOException,
                JsonProcessingException {
            jgen.writeStartObject();
            
            // Cannot cast directly to MapSerializer due to generics abuse.
            JsonSerializer<?> serializer = provider.findTypedValueSerializer(Map.class, true, null);
            MapSerializer mapSerializer = (MapSerializer)serializer;
            
            mapSerializer.serializeFields(part.getProperties(), jgen, provider);
            jgen.writeObjectField("memories", part.getMemories());
            jgen.writeEndObject();
        }
    }
}