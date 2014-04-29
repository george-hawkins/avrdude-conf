package net.betaengine.avrdude.rest;

import java.io.IOException;

import net.betaengine.avrdude.Value.HexListValue;
import net.betaengine.avrdude.Value.HexValue;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

// Important: the ObjectMapper created here outputs some integer value as
// hex which is not valid JSON.
public class HexObjectMapperFactory {
    public static ObjectMapper createObjectMapper() {
        CustomObjectMapperBuilder builder = new CustomObjectMapperBuilder();

        builder.addSerializer(HexValue.class, new HexValueSerializer());
        builder.addSerializer(HexListValue.class, new HexListValueSerializer());
        
        return builder.build();
    }
    
    private static class HexValueSerializer extends JsonSerializer<HexValue> {
        @Override
        public void serialize(HexValue value, JsonGenerator jgen,
                SerializerProvider provider) throws IOException,
                JsonProcessingException {
            jgen.writeRawValue(toHex(value.getValue(), value.getNibbles()));
        }
    }
    
    private static class HexListValueSerializer extends JsonSerializer<HexListValue> {
        @Override
        public void serialize(HexListValue value, JsonGenerator jgen,
                SerializerProvider provider) throws IOException,
                JsonProcessingException {
            jgen.writeStartArray();

            // There's writeRaw(...) and writeRawValue(...), the latter handles commas etc. between elements.
            for (Integer i : value.getValue()) {
              jgen.writeRawValue(toHex(i, value.getNibbles()));
            }
            
            jgen.writeEndArray();
        }
    }
    
    private static String toHex(long value, int nibbles) {
        return String.format("0x%0" + nibbles + "x", value);
    }
}
