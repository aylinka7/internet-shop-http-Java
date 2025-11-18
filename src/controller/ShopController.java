import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ShopController {
    private Map<Integer, Product> products = new HashMap<>();

    public ShopController() {
        products.put(1, new Product(1, "Nike Air", 100, "/img/nike_air.jpeg"));
        products.put(2, new Product(2, "Adidas Superstar", 90, "/img/adidas_superstar.jpeg"));
        products.put(3, new Product(3, "Puma Classic", 80, "/img/puma_suede.jpeg"));
        products.put(4, new Product(1, "Nike Max", 100, "/img/nike_max.jpeg"));
        products.put(5, new Product(2, "Adidas Samba", 90, "/img/adidas_samba.jpeg"));
        products.put(6, new Product(3, "Puma Suede", 80, "/img/puma_suede.jpeg"));
    }

    public Collection<Product> getProducts() {
        return products.values();
    }

    public Product getProductById(int id) {
        return products.get(id);
    }
}
