import java.util.Collection;

public class SimpleHttpView {

    public static String renderProductList(Collection<Product> products, User currentUser) {
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

        // Условное отображение: Войти/Регистрация или Выйти
        if (currentUser != null) {
            sb.append("<a href='/logout'>Выйти</a>");
        } else {
            sb.append("<a href='/login'>Войти</a> | <a href='/register'>Регистрация</a>");
        }

        String header = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='UTF-8'>
                <title>Магазин</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #f4f4f4; }
                    h1 { color: #333; }
                    ul { list-style: none; padding: 0; }
                    li { background: white; margin: 10px 0; padding: 15px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
                    button { background: #007bff; color: white; border: none; padding: 8px 12px; border-radius: 4px; cursor: pointer; }
                    button:hover { background: #0056b3; }
                    a { color: #007bff; text-decoration: none; margin: 0 10px; }
                    a:hover { text-decoration: underline; }
                    form { display: inline; }
                    hr { margin: 30px 0; }
                </style>
            </head>
            <body>
            """;
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

        String header = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='UTF-8'>
                <title>Корзина</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #f4f4f4; }
                    h1 { color: #333; }
                    ul { list-style: none; padding: 0; }
                    li { background: white; margin: 10px 0; padding: 15px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
                    button { background: #007bff; color: white; border: none; padding: 8px 12px; border-radius: 4px; cursor: pointer; }
                    button:hover { background: #0056b3; }
                    a { color: #007bff; text-decoration: none; margin: 0 10px; }
                    a:hover { text-decoration: underline; }
                    form { display: inline; }
                    hr { margin: 30px 0; }
                </style>
            </head>
            <body>
            """;
        String footer = "</body></html>";
        return header + sb.toString() + footer;
    }

    public static String renderMessage(String msg) {
      String header = """
          <!DOCTYPE html>
          <html>
          <head>
              <meta charset='UTF-8'>
              <title>Сообщение</title>
              <style>
                  body { font-family: Arial, sans-serif; margin: 40px; background: #f4f4f4; }
                  h1 { color: #333; }
                  ul { list-style: none; padding: 0; }
                  li { background: white; margin: 10px 0; padding: 15px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
                  button { background: #007bff; color: white; border: none; padding: 8px 12px; border-radius: 4px; cursor: pointer; }
                  button:hover { background: #0056b3; }
                  a { color: #007bff; text-decoration: none; margin: 0 10px; }
                  a:hover { text-decoration: underline; }
                  form { display: inline; }
                  hr { margin: 30px 0; }
              </style>
          </head>
          <body>
          """;
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

        String header = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='UTF-8'>
                <title>Решистрация</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #f4f4f4; }
                    h1 { color: #333; }
                    ul { list-style: none; padding: 0; }
                    li { background: white; margin: 10px 0; padding: 15px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
                    button { background: #007bff; color: white; border: none; padding: 8px 12px; border-radius: 4px; cursor: pointer; }
                    button:hover { background: #0056b3; }
                    a { color: #007bff; text-decoration: none; margin: 0 10px; }
                    a:hover { text-decoration: underline; }
                    form { display: inline; }
                    hr { margin: 30px 0; }
                </style>
            </head>
            <body>
            """;
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

        String header = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset='UTF-8'>
                <title>Вход</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background: #f4f4f4; }
                    h1 { color: #333; }
                    ul { list-style: none; padding: 0; }
                    li { background: white; margin: 10px 0; padding: 15px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
                    button { background: #007bff; color: white; border: none; padding: 8px 12px; border-radius: 4px; cursor: pointer; }
                    button:hover { background: #0056b3; }
                    a { color: #007bff; text-decoration: none; margin: 0 10px; }
                    a:hover { text-decoration: underline; }
                    form { display: inline; }
                    hr { margin: 30px 0; }
                </style>
            </head>
            <body>
            """;
        String footer = "</body></html>";
        return header + form + footer;
    }
}
