import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;
import model.User;
import model.Product;
import controller.ShopController;
import view.SimpleHttpView;

public class Server {

    private static final int PORT = 8080;
    private ShopController shop = new ShopController();
    private Map<String, User> users = new HashMap<>();
    private Map<String, User> sessions = new HashMap<>();

    private final String USERS_FILE = "users.txt";
    private final String CARTS_FILE = "carts.txt";

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        loadUsers();
        loadCarts();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        File usersFile = new File(USERS_FILE);
        if (!usersFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(usersFile), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0], new User(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCarts() {
        File cartsFile = new File(CARTS_FILE);
        if (!cartsFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(cartsFile), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    User user = users.get(parts[0]);
                    if (user != null && !parts[1].isEmpty()) {
                        String[] productIds = parts[1].split(",");
                        for (String idStr : productIds) {
                            try {
                                int id = Integer.parseInt(idStr);
                                Product p = shop.getProductById(id);
                                if (p != null) user.addToCart(p);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(USERS_FILE), "UTF-8"))) {
            for (User u : users.values()) {
                writer.write(u.getUsername() + ":" + u.getPassword());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCarts() {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(CARTS_FILE), "UTF-8"))) {
            for (User u : users.values()) {
                String cartStr = u.getCart().stream()
                                  .map(p -> String.valueOf(p.getId()))
                                  .collect(Collectors.joining(","));
                writer.write(u.getUsername() + ":" + cartStr);
                writer.newLine();
            }
            System.out.println("Carts saved!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))
        ) {
            String line = reader.readLine();
            if (line == null || line.isEmpty()) return;

            System.out.println("Request: " + line);

            String[] requestParts = line.split(" ");
            String method = requestParts[0];
            String path = requestParts[1];

            Map<String, String> postParams = new HashMap<>();
            String sessionId = null;

            if (method.equals("POST")) {
                int contentLength = 0;
                String header;
                while (!(header = reader.readLine()).isEmpty()) {
                    if (header.startsWith("Content-Length:")) {
                        contentLength = Integer.parseInt(header.split(":")[1].trim());
                    } else if (header.startsWith("Cookie:")) {
                        String[] cookies = header.substring(7).split(";");
                        for (String cookie : cookies) {
                            String[] kv = cookie.trim().split("=");
                            if (kv.length == 2 && kv[0].equals("SESSIONID")) {
                                sessionId = kv[1];
                            }
                        }
                    }
                }

                char[] body = new char[contentLength];
                reader.read(body);
                String[] pairs = new String(body).split("&");
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2)
                        postParams.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
                }
            } else {
                while (!(line = reader.readLine()).isEmpty()) {
                    if (line.startsWith("Cookie:")) {
                        String[] cookies = line.substring(7).split(";");
                        for (String cookie : cookies) {
                            String[] kv = cookie.trim().split("=");
                            if (kv.length == 2 && kv[0].equals("SESSIONID")) {
                                sessionId = kv[1];
                            }
                        }
                    }
                }
            }

            User currentUser = sessionId != null ? sessions.get(sessionId) : null;

            if (path.equals("/")) {
                sendPage(out, SimpleHttpView.renderProductList(shop.getProducts()), sessionId);
            } else if (path.equals("/register")) {
                if (method.equals("GET")) {
                    sendPage(out, SimpleHttpView.renderRegisterForm(), sessionId);
                } else {
                    String username = postParams.get("username");
                    String password = postParams.get("password");
                    if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                        sendPage(out, SimpleHttpView.renderMessage("Ошибка: заполните все поля"), sessionId);
                    } else if (users.containsKey(username)) {
                        sendPage(out, SimpleHttpView.renderMessage("Пользователь уже существует"), sessionId);
                    } else {
                        User user = new User(username, password);
                        users.put(username, user);
                        saveUsers();
                        saveCarts();
                        sendPage(out, SimpleHttpView.renderMessage("Регистрация успешна"), sessionId);
                    }
                }
            } else if (path.equals("/login")) {
                if (method.equals("GET")) {
                    sendPage(out, SimpleHttpView.renderLoginForm(), sessionId);
                } else {
                    String username = postParams.get("username");
                    String password = postParams.get("password");
                    User user = users.get(username);
                    if (user != null && user.getPassword().equals(password)) {
                        String newSessionId = UUID.randomUUID().toString();
                        sessions.put(newSessionId, user);
                        sendPage(out, SimpleHttpView.renderMessage("Вход успешен!"), newSessionId);
                    } else {
                        sendPage(out, SimpleHttpView.renderMessage("Неверный логин или пароль"), sessionId);
                    }
                }
            } else if (path.equals("/cart")) {
                if (currentUser != null) {
                    sendPage(out, SimpleHttpView.renderCart(currentUser), sessionId);
                } else {
                    sendPage(out, SimpleHttpView.renderMessage("Сначала войдите"), sessionId);
                }
            } else if (path.equals("/add") && method.equals("POST")) {
                if (currentUser != null) {
                    String idStr = postParams.get("id");
                    if (idStr != null) {
                        int id = Integer.parseInt(idStr);
                        Product p = shop.getProductById(id);
                        if (p != null) currentUser.addToCart(p);
                    }
                    saveCarts(); 
                    sendPage(out, SimpleHttpView.renderCart(currentUser), sessionId);
                } else {
                    sendPage(out, SimpleHttpView.renderMessage("Сначала войдите"), sessionId);
                }
            } else {
                sendPage(out, SimpleHttpView.renderMessage("Страница не найдена"), sessionId);
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPage(OutputStream out, String content, String sessionId) {
        try {
            byte[] bytes = content.getBytes("UTF-8");
            out.write(("HTTP/1.1 200 OK\r\n").getBytes("UTF-8"));
            out.write(("Content-Type: text/html; charset=UTF-8\r\n").getBytes("UTF-8"));
            if (sessionId != null) {
                out.write(("Set-Cookie: SESSIONID=" + sessionId + "\r\n").getBytes("UTF-8"));
            }
            out.write(("Content-Length: " + bytes.length + "\r\n").getBytes("UTF-8"));
            out.write(("\r\n").getBytes("UTF-8"));
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
