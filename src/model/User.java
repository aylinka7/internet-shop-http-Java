import java.util.List;

public class User {
    private String username;
    private String password;
    private Cart cart = new Cart();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Cart getCart() { return cart; }
}
