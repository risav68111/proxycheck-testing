package proxytest.sel;

import java.io.File;
import net.sourceforge.tess4j.*;

public class Tes {

    public static void run() {
        // Tesseract tesseract = new Tesseract();
        //
        // // Path to tessdata directory
        // tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata/");
        // tesseract.setLanguage("eng");

        // try {
        // String currdir = System.getProperty("user.dir");

        // Tesseract t = new Tesseract();
        // t.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
        // t.setLanguage("eng");
        // t.setOcrEngineMode(1);
        // t.setPageSegMode(6);
        //
        // System.out.println("res: "+t.doOCR(new File(currdir + "/img/img1.png")));

        // File img = new File("img/img1.png");
        //
        // if (!img.exists()) {
        // throw new RuntimeException("Image not found: " + img.getAbsolutePath());
        // }
        // System.out.println("image: " + img);
        // // File image = new File(file);
        // String result = tesseract.doOCR(img);
        // System.out.println("Result: " + result);
        // } catch (TesseractException e) {
        // e.printStackTrace();
        // }

        File imageFile = new File("img/securimage_show.png");
        System.out.println("imge file path: "+ imageFile );
        ITesseract instance = new Tesseract(); // JNA Interface Mapping
        // ITesseract instance = new Tesseract1(); // JNA Direct Mapping
        instance.setDatapath("/usr/share/tesseract-ocr/5/tessdata"); // path to tessdata directory

        try {
            String result = instance.doOCR(imageFile);
            System.out.println("Result: "+ result);
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
    }

}
