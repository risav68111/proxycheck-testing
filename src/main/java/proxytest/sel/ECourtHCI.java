package com.fios.fiosspringbootapi.service.scripts;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.fios.fiosspringbootapi.logic.RequestLogic;
import com.fios.fiosspringbootapi.utlis.S3Upload;
import com.fios.fiosspringbootapi.utlis.ZipDirectory;

public class ECourtHCI implements Runnable {

    public String targetName;
    public String searchedBy;
    public String caseId;
    public String service;
    public String search_date;
    public String currentDir = System.getProperty("user.dir");
    public String folderName;
    public String requestId;
    public String serviceId;
    public String fromYear;
    public String toYear;
    public String state;
    public String district;
    public String courtName;
    public String status;
    public List<String> bench;

    public ECourtHCI(String caseId, String service, String targetName, String search_date, String searchedBy,
            String requestId, String serviceId, String fromYear, String toYear, String courtName, List<String> bench) {

        String curr = caseId + "_" + service + "_" + "ECourtHCI" + "_" + "ECourtHCI" + "_" + targetName + "_"
                + search_date;
        this.folderName = curr;
        this.targetName = targetName;
        this.searchedBy = searchedBy;
        this.caseId = caseId;
        this.service = service;
        this.search_date = search_date;
        this.requestId = requestId;
        this.serviceId = serviceId;
        this.fromYear = fromYear;
        this.toYear = toYear;
        this.courtName = courtName;
        this.bench = bench;

    }

    @Override
    public void run() {

        try {
            processECourtHI();
            RequestLogic.portalStatus("High Court of India", requestId, serviceId, "COMPLETED");
        } catch (Exception e) {
            RequestLogic.portalStatus("High Court of India", requestId, serviceId, "ERROR");
            // throw new RuntimeException(e);
        }

    }

    private void processECourtHI() {

        this.folderName = caseId + "_" + service + "_" + "ECourt" + "_" + "HCI" + "_" + targetName + "_" + search_date;
        String path = currentDir + "/" + folderName;
        String startTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future<?>> futures = new ArrayList<>();

        try {
            HelperClass.createDirectory(path);
            int minY = Integer.parseInt(fromYear);
            int maxY = Integer.parseInt(toYear);

            for (int checkYear = minY; checkYear <= maxY; checkYear++) {
                Thread.sleep(1000);
                for (String b : bench) {
                    Future<?> future = executorService
                            .submit(new ECourtHCIRunnable(caseId, service, targetName, search_date, searchedBy,
                                    requestId, serviceId, String.valueOf(checkYear), courtName, b, folderName));

                    futures.add(future);
                }
            }

            long maxExecutionTime = 4;
            TimeUnit timeUnit = TimeUnit.HOURS;
            for (Future<?> future : futures) {
                future.get(maxExecutionTime, timeUnit);
            }

            int check = 0;
            String status = "NOT FOUND";
            String s3_url = null;

            check = countFilesInDirectory(path);
            System.out.println("Check : " + check);
            try {
                HelperClass.deleteDirectory(path + File.separator + "captcha");
            } catch (Exception e) {
                System.out.println("Error in deleting captcha folder");
            }

            if (check == 0) {
                System.out.println("No Data Found in E court - High court of India");
            } else {
                status = "FOUND";
                String fileName = caseId + "_" + targetName + "_" + "ECourtHCI";
                s3_url = folderName + "/" + fileName + ".zip";

                ZipDirectory.zipFolder(path, path + "/" + fileName + ".zip");
                S3Upload.UploadDirectoryinS3withWord(caseId + "_" + service + "_" + "ECourt" + "_" + "HCI");
            }
            RequestLogic.UpdateDatabaseStatus(status, "ECourt - High Court of India", "High Court of India", requestId,
                    s3_url, serviceId, startTime);

        } catch (Exception e) {
            for (Future<?> future : futures) {
                future.cancel(true);
            }
            e.printStackTrace();
            RequestLogic.UpdateDatabaseStatus("ERROR", "ECourt - High Court of India", "High Court of India", requestId,
                    null, serviceId, startTime);
        } finally {
            executorService.shutdown();
            HelperClass.deleteDirectory(path);
        }

    }

    public static int countFilesInDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Invalid directory: " + path);
            return 0;
        }

        File[] files = directory.listFiles();
        if (files == null)
            return 0;

        int count = 0;
        for (File file : files) {
            if (file.isFile()) {
                count++;
            }
        }

        return count;
    }

}