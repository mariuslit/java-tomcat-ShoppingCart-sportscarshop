package lt.bta.java2.jpa.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Pekiu krepselio objetas atitinkantis DB lentele
 */
@Entity(name = "carts")
@NamedEntityGraph(
        name = Cart.GRAPH_CART_LINES,
        attributeNodes = @NamedAttributeNode(value = "cartLines"))
public class Cart {

    public static final String GRAPH_CART_LINES = "graph.cart.lines";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartLine> cartLines = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private BigDecimal total;


    public void setQtyIfHasProductOrAddItemIfProductIsNew(CartLine sessionCartLine ) {

        for (CartLine userCartLine : this.cartLines) {

            if (userCartLine.getProduct().getId() == sessionCartLine.getProduct().getId()) {
                userCartLine.setQty(sessionCartLine.getQty());
                return;
            }
        }
        CartLine cartLine = new CartLine();
        cartLine.setCart(this);
        cartLine.setProduct(sessionCartLine.getProduct());
        cartLine.setQty(sessionCartLine.getQty());
        this.cartLines.add(cartLine);
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", total=" + total +
                ", cartLines=" + cartLines +
//                ", user=" + user +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<CartLine> getCartLines() {
        return cartLines;
    }

    public void setCartLines(Set<CartLine> cartLines) {
        this.cartLines = cartLines;
    }

    public BigDecimal getTotal() {

        BigDecimal cartSum = BigDecimal.ZERO;

        for (CartLine cartLine : cartLines) {

            if (cartLine.getProduct().getPrice() != null) {
                cartSum = cartSum.add(BigDecimal.valueOf(cartLine.getQty()).multiply(cartLine.getProduct().getPrice()));
            }
        }
        return cartSum;
    }

    public void setTotal(BigDecimal sum) {
        this.total = sum;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
