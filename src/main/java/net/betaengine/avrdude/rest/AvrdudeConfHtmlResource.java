package net.betaengine.avrdude.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.html.HtmlEscapers;
import com.google.common.io.Resources;
import com.google.common.primitives.Ints;

@Singleton
@Path("/conf")
public class AvrdudeConfHtmlResource {
    private final static Pattern CONTENT_PATTERN = Pattern.compile("\\[CONTENT\\]");
    private final static Pattern PRETTYPRINT_CLASS_PATTERN = Pattern.compile(" class=\"prettyprint\"");
    
    private ResourceHelper helper = new ResourceHelper();
    private ObjectMapper mapper = HexObjectMapperFactory.createObjectMapper();
    private final String template = getTemplate();

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getAllIds() {
        // See this method in commit 8b91516eae9d002fa1c4b4a016b268fe63c965aa
        // for a nice example of using DefaultPrettyPrinter with Lf2SpacesIndenter
        // to format a long array with each element split out onto its own line.

        StringBuilder b = new StringBuilder("{\n  \"programmers\" : ");

        b.append(getCommentedArray((helper.getProgrammerIds())).replace("\n", "\n  "));
        b.append(",\n  \"parts\" : ");
        b.append(getCommentedArray((helper.getPartIds())).replace("\n", "\n  "));
        b.append("\n}");

        return html(b.toString());
    }
    
    @GET @Path("/content")
    @Produces(MediaType.TEXT_HTML)
    public String getContent() {
        String s = stringify(helper.getContent());

        // Trying to pretty printing the entire contents "kills" the browser
        // so remove the "prettyprint" class from the <pre> tag.
        return PRETTYPRINT_CLASS_PATTERN.matcher(s).replaceFirst("");
    }
    
    @GET @Path("/programmers/ids")
    @Produces(MediaType.TEXT_HTML)
    public String getProgrammerIds() {
        return html(getCommentedArray((helper.getProgrammerIds())));
    }
    
    @GET @Path("/programmers/ids/{id}")
    @Produces(MediaType.TEXT_HTML)
    public String findProgrammerById(@PathParam("id")String id) {
        return stringify(helper.findProgrammerById(id));
    }
    
    @GET @Path("/parts/ids")
    @Produces(MediaType.TEXT_HTML)
    public String getPartIds() {
        return html(getCommentedArray((helper.getPartIds())));
    }
    
    @GET @Path("/parts/ids/{id}")
    @Produces(MediaType.TEXT_HTML)
    public String findPartById(@PathParam("id")String id) {
        return stringify(helper.findPartById(id));
    }
    
    @GET @Path("/parts/signatures")
    @Produces(MediaType.TEXT_HTML)
    public String getPartSignatures() {
        return html(getCommentedSignatureArray((helper.getPartSignatures())));
    }
    
    @GET @Path("/parts/signatures/{signature}")
    @Produces(MediaType.TEXT_HTML)
    public String findPartBySignature(@PathParam("signature")String hex) {
        return stringify(helper.findPartBySignature(hex));
    }
    
    private String getCommentedArray(Map<String, String> descriptions) {
        StringBuilder b = new StringBuilder("[\n");

        for (Map.Entry<String, String> entry : descriptions.entrySet()) {
            b.append(String.format("  \"%s\", // %s\n", entry.getKey(), entry.getValue()));
        }
        
        b.append(']');
        
        return b.toString();
    }
    
    private String getCommentedSignatureArray(Map<List<Integer>, String> descriptions) {
        StringBuilder b = new StringBuilder("[\n");

        for (Map.Entry<List<Integer>, String> entry : descriptions.entrySet()) {
            int[] s = Ints.toArray(entry.getKey());
            b.append(String.format("  [ 0x%02x, 0x%02x, 0x%02x ], // 0x%1$02x%2$02x%3$02x - %s\n",
                    s[0], s[1], s[2], entry.getValue()));
        }
        
        b.append(']');
        
        return b.toString();
    }
    
    private String stringify(Object o) {
        try {
            return html(mapper.writeValueAsString(o));
        } catch (JsonProcessingException e) {
            throw new ResourceException(e);
        }
    }
    
    private String html(String s) {
        s = HtmlEscapers.htmlEscaper().escape(s);
        
        return CONTENT_PATTERN.matcher(template).replaceFirst(s);
    }
    
    private String getTemplate() {
        try {
            return Resources.toString(
                    getClass().getResource("template.html"), Charsets.UTF_8);
        } catch (IOException e) {
            throw new ResourceException(e);
        }
    }
}
