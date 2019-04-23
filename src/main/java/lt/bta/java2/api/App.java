package lt.bta.java2.api;

import lt.bta.java2.api.filters.AuthenticationFilter;
import lt.bta.java2.api.services.*;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import javax.ws.rs.ApplicationPath;

/**
 * komponentu konfiguravimas - servisu uzregistravimas
 */

@ApplicationPath("/api")
public class App extends ResourceConfig {

    public App() {
        register(ObjectMapperContextResolver.class);
        register(ProductService.class);
        register(CartService.class);
        register(MarService.class);
        register(UserService.class);
        register(AuthenticationFilter.class);
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
    }
}
