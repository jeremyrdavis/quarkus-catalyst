package io.arrogantprogrammer;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface DaprRESTClient {

    @POST
    @Path("/invoke/neworders")
    @Produces(MediaType.APPLICATION_JSON)
    @ClientHeaderParam(name = "dapr-api-token", value = "${darp.api.token}")
    @ClientHeaderParam(name = "dapr-app-id", value = "${dapr.app.id}")
    public Uni<Void> invokeNewOrders(Order order);
}
