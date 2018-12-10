import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.print.DocFlavor;

public class DatabaseProcessor {
    static {
        int BAUD_RATE;
    }

    static final String USB_PORT_NAME = "/dev/ttyUSB0";
    // PORT READOUTS
    private static String dataReadin = "/home/pi/Desktop/received.txt";
    public static FileInputStream inputStream;
    private static byte[] buffer = new byte[256];

    // TIMER FOR SCHEDULER
    private static Timer threadTimer = new Timer("ScheduleWrite");
    private static long delayMilliseconds = 30000;

    // CAMERA AND CAMERA DATA
    private static Javacam camera;
    private static String imageString;
    private static BufferedImage imageCaptured;

    // DATABASE PROCESSING
    private static Javabase database;
    private static String[] data;
    private static HashMap<String, HashMap<String, String>> payload;
    private static String prevdataYaxis;
    private static String prevdataXaxis;
    private static boolean[] changes = new boolean[3];

    public static void scheduleTasks(){
        TimerTask update = new TimerTask() {
            @Override
            public void run() {
                System.out.print("Reading sensor data.");
                readFileForSensorData();
                System.out.print("Done. Acquiring Photo.");
                getPhoto();
                System.out.println("Photo acquired.");

                System.out.print("Generating data mappings from sensor data");
                payload = generateMapFromString(data);
                System.out.println("Done.");

                System.out.print("Generating data mappings for image");
                System.out.print(".");

                HashMap<String, String> images = new HashMap<>();
                System.out.print(".");

                images.put("001", imageString);
                System.out.print(".");

                payload.put("Images", images);
                System.out.println("Done.");

                System.out.print("Attempting to open connection to database..");
                database = Javabase.getJavabase();
                System.out.print(".");
                database.openConnection();
                System.out.println("Done.");

                System.out.print("Attempting upload to database.");
                uploadToDB();
                System.out.println("Done.");
            }
        };
        threadTimer.scheduleAtFixedRate(update, 0, delayMilliseconds);
        System.out.println("Data Collection and update Daemon started.");
    }

    private static void readFileForSensorData(){
        File textFile = new File(dataReadin);
        System.out.print(".");
        try {
            inputStream = new FileInputStream(textFile);
            System.out.print(".");
            String unsplit = readInputStreamForString(inputStream);
            System.out.print(".");
            data = unsplit.split("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String readInputStreamForString(InputStream stream){
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try{
            int length;
            while ( (length = stream.read(buffer)) != -1){
                result.write(buffer, 0, length);
            }
            return result.toString();
        } catch (Exception k){
            k.printStackTrace();
            return null;
        }
    }

    private static void getPhoto(){
        imageCaptured = camera.getImage();
        System.out.print(".");
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(imageCaptured, "png", os);
            System.out.print(".");
            imageString = Base64.getEncoder().encodeToString(os.toByteArray());
            System.out.print(".");
        } catch (IOException e){
            System.out.println("Image Conversion to png byte string failed.");
        }
    }

    private static void uploadToDB(){
        database.postData(payload);
        System.out.print(".");
    }

    static HashMap<String, HashMap<String, String>> generateMapFromString(String[] data){
        HashMap<String, String> subtable = new HashMap<>();
        for (String substring : data){
            if (substring.isEmpty() || substring.equals(" ")){
                continue;
            } else {
                String[] keyvalue = substring.split("=");
                subtable.put(keyvalue[0], keyvalue[1]);
                if (keyvalue[0] == "SRF05"){
                    changes[0] = !prevdataXaxis.equals(keyvalue[1]);
                    prevdataXaxis = keyvalue[1];
                }
                if (keyvalue[0] == "SR04"){
                    changes[1] = !prevdataYaxis.equals(keyvalue[1]);
                    prevdataYaxis = keyvalue[1];
                }
                if (keyvalue[0] == "RPM"){
                    changes[2] = Integer.getInteger(keyvalue[1]) > 0;
                }
            }
        }
        System.out.print(".");

        HashMap<String, String> checktable = new HashMap<>();
        boolean stillOk = (changes[0] || changes[1]) && changes[2];
        checktable.put("Activity", String.valueOf(stillOk));
        System.out.print(".");

        HashMap<String, HashMap<String, String>> output = new HashMap<>();
        output.put("Data", subtable);
        output.put("Printer", checktable);
        System.out.print(".");

        return output;
    }



    public static void main(String[] args){
        try {
            camera = Javacam.getJavacam();
            scheduleTasks();
            System.out.println("Task Scheduler successfully Completed.");
        } catch (Exception e){
            e.printStackTrace();
        }
        return;
    }

    /*
    public static void main(String[] args) throws IOException {

        SerialPort serialPorts[] = SerialPort.getCommPorts();
        System.out.println( "select" );
        int length = serialPorts.length;

        for (int i = 0; i < length; i++) {
            String TemName = serialPorts[i].getDescriptivePortName();
            System.out.println( i + ". " + TemName );


        }
        Scanner serial = new Scanner( System.in );
        int number = serial.nextInt();
        serialPort = serialPorts[number - 1];
        if (serialPort.openPort()) {
            System.out.println( "Port opened Successfully." );
        } else {
            System.out.println( "Port cannot be opened." );
            return;
        }


        serialPort.openPort();
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
        InputStream in = serialPort.getInputStream();
        do {
            try {

                System.out.println( (char) in.read() );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }while(serialPort.isOpen());
        serialPort.closePort();
    }
    */
}
