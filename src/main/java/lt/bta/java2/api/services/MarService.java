package lt.bta.java2.api.services;

import lt.bta.java2.api.filters.AccessRoles;
import lt.bta.java2.api.filters.Role;
import lt.bta.java2.jpa.entities.Cart;
import lt.bta.java2.jpa.entities.CartLine;
import lt.bta.java2.jpa.entities.User;
import lt.bta.java2.jpa.services.Dao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Cart operacijų servisas (su autorizacijomis)
 */
@Path("/mar")
public class MarService extends BaseService<Cart> {

    // reikalaujama autorizacijos
    @Context
    private HttpServletRequest servletRequest;

    @Override
    protected Class<Cart> getEntityClass() {
        return Cart.class;
    }

    @AccessRoles({Role.USER, Role.ADMIN})
    @PUT
    @Path("/cart-cart")
    public Response sudetiDuKepselius() {

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

            if (userCart != null)
                for (CartLine item : userCart.getCartLines())
                    sessionCart.sumQtyIfHasProductOrAddItemIfProductIsNew(item);

        }

        session.setAttribute("cart", sessionCart);
        return Response.ok(sessionCart).build();
    }

    // todo
    @AccessRoles({Role.USER, Role.ADMIN})
    @PUT
    @Path("/keepusercart")
    public Response keepUserCartInDatabase() {

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
            userCart.setCartLines(sessionCart.getCartLines());
            userCart.setTotalSum(sessionCart.getTotalSum());
            userCart = cartDao.update(userCart);

            return Response.ok(userCart).build();
        }
    }
}
