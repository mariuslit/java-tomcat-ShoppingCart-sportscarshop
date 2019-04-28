package lt.bta.java2.api.services;

import lt.bta.java2.api.helpers.JWTHelper;
import lt.bta.java2.api.requests.CredentialsRequest;
import lt.bta.java2.jpa.entities.Cart;
import lt.bta.java2.jpa.entities.User;
import lt.bta.java2.jpa.services.Dao;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.util.*;

/**
 * User operacijų servisas
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/auth")
public class UserService {

    @Context
    private HttpServletRequest servletRequest;

    @GET
    @Path("/genkey/{bits}")
    public Response genkey(@PathParam("bits") int bits) {
        if (bits % 8 != 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Must be full bytes length, i.e. N x 8, example: 256 or 384 or 512").build();
        }

        Random random = new Random();
        byte[] bytes = new byte[bits / 8];
        random.nextBytes(bytes);
        return Response.ok().entity(DatatypeConverter.printBase64Binary(bytes)).build();
    }

    @POST
    @Path("/create")
    public Response create(CredentialsRequest userRequest) {
        Dao<User> userDao = new Dao<>(User.class);
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setSecret(BCrypt.hashpw(userRequest.getPassword(), BCrypt.gensalt()));
        user.setRole(userRequest.getRole());
        HttpSession session = servletRequest.getSession();
        session.setAttribute("user", user);
        userDao.create(user);
        return Response.ok().build();
    }

    @POST
    @Path("/newuser")
    public Response newUser(CredentialsRequest userRequest) {

        // todo ?? kaip logiškai išskaidyti šiuos du veiksmus
        //      ar kurti dvi atskiras užklausas iš front-end
        //      ar palikti kaip čia
        // new user
        Dao<User> userDao = new Dao<>(User.class);
        List<User> users = userDao.findBy("username", userRequest.getUsername());
        // validacija username
        if (users.size() > 0) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setSecret(BCrypt.hashpw(userRequest.getPassword(), BCrypt.gensalt()));
        user.setRole("user");
        HttpSession session = servletRequest.getSession();
        session.setAttribute("user", user);
        userDao.create(user);

        // nwe cart in DB for new user
        Dao<Cart> cartDao = new Dao<>(Cart.class);
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCartLines(new HashSet<>());
        cart.setTotal(BigDecimal.ZERO);
        cartDao.create(cart);

        String token = JWTHelper.createJWT("my-app",
                user.getId(), user.getUsername(), user.getRole(), 1000L * 60 * 5);//todo token

        return Response.ok(Collections.singletonMap("login", "ok")).entity(Collections.singletonMap("token", token)).build();
    }

    @POST
    @Path("/login")
    public Response login(CredentialsRequest userRequest) {

        Dao<User> userDao = new Dao<>(User.class);
        List<User> users = userDao.findBy("username", userRequest.getUsername());

        // validacija username
        if (users == null || users.size() != 1) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (!BCrypt.checkpw(userRequest.getPassword(), users.get(0).getSecret())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        HttpSession session = servletRequest.getSession();
        session.setAttribute("user", users.get(0));

        User user = users.get(0);
        String token = JWTHelper.createJWT("my-app",
                user.getId(), user.getUsername(), user.getRole(), 1000L * 60 * 60);

//        return Response.ok(Collections.singletonMap("login", "ok")).build();
        return Response.ok(Collections.singletonMap("login", "ok")).entity(Collections.singletonMap("token", token)).build();
    }

    @POST
    @Path("/logout")
    public Response logout() {

        HttpSession session = servletRequest.getSession(false);
        if (session != null) {
            session.removeAttribute("user");
            session.removeAttribute("cart");

            Cart cart = new Cart();
            session.setAttribute("cart", cart);
        }

        return Response.ok().build();
    }
}

