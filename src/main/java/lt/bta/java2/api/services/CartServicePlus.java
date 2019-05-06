package lt.bta.java2.api.services;

import lt.bta.java2.api.requests.AddCartLineRequest;
import lt.bta.java2.jpa.entities.Cart;
import lt.bta.java2.jpa.entities.CartLine;
import lt.bta.java2.jpa.entities.Product;
import lt.bta.java2.jpa.services.Dao;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Optional;

/**
 * Cart operacijų servisas
 * cia laikomas kodo balastas kuris gali buti reikaliingas tolesniam cat vystymui
 */
@Path("/cart-balast")
public class CartServicePlus extends BaseService<Cart> {

    // reikalaujama autorizacijos
    @Context
    private HttpServletRequest servletRequest;

    @Override
    protected Class<Cart> getEntityClass() {
        return Cart.class;
    }

    // CRUD

    // CREATE veikia be priekaistu
    @POST
    @Override
    public Response add(Cart cart) {

        try (Dao<Cart> dao = createDao()) {

            dao.create(cart);
            return Response.ok(cart).build();
        }
    }

    // CREATE cartLine by cart id

    // add cart line in DB
    @POST
    @Path("/{id}")
    public Response addCartLine(@PathParam("id") int cartId, AddCartLineRequest addCartLineRequest) {

        int productId = addCartLineRequest.getId();
        int qty = addCartLineRequest.getQty();

        try (Dao<Cart> cartDao = createDao()) {

            // 1
            Cart cart = cartDao.read(cartId);
            if (cart == null)
                // jeigu cart=null sukurti nauja
                return Response.status(Response.Status.NOT_FOUND).build();

            // 2
            Dao<Product> productDao = new Dao<>(Product.class);
            Product product = productDao.read(productId);
            if (product == null)
                return Response.status(Response.Status.NOT_FOUND).build();

            // 3 patikrinti ar cart.crtLine turi tokia preke
            boolean isCartLine = false;
            for (CartLine cartLine : cart.getCartLines()) {

                // jei turi pridėti qty
                if (cartLine.getProduct().getId() == product.getId()) {

                    cartLine.setQty(cartLine.getQty() + qty);
                    isCartLine = true;
                    break;
                }
            }

            // jei neturi sukurti nauja cartLine ir paduoti i cart
            if (!isCartLine) {

                CartLine cartLine = new CartLine();
                cartLine.setCart(cart);
                cartLine.setQty(qty);
                cartLine.setProduct(product);
                cart.getCartLines().add(cartLine);
            }

            cart = cartDao.update(cart);
            return Response.ok(cart).build();
        }
    }

    @GET // READ_LIST
    @Path("/list")
    public Response list(@QueryParam("size") @DefaultValue("10") int size, @QueryParam("skip") @DefaultValue("0") int skip) {

        try (Dao<Cart> dao = createDao()) {

            return Response.ok().entity(dao.list(size, skip)).build();
        }
    }

    // gauti full list
    @GET
    @Path("/{id}/f")
    public Response getFull(@PathParam("id") int id) {

        HttpSession session = servletRequest.getSession();
        if (session.getAttribute("user") == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try (Dao<Cart> dao = createDao()) {
            Cart entity = dao.read(id, Cart.GRAPH_CART_LINES);

            if (entity == null)
                return Response.status(Response.Status.NOT_FOUND).build();

            return Response.ok(entity).build();
        }
    }
}
