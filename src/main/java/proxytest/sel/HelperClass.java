package proxytest.sel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Proxy;

import io.github.cdimascio.dotenv.Dotenv;

public class HelperClass {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private static final String user = dotenv.get("W_USER");
    private static final String pass = dotenv.get("W_PASS");
    private static final String proxy = dotenv.get("PROXY");

    public static Browser launchChromiumBrowser(ProxyVar p) {
        // String userDataDir = "/tmp/playwright-chrome-user-data-" + UUID.randomUUID();
        // ProxyVar p =null;
        // try {
        // p = ProxyChecker.getNextEligibleProxy();
        // } catch (Exception e) {
        // throw new RuntimeException(e);
        // }
        System.out.println("PROXY: "+ proxy);

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setProxy(new Proxy(proxy)
                        .setUsername(user)
                        .setPassword(pass));

        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(options);

        // Browser browser = playwright.webkit().launch();
        return browser;
    }

    public static void createDirectory(String path) {
        try {
            Path dir = Paths.get(path);
            if (Files.notExists(dir)) {
                Files.createDirectory(dir);
            }
        } catch (Exception e) {
            System.out.println("Unable to create directory");
        }
    }

    public static void deleteDirectory(String directoryPath) {
        Path dir = Paths.get(directoryPath); // path to the directory
        try {
            Files
                    .walk(dir) // Traverse the file tree in depth-first order
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            // System.out.println("Deleting: " + path);
                            Files.delete(path); // delete each file or directory
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            System.out.println("Delete");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
