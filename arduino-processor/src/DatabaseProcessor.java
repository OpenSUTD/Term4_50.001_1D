import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import com.fazecast.jSerialComm.*;

public class DatabaseProcessor {
    static final String USB_PORT_NAME = "/dev/ttyUSB0";
    static int BAUD_RATE;
    public static InputStream inputStream;
    public static SerialPort serialPort;
    static String fileName = "/home/pi/Documents/writefile.txt";
    static Timer threadTimer = new Timer("ScheduleWrite", true);
    static byte[] buffer = new byte[256];
    static int delayMilliseconds = 5000;

    public static void scheduleFileWrite(){
        TimerTask writeToFile = new TimerTask() {
            @Override
            public void run() {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
                    if(!serialPort.isOpen()){
                        serialPort.openPort();
                    }
                    do {
                        inputStream = serialPort.getInputStream();
                        String output = readInputStreamForString(inputStream);
                        writer.write(output);
                        writer.close();
                    } while(serialPort.isOpen());
                    serialPort.closePort();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        Timer scheduler = new Timer();
        scheduler.scheduleAtFixedRate(writeToFile, 0, delayMilliseconds);
    }

    public static String readInputStreamForString(InputStream stream){
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try{
            int length;
            while ( (length = stream.read(buffer)) != -1){
                result.write(buffer,0, length);
            }
            return result.toString();
        } catch (Exception k){
            k.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            serialPort = SerialPort.getCommPort(USB_PORT_NAME);
            BAUD_RATE = serialPort.getBaudRate();
            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 4950, 0);
            scheduleFileWrite();
        } catch (Exception e){
            e.printStackTrace();
        }
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
