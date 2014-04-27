package net.betaengine.avrdude;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
