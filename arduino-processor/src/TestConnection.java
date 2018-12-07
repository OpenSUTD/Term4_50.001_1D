import java.util.HashMap;

public class TestConnection {

    public static void main(String[] args){
        Javabase j = Javabase.getJavabase();
        HashMap<String, String> zeroOne = new HashMap<>();
        zeroOne.put("ImagePath", "image/image.jpg");
        zeroOne.put("Status", "Active");
        HashMap<String, String> threeDP = new HashMap<>();
        threeDP.put("001", "Off");
        threeDP.put("002", "On");
        HashMap<String, HashMap<String, String>> base = new HashMap<>();
        base.put("001", zeroOne);
        base.put("3DP", threeDP);
        j.postData(base);
    }
}
