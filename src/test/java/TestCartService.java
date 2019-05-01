import lt.bta.java2.api.services.CartService;
import org.junit.Assert;
import org.junit.Test;
import javax.ws.rs.core.Response;

public class TestCartService {

    @Test
    public void testResponseStatus() {

        CartService cartService = new CartService();
        // Arrange
        final Response.Status expected = Response.Status.OK;

        // Act
        final Response.Status actual = Response.Status.OK;

        // Assert
//        Assert.assertEquals(cartService.getSessionCart(), Response.Status.OK);
    }
}
