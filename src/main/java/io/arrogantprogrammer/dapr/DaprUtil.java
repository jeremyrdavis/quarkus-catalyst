package io.arrogantprogrammer.dapr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.arrogantprogrammer.DaprRESTClient;
import io.arrogantprogrammer.Order;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import reactor.core.publisher.Mono;
import io.smallrye.mutiny.converters.multi.MultiReactorConverters;
import io.smallrye.mutiny.converters.uni.UniReactorConverters;


@ApplicationScoped
public class DaprUtil {

    private DaprClient daprClient;

    @RestClient
    DaprRESTClient daprRESTClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    @ConfigProperty(name = "dapr.pubsub")
    private String PUBSUB_NAME;
    @ConfigProperty(name = "dapr.kvstore")
    private String KVSTORE_NAME;
    @ConfigProperty(name = "dapr.http-endpoint")
    private String DAPR_HTTP_ENDPOINT;
    @ConfigProperty(name = "dapr.api-token")
    private String DAPR_API_TOKEN;
    @ConfigProperty(name = "dapr.appid")
    private String INVOKE_TARGET_APPID;

    @PostConstruct
    public void init() {
        daprClient = new DaprClientBuilder().build();
        Log.debug("DaprClient initialized");
        Log.debugf("PUBSUB_NAME: %s", PUBSUB_NAME);
        Log.debugf("KVSTORE_NAME: %s", KVSTORE_NAME);
        Log.debugf("DAPR_HTTP_ENDPOINT: %s", DAPR_HTTP_ENDPOINT);
        Log.debugf("DAPR_API_TOKEN: %s", DAPR_API_TOKEN);
        Log.debugf("INVOKE_TARGET_APPID: %s", INVOKE_TARGET_APPID);
    }

    public Uni<Void> publishEvent(String topic, Order order) {
        try {
            daprClient.publishEvent(PUBSUB_NAME, "orders", objectMapper.writeValueAsString(order)).block();
            Log.debugf("Published event to %s: %s", PUBSUB_NAME, order);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Uni<Order> placeOrder(Order order) {
        return Uni.createFrom().item(() -> {
            Order result = daprRESTClient.invokeNewOrders(order).await().indefinitely();
            Log.infof("Order placed: %s", result);
            return result;
        });
    }

    public Uni<Void> saveState(int orderId, Order order) {
        daprClient.saveState(KVSTORE_NAME, Integer.toString(orderId), order).block();
        Log.infof("Saved state: %s", order);
        return null;
    }

    public Uni<Order> getState(int orderId) {
        return Uni.createFrom().item(() -> {
            Order responseOrder = null;
            try {
                Order response = daprClient.getState(KVSTORE_NAME, "" + orderId, Order.class).block().getValue();
                Log.infof("Get KV Successful. Order retrieved: %s", response);
                return response;
            } catch (Exception e) {
                Log.error("Error occurred while retrieving order: " + responseOrder);
                throw new RuntimeException(e);
            }
        });
    }

    public Uni<Void> deleteState(int orderId) {
        return Uni.createFrom().item(() -> {
            daprClient.deleteState(KVSTORE_NAME, "" + orderId).block();
            Log.infof("Deleted state: %s", orderId);
            return null;
        });
    }
}
