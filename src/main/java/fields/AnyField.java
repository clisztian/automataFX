package fields;

public class AnyField<V> {

	private String name;
	private V value;
	
	public AnyField(String name, V val) {
		this.name = name; 
		this.value = val;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public V getValue() {
		return value;
	}
	
	public void setValue(V value) {
		this.value = value;
	}
}
