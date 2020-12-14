package records;


/**
 * Generic placeholder for a record from csv or database
 * @author lisztian
 *
 */
public class AnyRecord {

	private Object[] values;
	private String[] field_names = null;
		

	public Object[] getValues() {
		return values;
	}
	public void setValues(Object[] values) {
		this.values = values;
	}
	public String[] getField_names() {
		return field_names;
	}
	public void setField_names(String[] field_names) {
		this.field_names = field_names;
	}
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Value: ");
		
		for(int i = 0; i < values.length; i++) {
			sb.append(values[i].toString() + ", ");
		}
		
		return sb.toString();
		
	}
}