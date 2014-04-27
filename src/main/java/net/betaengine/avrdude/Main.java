package net.betaengine.avrdude;

import java.io.OutputStreamWriter;

public class Main {
	public static void main(String[] args) {
		ConfParser parser = new ConfParser();
		Maker maker = new Maker();
		
		parser.parse(maker);
		
		IndentPrintWriter writer = new IndentPrintWriter(new OutputStreamWriter(System.out));
		BodyPrinter bodyPrinter = new BodyPrinter(false);
		NamePrinter namePrinter = new NamePrinter();
		
		namePrinter.printAll(writer, maker);
		writer.println();
		bodyPrinter.printAll(writer, maker);
		
		writer.flush();
	}
}
