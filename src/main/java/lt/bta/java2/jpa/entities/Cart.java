package lt.bta.java2.jpa.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

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

    private UUID uuid;

    @Column(name = "total")
    private BigDecimal totalSum;


    // tikrina ar krepselyje yra preke
    public boolean hasProduct(CartLine item) {

        for (CartLine cartLine : this.cartLines)
            if (cartLine.getProduct().getId() == item.getProduct().getId()) return true;

        return false;
    }

    public void sumQtyIfHasProductOrAddItemIfProductIsNew(CartLine item) {

        for (CartLine cartLine : this.cartLines) {

            if (cartLine.getProduct().getId() == item.getProduct().getId()) {
                cartLine.setQty(cartLine.getQty() + item.getQty());
                return;
            }
        }
        this.cartLines.add(item);
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id=" + id +
                ", sum=" + totalSum +
                ", cartLines=" + cartLines +
//                ", user=" + user +
//                ", uuid=" + uuid +
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

    public BigDecimal getTotalSum() {

        BigDecimal cartSum = BigDecimal.ZERO;

        for (CartLine cartLine : cartLines) {

            if (cartLine.getProduct().getPrice() != null) {
                cartSum = cartSum.add(BigDecimal.valueOf(cartLine.getQty()).multiply(cartLine.getProduct().getPrice()));
            }
        }
        return cartSum;
    }

    public void setTotalSum(BigDecimal sum) {
        this.totalSum = sum;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
