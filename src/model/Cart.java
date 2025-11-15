import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<Product> items = new ArrayList<>();

    public void add(Product p) { items.add(p); }
    public void remove(Product p) { items.remove(p); }
    public List<Product> getItems() { return items; }
    public double getTotal() {
        return items.stream().mapToDouble(Product::getPrice).sum();
    }
}
