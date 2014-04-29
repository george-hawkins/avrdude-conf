package net.betaengine.avrdude.rest;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

public class AvrdudeConfResourceTest extends JerseyTest {
    @Override
    protected Application configure() {
        return new ResourceConfig(AvrdudeConfResource.class);
    }

    @Test
    public void testGetIt() {
        String response = target()
                .path("/conf/parts/ids").request().get(String.class);

        Assert.assertTrue(response.contains("m328"));
    }
}
