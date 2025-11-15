import java.util.Collection;

public class SimpleHttpView {

    public static String renderProductList(Collection<Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Магазин обуви</h1>");
        sb.append("<ul>");
        for (Product p : products) {
            sb.append("<li>")
              .append(p.getName()).append(" - $").append(p.getPrice())
              .append(" <form method='POST' action='/add'>")
              .append("<input type='hidden' name='id' value='").append(p.getId()).append("'>")
              .append("<button>В корзину</button></form></li>");
        }
        sb.append("</ul>");
        sb.append("<a href='/cart'>Перейти в корзину</a>");
        sb.append("<hr>");
        sb.append("<a href='/login'>Войти</a> | <a href='/register'>Регистрация</a>");

        String header = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Shop</title></head><body>";
        String footer = "</body></html>";
        return header + sb.toString() + footer;
    }

    public static String renderCart(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Корзина пользователя: ").append(user.getUsername()).append("</h1>");
        sb.append("<ul>");
        for (Product p : user.getCart().getItems()) {
            sb.append("<li>").append(p.getName()).append(" - $").append(p.getPrice())
              .append(" <form method='POST' action='/remove'>")
              .append("<input type='hidden' name='id' value='").append(p.getId()).append("'>")
              .append("<button>Удалить</button></form></li>");
        }
        sb.append("</ul>");
        sb.append("<a href='/'>На главную</a>");

        String header = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Cart</title></head><body>";
        String footer = "</body></html>";
        return header + sb.toString() + footer;
    }

    public static String renderMessage(String msg) {
        String header = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Message</title></head><body>";
        String footer = "</body></html>";
        return header + "<h1>" + msg + "</h1><a href='/'>На главную</a>" + footer;
    }

    public static String renderRegisterForm() {
        String form = "<h1>Регистрация</h1>" +
                "<form method='POST' action='/register'>" +
                "Имя: <input type='text' name='username'><br>" +
                "Пароль: <input type='password' name='password'><br>" +
                "<button>Зарегистрироваться</button>" +
                "</form><a href='/'>На главную</a>";

        String header = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Регистрация</title></head><body>";
        String footer = "</body></html>";
        return header + form + footer;
    }

    public static String renderLoginForm() {
        String form = "<h1>Вход</h1>" +
                "<form method='POST' action='/login'>" +
                "Имя: <input type='text' name='username'><br>" +
                "Пароль: <input type='password' name='password'><br>" +
                "<button>Войти</button>" +
                "</form><a href='/'>На главную</a>";

        String header = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Вход</title></head><body>";
        String footer = "</body></html>";
        return header + form + footer;
    }
}
