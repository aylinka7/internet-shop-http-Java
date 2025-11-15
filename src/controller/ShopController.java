import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShopController {
    private Map<Integer, Product> products = new HashMap<>();

    public ShopController() {
        products.put(1, new Product(1, "Nike Air", 100));
        products.put(2, new Product(2, "Adidas Superstar", 90));
        products.put(3, new Product(3, "Puma Classic", 80));
    }

    public Collection<Product> getProducts() {
        return products.values();
    }

    public Product getProductById(int id) {
        return products.get(id);
    }
}
