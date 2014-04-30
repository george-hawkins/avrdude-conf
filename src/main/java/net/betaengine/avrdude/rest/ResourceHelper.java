package net.betaengine.avrdude.rest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.betaengine.avrdude.Maker;
import net.betaengine.avrdude.Part;
import net.betaengine.avrdude.Value;
import net.betaengine.avrdude.Value.HexListValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class ResourceHelper {
    private Maker maker() { return MakerSingleton.getInstance(); }
    
    public Map<String, List<String>> getAllIds() {
        return ImmutableMap.<String, List<String>>of(
                "programmers", ImmutableList.copyOf(getProgrammerIds().keySet()),
                "parts", ImmutableList.copyOf(getPartIds().keySet()));
    }
    
    public Map<String, List<?>> getContent() {
        return ImmutableMap.of(
                "programmers", maker().getProgrammers(),
                "parts", maker().getParts());
    }
    
    public Map<String, String> getProgrammerIds() {
        Map<String, String> result = new LinkedHashMap<>();
        
        for (Map<String, Value<?>> programmer : maker().getProgrammers()) {
            result.put(
                    (String)programmer.get("id").getValue(),
                    (String)programmer.get("desc").getValue());
        }
        
        return result;
    }
    
    public Map<String, Value<?>> findProgrammerById(String id) {
        for (Map<String, Value<?>> programmer : maker().getProgrammers()) {
            if (programmer.get("id").getValue().equals(id)) {
                return programmer;
            }
        }
        
        throw new NotFoundException("could not find programmer with id \"" + id + "\".");
    }
    
    public Part findPartById(String id) {
        for (Part part : maker().getParts()) {
            if (part.getProperties().get("id").getValue().equals(id)) {
                return part;
            }
        }
        
        throw new NotFoundException("could not find part with id \"" + id + "\".");
    }
    
    public Map<String, String> getPartIds() {
        Map<String, String> result = new LinkedHashMap<>();
        
        for (Part part : maker().getParts()) {
            result.put(
                    (String)part.getProperties().get("id").getValue(),
                    (String)part.getProperties().get("desc").getValue());
        }
        
        return result;
    }
    
    public Map<List<Integer>, String> getPartSignatures() {
        Map<List<Integer>, String> result = new LinkedHashMap<>();
        
        for (Part part : maker().getParts()) {
            HexListValue signature = (HexListValue)part.getProperties().get("signature");
            // Dummy parts like ".xmega" don't have signatures.
            if (signature != null) {
                result.put(
                        signature.getValue(),
                        (String)part.getProperties().get("id").getValue());
            }
        }
        
        return result;
    }
    
    private final static Pattern SIGNATURE_PATTERN = Pattern
            .compile("0x([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])([0-9a-f][0-9a-f])");
    
    private List<Integer> parseSignature(String hex) {
        Matcher m = SIGNATURE_PATTERN.matcher(hex.toLowerCase());
        
        if (!m.matches()) {
            throw new NotFoundException("Signature should be a 6 nibble hex value, e.g. 0x1e9514.");
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
        
        throw new NotFoundException("could not find part with signature " + signature + ".");
    }
    
    @SuppressWarnings("serial")
    private static class NotFoundException extends WebApplicationException {
        // The given message appears in the browser as the 404 text.
        public NotFoundException(String message) {
            super(Response
                    .status(Status.NOT_FOUND)
                    .entity(message)
                    .type(MediaType.TEXT_PLAIN)
                    .build());
        }
   }
}