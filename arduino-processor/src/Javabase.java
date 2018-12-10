import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.MalformedInputException;
import java.util.HashMap;

// import org.json.simple.JSONObject;

public class Javabase {
    private static final Javabase instance = new Javabase();
    static URL DB_URL;
    static String BASE_URL = "https://d-guian.firebaseio.com/";
    static HttpURLConnection connection;
    final static String GET_REQ = "GET";
    final static String POST_REQ = "POST";
    final static String PUT_REQ = "PUT";

    private Javabase(){
        try{
            openConnection();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Javabase getJavabase(){
        return instance;
    }

    static void openConnection(){
        try {
            DB_URL = new URL("https://d-guian.firebaseio.com/.json");
        } catch (MalformedURLException m) {
            System.out.println("base .json failed.");
            try {
                DB_URL = new URL("https://d-guian.firebaseio.com/data.json");
            } catch (MalformedURLException f){
                System.out.println("data.json failed");
            }
        } finally {
            try {
                connection = (HttpURLConnection) DB_URL.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestMethod(PUT_REQ);
            } catch (IOException c){
                c.printStackTrace();
                System.out.println("Connection could not be established!");
            }
        }
    }

    public void closeConnection(){
        connection.disconnect();
    }

    public void postData(HashMap<String, HashMap<String, String>> newDataSet){
        openConnection();
        System.out.print(".");
        JSONObject payload = new JSONObject();
        for (String top : newDataSet.keySet()){
            HashMap<String, String> sublist = newDataSet.get(top);
            JSONObject subPayload = new JSONObject();
            subPayload.putAll(sublist);

            String subPayloadStr = subPayload.toString();
            StringBuilder subhandler = new StringBuilder();
            subhandler.append(subPayloadStr);

            payload.put(top, subhandler.toString());
            System.out.print(".");
        }

        StringBuilder payloadStr = new StringBuilder();
        payloadStr.append("\"");
        payloadStr.append(payload.toString());
        payloadStr.append("\"");

        try {
            OutputStreamWriter pusher = new OutputStreamWriter(connection.getOutputStream());
            System.out.print(".");
            pusher.write(payloadStr.toString());
            System.out.print(".");
            pusher.flush();
            pusher.close();
            System.out.print("Posted.");

            StringBuilder sb = new StringBuilder();
            int HttpResult = connection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                System.out.print("HTTP response OK");
            } else {
                System.out.println(connection.getResponseMessage());
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }
}
