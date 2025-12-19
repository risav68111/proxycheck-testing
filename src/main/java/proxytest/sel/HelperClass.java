package proxytest.sel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.microsoft.playwright.Browser;
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
        String userDataDir = "/tmp/playwright-chrome-user-data-" + UUID.randomUUID();
        System.out.println("PROXY: "+ proxy);

        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(true)
                // .setArgs(List.of("--window-size=1920,1080"))
                .setProxy(new Proxy(proxy)
                        .setUsername(user)
                        .setPassword(pass));

        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(options);

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

    
    public static WebDriver createWebDriver() {

        String os = System.getProperty("os.name").toLowerCase();
        // Get current directory (likely target folder when running JAR)
        File currentDir = new File(System.getProperty("user.dir"));
        // Go one level up to project root
        File projectRoot = currentDir.getParentFile();
        // Build chromedriver file path

        if(os.contains("windows")){
            projectRoot =  new File(System.getProperty("user.dir"));
        }
        File chromeDriverFile = new File(projectRoot, os.contains("windows") ? "chromedriver.exe" : "chromedriver");
        String chromeDriverPath = chromeDriverFile.getAbsolutePath();
        // Set the system property so Selenium knows where chromedriver is
        System.out.println("Setting chromedriver path to: " + chromeDriverPath);
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);

        ChromeOptions options = new ChromeOptions();

        String uniqueUserDataDir = "/tmp/chrome-user-data-" + UUID.randomUUID();
        options.addArguments("--user-data-dir=" + uniqueUserDataDir);


        // Make less detectable
        options.addArguments("--headless"); // use --headless=new if supported
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("window-size=1920,1080");
        options.addArguments("user-agent=Mozilla/5.0 (X11; Linux x86_64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/126.0.0.0 Safari/537.36");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        // Disable logging
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-logging"));

        // Download prefs
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", currentDir + "/");
        prefs.put("download.prompt_for_download", false);
        prefs.put("plugins.always_open_pdf_externally", true);
        options.setExperimentalOption("prefs", prefs);

        WebDriver driver = new ChromeDriver(options);

        // Remove webdriver flag
        ((JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
        );

        return driver;
    }

    public static WebDriver createHeadlessWebDriver() {


        String os = System.getProperty("os.name").toLowerCase();
        // Get current directory (likely target folder when running JAR)
        File currentDir = new File(System.getProperty("user.dir"));
        // Go one level up to project root
        File projectRoot = currentDir.getParentFile();
        // Build chromedriver file path
        File chromeDriverFile = new File(projectRoot, os.contains("windows") ? "chromedriver.exe" : "chromedriver");
        String chromeDriverPath = chromeDriverFile.getAbsolutePath();
        // Set the system property so Selenium knows where chromedriver is
        System.out.println("Setting chromedriver path to: " + chromeDriverPath);
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);

        ChromeOptions options = new ChromeOptions();

        String uniqueUserDataDir = "/tmp/chrome-user-data-" + UUID.randomUUID();
        options.addArguments("--user-data-dir=" + uniqueUserDataDir);


        // Make less detectable
        options.addArguments("--headless"); // use --headless=new if supported
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("window-size=1920,1080");
        options.addArguments("user-agent=Mozilla/5.0 (X11; Linux x86_64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/126.0.0.0 Safari/537.36");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        // Disable logging
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-logging"));

        // Download prefs
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", currentDir + "/");
        prefs.put("download.prompt_for_download", false);
        prefs.put("plugins.always_open_pdf_externally", true);
        options.setExperimentalOption("prefs", prefs);

        WebDriver driver = new ChromeDriver(options);

        // Remove webdriver flag
        ((JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
        );

        return driver;
    }
}
