import java.util.HashMap;

public class TestDB {
    public static void main(String[] args){
        Javabase newdb = Javabase.getJavabase();
        HashMap<String, HashMap<String, String>> k = new HashMap<>();
        HashMap<String, String> i = new HashMap<>();
        HashMap<String, String> l = new HashMap<>();
        i.put("A", "b");
        l.put("C", "d");
        k.put("1", i);
        k.put("2", l);
        newdb.postData(k);
    }
}
