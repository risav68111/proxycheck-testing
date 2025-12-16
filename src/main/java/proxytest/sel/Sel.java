// package proxytest.sel;
//
// import java.io.File;
// import java.io.FileOutputStream;
// import java.time.Duration;
// import java.util.Collections;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.zip.ZipEntry;
// import java.util.zip.ZipOutputStream;
//
// import org.openqa.selenium.By;
// import org.openqa.selenium.Proxy;
// import org.openqa.selenium.WebDriver;
// import org.openqa.selenium.WebElement;
// import org.openqa.selenium.chrome.ChromeDriver;
// import org.openqa.selenium.chrome.ChromeOptions;
// import org.openqa.selenium.support.ui.ExpectedConditions;
// import org.openqa.selenium.support.ui.WebDriverWait;
//
// import io.github.cdimascio.dotenv.Dotenv;
//
// public class Sel {
//
//     public static void run() {
//
//         String downloadDir = new File("downloads").getAbsolutePath();
//         new File(downloadDir).mkdirs();
//
//         WebDriver driver = createWebDriverUIProxy(downloadDir);
//         try {
//             driver.get("https://www.showmyip.com");
//             WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
//             List<WebElement> datas = wait
//                     .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.iptab")))
//                     .findElements(By.tagName("tr"));
//             datas.forEach(d -> System.out.println(d.getText()));
//             // System.out.println("Title: " + driver.getTitle());
//         } finally {
//             driver.quit();
//         }
//     }
//
//     public static WebDriver createWebDriverUIProxy(String path) {
//         String os = System.getProperty("os.name").toLowerCase();
//         File currentDir = new File(System.getProperty("user.dir"));
//         System.out.println("Project Root: " + currentDir.getAbsolutePath());
//         File chromeDriverFile = new File(currentDir, "chromedriver");
//         System.setProperty("webdriver.chrome.driver", chromeDriverFile.getAbsolutePath());
//         ChromeOptions chromeOptions = new ChromeOptions();
//
//         // PROXY Webshare
//         Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
//         String user = dotenv.get("WEBSHARE_USER"); 
//         String pass = dotenv.get("WEBSHARE_PASS");
//         String host = dotenv.get("PROXY_HOST");
//         int port = Integer.parseInt(dotenv.get("PROXY_PORT"));
//         String ipPort = host + ":" + port;
//
//         try {
//             String ext = createAuthExtension(host, port, user, pass);
//             System.out.println("extension path: " + ext);
//             chromeOptions.addExtensions(new File(ext));
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//
//         // Proxy proxy = new Proxy();
//         // String proxyServer = user + ":" + pass + "@rotating.proxy.webshare.io:80";
//
//         // proxy.setHttpProxy(ipPort);
//         // proxy.setSslProxy(ipPort);
//         // proxy.setHttpProxy(user+ ":" + pass + "@" + ipPort);
//         // proxy.setSslProxy(user+ ":" + pass + "@" + ipPort);
//         // proxy.setProxyType(Proxy.ProxyType.MANUAL); chromeOptions.setProxy(proxy);
//         // chromeOptions.setCapability("proxy", proxy);
//         // chromeOptions.setProxy(proxy);
//
//         // chromeOptions.addArguments("--proxy-server=http://" + user + ":" + pass + "@"
//         // + ipPort);
//         // chromeOptions.addArguments("--proxy-server=https://" + user + ":" + pass +
//         // "@rotating.proxy.webshare.io:80");
//         // chromeOptions.addExtensions(new File("proxy-extension.zip"));
//
//         chromeOptions.addArguments("--log-level=3");
//         chromeOptions.addArguments("--disable-plugins");
//         chromeOptions.addArguments("--disable-notifications");
//         chromeOptions.addArguments("--disable-extensions");
//         chromeOptions.addArguments("--no-sandbox");
//         chromeOptions.addArguments("--disable-dev-shm-usage");
//         chromeOptions.addArguments("--ignore-certificate-errors");
//         chromeOptions.addArguments("--ignore-ssl-errors=yes");
//         chromeOptions.setAcceptInsecureCerts(true);
//         chromeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-logging"));
//         Map<String, Object> prefs = new HashMap<>();
//         prefs.put("plugins.plugins_list",
//                 Collections.singletonList(Map.of("enabled", false, "name", "Chrome PDF Viewer")));
//         prefs.put("profile.default_content_setting_values.notifications", 2);
//         prefs.put("download.default_directory", path);
//         prefs.put("download.prompt_for_download", false);
//         prefs.put("download.directory_upgrade", true);
//         prefs.put("download.extensions_to_open", "");
//         prefs.put("plugins.always_open_pdf_externally", true);
//         chromeOptions.setExperimentalOption("prefs", prefs);
//         return new ChromeDriver(chromeOptions);
//     }
//
//     public static String createAuthExtension(String host, int port, String username, String password) throws Exception {
//         String manifest = """
//                 {
//                   "version": "1.0.0",
//                   "manifest_version": 2,
//                   "name": "Chrome Proxy",
//                   "permissions": ["proxy", "tabs", "unlimitedStorage", "storage", "<all_urls>", "webRequest", "webRequestBlocking"],
//                   "background": {
//                     "scripts": ["background.js"]
//                   }
//                 }
//                 """;
//
//         String background = """
//                 var config = {
//                     mode: "fixed_servers",
//                     rules: {
//                       singleProxy: {
//                         scheme: "http",
//                         host: "%s",
//                         port: %d
//                       }
//                     }
//                 };
//
//                 chrome.proxy.settings.set({value: config, scope: "regular"}, function() {});
//
//                 function callbackFn(details) {
//                     return {
//                         authCredentials: {
//                             username: "%s",
//                             password: "%s"
//                         }
//                     };
//                 }
//
//                 chrome.webRequest.onAuthRequired.addListener(
//                         callbackFn,
//                         {urls: ["<all_urls>"]},
//                         ['blocking']
//                 );
//                 """.formatted(host, port, username, password);
//
//         File extensionFile = File.createTempFile("proxyAuth", ".zip");
//         try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(extensionFile))) {
//             zip.putNextEntry(new ZipEntry("manifest.json"));
//             zip.write(manifest.getBytes());
//             zip.closeEntry();
//
//             zip.putNextEntry(new ZipEntry("background.js"));
//             zip.write(background.getBytes());
//             zip.closeEntry();
//         }
//
//         return extensionFile.getAbsolutePath();
//     }
// }
