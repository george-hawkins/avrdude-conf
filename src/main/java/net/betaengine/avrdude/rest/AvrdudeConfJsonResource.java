package net.betaengine.avrdude.rest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.betaengine.avrdude.Part;
import net.betaengine.avrdude.Value;

@Singleton
@Path("/conf")
public class AvrdudeConfJsonResource {
    private ResourceHelper helper = new ResourceHelper();
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<String>> getAllIds() {
        return helper.getAllIds();
    }
    
    @GET @Path("/content")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<?>> getContent() {
        return helper.getContent();
    }
    
    @GET @Path("/programmers/ids")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getProgrammerIds() {
        return helper.getProgrammerIds().keySet();
    }
    
    @GET @Path("/programmers/ids/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Value<?>> findProgrammerById(@PathParam("id")String id) {
        return helper.findProgrammerById(id);
    }
    
    @GET @Path("/parts/ids")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getPartIds() {
        return helper.getPartIds().keySet();
    }
    
    @GET @Path("/parts/ids/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Part findPartById(@PathParam("id")String id) {
        return helper.findPartById(id);
    }
    
    @GET @Path("/parts/signatures")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<List<Integer>> getPartSignatures() {
        return helper.getPartSignatures().keySet();
    }
    
    @GET @Path("/parts/signatures/{signature}")
    @Produces(MediaType.APPLICATION_JSON)
    public Part findPartBySignature(@PathParam("signature")String hex) {
        return helper.findPartBySignature(hex);
    }
}