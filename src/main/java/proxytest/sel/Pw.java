package proxytest.sel;

import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class Pw {
    public int i;

    // public Pw(int i) {
    //     this.i = i;
    // }

    // @Override
    // public void run() {
    //     runIt();
    // }

    public static void runIt() {

        try  {
            ProxyVar p = ProxyChecker.getNextEligibleProxy();
            Browser browser = HelperClass.launchChromiumBrowser(p);
            Page page = browser.newPage();
            Page page1 = browser.newPage();
            page.navigate("https://www.showmyip.com");
            page1.navigate("https://services.ecourts.gov.in");

            Thread.sleep(10000);

            List<Locator> rows = page.locator("table.iptab tbody tr").all();
            System.out.println("------------------------------------------");
            rows.forEach(r -> System.out.println("row: " + r.textContent().trim()));
            System.out.println("------------------------------------------");

            // System.out.println("IP: " + page.locator("table.iptab tr:nth-child(2)
            // td:nth-child(2)").textContent());
            // System.out.println("Location: " + page.locator("table.iptab tr:nth-child(4)
            // td:nth-child(2)").textContent());

            page.waitForTimeout(5000);
            // browser.close();
        } catch(Exception e) {
            System.out.println("ERROR");
            e.printStackTrace();
        }
    }

}
