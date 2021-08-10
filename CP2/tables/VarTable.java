package tables;

import java.util.Map;
import java.util.Hashtable;
import java.util.Formatter;
import java.util.List;

import typing.Type;

public final class VarTable{
    private Hashtable<Key, Entry> table = new Hashtable<Key, Entry>();

    public boolean lookupVar(String s, int id) {
		Key aux_key = new Key(s, id);
        return table.containsKey(aux_key);
	}
	
	public void addVar(String s, int line, Type type, int id_escopo) {
		Key key = new Key(s, id_escopo);
		Entry entry = new Entry(s, line, type, id_escopo);
		table.put(key, entry);
	}
	
	public String getName(Key k) {
		return table.get(k).name;
	}
	
	public int getLine(Key k) {
		return table.get(k).line;
	}
	
	public Type getType(Key k) {
		return table.get(k).type;
	}

    public int getIdEscopo(Key k) {
		return table.get(k).id_escopo;
	}

	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		f.format("Variables table:\n");
		for (Map.Entry mapElement : table.entrySet()) {
            Key key = (Key)mapElement.getKey();

			f.format("Entry %d -- name: %s, line: %d, type: %s, id_escopo: %d\n",
	                 getName(key), getLine(key), getType(key).toString(), 
                     getIdEscopo(key));
		}
		f.close();
		return sb.toString();
	}

    private static final class Entry {
		private final String name;
		private final int line;
		private final Type type;
        private final int id_escopo;        
		
		Entry(String name, int line, Type type, int id_escopo) {
			this.name = name;
			this.line = line;
			this.type = type;
            this.id_escopo = id_escopo;
		}
	}

}