import java.util.*;

public class SimpleHttpView {

    private static String h(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    public static String renderProductList(Collection<Product> products, User currentUser) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Магазин обуви</title>");
        sb.append("<style>body{font-family:Arial,sans-serif;margin:40px;background:#f9f9f9;}");
        sb.append("h1{color:#333;} .grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:25px;}");
        sb.append(".card{background:white;padding:20px;border-radius:12px;box-shadow:0 4px 12px rgba(0,0,0,0.1);text-align:center;}");
        sb.append("img{max-width:100%;height:220px;object-fit:cover;border-radius:8px;}");
        sb.append("button{background:#1976d2;color:white;border:none;padding:12px 24px;border-radius:6px;cursor:pointer;font-size:16px;}");
        sb.append("button:hover{background:#1565c0;} button:disabled{background:#ccc;cursor:not-allowed;}</style></head><body>");
        sb.append("<h1>Магазин спортивной обуви</h1>");

        if (currentUser != null) {
            sb.append("<p>Привет, <b>").append(h(currentUser.getUsername())).append("</b>! ")
              .append("<a href='/cart'>Корзина</a> | <a href='/logout'>Выйти</a></p>");
        } else {
            sb.append("<p><a href='/login'>Войти</a> | <a href='/register'>Регистрация</a></p>");
        }

        sb.append("<div class='grid'>");
        for (Product p : products) {
            sb.append("<div class='card'>")
              .append("<img src='").append(p.getImage()).append("' alt='").append(h(p.getName())).append("'>")
              .append("<h3>").append(h(p.getName())).append("</h3>")
              .append("<p style='font-size:1.4em;color:#d32f2f;'>$").append(String.format("%.2f", p.getPrice())).append("</p>");

            if (currentUser != null) {
                sb.append("<form method='post' action='/add'>")
                  .append("<input type='hidden' name='id' value='").append(p.getId()).append("'>")
                  .append("<button type='submit'>В корзину</button></form>");
            } else {
                sb.append("<button disabled>Войдите, чтобы купить</button>");
            }
            sb.append("</div>");
        }
        sb.append("</div></body></html>");
        return sb.toString();
    }

    public static String renderCart(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Корзина</title>");
        sb.append("<style>body{font-family:Arial;margin:40px;background:#f9f9f9;} img{width:80px;height:80px;object-fit:cover;}");
        sb.append(".item{display:flex;gap:20px;align-items:center;background:white;padding:15px;margin:10px 0;border-radius:8px;}</style></head><body>");
        sb.append("<h1>Корзина — ").append(h(user.getUsername())).append("</h1>");
        if (user.getCart().getItems().isEmpty()) {
            sb.append("<p>Корзина пуста</p>");
        } else {
            for (Product p : user.getCart().getItems()) {
                sb.append("<div class='item'>")
                  .append("<img src='").append(p.getImage()).append("'>")
                  .append("<div><b>").append(h(p.getName())).append("</b> — $").append(String.format("%.2f", p.getPrice())).append("</div>")
                  .append("<form method='post' action='/remove'><input type='hidden' name='id' value='").append(p.getId()).append("'>")
                  .append("<button style='background:#d32f2f;'>Удалить</button></form></div>");
            }
        }
        sb.append("<hr><a href='/'>На главную</a></body></html>");
        return sb.toString();
    }

    public static String renderMessage(String msg) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Сообщение</title></head><body style='font-family:Arial;margin:40px;'>" +
               "<h1>" + h(msg) + "</h1><a href='/'>На главную</a></body></html>";
    }

    public static String renderLoginForm() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Вход</title></head><body style='font-family:Arial;margin:40px;'>" +
               "<h1>Вход</h1><form method='post' action='/login'>" +
               "Логин: <input type='text' name='username'><br><br>" +
               "Пароль: <input type='password' name='password'><br><br>" +
               "<button style='padding:10px 20px;font-size:16px;'>Войти</button></form><br>" +
               "<a href='/'>На главную</a> | <a href='/register'>Регистрация</a></body></html>";
    }

    public static String renderRegisterForm() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Регистрация</title></head><body style='font-family:Arial;margin:40px;'>" +
               "<h1>Регистрация</h1><form method='post' action='/register'>" +
               "Логин: <input type='text' name='username'><br><br>" +
               "Пароль: <input type='password' name='password'><br><br>" +
               "<button style='padding:10px 20px;font-size:16px;'>Зарегистрироваться</button></form><br>" +
               "<a href='/'>На главную</a></body></html>";
    }
}
