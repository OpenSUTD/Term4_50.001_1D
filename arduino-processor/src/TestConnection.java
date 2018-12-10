import java.util.HashMap;

public class TestConnection {

    public static void main(String[] args){
        String k = "SRF05=10000";
        String[] j = k.split("=");
        for (String i : j){
            System.out.println(i);
        }
    }
}
