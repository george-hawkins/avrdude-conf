package net.betaengine.avrdude;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class ConfParser {
	private final static Logger log = LoggerFactory.getLogger(ConfParser.class);
	
	public void parse(Maker maker, String spec) {
		try {
			process(new TopLevelBuilder(maker), new LineSupplier(spec));
		} catch (IOException e) {
			throw new ParseException(e);
		}
	}

	private void process(ConfBuilder parent, LineSupplier lines) {
		String line;

		while ((line = lines.next()) != null) {
			if (line.equals(";")) {
				return;
			} else {
				if (line.indexOf('=') != -1) {
					StringBuilder builder = new StringBuilder(line);

					while (line.indexOf(';') == -1) {
						line = lines.next();
						builder.append(' ');
						builder.append(line);
					}

					parent.addProperty(builder.toString());
				} else {
					ConfBuilder child = parent.addContainer(line);

					process(child, lines);
				}
			}
		}
	}
	
	private interface ConfBuilder {
		ConfBuilder addContainer(String s);
		
		void addProperty(String s);
	}
	
	private static class TopLevelBuilder implements ConfBuilder {
		private final static Pattern PATTERN = Pattern.compile("(programmer|part)(?: parent \"(.*)\")?");
		private final Maker maker;
		
		public TopLevelBuilder(Maker maker) {
			this.maker = maker;
		}

		@Override
		public ConfBuilder addContainer(String s) {
			Matcher m = PATTERN.matcher(s);
			
			assert m.matches();
			
			String type = m.group(1);
			String parent = m.group(2);

			return type.equals("programmer") ?
					new PropertiesBuilder(maker.createProgrammer(parent)) :
					new PartBuilder(maker.createPart(parent));
		}

		@Override
		public void addProperty(String s) {
			log.info("ignoring {}", s);
		}
	}
	
	private static class PropertiesBuilder implements ConfBuilder {
		private final static ValueParser VALUE_PARSER = new ValueParser();

		private final Map<String, Value<?>> properties;

		public PropertiesBuilder(Map<String, Value<?>> properties) {
			this.properties = properties;
		}
		
		@Override
		public ConfBuilder addContainer(String s) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void addProperty(String s) {
			int i = s.indexOf('=');
			String key = s.substring(0, i).trim();
			i++;
			String text = s.substring(i, s.indexOf(';', i)).trim();
			Value<?> value = VALUE_PARSER.parse(text);
			
			if (value == Value.DELETE_VALUE) {
				properties.remove(key);
			} else {
				properties.put(key, value);
			}
		}
	}
	
	private static class PartBuilder extends PropertiesBuilder {
		private final static Pattern PATTERN = Pattern.compile("memory \"(.*)\"");
		private final Part part;

		public PartBuilder(Part part) {
			super(part.getProperties());
			this.part = part;
		}
		
		@Override
		public ConfBuilder addContainer(String s) {
			Matcher m = PATTERN.matcher(s);
			
			assert m.matches();
			
			String qualifier = m.group();
			Map<String, Value<?>> memory = new LinkedHashMap<>();
			
			part.addMemory(qualifier, memory);
			
			return new PropertiesBuilder(memory);
		}
	}
	
	private static class LineSupplier {
		private final Iterator<String> lines;

		public LineSupplier(String spec) throws IOException {
			lines = Resources.readLines(new URL(spec), Charsets.UTF_8).iterator();
		}

		public String next() {
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
	}
}