package net.betaengine.avrdude;

@SuppressWarnings("serial")
public class ParseException extends RuntimeException {
	public ParseException(String s) { super(s); }
	
	public ParseException(Throwable t) { super(t); }
}