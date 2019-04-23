package lt.bta.java2.api.filters;

import io.jsonwebtoken.Claims;
import lt.bta.java2.api.helpers.JWTHelper;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Autorizacijos
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final String AUTHENTICATION_SCHEME = "Bearer";

    // sukūrus objektą @Context žiūri kokiame kontekste buvo sukurtas ojketas ir automatiškai priskiria reikšmę atsižvelgdamas į contekstą
    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        Method method = resourceInfo.getResourceMethod();

        if (!method.isAnnotationPresent(AccessRoles.class)) return;

        // Get the Authorization header from the request
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Validate the Authorization header
        if (!isTokenBasedAuthentication(authorizationHeader)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        // Extract the token from the Authorization header
        String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

        // Check if token is valid?
        Claims claims;
        try {
            claims = JWTHelper.decodeJWT(token);
        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Invalid token").build());
            return;
        }

        //Verify user access:

        // 1) get user role from token
        String userRole = claims.get("role", String.class);

        // 2) get roles from annotation
        AccessRoles accessRoles = method.getAnnotation (AccessRoles.class);
        Role[] roles = accessRoles.value();

        // 3) check access, i.e. is user role in roles list
        if (Arrays.stream(roles).map(Enum::name).noneMatch(x -> x.equalsIgnoreCase(userRole))) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Insufficient privileges").build());
        }
    }

    // Check if the Authorization header is valid
    // It must not be null and must be prefixed with "Bearer" plus a whitespace
    // The authentication scheme comparison must be case-insensitive
    private boolean isTokenBasedAuthentication(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.toLowerCase()
                .startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
    }

}