package net.betaengine.avrdude;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.betaengine.avrdude.Value.BooleanValue;
import net.betaengine.avrdude.Value.DecimalListValue;
import net.betaengine.avrdude.Value.DecimalValue;
import net.betaengine.avrdude.Value.EnumValue;
import net.betaengine.avrdude.Value.HexListValue;
import net.betaengine.avrdude.Value.HexValue;
import net.betaengine.avrdude.Value.InvertedValue;
import net.betaengine.avrdude.Value.StringListValue;
import net.betaengine.avrdude.Value.StringValue;

import com.google.common.base.Joiner;

public class BodyPrinter extends AbstractPrinter {
    private boolean strict = true;
    
    public BodyPrinter() { this(true); }
    
    public BodyPrinter(boolean strict) {
        this.strict = strict;
    }
    
	@Override
    public void printProgrammer(IndentPrintWriter writer,
			Map<String, Value<?>> properties) {
		writer.println('{');
		writer.increment();
		
		printProperties(writer, properties);
		writer.println();
		
		writer.decrement();
		writer.print('}');
	}
	
	@Override
    public void printPart(IndentPrintWriter writer, Part part) {
		writer.println('{');
		writer.increment();
		
		printProperties(writer, part.getProperties());
		
		if (!part.getProperties().isEmpty()) {
			writer.println(',');
		}
		
		writer.println("\"memories\": {");
		writer.increment();
		
		Iterator<Map.Entry<String, Map<String, Value<?>>>> i =
				part.getMemories().entrySet().iterator();

		while (i.hasNext()) {
			Map.Entry<String, Map<String, Value<?>>> entry = i.next();
			
			writer.format("\"%s\": {%n", entry.getKey());
			writer.increment();
			
			printProperties(writer, entry.getValue());
			writer.println();
			
			writer.decrement();
			writer.println(i.hasNext() ? "}," : "}");
		}
		
		writer.decrement();
		writer.println('}');
		
		writer.decrement();
		writer.print('}');
	}
	
	private void printProperties(IndentPrintWriter writer,
			Map<String, Value<?>> properties) {
		PrintValueVisitor printer = new PrintValueVisitor(strict, writer);
		Iterator<Map.Entry<String, Value<?>>> i = properties.entrySet().iterator();
		
		while (i.hasNext()) {
			Map.Entry<String, Value<?>> entry = i.next();
			
			printer.print(entry.getKey(), entry.getValue());
			if (i.hasNext()) {
				writer.println(',');
			}
		}
	}

	private static class PrintValueVisitor implements ValueVisitor {
	    private final boolean strict;
		private final PrintWriter writer;
		
		public PrintValueVisitor(boolean strict, PrintWriter writer) {
		    this.strict = strict;
			this.writer = writer;
		}

		public void print(String key, Value<?> value) {
			writer.print("\"" + key + "\": ");
			value.accept(this);
		}
		
		@Override
		public void visit(StringValue value) {
			writer.format("\"%s\"", value.getValue());
		}

		@Override
		public void visit(StringListValue value) {
			List<String> l = value.getValue();
			String s = l.isEmpty() ? "" : "\"" + Joiner.on("\", \"").join(l) + "\"";
			
			writer.format("[ %s ]", s);
		}

		@Override
		public void visit(DecimalValue value) {
			writer.print(value.getValue());
		}

		@Override
		public void visit(DecimalListValue value) {
			List<Integer> l = value.getValue();
			String s = l.isEmpty() ? "" : Joiner.on(", ").join(l);
			
			writer.format("[ %s ]", s);
		}

		@Override
		public void visit(InvertedValue value) {
			writer.format("\"~%d\"", value.getValue());
		}

		@Override
		public void visit(BooleanValue value) {
			writer.print(value.getValue());
		}

		@Override
		public void visit(EnumValue value) {
			writer.format("\"%s\"", value.getValue());
		}

		// JSON doesn't allow hex numerical literals, e.g. 0xFF.
		// However you can force the printing of such values by setting strict to false.
		private void printHex(long value, int nibbles) {
            if (strict) {
                writer.print(value);
            } else {
                writer.format("0x%0" + nibbles + "x", value);
            }
		}

		@Override
		public void visit(HexValue value) {
		    printHex(value.getValue(), value.getNibbles());
		}

		@Override
		public void visit(HexListValue value) {
			Iterator<Integer> i = value.getValue().iterator();
			
            writer.print("[ ");
            
            while (i.hasNext()) {
                printHex(i.next(), value.getNibbles());
                
                if (i.hasNext()) {
                    writer.print(", ");
                }
            }
            
            writer.print(" ]");
		}
	}
}