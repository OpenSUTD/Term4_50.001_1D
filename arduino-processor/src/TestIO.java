import java.io.*;

public class TestIO {
    private static String dataReadin = "/home/pi/Desktop/received.txt";
    public static FileInputStream inputStream;
    private static byte[] buffer = new byte[512];
    private static String[] data;

    static String readInputStreamForString(InputStream stream){
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try{
            int length;
            while ( (length = stream.read()) != -1){
                result.write( buffer,0, length);
            }
            return result.toString();
        } catch (Exception k){
            k.printStackTrace();
            return null;
        }
    }

    private static void readFileForSensorData(){
        File textFile = new File(dataReadin);
        System.out.print(".");
        try {
            inputStream = new FileInputStream(textFile);
            System.out.print(".");
            String unsplit = readInputStreamForString(inputStream);
            System.out.print(".");
            System.out.print(unsplit);
            data = unsplit.split("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        readFileForSensorData();
        System.out.println(data);
    }
}

