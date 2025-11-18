import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Server {
    private static final int PORT = 8080;
    private final ShopController shop = new ShopController();
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, User> sessions = new HashMap<>();
    private final String USERS_FILE = "users.txt";
    private final String CARTS_FILE = "carts.txt";

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        loadUsers();
        loadCarts();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on  http://localhost:" + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(":", 2);
                if (p.length == 2) {
                    users.put(p[0], new User(p[0], p[1]));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void saveUsers() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(USERS_FILE), StandardCharsets.UTF_8))) {
            for (User u : users.values()) {
                bw.write(u.getUsername() + ":" + u.getPassword());
                bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadCarts() {
        File file = new File(CARTS_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(":", 2);
                if (p.length != 2) continue;
                User user = users.get(p[0]);
                if (user == null || p[1].isEmpty()) continue;
                for (String idStr : p[1].split(",")) {
                    try {
                        int id = Integer.parseInt(idStr.trim());
                        Product prod = shop.getProductById(id);
                        if (prod != null) user.getCart().add(prod);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void saveCarts() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(CARTS_FILE), StandardCharsets.UTF_8))) {
            for (User u : users.values()) {
                String ids = u.getCart().getItems().stream()
                        .map(p -> String.valueOf(p.getId()))
                        .collect(Collectors.joining(","));
                bw.write(u.getUsername() + ":" + ids);
                bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleClient(Socket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             OutputStream out = socket.getOutputStream()) {

            String requestLine = in.readLine();
            if (requestLine == null) return;
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) return;
            String method = parts[0];
            String path = parts[1];

            Map<String, String> headers = new HashMap<>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                int colon = line.indexOf(':');
                if (colon > 0) {
                    headers.put(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
                }
            }

            String sessionId = null;
            String cookieHeader = headers.get("Cookie");
            if (cookieHeader != null) {
                for (String c : cookieHeader.split(";")) {
                    String[] kv = c.trim().split("=", 2);
                    if (kv.length == 2 && "SESSIONID".equals(kv[0])) sessionId = kv[1];
                }
            }
            User currentUser = sessionId != null ? sessions.get(sessionId) : null;

            Map<String, String> postParams = new HashMap<>();
            if ("POST".equals(method)) {
                String lenStr = headers.get("Content-Length");
                if (lenStr != null) {
                    int len = Integer.parseInt(lenStr);
                    if (len > 0) {
                        char[] buf = new char[len];
                        in.read(buf, 0, len);
                        String body = new String(buf);
                        for (String pair : body.split("&")) {
                            String[] kv = pair.split("=", 2);
                            if (kv.length == 2) {
                                postParams.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                                               URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                            }
                        }
                    }
                }
            }

            if (path.startsWith("/img/")) {
                serveStaticFile(path, out);
                return;
            }

            if (path.equals("/") || path.equals("/index.html")) {
                sendPage(out, SimpleHttpView.renderProductList(shop.getProducts(), currentUser), sessionId);

            } else if (path.equals("/cart")) {
                if (currentUser != null) sendPage(out, SimpleHttpView.renderCart(currentUser), sessionId);
                else sendPage(out, SimpleHttpView.renderMessage("Сначала войдите в систему"), sessionId);

            } else if (path.equals("/add") && "POST".equals(method) && currentUser != null) {
                String idStr = postParams.get("id");
                if (idStr != null) {
                    int id = Integer.parseInt(idStr);
                    Product p = shop.getProductById(id);
                    if (p != null) {
                        currentUser.getCart().add(p);
                        saveCarts();
                    }
                }
                redirect(out, "/cart");

            } else if (path.equals("/remove") && "POST".equals(method) && currentUser != null) {
                String idStr = postParams.get("id");
                if (idStr != null) {
                    int id = Integer.parseInt(idStr);
                    Product p = shop.getProductById(id);
                    if (p != null) {
                        currentUser.getCart().remove(p);
                        saveCarts();
                    }
                }
                redirect(out, "/cart");

            } else if (path.equals("/login")) {
                if ("GET".equals(method)) sendPage(out, SimpleHttpView.renderLoginForm(), sessionId);
                else handleLogin(postParams, out);

            } else if (path.equals("/register")) {
                if ("GET".equals(method)) sendPage(out, SimpleHttpView.renderRegisterForm(), sessionId);
                else handleRegister(postParams, out);

            } else if (path.equals("/logout") && currentUser != null) {
                sessions.remove(sessionId);
                redirect(out, "/");

            } else {
                sendPage(out, SimpleHttpView.renderMessage("Страница не найдена"), null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRegister(Map<String, String> p, OutputStream out) throws IOException {
        String u = p.get("username"), pw = p.get("password");
        if (u == null || pw == null || u.isBlank() || pw.isBlank()) {
            sendPage(out, SimpleHttpView.renderMessage("Заполните все поля"), null);
            return;
        }
        if (users.containsKey(u)) {
            sendPage(out, SimpleHttpView.renderMessage("Пользователь уже существует"), null);
            return;
        }
        users.put(u, new User(u, pw));
        saveUsers();
        saveCarts();
        sendPage(out, SimpleHttpView.renderMessage("Регистрация успешна! Теперь войдите."), null);
    }

    private void handleLogin(Map<String, String> p, OutputStream out) throws IOException {
        String u = p.get("username"), pw = p.get("password");
        User user = users.get(u);
        if (user != null && user.getPassword().equals(pw)) {
            String sid = UUID.randomUUID().toString();
            sessions.put(sid, user);
            sendPage(out, SimpleHttpView.renderMessage("Добро пожаловать, " + u + "!"), sid);
        } else {
            sendPage(out, SimpleHttpView.renderMessage("Неверный логин или пароль"), null);
        }
    }

    private void sendPage(OutputStream out, String html, String sessionId) throws IOException {
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        out.write("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=UTF-8\r\n".getBytes());
        if (sessionId != null) out.write(("Set-Cookie: SESSIONID=" + sessionId + "; Path=/; HttpOnly\r\n").getBytes());
        out.write(("Content-Length: " + data.length + "\r\n\r\n").getBytes());
        out.write(data);
    }

    private void redirect(OutputStream out, String location) throws IOException {
        out.write(("HTTP/1.1 303 See Other\r\nLocation: " + location + "\r\n\r\n").getBytes());
    }

    private void serveStaticFile(String path, OutputStream out) {
        File file = new File("." + path);
        if (!file.exists() || file.isDirectory()) {
            send404(out);
            return;
        }
        String ext = path.contains(".") ? path.substring(path.lastIndexOf(".") + 1).toLowerCase() : "";
        String mime = switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "ico" -> "image/x-icon";
            default -> "application/octet-stream";
        };
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = fis.readAllBytes();
            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            out.write(("Content-Type: " + mime + "\r\n").getBytes());
            out.write(("Content-Length: " + data.length + "\r\n").getBytes());
            out.write("Cache-Control: public, max-age=86400\r\n\r\n".getBytes());
            out.write(data);
        } catch (Exception e) {
            send404(out);
        }
    }

    private void send404(OutputStream out) {
        String msg = "<h1>404 — Файл не найден</h1>";
        try {
            byte[] b = msg.getBytes(StandardCharsets.UTF_8);
            out.write("HTTP/1.1 404 Not Found\r\nContent-Type: text/html; charset=UTF-8\r\n".getBytes());
            out.write(("Content-Length: " + b.length + "\r\n\r\n").getBytes());
            out.write(b);
        } catch (Exception ignored) {}
    }
}
