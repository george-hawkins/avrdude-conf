package net.betaengine.avrdude;

import java.io.IOException;

import net.betaengine.avrdude.rest.AvrdudeConfResource;
import net.betaengine.avrdude.rest.CustomObjectMapperBuilder;
import net.betaengine.avrdude.rest.HexObjectMapperFactory;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

public class JsonTest {
    private final AvrdudeConfResource resource = new AvrdudeConfResource();
    
    @Test
    public void testJsonNames() throws IOException {
        ObjectMapper mapper = new CustomObjectMapperBuilder().build();
        
        check(mapper.writeValueAsString(resource.getAllIds()));
    }
    
    @Test
    public void testJsonBodies() throws IOException {
        ObjectMapper mapper = new CustomObjectMapperBuilder().build();

        check(mapper.writeValueAsString(resource.getContent()));
    }
    
    @Test
    public void testJsonBooleanList() throws JsonProcessingException {
        ObjectMapper mapper = new CustomObjectMapperBuilder().build();
        
        mapper.writeValueAsString(ImmutableList.of(Boolean.TRUE, Boolean.FALSE));
    }
    
    // Should fail on trying to parse hex formatted integer literals.
    @Test(expected=JsonParseException.class)
    public void testJsonHexBodies() throws IOException {
        ObjectMapper mapper = HexObjectMapperFactory.createObjectMapper();
        
        // "avrftdi" contains hex values.
        check(mapper.writeValueAsString(resource.getProgrammerById("avrftdi")));
    }
    
    
    // Should fail on trying to parse hex formatted integer literals.
    @Test(expected=JsonParseException.class)
    public void testJsonHexListBodies() throws IOException {
        ObjectMapper mapper = HexObjectMapperFactory.createObjectMapper();
        
        // "jtag3" contains a hex list value.
        check(mapper.writeValueAsString(resource.getProgrammerById("jtag3")));
    }
    
    private void check(String jsonString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        
        JsonNode root = mapper.readTree(jsonString);
        
        Assert.assertTrue(root.get("programmers").isArray());
        Assert.assertTrue(root.get("parts").isArray());
    }
}
