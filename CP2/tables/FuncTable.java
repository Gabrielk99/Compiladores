package tables;

import java.util.Map;
import java.util.Hashtable;
import java.util.Formatter;
import java.util.ArrayList;
import java.util.Arrays;
import typing.Type;
import static typing.Type.NO_TYPE;
import typing.Inner;

public final class FuncTable{
    private Hashtable<Key,Entry> table = new Hashtable<Key,Entry> ();

    public boolean lookupFunc(String s, int id){
        Key aux_key = new Key(s,id);
        return table.containsKey(aux_key);
    }
    public void addFunc(String s, int line, Inner type, int id_escopo, ArrayList <Inner> parameters,boolean bultin){
        Key key = new Key (s, id_escopo);
        Entry entry = new Entry (s,line,type,id_escopo,parameters,bultin);
        table.put(key,entry);
    }
    //retorna uma lista com os tipos dos parametros (ordenados como se espera pela função)
    public ArrayList <Inner> getParameters(Key k){
        if(lookupFunc(k.getName(),k.getId()))
            return table.get(k).parameters;
        else return new ArrayList<Inner> ();
    }

    public Inner getType(Key k){
        if(lookupFunc(k.getName(),k.getId()))
            return table.get(k).type;
        else return new Inner(NO_TYPE,NO_TYPE);
    }

    public int getLine(Key k){
        if(lookupFunc(k.getName(),k.getId()))
            return table.get(k).line;
        else return -1;
    }

    public String getName (Key k){
        if(lookupFunc(k.getName(),k.getId()))
            return table.get(k).name;
        else return "";
    }
    public int getIdEscopo (Key k){
        if(lookupFunc(k.getName(),k.getId()))
            return table.get(k).id_escopo;
        else return -1;
    }
    public boolean getBultin(Key k){
        if(lookupFunc(k.getName(),k.getId()))
            return table.get(k).bultin;
        else return false;
    }

    public String toString() {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		f.format("Functions table:\n");

       

		for (Map.Entry mapElement : table.entrySet()) {
            Key key = (Key)mapElement.getKey();

        
			f.format("Entry %s -- name: %s, line: %d, type: %s, id_escopo: %d, parameters_type:%s, bultin:%s \n",
	                 key,getName(key), getLine(key), getType(key).toString(), 
                     getIdEscopo(key),getParameters(key),Boolean.toString(getBultin(key)));
		}
		f.close();
		return sb.toString();
	}

    private static final class Entry {
		private final String name;
		private final int line;
		private final Inner type;
        private final int id_escopo;
        private ArrayList <Inner> parameters = new ArrayList <Inner> ();      
		private final boolean bultin;


		Entry(String name, int line, Inner type, int id_escopo, ArrayList <Inner> parameters, boolean bultin) {
			this.name = name;
			this.line = line;
			this.type = type;
            this.id_escopo = id_escopo; 
            this.parameters = parameters;
            this.bultin = bultin;
		}
	}
}