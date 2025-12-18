package proxytest.sel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;

public class DistrictECourt implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(DistrictECourt.class);

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
    public String courtComplex;

    public DistrictECourt(String caseId, String service, String targetName, String search_date,
            String searchedBy, String requestId, String serviceId, String fromYear, String toYear, String state,
            String district, String courtComplex) {
        this.targetName = targetName;
        String curr = caseId + "_" + service + "_" + "DistrictECourt" + "_" + "DistrictECourt" + "_" + targetName
                + "_" + search_date;
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
        this.state = state;
        this.district = district;
        this.courtComplex = courtComplex;
    }

    @Override
    public void run() {

        try {
            callAll();
        } catch (IOException e) {
            // Handle the exceptions
            e.printStackTrace();
        }
    }

    public void callAll() throws IOException {
        log.info("DISTRICT E COURT ");
        log.info("courtComplex: {}", courtComplex);
        log.info("district: {}", district);
        log.info("state: {}", state);
        log.info("toYear: {}", toYear);
        log.info("fromYear: {}", fromYear);
        log.info("serviceId: {}", serviceId);
        log.info("requestId: {}", requestId);
        log.info("searchedBy: {}", searchedBy);
        log.info("search_date: {}", search_date);
        log.info("targetName: {}", targetName);
        log.info("service: {}", service);
        log.info("caseId: {}", caseId);

        // createMap(Integer.parseInt(year));
        ExecutorService executorService = Executors.newFixedThreadPool(5); // ERROR change threadpool to 3
        ArrayList<Integer> check = new ArrayList<Integer>();
        List<Future<?>> futures = new ArrayList<>(); // Use a wildcard with an upper bound
        String startTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

        folderName = caseId + "_" + service + "_" + "DistrictECourt" + "_" + "DistrictECourt" + "_" + targetName + "_"
                + search_date;
        String path1 = currentDir + "/" + folderName;
        HelperClass.createDirectory(path1);

        long maxExecutionTime = 24;
        TimeUnit timeUnit = TimeUnit.HOURS;

        // Wait for all tasks to complete or time out
        // WebDriver driver = HelperClass.createWebDriver(path1);
        // WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        // driver.manage().window().maximize();
        ProxyVar p = null;
        Browser browser = null;

        try {
            p = ProxyChecker.getNextEligibleProxy();
            browser = HelperClass.launchChromiumBrowser(p);

            Page page = browser.newPage();
            page.navigate(
                    "https://services.ecourts.gov.in/ecourtindia_v6/?p=home&app_token=34377bb73c3a60ac4a139c92149c2a9abcf55b142bd38392aeba5167dbbc62aa");
            // log.info(
            // "printing body: ------------------------------------------------\n{}\n
            // __________________________________________________________________",
            // page.locator("body").textContent());

            page.locator("//a[@id='leftPaneMenuCS']").click();
            log.info("leftPaneMenuCS ");
            Thread.sleep(2000);
            page.locator("(//button[@class=\"btn-close\"])[1]").click();
            log.info("close: ");
            page.selectOption("#sess_state_code", state);

            Thread.sleep(2000);
            page.selectOption("//*[@id=\"sess_dist_code\"]", district);

            Thread.sleep(3000);

            // Select courtComplexCode = new Select(wait.until(
            // ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#court_complex_code"))));
            Locator courtComplexCode = page.locator("#court_complex_code");
            List<String> allComplexCode = courtComplex.equals("All")
                    ? courtComplexCode.locator("option").allTextContents()
                    : List.of(courtComplex);

            Thread.sleep(3000);
            allComplexCode.forEach(e -> log.info("complexCode: {}", e));
            if (allComplexCode.size() > 1)
                allComplexCode.remove("Select court complex");
            // log.info("deleted defualt content: {}", allComplexCode.remove("Select court
            // complex"));

            int processNumber = 0;
            for (String courtComp : allComplexCode) {
                Thread.sleep(2000);
                page.selectOption("#court_complex_code", new SelectOption().setLabel(courtComp));
                Thread.sleep(2000);
                List<String> estCodesList = new ArrayList<>();
                try {
                    Thread.sleep(2000);
                    Locator estCodes = page.locator("div#est_codes");
                    if (estCodes.isVisible()) {
                        estCodesList = estCodes.locator("option").allTextContents();
                        estCodesList.remove(0);
                    }
                } catch (Exception e) {
                    log.info("no court establishment found");
                    e.printStackTrace();
                }

                if (estCodesList.isEmpty())
                    estCodesList.add(null);
                estCodesList.forEach(est -> log.info("est codes found  on district {}: {}", district, est));
                for (String estCode : estCodesList) {
                    for (int checkYear = Integer.parseInt(fromYear); checkYear <= Integer
                            .parseInt(toYear); checkYear++) {
                        String workbookName = (checkYear + "_"
                                + (estCode == null ? "" : estCode) + "_"
                                + (courtComp == null ? "" : courtComp) + "_"
                                + district + "_"
                                + (processNumber++)).replaceAll("^_+|_+$", "").replaceAll("[^a-zA-Z0-9]", "_")
                                + ".xlsx";
                        log.info("workbookName: {}", workbookName);

                        Future<?> future = executorService
                                .submit(new DistrictECourtRunnable(caseId, service, targetName, search_date,
                                        searchedBy, requestId, serviceId, String.valueOf(checkYear), state, district,
                                        courtComp, estCode, check, workbookName));
                        futures.add(future);
                    }
                }
            }

            for (Future<?> future : futures) {
                future.get(maxExecutionTime, timeUnit);
            }
            folderName = caseId + "_" + service + "_" + "DistrictECourt" + "_" + "DistrictECourt" + "_" + targetName
                    + "_" + search_date;
            // Arra = new ArrayList<Integer>();
            saveData(path1, startTime, check);
            check.forEach(c -> log.info("check: {}", c));
            // RequestLogic.portalStatus("District E Court", requestId, serviceId,
            // "COMPLETED");

        } catch (Exception e) {
            e.printStackTrace();
            for (Future<?> future : futures) {
                future.cancel(true);
            }

            // RequestLogic.UpdateDatabaseStatus("ERROR", "District E Court", "District E Court", requestId, null, serviceId, startTime);
            // RequestLogic.portalStatus("District E Court", requestId, serviceId, "ERROR");
        } finally {
            try {
                if (p != null) {
                    ProxyChecker.decreaseProxyCount(p.getId());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                if (browser != null) {
                    browser.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            executorService.shutdown();
        }
    }

    public void saveData(String path, String startTime, ArrayList<Integer> check) {
        try {
            mergeWorkbookByYear(path);

            // HelperClass.saveWorkbook(path, targetName + ".xlsx", sheet);
            Thread.sleep(2000);
            HelperClass.deleteDirectory(path + "/" + "captcha");
            String status1 = "NOT FOUND";
            String s3_url = null;
            if (check.size() > 0) {
                String zipName = caseId + "_" + targetName + "_" + "DEC";
                status1 = "FOUND";
                s3_url = folderName + "/" + zipName + ".zip";
                // ZipDirectory.zipFolder(path, path + "/" + zipName + ".zip");
                // S3Upload.UploadDirectoryinS3withWord(caseId + "_" + service + "_" +
                // "DistrictECourt");
                System.out.println("printing s3 url -> " + s3_url);
            }
            System.out.println("status" + status1 + "request" + requestId + "S3url" + s3_url + "serviceID" + serviceId
                    + "startTime" + startTime);
            // RequestLogic.UpdateDatabaseStatus(status1, "District E Court", "District E
            // Court", requestId, s3_url, serviceId, startTime);

        } catch (Exception e) {
            System.out.println("ERROR");

        } finally {
            // HelperClass.deleteDirectory(path);
        }
    }

    public void mergeWorkbookByYear(String folderPath) {
        try {
            List<Path> excelFiles = Files.list(Paths.get(folderPath)).filter(p -> p.toString().endsWith(".xlsx"))
                    .collect(Collectors.toList());
            Map<String, List<Path>> excelGroupYear = excelFiles.stream()
                    .collect(Collectors.groupingBy(p -> p.getFileName().toString().substring(0, 4)));
            for (String year : excelGroupYear.keySet()) {
                XSSFWorkbook newWorkbook = new XSSFWorkbook();
                int sheetCount = 0;
                for (Path file : excelGroupYear.get(year)) {
                    try (FileInputStream fis = new FileInputStream(file.toFile());
                            XSSFWorkbook oldWorkbook = new XSSFWorkbook(fis)) {
                        for (int oldWbSheetCount = 0; oldWbSheetCount < oldWorkbook
                                .getNumberOfSheets(); oldWbSheetCount++) {
                            XSSFSheet sheetFromOld = oldWorkbook.getSheetAt(oldWbSheetCount);
                            String sheetName = "sheet_" + sheetCount++;
                            XSSFSheet sheetFromNew = newWorkbook.createSheet(sheetName);
                            copySheet(sheetFromOld, sheetFromNew);
                        }
                    } catch (Exception e) {
                        log.info("Error for saving file for year: {}", year);
                        e.printStackTrace();
                    }
                }
                String newWbFileName = "DistrictEcourt_" + year + ".xlsx";
                try (FileOutputStream fos = new FileOutputStream(folderPath + "/" + newWbFileName)) {
                    newWorkbook.write(fos);
                }
                newWorkbook.close();
                log.info("Workbook for year : {} saved successfully.", year);
                for (Path file : excelGroupYear.get(year)) {
                    Files.deleteIfExists(file);
                }
            }
        } catch (Exception e) {
            log.info("Error while saving file for: ");
            log.info("DISTRICT E COURT ");
            log.info("caseId: {}", caseId);
            log.info("targetName: {}", targetName);
            log.info("fromYear: {}", fromYear);
            log.info("toYear: {}", toYear);
            log.info("searchedBy: {}", searchedBy);
            log.info("service: {}", service);
            log.info("serviceId: {}", serviceId);
            log.info("requestId: {}", requestId);
            log.info("courtComplex: {}", courtComplex);
            log.info("district: {}", district);
            log.info("state: {}", state);
            log.info("search_date: {}", search_date);
        }
    }

    private void copySheet(XSSFSheet src, XSSFSheet dest) {
        for (int r = 0; r <= src.getLastRowNum(); r++) {
            XSSFRow srcRow = src.getRow(r);
            XSSFRow destRow = dest.createRow(r);
            if (srcRow == null)
                continue;
            for (int c = 0; c < srcRow.getLastCellNum(); c++) {
                XSSFCell srcCell = srcRow.getCell(c);
                XSSFCell destCell = destRow.createCell(c);

                if (srcCell != null) {
                    switch (srcCell.getCellType()) {
                        case STRING -> destCell.setCellValue(srcCell.getStringCellValue());
                        case NUMERIC -> destCell.setCellValue(srcCell.getNumericCellValue());
                        case BOOLEAN -> destCell.setCellValue(srcCell.getBooleanCellValue());
                        default -> destCell.setCellValue(srcCell.toString());
                    }
                }
            }
        }
    }
}
