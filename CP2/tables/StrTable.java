package tables;

import java.util.Map;
import java.util.Hashtable;
import java.util.Formatter;

public final class StrTable {
    private Hashtable <String, String> table = new Hashtable <String, String> () ;

    public void addStr(String s){

        if(!this.table.containsKey(s)){
            this.table.put(s,s);
        }


    }
    public boolean containString(String s){
        return this.table.containsKey(s);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("Strings table");
        for (Map.Entry mapElement : table.entrySet()){
            String key = mapElement.getKey();

            f.format("Entry %s -- Key and Value\n", key);
        }

        f.close();
        return sb.toString();
    }

}
