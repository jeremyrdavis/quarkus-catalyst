package io.arrogantprogrammer;

import io.arrogantprogrammer.dapr.DaprUtil;
import io.dapr.client.domain.CloudEvent;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/catalyst")
public class CatalystResource {

    @Inject
    DaprUtil dapr;

    // Publish messages
    @POST
    @Path("/pubsub/orders")
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    public Response publish(Order order) {
        Log.infof("Publishing Order: " + order.getOrderId());
        dapr.publishEvent("orders", order).subscribe().with(
                v -> Log.infof("Order published: " + order.getOrderId()),
                e -> Log.errorf("Error publishing order: %s", e.getMessage())
        );
        Log.info("returning response");
        return Response.ok().entity("SUCCESS").build();
    }

    // Subscribe to messages
    @POST
    @Path("/pubsub/neworders")
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    public Uni<Response> subscribe(CloudEvent<Order> cloudEvent) {

        return Uni.createFrom().item(() -> {
            try {
                int orderId = cloudEvent.getData().getOrderId();
                Log.infof("Order received: " + orderId);
                return Response.ok().entity("SUCCESS").build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @POST
    @Path("/invoke/orders")
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    public Uni<Response> request(Order order) {
        return dapr.placeOrder(order).onItem().transform(o -> {
            Log.infof("Order placed: " + o.getOrderId());
            return Response.ok(o).build();
        });
    }

    // Service to be invoked
    @POST
    @Path("/invoke/neworders")
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    public Response reply(Order order) {
        Log.infof("Request received : " + order.getOrderId());
        return Response.ok().entity(order).build();
    }

    // Save state
    @POST
    @Path("/kv/orders")
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    public Uni<Response> saveKV(Order order) {

        return dapr.saveState(order.getOrderId(), order).onItem().transform(v -> {
            Log.infof("Save KV Successful. Order saved: " + order.getOrderId());
            return Response.ok("SUCCESS").build();
        });
    }

    // Retrieve state
    @GET
    @Path("/kv/orders/{orderId}")
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    public Uni<Response> getKV(final int orderId) {
        Log.infof("Retrieving KV: " + orderId);
        return dapr.getState(orderId).onItem().transform(o -> {
            Log.infof("Get KV Successful. Order retrieved: " + o.getOrderId());
            return Response.ok(o).build();
        });
    }

    @DELETE
    @Path("/kv/orders/{orderId}")
    @Consumes(MediaType.MEDIA_TYPE_WILDCARD)
    public Response deleteKV(int orderId){
        Log.infof("Deleting KV: " + orderId);
        dapr.deleteState(orderId);
        Log.infof("Delete KV Successful. Order deleted: " + orderId);
        return Response.ok().build();
    }



}
