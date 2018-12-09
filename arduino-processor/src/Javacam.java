import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Javacam {
    private static Javacam INSTANCE = null;
    private static Runtime imgCapture;
    private static BufferedImage image;
    private static String cmd = "fswebcam --no-timestamp --no-underlay --no-banner -S 20 image.jpg -r 1028x768";
    private static String filePath = "/home/pi/image.jpg";

    private Javacam(){imgCapture = Runtime.getRuntime();}

    public static Javacam getJavacam(){
        if (INSTANCE == null){
            INSTANCE = new Javacam();
        }
        return INSTANCE;
    }

    public BufferedImage getImage(){
        try{
            imgCapture.exec(cmd);
            File file = new File(filePath);
            image = ImageIO.read(file);
        } catch (IOException e){
            e.printStackTrace();
        }
        return image;
    }
}
