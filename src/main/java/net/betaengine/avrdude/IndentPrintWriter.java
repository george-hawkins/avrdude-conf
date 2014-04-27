package net.betaengine.avrdude;

import java.io.PrintWriter;
import java.io.Writer;

public class IndentPrintWriter extends PrintWriter implements Indenter {
	private final IndentWriter out;
	
	public IndentPrintWriter(Writer out) {
		this(new IndentWriter(out));
	}

	private IndentPrintWriter(IndentWriter out) {
		super(out);
		
		this.out = out;
	}

	@Override
	public void increment() { out.increment(); }

	@Override
	public void decrement() { out.decrement(); }
}
