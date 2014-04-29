package net.betaengine.avrdude.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import net.betaengine.avrdude.Maker;
import net.betaengine.avrdude.Part;
import net.betaengine.avrdude.Value;
import net.betaengine.avrdude.Value.HexListValue;

import com.google.common.collect.ImmutableMap;

public class ResourceHelper {
    private Maker maker() { return MakerSingleton.getInstance(); }
    
    public Map<String, List<String>> getAllIds() {
        return ImmutableMap.of(
                "programmers", getProgrammerIds(),
                "parts", getPartIds());
    }
    
    public Map<String, List<?>> getContent() {
        return ImmutableMap.of(
                "programmers", maker().getProgrammers(),
                "parts", maker().getParts());
    }
    
    public List<String> getProgrammerIds() {
        List<String> result = new ArrayList<>();
        
        for (Map<String, Value<?>> programmer : maker().getProgrammers()) {
            result.add((String)programmer.get("id").getValue());
        }
        
        return result;
    }
    
    public Map<String, Value<?>> findProgrammerById(String id) {
        for (Map<String, Value<?>> programmer : maker().getProgrammers()) {
            if (programmer.get("id").getValue().equals(id)) {
                return programmer;
            }
        }
        
        throw new WebApplicationException(Status.NOT_FOUND);
    }
    
    public Part findPartById(String id) {
        for (Part part : maker().getParts()) {
            if (part.getProperties().get("id").getValue().equals(id)) {
                return part;
            }
        }
        
        throw new WebApplicationException(Status.NOT_FOUND);
    }
    
    public List<String> getPartIds() {
        List<String> result = new ArrayList<>();
        
        for (Part part : maker().getParts()) {
            result.add((String)part.getProperties().get("id").getValue());
        }
        
        return result;
    }
    
    public List<List<Integer>> getPartSignatures() {
        List<List<Integer>> result = new ArrayList<>();
        
        for (Part part : maker().getParts()) {
            HexListValue value = (HexListValue)part.getProperties().get("signature");
            // Dummy parts like ".xmega" don't have signatures.
            if (value != null) {
                result.add(value.getValue());
            }
        }
        
        return result;
    }
    
    private final static Pattern SIGNATURE_PATTERN = Pattern
            .compile("0x([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])");
    
    private List<Integer> parseSignature(String hex) {
        Matcher m = SIGNATURE_PATTERN.matcher(hex);
        
        if (!m.matches()) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        
        List<Integer> signature = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            signature.add(Integer.valueOf(m.group(i), 16));
        }
        
        return signature;
    }
    
    public Part findPartBySignature(String hex) {
        return findPartBySignature(parseSignature(hex));
    }
    
    private Part findPartBySignature(List<Integer> signature) {
        for (Part part : maker().getParts()) {
            Value<?> value = part.getProperties().get("signature");
            
            if (value != null && value.getValue().equals(signature)) {
                return part;
            }
        }
        
        throw new WebApplicationException(Status.NOT_FOUND);
    }
}
