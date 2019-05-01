package lt.bta.java2.jpa.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Entity(name = "orders")
@NamedEntityGraph(
        name = Order.GRAPH_ORDER_LINES,
        attributeNodes =
                { // todo ?? NamedEntityGraph
                        @NamedAttributeNode(value = "user"),
                        @NamedAttributeNode(value = "orderLines")
                }
)
public class Order {

    public static final String GRAPH_ORDER_LINES = "graph.order.lines";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private BigDecimal total;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderLine> orderLines;

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", data=" + date +
                ", user=" + user +
                ", total=" + total +
                ", orderLines=" + orderLines +
                '}';
    }

    public static String getGraphOrderLines() {
        return GRAPH_ORDER_LINES;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Set<OrderLine> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(Set<OrderLine> orderLines) {
        this.orderLines = orderLines;
    }
}
