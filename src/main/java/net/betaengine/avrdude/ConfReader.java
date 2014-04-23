package net.betaengine.avrdude;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class ConfReader {
	private final static Logger log = LoggerFactory.getLogger(ConfReader.class);

	private Iterator<String> lines;

	public static void main(String[] args) {
		ConfReader reader = new ConfReader();
		// Use a fixed revision of avrdude.conf.in that has been tested to parse as expected.
		ConfContainer base = reader.process("http://svn.savannah.nongnu.org/viewvc/*checkout*/trunk/avrdude/avrdude.conf.in?revision=1297&root=avrdude");
		
		System.err.println(base);
	}

	public ConfContainer process(String spec) {
		try {
			lines = Resources.readLines(new URL(spec), Charsets.UTF_8).iterator();

			ConfContainer base = new BaseContainer();
			
			process(base);
			
			return base;
		} catch (Exception e) {
			throw unchecked(e);
		}
	}

	private void process(ConfContainer parent) {
		String line;

		while ((line = next()) != null) {
			if (line.equals(";")) {
				return;
			} else {
				if (line.indexOf('=') != -1) {
					StringBuilder builder = new StringBuilder(line);

					while (line.indexOf(';') == -1) {
						line = next();
						builder.append(' ');
						builder.append(line);
					}

					addProperty(parent, builder.toString());
				} else {
					ConfContainer child = parent.addContainer(line);

					process(child);
				}
			}
		}
	}
	
	private void addProperty(ConfContainer container, String s) {
		int i = s.indexOf('=');
		String key = s.substring(0, i).trim();
		i++;
		String value = s.substring(i, s.indexOf(';', i)).trim();
		
		container.addProperty(key, value);
	}
	
	private interface ConfContainer {
		ConfContainer addContainer(String s);
		
		void addProperty(String key, String value);
	}
	
//	{
//	TODO ADD toString() TO PropertiesContainer AND Part
//	GET RID OF OLD ConfContainer
//	CONSIDER SPLITTING INTO Builder AND non-Builder CLASSES
//	ADD getParts() AND getProgrammers() METHODS TO BaseContainer THAT RETURN MAPS.
//	}
	
	private static class PropertiesContainer implements ConfContainer {
		private final Map<String, String> properties = new LinkedHashMap<>();
		
		@Override
		public ConfContainer addContainer(String s) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addProperty(String key, String value) {
			properties.put(key, value);
		}
		
		public String getProperty(String key) {
			return properties.get(key);
		}
		
		public PropertiesContainer() { }
		
		public PropertiesContainer(PropertiesContainer original) {
			properties.putAll(original.properties);
		}
	}

	private static class Part extends PropertiesContainer {
		private final static Pattern PATTERN = Pattern.compile("memory \"(.*)\"");
		private final Map<String, PropertiesContainer> memories = new LinkedHashMap<>();
		
		@Override
		public PropertiesContainer addContainer(String s) {
			Matcher m = PATTERN.matcher(s);
			
			assert m.matches();
			
			String qualifier = m.group();
			PropertiesContainer memory = new PropertiesContainer();
			
			memories.put(qualifier, memory);
			
			return memory;
		}
		
		public Part() { }
		
		public Part(Part original) {
			super(original);
			memories.putAll(original.memories);
		}
	}
	
	private static class BaseContainer implements ConfContainer {
		private final static Pattern PATTERN = Pattern.compile("(programmer|part)(?: parent \"(.*)\")?");
		private final List<PropertiesContainer> programmers = new ArrayList<>();
		private final List<Part> parts = new ArrayList<>();

		@Override
		public ConfContainer addContainer(String s) {
			Matcher m = PATTERN.matcher(s);
			
			assert m.matches();
			
			String type = m.group(1);
			String parent = m.group(2);

			return type.equals("programmer") ? addProgrammer(parent) : addPart(parent);
		}
		
		private PropertiesContainer addProgrammer(String parent) {
			PropertiesContainer child = parent == null ? new PropertiesContainer() : new PropertiesContainer(find(programmers, parent));
			
			programmers.add(child);
			
			return child;
		}
		
		private Part addPart(String parent) {
			Part child = parent == null ? new Part() : new Part(find(parts, parent));
			
			parts.add(child);
			
			return child;
		}
		
		private <T extends PropertiesContainer> T find(List<T> parents, String id) {
			for (T parent : parents) {
				if (parent.getProperty("id").equals("\"" + id + "\"")) {
					return parent;
				}
			}
			
			throw new RuntimeException("no parent with id \"" + id + "\"");
		}

		@Override
		public void addProperty(String key, String value) {
			log.info("ignoring property {} = {}", key, value);
		}
	}
	
//		public String toString() {
//			StringWriter stringer = new StringWriter();
//			IndentWriter indenter = new IndentWriter(stringer);
//			PrintWriter writer = new PrintWriter(indenter);
//			
//			toString(writer, indenter);
//			
//			writer.flush();
//			writer.close();
//			
//			return stringer.toString();
//		}
//		
//		private void toString(PrintWriter writer, IndentWriter indenter) {
//			writer.print("<" + type + "> ");
//			if (parent != null) {
//				writer.print("(" + parent + ") ");
//			}
//			if (qualifier != null) {
//				writer.println("[" + qualifier + "] {");
//			}
//			writer.println("{");
//			indenter.increment();
//			
//			for (Map.Entry<String, String> entry : properties.entrySet()) {
//				writer.println("<" + entry.getKey() + "> = <" + entry.getValue() + ">");
//			}
//			
//			for (ConfContainer container : containers) {
//				writer.println();
//				container.toString(writer, indenter);
//			}
//			
//			indenter.decrement();
//			writer.println("}");
//		}

	private String next() {
		while ((lines.hasNext())) {
			String line = clean(lines.next());

			if (!line.isEmpty()) {
				return line;
			}
		}

		return null;
	}

	private String clean(String s) {
		int i;

		if ((i = s.indexOf('#')) != -1) {
			s = s.substring(0, i);
		}

		// Can only handle simple single line C style comments.
		if ((i = s.indexOf("/*")) != -1) {
			int end = s.indexOf("*/", i);
			s = s.substring(0, i) + s.substring(end + 2);
		}
		
		if (s.startsWith("@HAVE_PARPORT")) {
			s = "";
		}

		return s.trim();
	}

	private static RuntimeException unchecked(Exception e) {
		return (e instanceof RuntimeException) ? (RuntimeException)e : new RuntimeException(e);
	}
}
