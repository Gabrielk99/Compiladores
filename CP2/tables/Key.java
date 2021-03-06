package tables;

public class Key{
	private String name;
	private int id;

	public Key(String name, int id){
		this.name = name;
		this.id = id;
	}

	public String getName(){
		return name;
	}

	public int getId(){
		return id;
	}

	@Override
	public boolean equals(Object obj){
        Key k = (Key) obj;
		return this.name.equals(k.getName()) && this.id == k.getId();
	}
	@Override
	public int hashCode(){
		int hash = this.name.hashCode();
		hash = hash*31+this.id;
		return hash;
	}
	@Override
	public String toString(){
		return "(" + name + ", " + id + ")";
	}

}