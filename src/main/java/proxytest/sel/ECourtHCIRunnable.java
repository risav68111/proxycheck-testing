package proxytest.sel;

// import com.fios.fiosspringbootapi.utlis.AntiCaptcha;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ECourtHCIRunnable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ECourtHCIRunnable.class);

    private String caseId;
    private String service;
    private String targetName;
    private String search_date;
    private String searchedBy;
    public String requestId;
    public String serviceId;
    public String currentDir = System.getProperty("user.dir");
    public String folderName;
    public String BASE_DIRECTORY;
    public String year;
    public String state;
    public String courtName;
    public String bench;
    public String directoryPath;

    String path1;
    String startTime;

    public ECourtHCIRunnable(String caseId, String service, String targetName, String search_date, String searchedBy,
                             String requestId, String serviceId, String year, String courtName, String bench, String folderName) {
        this.targetName = targetName;
        this.searchedBy = searchedBy;
        this.caseId = caseId;
        this.service = service;
        this.search_date = search_date;
        this.requestId = requestId;
        this.serviceId = serviceId;
        this.folderName = folderName;
        this.BASE_DIRECTORY = currentDir + File.separator + this.folderName;
        this.year = year;
        this.courtName = courtName;
        this.bench = bench;

    }

    public void run() {
        try {
            ecourtHCI();
        } catch (InterruptedException e) {
            // throw new RuntimeException(e);
        }
    }

    private void ecourtHCI() throws InterruptedException {

        String path1 = currentDir + "/" + folderName;
        WebDriver driver = HelperClass.createWebDriver(path1);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();

        try {
            driver.get("https://hcservices.ecourts.gov.in/hcservices/main.php#");

            // log.info(wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body"))).getText());
            // Closing the popup
            try {
                log.info("case status Btn");
                WebElement courtBtn = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"leftPaneMenuCS\"]/img")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", courtBtn);
            } catch (Exception e) {
                log.info("selecting court error occoured.");
                log.info("error: {}",e.getMessage());
            }
            // Closing the popup
            try {
                Thread.sleep(2000);
                log.info("closing popup");
                WebElement bsAlert = wait.until(ExpectedConditions
                        .presenceOfElementLocated(By.xpath("//*[@id=\"bs_alert\"]/div/div/div[2]/button")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", bsAlert);
            } catch (Exception e) {
                log.info("error while closing pop up");
                log.info("error: {}",e.getMessage());
                System.out.println("No close button");
            }

            log.info("court menu drop down");
            // Selecting the court
            Select courtDropdown = new Select(driver.findElement(By.xpath("//*[@id=\"sess_state_code\"]")));

            log.info("bench dropdown");
            // Selecting the bench
            Select benchDropdown = new Select(driver.findElement(By.xpath("//*[@id=\"court_complex_code\"]")));

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Filter Results");
            int rowNum = 0;

            String[] headers = { "Sr No", "Case Type / Case Number / Case Year",
                    "Petitioner Name Versus Respondent Name", "Filing Number", "Filing Date", "Registration Number",
                    "Registration Date", "CNR Number", "First Hearing Date", "Next Hearing Date", "Stage of Case",
                    "Court Number and Judge", "Bench Type", "Judicial Branch",
                    "State", "District", "Not Before Me", "Petitioner and Advocate", "Respondent and Advocate",
                    "Category Details", "Cause List Type", "Judge", "Business On Date",
                    "Hearing Date", "Purpose of hearing", "Link Details", "Order Number", "Order on", "Judge",
                    "Order Date", "Order Details" };

            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.length; i++) {
                Cell headerCell = headerRow.createCell(i);
                headerCell.setCellValue(headers[i]);
                // headerCell.setCellStyle(HelperClass.addHeaderStyle(workbook));
            }

            Thread.sleep(2000);

            if (!bench.equals("All")) {
                log.info("selecting court from dropdown: {}", courtName);
                courtDropdown.selectByVisibleText(courtName);
                Thread.sleep(2000);
                log.info("selecting bench from dropdown: {}", bench);
                benchDropdown = new Select(driver.findElement(By.xpath("//*[@id=\"court_complex_code\"]")));
                benchDropdown.selectByVisibleText(bench);
                // call the method
                log.info("calling districtComplex function");
                districtComplex(wait, Integer.parseInt(year), sheet, driver, path1, rowNum, workbook);
                log.info("after districtComplex function");

            } else {

                List<WebElement> options_bench = benchDropdown.getOptions();
                for (int ind = 1; ind < options_bench.size(); ind++) {
                    courtDropdown.selectByVisibleText(courtName);
                    benchDropdown.selectByIndex(ind);
                    Thread.sleep(2000);
                    log.info("calling districtComplex function");
                    districtComplex(wait, Integer.parseInt(year), sheet, driver, path1, rowNum, workbook);
                    log.info("after districtComplex function");
                }

            }
        } catch (Exception e) {
            log.info("some error occoured");
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }

    public void districtComplex(WebDriverWait wait, int j, XSSFSheet sheet1, WebDriver driver, String path1, int rowNum,
                                XSSFWorkbook workbook) {

        try {
            log.info("Entering districtComplex function for year: {}", year);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            int condition = 1;
            while (condition <= 6) {
                log.info("number of retries: {}", condition);
                try {
                    WebElement petresNameInput = wait
                            .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='petres_name']")));
                    petresNameInput.clear();
                    Thread.sleep(1000);
                    // petresNameInput.sendKeys(targetName);
                    log.info("entering name");
                    // for (char ch : targetName.toCharArray()) {
                    // petresNameInput.sendKeys(Character.toString(ch));
                    // Thread.sleep(200);
                    // }
                    js.executeScript("arguments[0].value = arguments[1];", petresNameInput, targetName);
                    Thread.sleep(1000);

                    log.info("entering year");
                    WebElement rgyearPInput = wait
                            .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='rgyearP']")));
                    rgyearPInput.clear();
                    Thread.sleep(1000);
                    rgyearPInput.sendKeys(String.valueOf(j));
                    Thread.sleep(1000);

                    WebElement statusInput = wait
                            .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input#radB")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", statusInput);
                    Thread.sleep(3000);
                    log.info("captcha image path");
                    WebElement captchaImage = wait.until(
                            ExpectedConditions.presenceOfElementLocated(By.xpath("//img[@id=\"captcha_image\"]")));

                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH_mm_ss");
                    LocalDateTime now = LocalDateTime.now();
                    HelperClass.createDirectory(path1 + "/captcha");
                    log.info("saving captcha image...");
                    String captchaName = path1 + "/captcha/captcha" + j + "_" + dtf.format(now) + ".png";
                    captchaImage.getScreenshotAs(OutputType.FILE)
                            .renameTo(new File(captchaName));
                    log.info("calling tesseract to get captcha text");
                    String captchaText = TesseractUtil.toString(captchaName);
                    // AntiCaptcha.CreateTask(path1 + "/captcha/captcha" + j + "_" + dtf.format(now) + ".png");

                    log.info("entering captcha");
                    WebElement varcode = wait
                            .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input#captcha")));
                    varcode.clear();

                    for (char ch : captchaText.toCharArray()) {
                        varcode.sendKeys(Character.toString(ch));
                        Thread.sleep(300); // Mimic slow typing (adjust delay if needed)
                    }
                    Thread.sleep(1000);
                    WebElement goBtn = wait
                            .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input.Gobtn")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", goBtn);
                    // HelperClass.deleteDirectory(path1 + File.separator + "captcha");

                } catch (Exception e) {
                    log.info("Uh oh error while solving captcha");
                    e.printStackTrace();

                    // Reenter captcha after refresh
                    WebElement captchaRefresh = wait
                            .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img.refresh-btn")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", captchaRefresh);

                    WebElement captchaImage = wait.until(
                            ExpectedConditions.presenceOfElementLocated(By.xpath("//img[@id=\"captcha_image\"]")));
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH_mm_ss");
                    LocalDateTime now = LocalDateTime.now();
                    HelperClass.createDirectory(path1 + "/captcha");
                    String captchaName = path1 + "/captcha/captcha" + j + "_" + dtf.format(now) + ".png";
                    captchaImage.getScreenshotAs(OutputType.FILE)
                            .renameTo(new File(captchaName));
                    log.info("calling tesseract to get captcha text");
                    String captchaText = TesseractUtil.toString(captchaName);

                    WebElement varcode = wait
                            .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input#captcha")));
                    varcode.clear();
                    for (char ch : captchaText.toCharArray()) {
                        varcode.sendKeys(Character.toString(ch));
                        Thread.sleep(300); // Mimic slow typing (adjust delay if needed)
                    }

                    WebElement goBtn = wait
                            .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input.Gobtn")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", goBtn);
                    // HelperClass.deleteDirectory(path1 + File.separator + "captcha");
                }

                String check = "";
                try {
                    Thread.sleep(3000);
                    WebElement checkElement = driver.findElement(By.xpath("//*[@id=\"errSpan\"]"));
                    check = checkElement.getText();
                    log.info("ANY Error: {}", check);
                } catch (Exception e) {
                    // e.printStackTrace();
                    check = "";
                    log.info("error while finding error: {} ", e.getMessage());
                }
                log.info("Error Text: {}", check);
                boolean anyError = "invalid captcha".equals(check.toLowerCase())
                        || check.toLowerCase().contains("oops! session timeout..!!!")
                        || check.toLowerCase().contains("there is an error");
                if (anyError) {
                    log.info(" Error occoured: {}", check);
                    Thread.sleep(1000);
                    condition++;
                } else {
                    log.info("exiting captcha solving loop");
                    // condition = 8;
                    break;
                }
            }
            if (condition > 6) {
                log.info("unable to solve captcha for year: {}", year);
                sheet1.createRow(0).createCell(0).setCellValue("CAPTCHA ERROR on this year search: " + year);
                HelperClass.saveWorkbook(path1, "ERROR_" + year + ".xlsx", sheet1);
            }

            List<WebElement> rows;
            int No_of_records = 0;
            try {
                Thread.sleep(2000);
                log.info("finding records...");
                WebElement records = wait
                        .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[@id='dispTable']")));
                rows = records.findElements(By.xpath("//table[@id='dispTable']//tr"));
                No_of_records = rows.size();
                Thread.sleep(2000);
            } catch (Exception e) {
                log.info("no record found.");
                System.out.println("No data found..");
            }
            if (No_of_records >= 2) {
                for (int k = 2; k < No_of_records; k++) {

                    rowNum = sheet1.getLastRowNum() + 1;
                    boolean check = false;
                    try {
                        log.info("scrolling link");
                        WebElement Scroll_link = wait.until(ExpectedConditions.presenceOfElementLocated(
                                By.xpath("(//table[@id='dispTable'])//tr[" + k + "]//td[4]//a")));
                        check = true;
                    } catch (Exception e) {
                        check = false;
                    }

                    if (check) {
                        Row dataRow = sheet1.createRow(rowNum++);

                        String Sr_No = wait.until(ExpectedConditions
                                        .presenceOfElementLocated(By.xpath("(//table[@id='dispTable'])//tr[" + k + "]//td[1]")))
                                .getText();
                        log.info("Sr_No: {}", Sr_No);
                        String caseType = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//*[@id=\"dispTable\"]/tbody/tr[" + k + "]/td[2]"))).getText();
                        log.info("caseType: {}", caseType);
                        String petitionerName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//*[@id=\"dispTable\"]/tbody/tr[" + k + "]/td[3]"))).getText();
                        log.info("petitionerName: {}", petitionerName);

                        Cell srCell = dataRow.createCell(0);
                        srCell.setCellValue(Sr_No);

                        Cell caseTypeCell = dataRow.createCell(1);
                        caseTypeCell.setCellValue(caseType);

                        Cell petitionerCell = dataRow.createCell(2);
                        petitionerCell.setCellValue(petitionerName);

                        WebElement Scroll_link = wait.until(ExpectedConditions.presenceOfElementLocated(
                                By.xpath("(//table[@id='dispTable'])//tr[" + k + "]//td[4]//a")));
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", Scroll_link);
                        Thread.sleep(1000);

                        try {
                            log.info("disptable ...");
                            WebElement viewBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                                    By.xpath("(//table[@id='dispTable'])//tr[" + k + "]//td[4]//a")));
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", viewBtn);
                            Thread.sleep(3000);
                        } catch (Exception e) {
                            log.info("error while finding tabel with id dispTable");
                            System.out.println("Again click on view");
                            try {
                                log.info("finding close btn.");
                                WebElement button_2 = wait.until(ExpectedConditions
                                        .presenceOfElementLocated(By.xpath("(//button[@class='btn-close'])[1]")));
                                log.info("button_2: {}", button_2);
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button_2);
                            } catch (Exception ex1) {
                                log.info("error while closing btn, ERROR: {}", ex1);
                                // Handle exception if needed
                            }

                            try {
                                log.info("again trying to find close btn");
                                WebElement button_2 = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(
                                                By.xpath("(//button[@class='btn-close'])[1]")));
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button_2);
                            } catch (Exception ex2) {
                                log.info("error while closing btn, ERROR");
                                // Handle exception if needed
                            }

                            try {
                                log.info("dispTable opening...");
                                WebElement btn2 = wait.until(ExpectedConditions.presenceOfElementLocated(
                                        By.xpath("(//table[@id='dispTable'])//tr[" + k + "]//td[4]//a")));
                                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn2);
                                Thread.sleep(3000);
                            } catch (Exception ex3) {
                                log.info("error while closing btn, ERROR: ex3 {}", ex3);
                                // Handle exception if needed
                            }
                        }

                        // String Case_Type = "";
                        String Filing_Number = "";
                        String Filing_Date = "";
                        String Registration_Number = "";
                        String Registration_Date = "";
                        String CNR_Number = "";

                        boolean check_table_1 = true;
                        try {
                            log.info(" extracting data from: table 1");
                            WebElement Table_1 = wait.until(ExpectedConditions.presenceOfElementLocated(
                                    By.xpath("//*[@id=\"caseBusinessDiv4\"]/div/table")));
                            log.info("Table_1: {}", Table_1);
                        } catch (Exception e) {
                            log.info("error while extracting table 1 data");
                            check_table_1 = false;
                        }
                        log.info("did data found in the table: {}", check_table_1);
                        if (check_table_1) {

                            Thread.sleep(2000);

                            try {
                                Filing_Number = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(
                                                By.xpath("//*[@id=\"caseBusinessDiv4\"]/div/table/tbody/tr[1]/td[2]")))
                                        .getText();
                                log.info("Filing_Number: {}", Filing_Number);
                                Cell fn = dataRow.createCell(3);
                                fn.setCellValue(Filing_Number);
                            } catch (Exception e2) {
                                Filing_Number = "NA";
                                log.info("Error Filing_Number: {}", Filing_Number);
                            }

                            try {
                                Filing_Date = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(
                                                By.xpath("//*[@id=\"caseBusinessDiv4\"]/div/table/tbody/tr[1]/td[4]")))
                                        .getText();
                                log.info("Filing_Number: {}", Filing_Date);
                                Cell fd = dataRow.createCell(4);
                                fd.setCellValue(Filing_Date);

                            } catch (Exception e3) {
                                Filing_Date = "NA";
                                log.info("Error Filing_Number: {}", Filing_Date);
                            }

                            try {
                                Registration_Number = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                                                "//*[@id=\"caseBusinessDiv4\"]/div/table/tbody/tr[2]/td[2]/label")))
                                        .getText();
                                log.info("Registration_Number: {}", Registration_Number);
                                Cell regNum = dataRow.createCell(5);
                                regNum.setCellValue(Registration_Number);
                            } catch (Exception e4) {
                                Registration_Number = "NA";
                            }

                            try {
                                Registration_Date = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                                                "//*[@id=\"caseBusinessDiv4\"]/div/table/tbody/tr[2]/td[4]/label")))
                                        .getText();
                                log.info("Registration_Date: {}", Registration_Date);
                                Cell regDate = dataRow.createCell(6);
                                regDate.setCellValue(Registration_Date);

                            } catch (Exception e5) {
                                log.info("e5: {}", e5);
                                Registration_Date = "NA";
                            }

                            try {
                                CNR_Number = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                                                "//*[@id=\"caseBusinessDiv4\"]/div/table/tbody/tr[3]/td[2]/strong")))
                                        .getText();
                                log.info("CNR_Number: {}", CNR_Number);
                                Cell cnrNum = dataRow.createCell(7);
                                cnrNum.setCellValue(CNR_Number);

                            } catch (Exception e6) {
                                log.info("e6: {}", e6);
                                CNR_Number = "NA";
                            }

                        }

                        boolean checkTable2 = true;
                        WebElement table2;

                        log.info("searching on table 2");
                        try {
                            table2 = wait.until(ExpectedConditions
                                    .presenceOfElementLocated(By.xpath("//*[@id=\"caseBusinessDiv4\"]/table")));
                        } catch (Exception e) {
                            checkTable2 = false;
                        }

                        String firstHearingDate = "";
                        String nextHearingDate = "";
                        String stageOfCase = "";
                        String courtNumberAndJudge = "";
                        String benchType = "";
                        String judicialBranch = "";
                        String caseState = "";
                        String caseDistrict = "";
                        String notBeforeMe = "";

                        if (checkTable2) {
                            try {
                                firstHearingDate = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(
                                                By.xpath("//*[@id=\"caseBusinessDiv4\"]/table/tbody/tr[1]/td[2]")))
                                        .getText();
                                log.info("firstHearingDate: {}", firstHearingDate);

                                Cell firstHearing = dataRow.createCell(8);
                                firstHearing.setCellValue(firstHearingDate);
                            } catch (Exception e) {
                                firstHearingDate = "NA";
                            }

                            try {
                                nextHearingDate = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(
                                                By.xpath("//*[@id=\"caseBusinessDiv4\"]/table/tbody/tr[2]/td[2]")))
                                        .getText();
                                log.info("nextHearingDate: {}", nextHearingDate);
                                Cell nextHearing = dataRow.createCell(9);
                                nextHearing.setCellValue(nextHearingDate);
                            } catch (Exception e) {
                                nextHearingDate = "NA";
                            }

                            try {
                                stageOfCase = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(
                                                By.xpath("//*[@id=\"caseBusinessDiv4\"]/table/tbody/tr[3]/td[2]")))
                                        .getText();
                                log.info("stageOfCase: {}", stageOfCase);
                                Cell stageCase = dataRow.createCell(10);
                                stageCase.setCellValue(stageOfCase);
                            } catch (Exception e) {
                                stageOfCase = "NA";
                            }

                            try {
                                courtNumberAndJudge = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(
                                                By.xpath("//*[@id=\"caseBusinessDiv4\"]/table/tbody/tr[4]/td[2]")))
                                        .getText();
                                log.info("courtNumberAndJudge: {}", courtNumberAndJudge);

                                Cell courtNum = dataRow.createCell(11);
                                courtNum.setCellValue(courtNumberAndJudge);
                            } catch (Exception e) {
                                courtNumberAndJudge = "NA";
                            }

                            try {
                                benchType = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(
                                                By.xpath("//*[@id=\"caseBusinessDiv4\"]/table/tbody/tr[5]/td[2]")))
                                        .getText();
                                log.info("benchType: {}", benchType);
                                Cell benchTypeCell = dataRow.createCell(12);
                                benchTypeCell.setCellValue(benchType);
                            } catch (Exception e) {
                                benchType = "NA";
                            }
                            try {
                                judicialBranch = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(
                                                By.xpath("//*[@id=\"caseBusinessDiv4\"]/table/tbody/tr[6]/td[2]")))
                                        .getText();
                                log.info("judicialBranch: {}", judicialBranch);
                                Cell benchTypeCell = dataRow.createCell(13);
                                benchTypeCell.setCellValue(judicialBranch);
                            } catch (Exception e) {
                                judicialBranch = "NA";
                            }

                            try {
                                caseState = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(
                                                By.xpath("//*[@id=\"caseBusinessDiv4\"]/table/tbody/tr[7]/td[2]")))
                                        .getText();
                                log.info("caseState: {}", caseState);
                                Cell caseStateCell = dataRow.createCell(14);
                                caseStateCell.setCellValue(caseState);
                            } catch (Exception e) {
                                caseState = "NA";
                            }

                            try {
                                caseDistrict = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(
                                                By.xpath("//*[@id=\"caseBusinessDiv4\"]/table/tbody/tr[8]/td[2]")))
                                        .getText();
                                log.info("caseDistrict: {}", caseDistrict);
                                Cell caseDis = dataRow.createCell(15);
                                caseDis.setCellValue(caseDistrict);

                            } catch (Exception e) {
                                caseDistrict = "NA";
                            }

                            try {
                                notBeforeMe = wait
                                        .until(ExpectedConditions.presenceOfElementLocated(
                                                By.xpath("//*[@id=\"caseBusinessDiv4\"]/table/tbody/tr[9]/td[2]")))
                                        .getText();
                                log.info("notBeforeMe: {}", notBeforeMe);
                                Cell notBefore = dataRow.createCell(16);
                                notBefore.setCellValue(notBeforeMe);

                            } catch (Exception e) {
                                notBeforeMe = "NA";
                            }
                        }

                        boolean checkTable3 = true;
                        WebElement table3;

                        log.info("searching table 3");
                        try {
                            table3 = wait.until(ExpectedConditions.presenceOfElementLocated(
                                    By.xpath("//*[@id=\"caseHistoryDiv\"]/div[2]/div[2]/h2[1]")));
                            if (table3.getText().equals("Petitioner and Advocate")) {
                                checkTable3 = true;
                            } else {
                                checkTable3 = false;
                            }

                        } catch (Exception e) {
                            checkTable3 = false;
                        }

                        List<String> petitioner = new ArrayList<>();

                        if (checkTable3) {
                            try {
                                WebElement petitionerAndAdvocateText = wait
                                        .until(ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//*[@id=\"caseHistoryDiv\"]/div[2]/div[2]/span[1]")));
                                // petitioner.add(petitionerAndAdvocateText.getText());
                                Cell petitionerAndAdvocate = dataRow.createCell(17);
                                petitionerAndAdvocate.setCellValue(petitionerAndAdvocateText.getText());
                            } catch (Exception e) {
                                petitioner.add("Not Found");
                            }
                        }

                        log.info("seaching tabel 4");

                        boolean checkTable4 = true;
                        WebElement table4;

                        try {
                            table4 = wait.until(ExpectedConditions.presenceOfElementLocated(
                                    By.xpath("//*[@id=\"caseHistoryDiv\"]/div[2]/div[2]/h2[2]")));
                            if (table4.getText().equals("Respondent and Advocate")) {
                                checkTable4 = true;
                            } else {
                                checkTable4 = false;
                            }
                        } catch (Exception e) {
                            checkTable4 = false;
                        }

                        List<String> respondent = new ArrayList<>();

                        if (checkTable4) {
                            try {
                                WebElement respondentAndAdvocateText = wait
                                        .until(ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//*[@id=\"caseHistoryDiv\"]/div[2]/div[2]/span[2]")));
                                respondent.add(respondentAndAdvocateText.getText());
                                Cell respondentAndAdvocate = dataRow.createCell(18);
                                respondentAndAdvocate.setCellValue(respondentAndAdvocateText.getText());
                            } catch (Exception e) {
                                respondent.add("Not Found");
                            }
                        }

                        // Category Details
                        boolean checkCetegoryData = false;
                        String categoryDetailsData = "";
                        log.info("category details");
                        try {
                            WebElement categoryDetails = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                    By.xpath("//*[@id=\"caseHistoryDiv\"]/div[2]/div[2]/div/h2")));
                            if (categoryDetails.getText().equals("Category Details")) {
                                checkCetegoryData = true;
                            }
                        } catch (Exception e) {
                            System.out.println("No category details");
                        }

                        if (checkCetegoryData) {
                            WebElement categoryDatatable = wait.until(ExpectedConditions
                                    .visibilityOfElementLocated(By.xpath("//*[@id=\"subject_table\"]")));
                            String catData = categoryDatatable.getText();

                            Cell catDataCell = dataRow.createCell(19);
                            catDataCell.setCellValue(catData);
                        }

                        // History Table
                        try {
                            log.info("historyTable");
                            WebElement historyTable = wait.until(ExpectedConditions
                                    .visibilityOfElementLocated(By.cssSelector("table.history_table")));
                            WebElement historyTBody = historyTable.findElement(By.tagName("tbody"));
                            List<WebElement> historyRow = historyTBody.findElements(By.tagName("tr"));
                            for (int jj = 1; jj < historyRow.size(); jj++) {

                                List<WebElement> tds = historyRow.get(jj).findElements(By.tagName("td"));

                                if (jj == 1) {
                                    for (int kk = 0; kk < tds.size(); kk++) {
                                        Cell newCell = dataRow.createCell((20 + kk));
                                        newCell.setCellValue(tds.get(kk).getText());

                                        if (kk == 2) {
                                            WebElement link = tds.get(kk).findElement(By.tagName("a"));
                                            ((JavascriptExecutor) driver)
                                                    .executeScript("arguments[0].scrollIntoView();", link);
                                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
                                            Thread.sleep(2000);
                                            try {
                                                String bodyContent = wait.until(ExpectedConditions
                                                                .presenceOfElementLocated(By.xpath("//*[@id=\"mydiv\"]")))
                                                        .getText();
                                                // ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,
                                                // document.body.scrollHeight);");
                                                Cell extraDetailsCell = dataRow.createCell(25);
                                                extraDetailsCell.setCellValue(bodyContent);

                                            } catch (Exception e) {
                                                System.out.println("No data inside order");
                                            }
                                            WebElement backButton = wait.until(ExpectedConditions
                                                    .elementToBeClickable(By.cssSelector("input#bckbtn")));
                                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                                                    backButton);
                                        }
                                    }
                                } else {
                                    // Row newRow = sheet1.createRow((rowNum + jj));
                                    Row newRow = sheet1.createRow((sheet1.getLastRowNum() + 1));
                                    for (int kk = 0; kk < tds.size(); kk++) {
                                        Cell newDataCell = newRow.createCell((20 + kk));
                                        newDataCell.setCellValue(tds.get(kk).getText());

                                        if (kk == 2) {
                                            WebElement link = tds.get(kk).findElement(By.tagName("a"));
                                            ((JavascriptExecutor) driver)
                                                    .executeScript("arguments[0].scrollIntoView();", link);
                                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
                                            Thread.sleep(2000);
                                            try {
                                                String bodyContent = wait.until(ExpectedConditions
                                                                .presenceOfElementLocated(By.xpath("//*[@id=\"mydiv\"]")))
                                                        .getText();
                                                // ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,
                                                // document.body.scrollHeight);");
                                                Cell extraDetailsCell = newRow.createCell(25);
                                                extraDetailsCell.setCellValue(bodyContent);
                                            } catch (Exception e) {
                                                System.out.println("No data insdie order");
                                            }
                                            WebElement backButton = wait.until(ExpectedConditions
                                                    .elementToBeClickable(By.cssSelector("input#bckbtn")));
                                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                                                    backButton);
                                        }
                                    }
                                }

                            }

                        } catch (Exception e) {
                            System.out.println("No history data");
                        }

                        // Orders Details
                        try {
                            log.info("orderTable");
                            WebElement orderTable = wait.until(
                                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.order_table")));
                            WebElement tbody = orderTable.findElement(By.tagName("tbody"));
                            List<WebElement> trs = tbody.findElements(By.tagName("tr"));
                            for (int q = 1; q < trs.size(); q++) {
                                List<WebElement> tds = trs.get(q).findElements(By.tagName("td"));

                                if (q == 1) {
                                    for (int r = 0; r < tds.size(); r++) {
                                        if (r == 4) {
                                            String docLink = tds.get(r).findElement(By.tagName("a"))
                                                    .getAttribute("href");
                                            Cell linkcell = dataRow.createCell(30);
                                            linkcell.setCellValue(docLink);
                                            // linkcell.setHyperlink(HelperClass.addClickableLink(docLink, workbook));
                                        } else {
                                            Cell orderCell = dataRow.createCell((26 + r));
                                            orderCell.setCellValue(tds.get(r).getText());
                                        }

                                    }
                                } else {
                                    Row newRow = sheet1.getRow(rowNum);
                                    if (newRow == null) {
                                        newRow = sheet1.createRow(rowNum++); // row did NOT exist
                                    } else {
                                        rowNum++;
                                    }

                                    for (int r = 0; r < tds.size(); r++) {
                                        if (r == 4) {
                                            String docLink = tds.get(r).findElement(By.tagName("a"))
                                                    .getAttribute("href");
                                            Cell linkcell = newRow.createCell(30);
                                            linkcell.setCellValue(docLink);
                                            // linkcell.setHyperlink(HelperClass.addClickableLink(docLink, workbook));
                                        } else {
                                            Cell orderCell = newRow.createCell((26 + r));
                                            orderCell.setCellValue(tds.get(r).getText());
                                        }

                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("No order details");
                        }

                        try {
                            log.info("back button");
                            WebElement button = wait.until(ExpectedConditions
                                    .presenceOfElementLocated(
                                            By.xpath("//*[@id=\"bckbtn\"]")));
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                                    button);
                            Thread.sleep(3000);
                        } catch (Exception e) {
                            log.info("trying again back btn: ");
                            WebElement button_1 = wait
                                    .until(ExpectedConditions.presenceOfElementLocated(
                                            By.xpath("//*[@id=\"bckbtn\"]")));
                            ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                                    button_1);
                            Thread.sleep(3000);
                            System.out.println("error");
                        }
                    } else {
                        System.out.println("No proper table");
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < 31; i++) {
            sheet1.autoSizeColumn(i);
        }
        if (sheet1.getLastRowNum() > 0) {
            HelperClass.saveWorkbook(path1, targetName + year + ".xlsx", sheet1);
        }
    }
}
