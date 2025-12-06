package proxytest.sel;

import com.microsoft.playwright.options.Proxy;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;

import com.microsoft.playwright.*;

public class Pw {
    public static void run() {

        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        String user = dotenv.get("WEBSHARE_USER");
        String pass = dotenv.get("WEBSHARE_PASS");
        String host = dotenv.get("PROXY_HOST");
        int port = Integer.parseInt(dotenv.get("PROXY_PORT"));

        try (Playwright playwright = Playwright.create()) {
            System.out.println(user);
            System.out.println(pass);
            System.out.println(host);
            System.out.println(port);

            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setProxy(new Proxy(host + ":" + port)
                            .setUsername(user)
                            .setPassword(pass));

            Browser browser = playwright.chromium().launch(options);
            Page page = browser.newPage();
            page.navigate("https://www.showmyip.com");

            List<Locator> rows = page.locator("table.iptab tr")).all();
            rows.forEach(r -> System.out.println("row: " + r.textContent()));

            // System.out.println("IP: " + page.locator("table.iptab tr:nth-child(2)
            // td:nth-child(2)").textContent());
            // System.out.println("Location: " + page.locator("table.iptab tr:nth-child(4)
            // td:nth-child(2)").textContent());

            page.waitForTimeout(5000);
            browser.close();
        }
    }
}
