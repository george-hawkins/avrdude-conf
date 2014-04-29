package net.betaengine.avrdude.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import net.betaengine.avrdude.Maker;
import net.betaengine.avrdude.Part;
import net.betaengine.avrdude.Value;
import net.betaengine.avrdude.Value.HexListValue;

import com.google.common.collect.ImmutableMap;

@Singleton
@Path("/conf")
public class AvrdudeConfJsonResource {
    private Maker maker() { return MakerSingleton.getInstance(); }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<String>> getAllIds() {
        return ImmutableMap.of(
                "programmers", getProgrammerIds(),
                "parts", getPartIds());
    }
    
    @GET @Path("/content")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<?>> getContent() {
        return ImmutableMap.of(
                "programmers", maker().getProgrammers(),
                "parts", maker().getParts());
    }
    
    @GET @Path("/programmers/ids")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getProgrammerIds() {
        List<String> result = new ArrayList<>();
        
        for (Map<String, Value<?>> programmer : maker().getProgrammers()) {
            result.add((String)programmer.get("id").getValue());
        }
        
        return result;
    }
    
    @GET @Path("/programmers/ids/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Value<?>> getProgrammerById(@PathParam("id")String id) {
        return findProgrammerById(id);
    }
    
    private Map<String, Value<?>> findProgrammerById(String id) {
        for (Map<String, Value<?>> programmer : maker().getProgrammers()) {
            if (programmer.get("id").getValue().equals(id)) {
                return programmer;
            }
        }
        
        throw new WebApplicationException(Status.NOT_FOUND);
    }
    
    private Part findPartById(String id) {
        for (Part part : maker().getParts()) {
            if (part.getProperties().get("id").getValue().equals(id)) {
                return part;
            }
        }
        
        throw new WebApplicationException(Status.NOT_FOUND);
    }
    
    @GET @Path("/parts/ids")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getPartIds() {
        List<String> result = new ArrayList<>();
        
        for (Part part : maker().getParts()) {
            result.add((String)part.getProperties().get("id").getValue());
        }
        
        return result;
    }
    
    @GET @Path("/parts/ids/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Part getPartById(@PathParam("id")String id) {
        return findPartById(id);
    }
    
    @GET @Path("/parts/signatures")
    @Produces(MediaType.APPLICATION_JSON)
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
    
    @GET @Path("/parts/signatures/{signature}")
    @Produces(MediaType.APPLICATION_JSON)
    public Part getPartBySignature(@PathParam("signature")String hex) {

        return findPartBySignature(parseSignature(hex));
    }
    
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
