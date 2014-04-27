package net.betaengine.avrdude;

import java.util.Map;

public class NamePrinter extends AbstractPrinter {
    @Override
    public void printProgrammer(IndentPrintWriter writer,
            Map<String, Value<?>> properties) {
        writer.format("\"%s\"", getName(properties));
    }
    
    @Override
    public void printPart(IndentPrintWriter writer, Part part) {
        writer.format("\"%s\"", getName(part.getProperties()));
    }
    
    private String getName(Map<String, Value<?>> properties) {
        return (String)properties.get("id").getValue();
    }
}
