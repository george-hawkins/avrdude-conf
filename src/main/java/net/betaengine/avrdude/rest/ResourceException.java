package net.betaengine.avrdude.rest;

@SuppressWarnings("serial")
public class ResourceException extends RuntimeException {
    public ResourceException(Throwable t) {
        super(t);
    }
}