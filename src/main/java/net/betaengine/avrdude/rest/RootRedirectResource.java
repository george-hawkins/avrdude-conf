package net.betaengine.avrdude.rest;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Singleton
@Path("/")
public class RootRedirectResource {
    // Avoid a scary 404 for users trying the base URL with no path component.
    @GET
    public Response redirect() {
        try {
            // An absolute URI with the correct base will be constructed
            // by Jersey from the one supplied here.
            return Response.temporaryRedirect(new URI("/conf")).build();
        } catch (URISyntaxException e) {
            throw new ResourceException(e);
        }
    }
}
