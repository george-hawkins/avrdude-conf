package net.betaengine.avrdude;

import java.io.OutputStreamWriter;

public class Main {
    public static void main(String[] args) {
        boolean names = args.length == 1 && args[0].equals("--names");
        
        if (args.length != 0 && !names) {
            System.err.println("Usage: java " + Main.class.getName() + " [--names]");
            System.exit(1);
        }
        ConfParser parser = new ConfParser();
        Maker maker = new Maker();
        
        parser.parse(maker);
        
        IndentPrintWriter writer = new IndentPrintWriter(new OutputStreamWriter(System.out));
        
        // Warning: using non-strict JSON with hex formatted integer literals.
        AbstractPrinter printer = names ? new NamePrinter() : new BodyPrinter(false);

        printer.printAll(writer, maker);
        
        writer.flush();
    }
}