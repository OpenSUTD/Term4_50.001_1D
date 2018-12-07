import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Javacam {
    private static Javacam INSTANCE = null;
    private static Webcam piCam;
    private static BufferedImage image;

    private Javacam(){piCam = Webcam.getDefault();}

    public static Javacam getJavacam(){
        if (INSTANCE == null){
            INSTANCE = new Javacam();
        }
        return INSTANCE;
    }

    public BufferedImage getImage(){
        piCam.open();
        image =  piCam.getImage();
        piCam.close();
        return image;
    }

    public void saveImage(){
        try{
            ImageIO.write(image, "JPG", new File("image.jpg"));
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
