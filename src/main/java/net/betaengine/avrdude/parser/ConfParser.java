package net.betaengine.avrdude.parser;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.betaengine.avrdude.Maker;
import net.betaengine.avrdude.Part;
import net.betaengine.avrdude.Value;
import net.betaengine.util.cache.Cache;
import net.betaengine.util.cache.Cache.ValueCreator;
import net.betaengine.util.cache.CacheException;
import net.betaengine.util.cache.CacheFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;

public class ConfParser {
    private final static Logger log = LoggerFactory.getLogger(ConfParser.class);
    
    // Use a fixed revision of avrdude.conf.in that has been tested to parse as expected.
    private final static String DEFAULT_AVRDUDE_CONF_IN_URL =
            "http://svn.savannah.nongnu.org/viewvc/*checkout*/trunk/avrdude/avrdude.conf.in?revision=1297&root=avrdude";
    private final static String AVRDUDE_CONF_IN_URL =
            System.getProperty("avrdude.conf.in.url", DEFAULT_AVRDUDE_CONF_IN_URL);
    
    public void parse(Maker maker) {
        parse(maker, AVRDUDE_CONF_IN_URL);
    }
    
    public void parse(Maker maker, String spec) {
        try {
            process(new TopLevelBuilder(maker), new LineSupplier(getContents(spec)));
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }
    
    // http://www.random.org/cgi-bin/randbyte?nbytes=16&format=h
    private final static String CONF_PARSER_UUID = "9cd8825c-6f91-f3c4-6972-34e5908c055c";
    private final static Cache CACHE = CacheFactory.getCache(CONF_PARSER_UUID);
    
    private String getContents(final String spec) {
        // Fetching from svn.savannah.nongnu.org is extremely slow (averaging
        // around 10 seconds) so we cache the retrieved content.
        return CACHE.get(spec, new ValueCreator() {
            @Override
            public String create() {
                try {
                    return Resources.toString(new URL(spec), Charsets.UTF_8);
                } catch (IOException e) {
                    throw new CacheException(e);
                }
            }
        });
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
            
            if (!m.matches()) {
                throw new ParseException(s);
            }
            
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
            
            if (!m.matches()) {
                throw new ParseException(s);
            }
            
            String qualifier = m.group(1);
            Map<String, Value<?>> memory = new LinkedHashMap<>();
            
            part.addMemory(qualifier, memory);
            
            return new PropertiesBuilder(memory);
        }
    }
    
    private static class LineSupplier {
        private final Iterator<String> lines;

        public LineSupplier(String content) throws IOException {
            lines = CharSource.wrap(content).readLines().iterator();
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

            // @HAVE_PARPORT is used for magic #ifdef like logic.
            if (s.startsWith("@HAVE_PARPORT")) {
                s = "";
            }

            return s.trim();
        }
    }
}