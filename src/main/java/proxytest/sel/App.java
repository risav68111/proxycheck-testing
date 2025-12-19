package proxytest.sel;

import java.time.LocalDate;
import java.util.List;

public class App {
    public static void main(String[] args) {
        System.out.println("test running.!");
        // Pw.run();
        String search_date = LocalDate.now().toString();
        System.out.println(search_date);
        Exec.runInThreads();
        // ProxyChecker.printProxies();
        // Tes.run();
        // try {
            // List<ProxyVar> ps= ProxyChecker.allProxies();
            // for (ProxyVar p: ps) {
            //     System.out.println("DECREASED FOR : " + p.toString());
            //     ProxyChecker.decreaseProxyCount(p.getId());
            //     // ProxyVar p = ProxyChecker.getNextEligibleProxy();
            //     // System.out.println(p.toString());
            // }
        // } catch (Exception e) {
        //     System.out.println("ERROR SQL.");
        //     e.printStackTrace();
        // }
    }
}
