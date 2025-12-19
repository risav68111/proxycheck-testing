package proxytest.sel;

import java.io.File;
import net.sourceforge.tess4j.*;

public class TesseractUtil {

    public static String toString(String imageDir) {
        File imageFile = new File(imageDir);
        System.out.println("Image file path: " + imageFile);
        ITesseract instance = new Tesseract();
        // ITesseract instance = new Tesseract1();
        instance.setDatapath("/usr/share/tesseract-ocr/5/tessdata"); // path to tessdata directory

        // "/usr/share/tesseract-ocr/5/tessdata"

        try {
            String result = instance.doOCR(imageFile);
            System.out.println("Result: " + result);
            return result.trim();
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
        return "";
    }
}

