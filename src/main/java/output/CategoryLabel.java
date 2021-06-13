package output;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import encoders.Encoder;
import records.RecordColumn;

/**
 * Labeling system for categories in the form of strings.
 * @author lisztian
 *
 */
public class CategoryLabel implements OutputLabel<String>{

	private int class_count;
	private int number_of_classes;
	private HashMap<String, Integer> label_encoder;
	private RecordColumn recordColumn;
	


	public CategoryLabel(HashMap<String, Integer> label_encoder) {
		
		this.label_encoder = label_encoder;
		this.setNumber_of_classes(label_encoder.size());
		class_count = 0;
	}
	
	public CategoryLabel(int number_of_classes) {
		
		label_encoder = new HashMap<String, Integer>();
		this.setNumber_of_classes(number_of_classes);
		class_count = 0;
	}
	
	@Override
	public void setLabel(Object obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getLabel(String val) {
		
		if(label_encoder.containsKey(val)) {
			return label_encoder.get(val);
		}
		else {
			
			if(class_count == number_of_classes) {
				return null;
			}
			
			label_encoder.put(val, class_count);
			class_count++;
			return label_encoder.get(val);
		}	
	}

	@Override
	public void setEncoder(Encoder ecnoder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String decode(Object obj) {		
		return getKeyByValue(label_encoder, (int)obj);
	}

	public int getNumber_of_classes() {
		return number_of_classes;
	}

	public void setNumber_of_classes(int number_of_classes) {
		this.number_of_classes = number_of_classes;
	}

	
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (Objects.equals(value, entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}

	public void setRecordColumn(RecordColumn recordColumn) {		
		this.recordColumn = recordColumn;
	}
	
	public RecordColumn getRecordColumn() {
		return recordColumn;
	}
	
	public HashMap<String, Integer> getLabel_encoder() {
		return label_encoder;
	}
	
}
