package tables;

import java.util.Map;
import java.util.Hashtable;
import java.util.Formatter;

public final class StrTable {
    private Hashtable <Integer, String> table = new Hashtable <Integer, String> () ;

    public void addStr(String s, int code){

        if(!this.table.containsKey(s)){
            this.table.put(code,s);
        }


    }
    public boolean containString(int  code){
        return this.table.containsKey(code);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("Strings table:\n");
        for (Map.Entry mapElement : table.entrySet()){
            
            Key key = (Key)mapElement.getKey();

            f.format("Entry %d -- %s\n", key, table.get(key));
        }

        f.close();
        return sb.toString();
    }

}
