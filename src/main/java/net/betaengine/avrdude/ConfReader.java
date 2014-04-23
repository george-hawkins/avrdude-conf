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
import java.util.TreeSet;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class ConfReader {
	private Iterator<String> lines;

	public static void main(String[] args) {
		ConfReader reader = new ConfReader();
		// Use a fixed revision of avrdude.conf.in that has been tested to parse as expected.
		ConfContainer base = reader.process("http://svn.savannah.nongnu.org/viewvc/*checkout*/trunk/avrdude/avrdude.conf.in?revision=1297&root=avrdude");
		
		System.err.println(base);
		System.err.println(allParts);
		System.err.println(allQualifiedParts);
		System.err.println(allParentedParts);
	}

	public ConfContainer process(String spec) {
		try {
			lines = Resources.readLines(new URL(spec), Charsets.UTF_8).iterator();

			ConfContainer base = new ConfContainer("base", null, null);
			
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

					parent.addProperty(builder.toString());
				} else {
					ConfContainer child = parent.addContainer(line);

					process(child);
				}
			}
		}
	}
	
	private final static Set<String> allParts = new HashSet<>();
	private final static Set<String> allQualifiedParts = new HashSet<>(); 
	private final static Set<String> allParentedParts = new HashSet<>(); 
	
	private static class ConfContainer {
		private final String type;
		private final String parent;
		private final String qualifier;
		private final Map<String, String> properties = new LinkedHashMap<>();
		private final List<ConfContainer> containers = new ArrayList<>();

		// Type is "programmer", "part" and "memory".
		// If type is "programmer" or "part" there maybe a specified parent.
		// If type is "memory" there must be a qualifier, e.g. "flash".
		public ConfContainer(String type, String parent, String qualifier) {
			this.type = type;
			this.parent = parent;
			this.qualifier = qualifier;

			assert qualifier == null ^ type.equals("memory");
			allParts.add(type);
			if (parent != null) {
				allParentedParts.add(type);
			}
			if (qualifier != null) {
				allQualifiedParts.add(type);
			}
		}

		public void addProperty(String s) {
			int i = s.indexOf('=');
			String key = s.substring(0, i).trim();
			i++;
			String value = dequote(s.substring(i, s.indexOf(';', i)).trim());

			assert !properties.containsKey(key);

			properties.put(key, value);
		}
		
		private String dequote(String s) {
			if (s.startsWith("\"") && s.endsWith("\"")) {
				String s2 = s.substring(1, (s.length() - 1));
				
				// Don't dequote things like z = "1 0 1 0", "x x x x".
				if (!s2.contains("\"")) {
					s = s2;
				}
			}
			return s;
		}

		public ConfContainer addContainer(String s) {
			String[] parts = s.split("\\s", 2);
			String parent = null;
			String qualifier = null;
			
			if (parts.length > 1) {
				String extra = parts[1];
				
				if (extra.startsWith("parent ")) {
					parent = dequote(extra.split("\\s", 2)[1]);
				} else {
					qualifier = dequote(extra);
				}
			}
			
			ConfContainer container = new ConfContainer(parts[0],
					parent, qualifier);

			containers.add(container);

			return container;
		}
		
		public String toString() {
			StringWriter stringer = new StringWriter();
			IndentWriter indenter = new IndentWriter(stringer);
			PrintWriter writer = new PrintWriter(indenter);
			
			toString(writer, indenter);
			
			writer.flush();
			writer.close();
			
			return stringer.toString();
		}
		
		private void toString(PrintWriter writer, IndentWriter indenter) {
			writer.print("<" + type + "> ");
			if (parent != null) {
				writer.print("(" + parent + ") ");
			}
			if (qualifier != null) {
				writer.println("[" + qualifier + "] {");
			}
			writer.println("{");
			indenter.increment();
			
			for (Map.Entry<String, String> entry : properties.entrySet()) {
				writer.println("<" + entry.getKey() + "> = <" + entry.getValue() + ">");
			}
			
			for (ConfContainer container : containers) {
				writer.println();
				container.toString(writer, indenter);
			}
			
			indenter.decrement();
			writer.println("}");
		}
	}

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
