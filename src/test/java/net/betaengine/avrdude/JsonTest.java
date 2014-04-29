package net.betaengine.avrdude;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;

public class JsonTest {
    private final static Maker MAKER = new Maker();
    
    @BeforeClass
    public static void setup() {
        ConfParser parser = new ConfParser();
        
        parser.parse(MAKER);
    }
    
    @Test
    public void testJsonNames() throws IOException {
        check(new NamePrinter());
    }
    
    @Test
    public void testJsonBodies() throws IOException {
        check(new BodyPrinter());
    }
    
    private static class FooBar extends JsonSerializer<Value> {
        @Override
        public void serialize(Value value, JsonGenerator jgen,
                SerializerProvider provider) throws IOException,
                JsonProcessingException {
            jgen.writeObject(value.getValue());
        }
    }
    
    private static class Bang extends JsonSerializer<Part> {
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
    
    @Test
    public void testXXX() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        SimpleModule module = new SimpleModule()
            .addSerializer(Value.class, new FooBar())
            .addSerializer(Part.class, new Bang());
        mapper.registerModule(module);
        
        System.err.println(mapper.writeValueAsString(MAKER.getProgrammers().get(0)));
        System.err.println(mapper.writeValueAsString(MAKER.getParts().get(0)));
    }
 
    // Should fail on trying to parse hex formatted integer literals.
    @Test(expected=JsonParseException.class)
    public void testJsonHexBodies() throws IOException {
        check(new BodyPrinter(false));
    }
    
    private void check(AbstractPrinter printer) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        
        JsonNode root = mapper.readTree(toString(printer));
        
        Assert.assertTrue(root.get("programmers").isArray());
        Assert.assertTrue(root.get("parts").isArray());
    }
    
    private String toString(AbstractPrinter printer) {
        StringWriter stringer = new StringWriter();
        IndentPrintWriter writer = new IndentPrintWriter(stringer);

        printer.printAll(writer, MAKER);
        
        writer.flush();
        writer.close();
        
        return stringer.toString();
    }
}
