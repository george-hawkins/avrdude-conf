package net.betaengine.avrdude;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AbstractPrinter {
    public void printAll(IndentPrintWriter writer, Maker maker) {
        writer.println('{');
        writer.increment();

        writer.print("\"programmers\": ");
        printProgrammers(writer, maker.getProgrammers());
        writer.println(",");
        
        writer.print("\"parts\": ");
        printParts(writer, maker.getParts());
        writer.println();
        
        writer.decrement();
        writer.print('}');
    }
    
    public void printProgrammers(IndentPrintWriter writer,
            List<Map<String, Value<?>>> programmers) {
        writer.println('[');
        writer.increment();
        
        Iterator<Map<String, Value<?>>> i = programmers.iterator();
        
        while (i.hasNext()) {
            printProgrammer(writer, i.next());

            writer.println(i.hasNext() ? "," : "");
        }
        
        writer.decrement();
        writer.print(']');
    }

    public void printParts(IndentPrintWriter writer, List<Part> parts) {
        writer.println('[');
        writer.increment();
        
        Iterator<Part> i = parts.iterator();
        
        while (i.hasNext()) {
            printPart(writer, i.next());

            writer.println(i.hasNext() ? "," : "");
        }
        
        writer.decrement();
        writer.print(']');
    }
    
    public abstract void printProgrammer(IndentPrintWriter writer,
            Map<String, Value<?>> programmer);
    
    public abstract void printPart(IndentPrintWriter writer,
            Part part);
}