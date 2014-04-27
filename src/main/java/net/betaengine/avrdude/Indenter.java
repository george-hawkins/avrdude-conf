package net.betaengine.avrdude;

import java.io.IOException;
import java.io.Writer;

public interface Indenter {
    void increment();
    void decrement();
    
    public class IndentWriter extends Writer implements Indenter {
        private final static String INDENTATION = "  ";
        private final Writer out;
        private boolean indent = true;
        private int count = 0;

        protected IndentWriter(Writer out) {
            super(out);
            this.out = out;
        }

        @Override
        public void increment() { count++; }

        @Override
        public void decrement() { count--; }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            for (int i = 0; i < len; i++) {
                char c = cbuf[off++];

                if (c == '\r' || c == '\n') {
                    indent = true;
                } else if (indent) {
                    indent = false;
                    for (int j = 0; j < count; j++) {
                        out.write(INDENTATION);
                    }
                }
                out.write(c);
            }
        }

        @Override
        public void flush() throws IOException {
            out.flush();
        }

        @Override
        public void close() throws IOException {
            out.close();
        }
    }
}