package net.betaengine.avrdude.rest;

import java.io.StringWriter;
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

import net.betaengine.avrdude.BodyPrinter;
import net.betaengine.avrdude.ConfParser;
import net.betaengine.avrdude.IndentPrintWriter;
import net.betaengine.avrdude.Maker;
import net.betaengine.avrdude.NamePrinter;
import net.betaengine.avrdude.Part;
import net.betaengine.avrdude.Value;
import net.betaengine.avrdude.Value.HexListValue;

@Singleton
@Path("/conf")
public class AvrdudeConfResource {
    private final Maker maker = new Maker();
    
    public AvrdudeConfResource() {
        ConfParser parser = new ConfParser();
        
        parser.parse(maker);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllIds() {
        Stringer stringer = new Stringer();
        new NamePrinter().printAll(stringer.getWriter(), maker);
        return stringer.toString();
    }
    
    @GET @Path("/content")
    @Produces(MediaType.APPLICATION_JSON)
    public String getContent() {
        Stringer stringer = new Stringer();
        new BodyPrinter().printAll(stringer.getWriter(), maker);
        return stringer.toString();
    }
    
    @GET @Path("/programmers/ids")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getProgrammerIds() {
        List<String> result = new ArrayList<>();
        
        for (Map<String, Value<?>> programmer : maker.getProgrammers()) {
            result.add((String)programmer.get("id").getValue());
        }
        
        return result;
    }
    
    @GET @Path("/programmers/ids/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getProgrammerById(@PathParam("id")String id) {
        Stringer stringer = new Stringer();
        new BodyPrinter().printProgrammer(stringer.getWriter(),
                findProgrammerById(id));
        return stringer.toString();
    }
    
    private Map<String, Value<?>> findProgrammerById(String id) {
        for (Map<String, Value<?>> programmer : maker.getProgrammers()) {
            if (programmer.get("id").getValue().equals(id)) {
                return programmer;
            }
        }
        
        throw new WebApplicationException(Status.NOT_FOUND);
    }
    
    private Part findPartById(String id) {
        for (Part part : maker.getParts()) {
            if (part.getProperties().get("id").getValue().equals(id)) {
                return part;
            }
        }
        
        throw new WebApplicationException(Status.NOT_FOUND);
    }
    
    @GET @Path("/parts/ids")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPartIds() {
        Stringer stringer = new Stringer();
        new NamePrinter().printParts(stringer.getWriter(), maker.getParts());
        return stringer.toString();
    }
    
    @GET @Path("/parts/ids/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPartById(@PathParam("id")String id) {
        Stringer stringer = new Stringer();
        new BodyPrinter().printPart(stringer.getWriter(),
                findPartById(id));
        return stringer.toString();
    }
    
    @GET @Path("/parts/signatures")
    @Produces(MediaType.APPLICATION_JSON)
    public List<List<Integer>> getPartSignatures() {
        List<List<Integer>> result = new ArrayList<>();
        
        for (Part part : maker.getParts()) {
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
    public String getPartBySignature(@PathParam("signature")String text) {
        Matcher m = SIGNATURE_PATTERN.matcher(text);
        
        if (!m.matches()) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        List<Integer> signature = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            signature.add(Integer.valueOf(m.group(i), 16));
        }

        Stringer stringer = new Stringer();
        new BodyPrinter().printPart(stringer.getWriter(),
                findPartBySignature(signature));
        return stringer.toString();
    }
    
    private Part findPartBySignature(List<Integer> signature) {
        for (Part part : maker.getParts()) {
            Value<?> value = part.getProperties().get("signature");
            
            if (value != null && value.getValue().equals(signature)) {
                return part;
            }
        }
        
        throw new WebApplicationException(Status.NOT_FOUND);
    }

    private static class Stringer {
        private final StringWriter stringer = new StringWriter();
        private final IndentPrintWriter writer = new IndentPrintWriter(stringer);
        
        public IndentPrintWriter getWriter() { return writer; }
        
        @Override
        public String toString() {
            writer.flush();
            writer.close();
            
            return stringer.toString();
        }
    }
}
