import java.util.HashMap;

public class JSONObject{
    HashMap<String, Object> mappings = new HashMap<>();
    String JSONstring;

    @Override
    public String toString() {
        StringBuilder total = new StringBuilder();
        total.append("{");
        for (String key: mappings.keySet()){
            StringBuilder s = new StringBuilder();
            if (mappings.get(key).toString().charAt(0) == '{'){
                s.append(String.format("'%s':%s,", key, mappings.get(key).toString()));
            } else {
                s.append(String.format("'%s':'%s',", key, mappings.get(key).toString()));
            }
            total.append(s.toString());
        }
        total.deleteCharAt(total.length()-1);
        total.append("}");
        JSONstring = total.toString();
        return JSONstring;
    }

    public JSONObject(){}

    public void putAll(HashMap map){
        mappings.putAll(map);
    }

    public void put(String key, Object value){
        mappings.put(key, value);
    }
}
