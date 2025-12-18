package proxytest.sel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Exec {
    public static void runInThreads() {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            List<Future<?>> futures = new ArrayList<>();
            String targetName = "Neha Gada";
            String searchedBy = "searchedBy";
            String caseId = "5404-003-26";
            String service = "service";
            String search_date = "search_date";
            String fromYear = "2016";
            String toYear = "2025";
            String state = "Maharashtra";
            String district = "Maharashtra Family Courts";
            String courtComplex = "All";
            // courtComplex = "Rouse Avenue Court Complex";
            String requestId = "requestId";
            String serviceId = "serviceId";
            String[] targetNames = new String[] {"jatin", "sushant", "rishav" };
            int i=0;
            // for (; i<4; ) {
                // targetName = tName;
                // futures.add(executorService.submit(new Pw(i++)));
                // }
            // }
            // futures.add(executorService.submit(new DistrictECourt(caseId, service,
            //     targetName, search_date, searchedBy, requestId, serviceId, fromYear, toYear, state, district, courtComplex)));
            // futures.add(executorService.submit(new DistrictECourt(caseId, service,
            //     targetName, search_date, searchedBy, requestId, serviceId, fromYear, toYear, state, "Maharashtra Industrial and Lab", courtComplex)));
            ArrayList<Integer> numList = new ArrayList<>();
            futures.add(executorService.submit(new DistrictECourtRunnable(caseId, service, targetName, search_date, searchedBy, requestId, serviceId,  "2025", "Delhi", "Central", "Rouse Avenue Court Complex",null, numList, "wb.xlsx" )));

            for (Future f : futures) {
                f.get(24, TimeUnit.HOURS);
            }
        } catch (Exception e) {
            System.out.println("Error found.");
            e.printStackTrace();
        }
    }

}
