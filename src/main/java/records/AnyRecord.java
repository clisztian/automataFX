package records;

import java.util.Map;

import records.RecordColumn.Type;

/**
 * Generic placeholder for a record from csv or database
 * @author lisztian
 *
 */
public class AnyRecord {

	private Object[] values;
	private String[] field_names = null;


	private Type[] type = null;
	
	private String[] meta_names = null;	
	private Integer label = null;


	private Map<String, Object> map;
	
	public AnyRecord() {
		field_names = null;
	}
	
	public AnyRecord(int label) {
		this.label = label;
	}
	
	
	public AnyRecord(String[] names) {
		setField_names(names);
	}
	
	public AnyRecord(Object[] values) {
		setValues(values);
	}
	
	public AnyRecord(String[] names, Object[] values) {
		setField_names(names);
		setValues(values);
	}
	
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
			sb.append(values[i].toString() + "(" + type[i] + "), ");
		}
		
		return sb.toString();
		
	}

	public Type[] getType() {
		return type;
	}

	public void setType(Type[] type) {
		this.type = type.clone();
	}
	
	public Integer getLabel() {
		return label;
	}

	public void setLabel(Integer label) {
		this.label = label;
	}
	
	public String[] getMeta_names() {
		return meta_names;
	}

	public void setMeta_names(String[] meta_names) {
		this.meta_names = meta_names;
	}

	public void setMap(Map<String, Object> map) {
		this.map=map;
	}
	
	public Map<String, Object> getMap() {
		return map;
	}
}
