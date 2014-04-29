package net.betaengine.avrdude.rest;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.html.HtmlEscapers;

@Singleton
@Path("/conf")
public class AvrdudeConfHtmlResource {
    private ResourceHelper helper = new ResourceHelper();
    private ObjectMapper mapper = HexObjectMapperFactory.createObjectMapper();
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAllIds() {
        return stringify(helper.getAllIds());
    }
    
    @GET @Path("/content")
    @Produces(MediaType.TEXT_HTML)
    public String getContent() {
        return stringify(helper.getContent());
    }
    
    @GET @Path("/programmers/ids")
    @Produces(MediaType.TEXT_HTML)
    public String getProgrammerIds() {
        return stringify(helper.getProgrammerIds());
    }
    
    @GET @Path("/programmers/ids/{id}")
    @Produces(MediaType.TEXT_HTML)
    public String findProgrammerById(@PathParam("id")String id) {
        return stringify(helper.findProgrammerById(id));
    }
    
    @GET @Path("/parts/ids")
    @Produces(MediaType.TEXT_HTML)
    public String getPartIds() {
        return stringify(helper.getPartIds());
    }
    
    @GET @Path("/parts/ids/{id}")
    @Produces(MediaType.TEXT_HTML)
    public String findPartById(@PathParam("id")String id) {
        return stringify(helper.findPartById(id));
    }
    
    @GET @Path("/parts/signatures")
    @Produces(MediaType.TEXT_HTML)
    public String getPartSignatures() {
        return stringify(helper.getPartSignatures());
    }
    
    @GET @Path("/parts/signatures/{signature}")
    @Produces(MediaType.TEXT_HTML)
    public String findPartBySignature(@PathParam("signature")String hex) {
        return stringify(helper.findPartBySignature(hex));
    }
    
    private String stringify(Object o) {
        try {
            String s = mapper.writeValueAsString(o);
            
            s = HtmlEscapers.htmlEscaper().escape(s);
            
            return HTML_START + s + HTML_END;
        } catch (JsonProcessingException e) {
            throw new ResourceException(e);
        }
    }
    
    @SuppressWarnings("serial")
    private static class ResourceException extends RuntimeException {
        public ResourceException(Throwable t) {
            super(t);
        }
    }
    
    private final static String HTML_START =
            "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "  <head>\n" +
            "    <meta charset=\"utf-8\">\n" +
            "    <title>avrdude.conf</title>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <pre>";

    private final static String HTML_END =
            "</pre>\n" +
            "  </body>\n" +
            "</html>\n";
}
