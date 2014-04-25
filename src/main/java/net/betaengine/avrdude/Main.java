package net.betaengine.avrdude;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

public class Main {
	// Use a fixed revision of avrdude.conf.in that has been tested to parse as expected.
	private static String AVRDUDE_CONF_IN_URL =
			"http://svn.savannah.nongnu.org/viewvc/*checkout*/trunk/avrdude/avrdude.conf.in?revision=1297&root=avrdude";
	
	public static void main(String[] args) {
		ConfParser parser = new ConfParser();
		Maker maker = new Maker();
		
		parser.parse(maker, AVRDUDE_CONF_IN_URL);
		
		dump(maker);
	}
	
	private static void dump(Maker maker) {
		IndentWriter indenter = new IndentWriter(new OutputStreamWriter(System.out));
		@SuppressWarnings("resource")
		PrintWriter writer = new PrintWriter(indenter);
		
		writer.println("programmers");
		indenter.increment();
		for (Map<String, Value<?>> programmer : maker.getProgrammers()) {
			writer.println(programmer.get("id").getValue());
		}
		indenter.decrement();
		
		writer.println();
		
		writer.println("parts");
		indenter.increment();
		for (Part part : maker.getParts()) {
			writer.println(part.getProperties().get("id").getValue());
		}
		indenter.decrement();
		
		writer.flush();
		// Don't close the writer (as this would also close System.out).
	}
}
