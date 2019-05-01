package lt.bta.java2.api.services;

import lt.bta.java2.api.filters.AccessRoles;
import lt.bta.java2.api.filters.Role;
import lt.bta.java2.api.requests.AddCartLineRequest;
import lt.bta.java2.jpa.entities.Cart;
import lt.bta.java2.jpa.entities.CartLine;
import lt.bta.java2.jpa.entities.Product;
import lt.bta.java2.jpa.entities.User;
import lt.bta.java2.jpa.services.Dao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;

/**
 * Cart operacijų servisas
 */
@Path("/cart")
public class CartService extends BaseService<Cart> {

    // reikalaujama autorizacijos
    @Context
    private HttpServletRequest servletRequest;

    @Override
    protected Class<Cart> getEntityClass() {
        return Cart.class;
    }

    // CRUD

    // gauti cart is session, jei session neturi cart - sukurti
//    @AccessRoles({Role.USER, Role.ADMIN})
    @GET
    @Path("/getsessioncart")
    public Response getSessionCart() {

        HttpSession session = servletRequest.getSession();
        Object obj = session.getAttribute("cart");
        Cart cart;
        if (obj instanceof Cart) {
            cart = (Cart) obj;
        } else {
            cart = new Cart();
            cart.setTotal(BigDecimal.ZERO);
            session.setAttribute("cart", cart);
        }
        return Response.ok(cart).build();
    }

    // add cart line in session
    @POST
    @Path("/jamam")
    public Response addCartLine(AddCartLineRequest addCartLineRequest) {

        Dao<Product> productDao = new Dao<>(Product.class);
        Product product = productDao.read(addCartLineRequest.getId());
        if (product == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        HttpSession session = servletRequest.getSession();
        Object obj = session.getAttribute("cart");
        Cart sessionCart;
        if (obj instanceof Cart) {
            sessionCart = (Cart) obj;
        } else {
            sessionCart = new Cart();
            session.setAttribute("cart", sessionCart);
        }

        if (sessionCart.getCartLines() == null) {
            sessionCart.setCartLines(new HashSet<>());
        }

        Optional<CartLine> line = sessionCart.getCartLines().stream()
                .filter(x -> x.getProduct().getId() == addCartLineRequest.getId())
                .findFirst();

        if (line.isPresent()) {
            CartLine cartLine = line.get();
            cartLine.setQty(cartLine.getQty() + addCartLineRequest.getQty());
        } else {
            CartLine cartLine = new CartLine();
            cartLine.setProduct(product);
            cartLine.setQty(addCartLineRequest.getQty());
            sessionCart.getCartLines().add(cartLine);
        }

        keepUserCartInDatabase();
        return Response.ok(sessionCart).build();
    }

    // update cart line in session
//    @AccessRoles({Role.USER})
    @PUT
    @Path("/updateCartLine/{id}/{qty}")
    public Response updateCartLine(@PathParam("id") int id, @PathParam("qty") int qty) {

        HttpSession session = servletRequest.getSession();

        Object obj = session.getAttribute("cart");
        Cart cart;
        if (obj instanceof Cart) {
            cart = (Cart) obj;
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        for (CartLine cartLine : cart.getCartLines()) {

            if (cartLine.getProduct().getId() == id) {
                cartLine.setQty(qty);
                break;
            }
        }

        keepUserCartInDatabase();

        return Response.ok(cart).build();
    }

    // delete cart line in session
//    @AccessRoles({Role.USER})
    @DELETE
    @Path("/deleteCartLine/{id}")
    public Response deleteCart(@PathParam("id") int id) {

        HttpSession session = servletRequest.getSession();

        Object obj = session.getAttribute("cart");
        Cart cart;
        if (obj instanceof Cart) {
            cart = (Cart) obj;
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // surasti preke pagal id ir istrinti
        for (CartLine cartLine : cart.getCartLines()) {

            if (cartLine.getProduct().getId() == id) {

                cart.getCartLines().remove(cartLine);
                break;
            }
        }

        keepUserCartInDatabase();
        return Response.ok(cart).build();
    }

//    @AccessRoles({Role.USER})
    @PUT
    @Path("/synchronize")
    public Response synchronizeCarts() {

        HttpSession session = servletRequest.getSession();

        Object obj = session.getAttribute("cart");
        Cart sessionCart;
        if (obj instanceof Cart) {
            sessionCart = (Cart) obj;
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try (Dao<Cart> cartDao = createDao()) {
            Cart userCart = cartDao.read(user.getId(), Cart.GRAPH_CART_LINES);

            if (userCart != null) {

                for (CartLine sessionCartLine : sessionCart.getCartLines()) {

                    userCart.setQtyIfHasProductOrAddItemIfProductIsNew(sessionCartLine);
                }
            }

            cartDao.update(userCart);
            session.setAttribute("cart", userCart);
        }

        keepUserCartInDatabase();
        return Response.ok(sessionCart).build();
    }

//    @AccessRoles({Role.USER})
    @PUT
    @Path("/keepusercart")
    public Response keepUserCart() {
        return keepUserCartInDatabase();
    }

    // krepselio saugojimas DB
    private Response keepUserCartInDatabase() {

        HttpSession session = servletRequest.getSession();

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Object obj = session.getAttribute("cart");
        Cart sessionCart;
        if (obj instanceof Cart) {
            sessionCart = (Cart) obj;
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try (Dao<Cart> cartDao = createDao()) {

            final Cart userCart = cartDao.read(user.getId(), Cart.GRAPH_CART_LINES);
            if (userCart.getCartLines() == null) {
                userCart.setCartLines(new HashSet<>());
            } else {
                userCart.getCartLines().clear();
            }

            userCart.getCartLines().addAll(sessionCart.getCartLines());
            userCart.getCartLines().forEach(x -> x.setCart(userCart));
            userCart.setTotal(sessionCart.getTotal());
            cartDao.update(userCart);
            return Response.ok(userCart).build();
        }
    }
}
