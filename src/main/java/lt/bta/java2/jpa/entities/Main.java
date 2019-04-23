package lt.bta.java2.jpa.entities;

import lt.bta.java2.jpa.services.Dao;

/**
 * greitam testavimui skirta klase
 */
public class Main {

    public static void main(String[] args) {

        // tikslas - įdėti naują prekę į krepšelį
        int cartId = 1;
        int productId = 1;
        int qty = 1;

        System.out.println(addCartLine(cartId, productId, qty));
    }

    public static String addCartLine(int cartId, int productId, int qty) {

//        int productId = cartLineRequestQty.getId();
//        int qty = cartLineRequestQty.getQty();

        // 1
        Dao<Cart> cartDao = new Dao<>(Cart.class);
        Cart cart = cartDao.read(cartId);
        if (cart != null) {
            System.out.println(cart);
        } else {
            return "cart nerasta";
        }

        // 2
        Dao<Product> productDao = new Dao<>(Product.class);
        Product product = productDao.read(productId);
        if (product != null) {
            System.out.println(product);
        } else {
            return "product nerasta";
        }

        // 3 patikrinti ar cart.crtLine turi tokia preke
        boolean isCartLine = false;

        System.out.println("cartLines size=" + cart.getCartLines().size());

        for (CartLine cartLine : cart.getCartLines()) {

            // jei turi pridėti qty
            if (cartLine.getId() == product.getId()) {

                cartLine.setQty(cartLine.getQty() + qty);
                isCartLine = true;
                break;
            }
        }

        // jei neturi sukurti nauja cartLine ir paduoti i cart
        if (!isCartLine) {

            CartLine cartLine = new CartLine();
            cartLine.setCart(cart);
            cartLine.setProduct(product);
            cartLine.setQty(qty);
            cart.getCartLines().add(cartLine);
        }

        cart = cartDao.update(cart);

        return cart.toString();
    }
}
