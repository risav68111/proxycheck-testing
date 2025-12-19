// package com.fios.fiosspringbootapi.service.scripts;
package proxytest.sel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import com.fios.fiosspringbootapi.utlis.AntiCaptcha;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;

public class DistrictECourtRunnable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DistrictECourtRunnable.class);
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
    public String district;
    public String courtComplex;
    public String estCode;
    public String directoryPath;
    public int threadNumber;
    XSSFWorkbook workbook;
    ArrayList<Integer> checks;
    String workbookName;
    public boolean isWorkbookSave = false;

    public DistrictECourtRunnable(String caseId, String service, String targetName, String search_date,
            String searchedBy, String requestId, String serviceId, String year, String state, String district,
            String courtComplex, String estCode, ArrayList<Integer> checks, String workbookName) {
        this.targetName = targetName;
        this.searchedBy = searchedBy;
        this.caseId = caseId;
        this.service = service;
        this.search_date = search_date;
        this.requestId = requestId;
        this.serviceId = serviceId;
        this.folderName = caseId + "_" + service + "_" + "DistrictECourt" + "_" + "DistrictECourt" + "_" + targetName
                + "_" + search_date;
        this.BASE_DIRECTORY = currentDir + File.separator + this.folderName;
        this.year = year;
        this.state = state;
        this.district = district;
        this.courtComplex = courtComplex;
        this.estCode = estCode;
        this.checks = checks;
        this.workbookName = workbookName;
    }

    @Override
    public void run() {
        try {
            ecourt();
            // saveWorkBook();
        } catch (Exception ex) {
            log.info("error occoured.......^~_~^");
            XSSFSheet sheet = workbook.getSheet("sheet0");
            Cell cell = sheet.createRow(sheet.getLastRowNum() + 2).createCell(0);
            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.RED.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cell.setCellStyle(style);
            cell.setCellValue("ERROR OCCOURED");
            isWorkbookSave = true;
            ex.printStackTrace();
            // throw new RuntimeException();
        } finally {
            saveWorkBook();
        }
    }

    private void ecourt() throws Exception {
        String path = currentDir + "/" + folderName;
        log.info("ON THREAD>>>>>");
        log.info("estCode: {}", estCode);
        log.info("courtComplex: {}", courtComplex);
        log.info("district: {}", district);
        log.info("state: {}", state);
        log.info("year: {}", year);
        log.info("serviceId: {}", serviceId);
        log.info("requestId: {}", requestId);
        log.info("searchedBy: {}", searchedBy);
        log.info("search_date: {}", search_date);
        log.info("targetName: {}", targetName);
        log.info("service: {}", service);
        log.info("caseId: {}", caseId);

        workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sheet0");
        // ProxyVar p = null;
        ProxyVar p = new ProxyVar();
        Browser browser = null;
        try {
            // p = ProxyChecker.getNextEligibleProxy();
            browser = HelperClass.launchChromiumBrowser(p);
            // Browser browser ;

            // browser = HelperClass.launchChromiumBrowser(p);

            String[] sheetDetailsContent =  new String[]{ "Target Name", "Year", "State", "District", "Court Complex", "Court Establisment Code" };
            Row sheetHeader = sheet.createRow(0);
            for (int j = 0; j < sheetDetailsContent.length; j++) {
                Cell hCell = sheetHeader.createCell(j);
                hCell.setCellValue(sheetDetailsContent[j]);
            }

            String[] sheetDetails =  new String[]{ targetName, year, state, district, courtComplex, estCode };
            Row sheetDetailsRow = sheet.createRow(1);
            for (int j = 0; j < sheetDetails.length; j++) {
                Cell hCell = sheetDetailsRow.createCell(j);
                hCell.setCellValue(sheetDetails[j]);
            }

            String[] headings = new String[] { "Sr No.", "Case Type/Case Number/Case Year",
                    "Petitioner Name versus Respondent Name", "Case Type", "Registration Number", "CNR Number",
                    "Filing Date", "Registration Date", "Next Hearing Date", "Date of Disposal",
                    "Nature of Disposal", "Petitioner", "Respondent", "Act and Section",
                    "Orders/Judgements S No.", "Orders/Judgements Date", "Orders/Judgements Details" };
            Row headerRow = sheet.createRow(0);
            for (int j = 0; j < headings.length; j++) {
                Cell hCell = headerRow.createCell(j);
                hCell.setCellValue(headings[j]);
                // hCell.setCellStyle(HelperClass.addHeaderStyle(workbook));
            }

            int rowNum = 1, foundCount = 0;

            Page page = browser.newPage();
            page.navigate("https://services.ecourts.gov.in");

            Locator leftPaneMenuCSBtn = page.locator("#leftPaneMenuCS");
            leftPaneMenuCSBtn.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE));
            page.waitForLoadState(LoadState.LOAD);
            page.reload();

            // Open left pane
            leftPaneMenuCSBtn = page.locator("#leftPaneMenuCS");
            leftPaneMenuCSBtn.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE));
            leftPaneMenuCSBtn.click();
            log.info("leftPaneMenuCSBtn: {}");

            // Wait for close button
            Locator btnClose = page.locator("button.btn-close").first();
            btnClose.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE));
            log.info("btnClose: {}", btnClose.textContent());
            btnClose.click();
            log.info("btnClose");

            // State dropdown
            Locator stateDropdown = page.locator("#sess_state_code");
            stateDropdown.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE));
            stateDropdown.selectOption(new SelectOption().setLabel(state));
            log.info("stateDropdown");

            // District dropdown — wait until populated
            Locator districtDropdown = page.locator("#sess_dist_code");
            page.waitForFunction(
                    "el => el && el.options.length > 1",
                    districtDropdown.elementHandle());
            districtDropdown.selectOption(new SelectOption().setLabel(district));
            log.info("districtDropdown");

            // Court complex dropdown — enabled + populated
            Locator courtComplexDropdown = page.locator("#court_complex_code");
            log.info("courtComplexDropdown");
            page.waitForFunction(
                    "el => el && !el.disabled && el.options.length > 1",
                    courtComplexDropdown.elementHandle());
            courtComplexDropdown.selectOption(new SelectOption().setLabel(courtComplex));

            // Optional establishment code
            if (estCode != null) {
                Locator estCodes = page.locator("div#est_codes select");
                log.info("estCodes");
                page.waitForFunction(
                        "el => el && el.options.length > 1",
                        estCodes.elementHandle());
                estCodes.selectOption(new SelectOption().setLabel(estCode));
            }

            int retries = 0;
            while (retries++ < 6) {
                try {
                    log.info("Attempt {}", retries);

                    Locator petresNameInput = page.locator("#petres_name");
                    petresNameInput.waitFor();
                    petresNameInput.fill(targetName);

                    Locator rgyearPInput = page.locator("input[name='rgyearP']");
                    rgyearPInput.waitFor();
                    rgyearPInput.fill(year);

                    page.locator("input[value='Both']").first().click();

                    Locator captchaImage = page.locator("#captcha_image");
                    captchaImage.waitFor();

                    String captchaFileName = String.format(
                            "%s/captcha/captcha_%s_%s.png",
                            path, year, LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH_mm_ss")));

                    HelperClass.createDirectory(path + "/captcha");
                    captchaImage.screenshot(
                            new Locator.ScreenshotOptions()
                                    .setPath(Paths.get(captchaFileName)));

                    String captchaText = TesseractUtil.toString(captchaFileName);
                    // System.out.print("INPT::");
                    // captchaText = (new Scanner(System.in)).nextLine();
                    log.info("captchaText: {}", captchaText);

                    Locator captchaInput = page.locator("input[name='fcaptcha_code']");
                    captchaInput.fill(captchaText);

                    page.locator("#frmsearch_name button").click();
                    log.info("Submitted form");

                    // Locator resultTable = page.locator("table#dispTable");
                    // log.info("resultTable: {}", resultTable.textContent());
                    // Locator noDataMsg = page.locator("#nodata");
                    // log.info("noDataMsg: {}", noDataMsg.textContent());
                    // Locator errorMsg = page.locator(".loader-txt div");
                    // log.info("errorMsg: {}", errorMsg.textContent());
                    try {
                        page.waitForSelector(
                                "table#dispTable, #nodata, .loader-txt div",
                                new Page.WaitForSelectorOptions().setTimeout(5000));
                    } catch (TimeoutError e) {
                        throw new RuntimeException("No response from server");
                    }
                    page.waitForFunction("""
                                () => {
                                    if (document.querySelector("table#dispTable")) return "TABLE";
                                    if (document.querySelector("#nodata")) return "NO_DATA";
                                    if (document.querySelector(".loader-txt div")) return "LOADER";
                                    return null;
                                }
                            """);

                    String result = (String) page.evaluate("""
                                () => {
                                    if (document.querySelector("table#dispTable")) return "TABLE";
                                    if (document.querySelector("#nodata")) return "NO_DATA";
                                    if (document.querySelector(".loader-txt div")) return "LOADER";
                                    return null;
                                }
                            """);

                    if (!result.equals("LOADER")) {
                        break;
                    }
                    // errorMsg.waitFor(new Locator.WaitForOptions().setTimeout(3000));
                    // log.info("new");

                    // Locator data = resultTable.or(noDataMsg);
                    // data.waitFor(new Locator.WaitForOptions().setTimeout(3000));
                    // log.info("waitFor");

                    Locator loaderText = page.locator(".loader-txt div").first();
                    log.info("loaderText");
                    // loaderText.waitFor(new Locator.WaitForOptions()
                    // .setTimeout(2000));
                    //
                    String message = loaderText.textContent();
                    log.info("Server response: {}", message);

                    boolean retry = "Invalid Captcha...".equalsIgnoreCase(message) ||
                            "Oops! Session timeout..!!!".equalsIgnoreCase(message) ||
                            "Server is under maintenance...".equalsIgnoreCase(message);

                    if (!retry) {
                        break; // SUCCESS
                    }

                    handleRetry(page);

                } catch (Exception e) {
                    log.warn("Retry due to exception: {}", e.getMessage());
                    handleRetry(page);
                }
            }

            List<Locator> rows = new ArrayList<>();
            int No_of_records = 0;
            try {
                // Locator records = page.locator("//table[@id='dispTable']");
                rows = page.locator("table#dispTable tbody tr").all();
                No_of_records = rows.size();
                log.info("No_of_records: {}", No_of_records);
                Thread.sleep(2000);
            } catch (Exception e) {
                List<Locator> noData = page.locator("#nodata").all();
                if (!noData.isEmpty()) {
                    log.info("No data found Text: xoxoxoxoxoxoxoxoxoxoxoxoxoxoxoxox \n{} : ",
                            noData.get(0).textContent());
                }
                log.info("NO result table found.");
                // e.printStackTrace();
            }

            for (int k = 1; k < No_of_records; k++) {
                // try {
                // Locator scroll_link = page.locator("table#dispTable
                // tr").nth(k-1).locator("td:nth-child(4) a");
                // Thread.sleep(1000);
                // } catch (Exception e) {
                // log.info("no view button found....??");
                // continue;
                // // e.printStackTrace();
                // }

                rows = page.locator("table#dispTable tbody tr").all();
                Locator row = rows.get(k);

                String sr_No = row.locator("td").nth(0).textContent().trim();
                String caseTypeNumberYear = row.locator("td").nth(1).textContent().trim();
                String petitonerRespondent = row.locator("td").nth(2).textContent().trim();
                log.info("Sr_No: {}", sr_No);
                log.info("case_Type_Number_Year: {}", caseTypeNumberYear);
                log.info("Petioner_Name_And_Respondent_Name: {}", petitonerRespondent);
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(sr_No);
                dataRow.createCell(1).setCellValue(caseTypeNumberYear);
                dataRow.createCell(2).setCellValue(petitonerRespondent);
                try {
                    Locator viewBtn = row.locator("a");
                    viewBtn.click();
                    log.info("viewBtn: {}", viewBtn.textContent());

                    Thread.sleep(3000);
                } catch (Exception e) {
                    log.info("Again click on view");
                    try {
                        Locator button_2 = page.locator("button.btn-close");
                        button_2.click();
                        // ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                        // button_2);
                        Thread.sleep(3000);
                    } catch (Exception ex1) {
                        anyPopUpOccured(page);
                    }
                    continue;
                }

                String caseType = "";
                String filingDate = "";
                String registrationNumber = "";
                String registrationDate = "";
                String cnrNumber = "";
                String nextHearingDate = "";
                String dateOfDisposal = "";
                String natureOfDisposal = "";
                String petitioner = "";
                String respondent = "";
                String actAndSection = "";

                try {
                    Locator csPartyName = page.locator("#CSpartyName");

                    List<Locator> caseDetailsTable = csPartyName.locator("table.case_details_table").all();

                    if (caseDetailsTable.isEmpty()) {
                        log.info("case details table not found...");
                        caseDetailsTable.get(0).locator("tbody").isVisible();
                        Thread.sleep(3000);

                        log.info("reloading the table again");
                        caseDetailsTable = csPartyName.locator("table.case_details_table").all();
                        Thread.sleep(3000);
                    }

                    if (!caseDetailsTable.isEmpty()) {
                        List<Locator> trs = caseDetailsTable.get(0).locator("tr").all();

                        // tr for each content position vary
                        for (Locator tr : trs) {
                            String data = extractContent("case type", tr);
                            caseType = data.isBlank() ? caseType : data;
                            data = extractContent("filing date", tr);
                            filingDate = data.isBlank() ? filingDate : data;
                            data = extractContent("registration number", tr);
                            registrationNumber = data.isBlank() ? registrationNumber : data;
                            data = extractContent("registration date", tr);
                            registrationDate = data.isBlank() ? registrationDate : data;
                            data = extractContent("cnr number", tr);
                            cnrNumber = data.isBlank() ? cnrNumber : data.split("\\s+")[0];
                        }
                    }

                    List<Locator> caseStatusTable = page.locator("table.case_status_table").all();
                    if (!caseStatusTable.isEmpty()) {
                        for (Locator tr : caseStatusTable.get(0).locator("tr").all()) {
                            String data = extractContent("next hearing date", tr);
                            nextHearingDate = data.isBlank() ? nextHearingDate : data;
                            data = extractContent("decision date", tr);
                            dateOfDisposal = data.isBlank() ? dateOfDisposal : data;
                            data = extractContent("nature of disposal", tr);
                            natureOfDisposal = data.isBlank() ? natureOfDisposal : data;
                        }
                    }

                    List<Locator> actsTable = csPartyName.locator("table.acts_table").all();
                    actAndSection = actsTable.isEmpty() ? "N/A" : actsTable.get(0).textContent();

                    List<Locator> petitionerTable = csPartyName.locator("table.Petitioner_Advocate_table").all();
                    petitioner = petitionerTable.isEmpty() ? "N/A" : petitionerTable.get(0).textContent().trim();
                    List<Locator> respondentTable = csPartyName.locator("table.Respondent_Advocate_table").all();
                    respondent = respondentTable.isEmpty() ? "N/A" : respondentTable.get(0).textContent().trim();
                    log.info("petitioner: {}", petitioner);
                    log.info("respondent: {}", respondent);

                    log.info("caseType: {}", caseType);
                    log.info("filingDate: {}", filingDate);
                    log.info("registrationNumber: {}", registrationNumber);
                    log.info("registrationDate: {}", registrationDate);
                    log.info("cnrNumber: {}", cnrNumber);
                    log.info("nextHearingDate: {}", nextHearingDate);
                    log.info("dateOfDisposal: {}", dateOfDisposal);
                    log.info("natureOfDisposal: {}", natureOfDisposal);
                    log.info("actAndSection: {}", actAndSection);

                } catch (Exception e) {
                    log.warn("some error occourd during extracting content");
                    e.printStackTrace();
                }
                String[] contents = { caseType, filingDate, registrationNumber, registrationDate, cnrNumber,
                        nextHearingDate, dateOfDisposal, natureOfDisposal, petitioner, respondent, actAndSection };
                log.info("updating sheet");
                for (int cn = 0; cn < contents.length; cn++) {
                    dataRow.createCell(cn + 3).setCellValue(contents[cn]);
                }

                try {
                    Locator mainBackParty = page.locator("button#main_back_party");
                    mainBackParty.click();
                    Thread.sleep(3000);
                    // ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                    // mainBackParty);
                } catch (Exception e) {
                    anyPopUpOccured(page);
                }
                foundCount++;
            }

            if (foundCount > 0) {
                isWorkbookSave = true;
                checks.add(foundCount);
            }
        } catch (Exception e1) {
            // anyPopUpOccured(page);
            // try {
            // Locator bodyContent = page.locator("body");
            // if (bodyContent.getText().contains("Welcome User Search Page not Found
            // here")) {
            // driver.navigate().refresh();
            // }
            // Locator errorOccoureElement = wait
            // .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#validateError")));
            // String errorText = errorOccoureElement.getText();
            // if (errorText.contains("Welcome User Search Page not Found here"))
            // driver.navigate().refresh();
            // } catch (Exception e) {
            // // e.printStackTrace();
            // driver.navigate().refresh();
            // Thread.sleep(2000);
            // }
            log.info("_________________");
            e1.printStackTrace();
        } finally {
            try {
                if (p != null && p.getId() != null) {
                    ProxyChecker.decreaseProxyCount(p.getId());
                }
            } catch (Exception ex) {
                log.info("Proxy Issue");
                ex.printStackTrace();
            }

            try {
                if (browser != null) {
                    browser.close();
                }
            } catch (Exception ex) {
                log.info("browser Issue");
                ex.printStackTrace();
            }
        }
        // } catch (Exception exp) {
        // log.info("UnExpected ERROR: ");
        // exp.printStackTrace();
        // throw new RuntimeException(exp);
        // }
    }

    public int saveTableDataInExcelSheet(XSSFSheet sheet, Page page, Locator tableLocator,
            int mainRowNum) {
        String tableHeading = Optional.ofNullable(tableLocator.getAttribute("id")).filter(id -> !id.isBlank())
                .orElse(Optional.ofNullable(tableLocator.getAttribute("class")).filter(c -> !c.isBlank())
                        .orElse("new Table"));
        if (tableHeading.contains("heading"))
            return mainRowNum;

        String[] parts = tableHeading.split("\\s+");
        // Arrays.stream(parts).forEach(e -> log.info(e));
        for (String part : parts) {
            if (part.contains("_"))
                tableHeading = part.replace("_", " ");
        }
        log.info("Table Heading: {}", tableHeading);
        Cell cell = sheet.createRow(mainRowNum++).createCell(0);
        cell.setCellValue(tableHeading);
        Font boldFont = sheet.getWorkbook().createFont();
        boldFont.setBold(true);
        CellStyle style = sheet.getWorkbook().createCellStyle();
        style.setFont(boldFont);
        cell.setCellStyle(style);

        int rowNum = mainRowNum;
        for (Locator tr : tableLocator.locator("tr").all()) {
            if (tr.textContent().isBlank())
                continue;
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                row = sheet.createRow(rowNum);
            }
            rowNum++;
            int cellNum = 1;
            log.info("header================x");
            for (Locator td : tr.locator("td").all()) {
                // log.info("table body txt: {}", td.getText());
                Cell cellB = row.createCell(cellNum++);
                cellB.setCellValue(td.textContent());
            }
            List<Locator> aTags = tr.locator("a").all();
            if (aTags.isEmpty())
                continue;
            try {
                log.info("A Tag Found...");
                for (Locator aTag : aTags) {
                    String getText = aTag.getAttribute("onClick");
                    log.info("onClick attribute of the a tag: {}", getText);
                    if (getText.contains("display_cas, cssSelectore_acknowlegement") || getText.isBlank())
                        continue;
                    if (getText.contains("viewBusiness")) {
                        aTag.click();
                        // ((JavascriptExecutor) driver).executeScript("arguments[0].click();", aTag);
                        Thread.sleep(3000);
                        Locator contentDiv = page.locator("div#mydiv");
                        log.info("contentDiv found");
                        row.createCell(cellNum++).setCellValue(contentDiv.textContent());

                        anyPopUpOccured(page);
                        Thread.sleep(3000);
                    }
                    // //Downloading PDF Files was Throwing error
                    // else if(getText.contains("displayPdf")) {
                    // log.info("opening to view pdf");
                    // ((JavascriptExecutor) driver).executeScript("arguments[0].click();", aTag);
                    // Thread.sleep(3000);
                    // Locator modalOrders =
                    // wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#modalOders")));
                    // String url =
                    // modalOrders.findElement(By.tagName("object")).getAttribute("data");
                    // log.info("URL: " + url);
                    // String originalTab = driver.getWindowHandle();
                    // driver.switchTo().newWindow(WindowType.TAB);
                    // driver.get(url);
                    // Thread.sleep(5000);
                    // if (driver.getWindowHandles().size() > 1) {
                    // driver.close();
                    // }
                    // driver.switchTo().window(originalTab);
                    // // Locator closeModalBtn =
                    // modalOrders.findElement(By.cssSelector("button.btn-close"));
                    // // ((JavascriptExecutor) driver).executeScript("arguments[0].click();",
                    // closeModalBtn);
                    // anyPopUpOccured(page);
                    // continue;
                    // }
                }
            } catch (Exception e) {
                // anyPopUpOccured(page);
                log.info("error occoured for error.");
                e.printStackTrace();
            }
        }
        return rowNum;
    }

    public void anyPopUpOccured(Page page) {
        String[] selectors = {
                "#caseBusinessDiv_back",
                "button.btn-close",
                "#main_back_party"
        };

        for (String selector : selectors) {
            try {
                Locator blocker = page.locator(selector).first();

                if (blocker.isVisible()) {
                    blocker.click();
                    page.waitForTimeout(2000); // short UI settle, not sleep
                    log.info("Closed popup using selector: {}", selector);
                }
            } catch (Exception ignored) {
                // intentionally ignored
            }
        }
    }

    public String extractContent(String targetWord, Locator tableRow) {
        List<Locator> tds = tableRow.locator("td").all();
        String result = "";
        for (int i = 0; i < tds.size() - 1; i++) {
            Locator td = tds.get(i);
            // log.info("content be compared b/w : {} : {}", targetWord,
            // td.getAttribute("textContent"));
            if (td.textContent().toLowerCase().contains(targetWord)) {
                result = tds.get(i + 1).textContent();
                log.info("content found for: {}", tds.get(i).textContent());
                log.info("content : {}", result);
                break;
            }
        }
        return result;
    }

    public void saveWorkBook() {
        if (!isWorkbookSave)
            return;
        try {
            String path = currentDir + "/" + folderName;
            String filePath = path + "/" + workbookName;

            // XSSFSheet sheet = workbook.getSheet("sheet0");
            // sheet.autoSizeColumn(sheet.getRow(0).getLastCellNum() + 1);
            FileOutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            outputStream.close();

            log.info("Workbook saved successfully to: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRetry(Page page) {

        String[] selectors = {
                "#caseBusinessDiv_back",
                "button.btn-close",
                "#main_back_party"
        };

        for (String selector : selectors) {
            try {
                Locator blocker = page.locator(selector).first();

                if (blocker.isVisible()) {
                    blocker.click();
                    page.waitForTimeout(2000); // short UI settle, not sleep
                    log.info("Closed popup using selector: {}", selector);
                }
            } catch (Exception ignored) {
                // intentionally ignored
            }
        }
    }

}
