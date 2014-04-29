package net.betaengine.avrdude.rest;

import java.io.IOException;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.CharSource;

public class AvrdudeConfResourceTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(CustomObjectMapperProvider.class,
                AvrdudeConfJsonResource.class, AvrdudeConfHtmlResource.class);
    }

    @Test
    public void testGetPartIds() {
        String response = target()
                .path("/conf/parts/ids").request().get(String.class);

        Assert.assertTrue(response.contains("m328"));
    }

    @Test
    public void testGetATmega328Json() {
        String response = target()
                .path("/conf/parts/ids/m328")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(String.class);
        String signature = getSignature(response);

        // JSON response contains decimal values while the HTML one contains hex.
        Assert.assertTrue(signature.contains("[ 30, 149, 20 ]"));
    }

    @Test
    public void testGetATmega328Html() {
        String response = target()
                .path("/conf/parts/ids/m328")
                .request()
                .accept(MediaType.TEXT_HTML)
                .get(String.class);
        String signature = getSignature(response);

        // JSON response contains decimal values while the HTML one contains hex.
        Assert.assertTrue(signature.contains("[ 0x1e, 0x95, 0x14 ]"));
    }
    
    private String getSignature(String content) {
        try {
            for (String line : CharSource.wrap(content).readLines()) {
                if (line.contains("signature")) {
                    return line;
                }
            }

            throw new AssertionError("could not find signature");
        } catch (IOException e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
