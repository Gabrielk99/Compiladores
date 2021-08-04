package tables;

import java.util.Map;
import java.util.Hashtable;
import java.util.Formatter;
import java.util.List;

import typing.Type;

public final class VarTable{
    private Hashtable<String, Entry> table = new Hashtable<String, Entry>();

    public boolean lookupVar(String s) {
        return table.containsKey(s);
	}
	
	public void addVar(String s, int line, Type type, int id_escopo, int escopo) {
		Entry entry = new Entry(s, line, type, id_escopo, escopo);
		table.put(s, entry);
	}
	
	public String getName(String s) {
		return table.get(s).name;
	}
	
	public int getLine(String s) {
		return table.get(s).line;
	}
	
	public Type getType(String s) {
		return table.get(s).type;
	}

    public int getIdEscopo(String s) {
		return table.get(s).id_escopo;
	}

    public int getEscopo(String s) {
		return table.get(s).escopo;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		f.format("Variables table:\n");
		for (Map.Entry mapElement : table.entrySet()) {
            String key = (String)mapElement.getKey();

			f.format("Entry %d -- name: %s, line: %d, type: %s, id_escopo: %d, escopo: %d\n",
	                 getName(key), getLine(key), getType(key).toString(), 
                     getIdEscopo(key), getEscopo(key));
		}
		f.close();
		return sb.toString();
	}

    private static final class Entry {
		private final String name;
		private final int line;
		private final Type type;
        private final int id_escopo;        
        private final int escopo;
		
		Entry(String name, int line, Type type, int id_escopo, int escopo) {
			this.name = name;
			this.line = line;
			this.type = type;
            this.id_escopo = id_escopo;
            this.escopo = escopo;
		}
	}
}