package lt.bta.java2.api.services;

import lt.bta.java2.jpa.entities.Product;

import javax.ws.rs.Path;

/**
 * Product operacijų servisas
 */
@Path("/product")
public class ProductService extends BaseService<Product> {

    @Override
    protected Class<Product> getEntityClass() {
        return Product.class;
    }

}
