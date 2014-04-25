package net.betaengine.avrdude;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Maker {
	private final List<Map<String, Value<?>>> programmers = new ArrayList<>();
	private final List<Part> parts = new ArrayList<>();
	
	public List<Map<String, Value<?>>> getProgrammers() {
		return programmers;
	}
	
	public final List<Part> getParts() {
		return parts;
	}
	
	public Map<String, Value<?>> createProgrammer(String parent) {
		Map<String, Value<?>> child = parent == null ?
				new LinkedHashMap<String, Value<?>>() : copyProgrammer(parent);
		
		programmers.add(child);
		
		return child;
	}
	
	public Part createPart(String parent) {
		Part child = parent == null ? new Part() : copyPart(parent);
		
		parts.add(child);
		
		return child;
	}
	
	private Map<String, Value<?>> copyProgrammer(String id) {
		for (Map<String, Value<?>> programmer : programmers) {
			if (matches(programmer, id)) {
				return new LinkedHashMap<>(programmer);
			}
		}
		
		throw new NoParentException(id);
	}
	
	private Part copyPart(String id) {
		for (Part part : parts) {
			if (matches(part.getProperties(), id)) {
				return new Part(part);
			}
		}
		
		throw new NoParentException(id);
	}
	
	private boolean matches(Map<String, Value<?>> properties, String id) {
		return properties.get("id").getValue().equals(id);
	}
	
	@SuppressWarnings("serial")
	private static class NoParentException extends RuntimeException {
		public NoParentException(String id) {
			super("no parent with id \"" + id + "\"");
		}
	}
}