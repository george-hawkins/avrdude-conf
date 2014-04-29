package net.betaengine.avrdude.rest;

import net.betaengine.avrdude.Maker;
import net.betaengine.avrdude.parser.ConfParser;

// Construction is thread safe (see JCIP 16.2.3 "Safe initialization idioms").
public class MakerSingleton {
    private final static Maker INSTANCE = createMaker();
    
    public static Maker getInstance() { return INSTANCE; }

    private static Maker createMaker() {
        Maker maker = new Maker();
        ConfParser parser = new ConfParser();

        parser.parse(maker);
        
        return maker;
    }
}
