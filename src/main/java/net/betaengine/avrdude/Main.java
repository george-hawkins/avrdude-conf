package net.betaengine.avrdude;

import java.io.IOException;

import net.betaengine.avrdude.rest.AvrdudeConfResource;
import net.betaengine.avrdude.rest.HexObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;


public class Main {
    public static void main(String[] args) {
        boolean names = args.length == 1 && args[0].equals("--names");
        
        if (args.length != 0 && !names) {
            System.err.println("Usage: java " + Main.class.getName() + " [--names]");
            System.exit(1);
        }
        
        ObjectMapper mapper = HexObjectMapperFactory.createObjectMapper();
        AvrdudeConfResource resource = new AvrdudeConfResource();

        try {
            mapper.writeValue(System.out,
                    names ? resource.getAllIds() : resource.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}