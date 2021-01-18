package encoders;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import encoders.TimeEncoder.Time_Encoder;
import records.AnyRecord;
import records.TimeIndicator;

/**
 * Encodes a record into several smaller encoders
 * @author lisztian
 *
 * @param <V>
 */
public class RecordEncoder<V>  {

	private ArrayList<V> values;
		
	private int feature_dimension;
	private Encoder[] encode_maps;
	private String[] field_names;
	
	public RecordEncoder() {
		setValues(new ArrayList<V>());
	}
	

	/**
	 * Initiates a RecordEncoder with information from the class V
	 * All double/float/int values are attributed to a real encoder
	 * all String values associated with categorical encoder
	 * @param val
	 * @return
	 */
	public RecordEncoder<V> initiate(Class<?> val) {
		
		List<Field> fields = getPrivateFields(val);
		encode_maps = new Encoder[fields.size()];
		field_names = new String[fields.size()];
		
		int count = 0;
		for(Field field : fields) {
			
			String type = field.getType().toString();
			if(type.contains("double") || type.contains("float") || type.contains("int")) {							
				encode_maps[count] = new RealEncoder(field.getName(), 10);				
			}
			else if(type.contains("Temporal")) {
				encode_maps[count] = new TimeEncoder(field.getName(), "yyyy-MM-dd HH:mm:ss");
			}
			else {
				encode_maps[count] = new CategoricalEncoder(field.getName());
			}
			field_names[count] = field.getName();
			count++;
		}
		return this;
	}
	
	/**
	 * Initiates a RecordEncoder with information from the class V
	 * All double/float/int values are attributed to a real encoder
	 * all String values associated with categorical encoder
	 * @param val
	 * @return
	 */
	public RecordEncoder<V> initiate(Class<?> val, int bits) {
		
		List<Field> fields = getPrivateFields(val);
		encode_maps = new Encoder[fields.size()];
		field_names = new String[fields.size()];
		
		int count = 0;
		for(Field field : fields) {
			
			String type = field.getType().toString();
			if(type.contains("double") || type.contains("float") || type.contains("int")) {							
				encode_maps[count] = new RealEncoder(field.getName(), bits);				
			}
			else if(type.contains("Temporal")) {
				encode_maps[count] = new TimeEncoder(field.getName(), "yyyy-MM-dd HH:mm:ss");
			}
			else {
				encode_maps[count] = new CategoricalEncoder(field.getName());
			}
			field_names[count] = field.getName();
			count++;
		}
		return this;
	}
	
	/**
	 * Initiates a RecordEncoder with information from the class V
	 * All double/float/int values are attributed to a real encoder
	 * all String values associated with categorical encoder
	 * 
	 * If time encoder, options are
	 * 	DAY_OF_WEEK, (by default)
	    HOUR_OF_DAY,
	    DAY_OF_MONTH,
	    MONTH_OF_YEAR,
	    WEEK_OF_YEAR
	 * 
	 * @param val
	 * @return
	 */
	public RecordEncoder<V> initiate(Class<?> val, int bits, boolean hours, boolean day_of_month, boolean month_of_year, boolean week_of_year) {
		
		List<Field> fields = getPrivateFields(val);
		encode_maps = new Encoder[fields.size()];
		field_names = new String[fields.size()];
		
		int count = 0;
		for(Field field : fields) {
			
			String type = field.getType().toString();
			if(type.contains("double") || type.contains("float") || type.contains("int")) {							
				encode_maps[count] = new RealEncoder(field.getName(), bits);				
			}
			else if(type.contains("Temporal")) {
				encode_maps[count] = new TimeEncoder(field.getName(), "yyyy-MM-dd HH:mm:ss");
				if(hours) {
					((TimeEncoder) encode_maps[count]).addTimeEncoder(Time_Encoder.HOUR_OF_DAY);
				}
				if(day_of_month) {
					((TimeEncoder) encode_maps[count]).addTimeEncoder(Time_Encoder.DAY_OF_MONTH);
				}
				if(month_of_year) {
					((TimeEncoder) encode_maps[count]).addTimeEncoder(Time_Encoder.MONTH_OF_YEAR);
				}
				if(week_of_year) {
					((TimeEncoder) encode_maps[count]).addTimeEncoder(Time_Encoder.WEEK_OF_YEAR);
				}
				
			}
			else {
				encode_maps[count] = new CategoricalEncoder(field.getName());
			}
			field_names[count] = field.getName();
			count++;
		}
		return this;
	}
	
	/**
	 * Intantiates a record encoder with a record containing field_names and a bit dimension
	 * @param record
	 * @param datetime_format
	 * @param dim
	 * @return
	 */
	public RecordEncoder<V> initiate(AnyRecord record, String datetime_format, int dim) {
		
		if(record.getField_names() != null) {
			
			field_names = record.getField_names();
			encode_maps = new Encoder[field_names.length];
						
			for(int i = 0; i < record.getValues().length; i++) {
				
				if(field_names[i].contains("time")) {
					encode_maps[i] = new TimeEncoder("timestamp", datetime_format);
				}
				else {
					
					if(record.getValues()[i] instanceof Double || record.getValues()[i] instanceof Float || record.getValues()[i] instanceof Integer) {
						encode_maps[i] = new RealEncoder(field_names[i], dim);
					}
					else {
						encode_maps[i] = new CategoricalEncoder(field_names[i]);
					}
				}				
			}
			
		}
		return this;
	}
	
	/**
	 * Instantiate a record encoder with anyRecord. Must contain field names
	 * @param record
	 * @return
	 */
	public RecordEncoder<V> initiate(AnyRecord record) {
		
		if(record.getField_names() != null) {
			
			field_names = record.getField_names();
			encode_maps = new Encoder[field_names.length];
						
			for(int i = 0; i < record.getValues().length; i++) {
				
				if(field_names[i].contains("time") || field_names[i].contains("date")) {
					encode_maps[i] = new TimeEncoder("timestamp", "yyyy-MM-dd HH:mm:ss");
				}
				else {
					
					if(record.getValues()[i] instanceof Double || record.getValues()[i] instanceof Float || record.getValues()[i] instanceof Integer) {
						encode_maps[i] = new RealEncoder(field_names[i], 10);
					}
					else {
						encode_maps[i] = new CategoricalEncoder(field_names[i]);
					}
				}				
			}	
		}
		return this;
	}
	
	
	public RecordEncoder<V> initiate(AnyRecord record, int dim) {
		
		if(record.getField_names() != null) {
			
			field_names = record.getField_names();
			encode_maps = new Encoder[field_names.length];
						
			for(int i = 0; i < record.getValues().length; i++) {
				
				if(field_names[i].contains("time") || field_names[i].contains("date")) {
					encode_maps[i] = new TimeEncoder("timestamp", "yyyy-MM-dd HH:mm:ss");
				}
				else {
					
					if(record.getValues()[i] instanceof Double || record.getValues()[i] instanceof Float || record.getValues()[i] instanceof Integer) {
						encode_maps[i] = new RealEncoder(field_names[i], dim);
					}
					else {
						encode_maps[i] = new CategoricalEncoder(field_names[i]);
					}
				}				
			}	
		}
		return this;
	}
	
	
	public void encoderTypes() {
		
		for(int i = 0; i < encode_maps.length; i++) {
			System.out.println(encode_maps[i].toString());
		}
		
	}
	
	
	/**
	 * Add a value (in terms of a record)
	 * All numerical values will be used Record
	 * @param val
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void addValue(V val) throws IllegalArgumentException, IllegalAccessException {
		
		if(val instanceof AnyRecord) { 
			addRecordValue((AnyRecord)val);
		}
		else {			
			addClassRecord(val);
		}
	}
	
	private void addRecordValue(AnyRecord any) {
		
		for(int i = 0; i < any.getValues().length; i++) {
			
			if(encode_maps[i] instanceof TimeEncoder && any.getValues()[i] instanceof String) {
				((TimeEncoder)encode_maps[i]).addValue(new Temporal((String)any.getValues()[i]));
			}
			else if(any.getValues()[i] instanceof Float) {
				Float value = (Float)any.getValues()[i];
				((RealEncoder)encode_maps[i]).addValue(value);
			}
			else if(any.getValues()[i] instanceof Double) {
				Float value = ((Double)any.getValues()[i]).floatValue();
				((RealEncoder)encode_maps[i]).addValue(value);
			}
			else if(any.getValues()[i] instanceof Integer) {
				Float value = ((Integer)any.getValues()[i]).floatValue();
				((RealEncoder)encode_maps[i]).addValue(value);
			}
			else {
				((CategoricalEncoder)encode_maps[i]).addValue((String)any.getValues()[i]);
			}							
		}		
	}
	
	private void addClassRecord(V val) throws IllegalArgumentException, IllegalAccessException {
		
		List<Field> fields = getPrivateFields(val.getClass());
		
		int count = 0;
		for(Field field : fields) {

			String type = field.getType().toString();
			if(type.contains("double")) {
							
				field.setAccessible(true);
				Float value = (float)field.getDouble(val);				
				((RealEncoder)encode_maps[count]).addValue(value);
			}
			else if(type.contains("float")) {
				
				field.setAccessible(true);
				Float value = field.getFloat(val);				
				((RealEncoder)encode_maps[count]).addValue(value);
				
			}
			else if(type.contains("int")) {
				
				field.setAccessible(true);
				Float value = (float)field.getInt(val);				
				((RealEncoder)encode_maps[count]).addValue(value);
			}
			else if(type.contains("Temporal")) {
				
				field.setAccessible(true);
				Temporal value = (Temporal)field.get(val);				
				((TimeEncoder)encode_maps[count]).addValue(value);			
			}
			else { 				
				field.setAccessible(true);
				String value = field.get(val).toString();
				((CategoricalEncoder)encode_maps[count]).addValue(value);
			}	
			count++;
		}	
		
	}

	/**
	 * Fit all with the uniform rule
	 */
	public void fit_uniform() {		
		for(int i = 0; i < encode_maps.length; i++) {
			encode_maps[i].fit_uniform();
		}	
	}
	
	/**
	 * Fit all with the dynamic rule
	 */
	public void fit_dynamic() {
		for(int i = 0; i < encode_maps.length; i++) {
			encode_maps[i].fit_dynamic();
		}
	}
	
	/**
	 * Transforms a record into an encoded bit representation
	 * @param val A generic record
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public int[] transform(V val) throws IllegalArgumentException, IllegalAccessException {
					
		if(val instanceof AnyRecord) { 		
			return transform_any_record((AnyRecord)val);
		}
			
		return transform_record(val);
	}
	
	public ArrayList<RecordDecodeResult> decode(int[] en) {
		
		ArrayList<RecordDecodeResult> decode_list = new ArrayList<RecordDecodeResult>();
		
		int start = 0;
		for(int i = 0; i < encode_maps.length; i++) {
			
			int bit_dim = encode_maps[i].getBitDimension();
			int[] subarray = ArrayUtils.subarray(en, start, start+bit_dim);
			start += bit_dim;
			
			RecordDecodeResult res = new RecordDecodeResult();
			res.setEncoded(subarray);
			res.setValue(encode_maps[i].decoder(subarray));
			res.setField_name(field_names[i]);
			
			decode_list.add(res);
		}				
		return decode_list;
	}
	
	
	/**
	 * Get number of total bits represented in record encoding 
	 * @return
	 */
	public int getBitDimension() {
		
		int dim = 0;
		for(int i = 0; i < encode_maps.length; i++) {
			dim += encode_maps[i].getBitDimension();
		}
		return dim;	
	}
	
	
	public  List<Field> getPrivateFields(Class<?> theClass){
        LinkedList<Field> privateFields = new LinkedList<Field>();

        Field[] fields = theClass.getDeclaredFields();

        for(Field field:fields){
        	privateFields.add(field);
        }
        return privateFields;
    }


	public ArrayList<V> getValues() {
		return values;
	}


	public void setValues(ArrayList<V> values) {
		this.values = values;
	}


	/**
	 * Get the field names of the records
	 * @return
	 */
	public String[] getField_names() {
		return field_names;
	}
	
	/**
	 * Get encoder maps
	 * @return all the encoding maps
	 */
	public Encoder<V>[] getEncode_maps() {
		return encode_maps;
	}
	
	
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
		
		Random rng = new Random();
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		DateTime dt = DateTime.now();
		
		String[] name_cat = new String[] {"economic", "financial", "weather", "health"};
		String[] reach_cat = new String[] {"global", "cantonal", "city", "EU"};
		
		
		RecordEncoder<TimeIndicator> rencoder = new RecordEncoder<TimeIndicator>();
		
		rencoder.initiate(TimeIndicator.class);
		
		for(int i = 0; i < 100; i++) {
			
			double value = rng.nextDouble()*10;
			int name_c = rng.nextInt(4);
			int reach_c = rng.nextInt(4);
			
			TimeIndicator ind = new TimeIndicator(name_cat[name_c], reach_cat[reach_c], new Temporal(dt.toString(formatter)), value);
			rencoder.addValue(ind);
			dt = dt.plusMinutes(120);
			
		}
		rencoder.fit_dynamic();
		
		int dim = rencoder.getBitDimension();
		
		System.out.println(dim);
		
		TimeIndicator ind = new TimeIndicator(name_cat[0], reach_cat[0], new Temporal(dt.toString(formatter)), 2);
		
		int[] enc = rencoder.transform(ind);
		
		for(int i = 0; i < enc.length; i++) {
			System.out.print(enc[i] + " ");
		} 
		System.out.println("");
		
		ArrayList<RecordDecodeResult> results = rencoder.decode(enc);
		
		for(int i = 0; i < results.size(); i++) {
			System.out.println(results.get(i));
		}
	}


	public int getFeature_dimension() {
		return feature_dimension;
	}


	public void setFeature_dimension(int feature_dimension) {
		this.feature_dimension = feature_dimension;
	}
	

	/**
	 * Transforms any generic record
	 * @param any
	 * @return
	 */
	private int[] transform_any_record(AnyRecord any) {
		
		int[] encoded = null;

		for(int i = 0; i < any.getValues().length; i++) {
			
			if(encode_maps[i] instanceof TimeEncoder && any.getValues()[i] instanceof String) {
				int[] bits = ((TimeEncoder)encode_maps[i]).transform(new Temporal((String)any.getValues()[i]));
				encoded = ArrayUtils.addAll(encoded, bits);	
			}
			else if(any.getValues()[i] instanceof Float) {
				int[] bits = ((RealEncoder)encode_maps[i]).transform((Float)any.getValues()[i]);
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else if(any.getValues()[i] instanceof Double) {
				int[] bits = ((RealEncoder)encode_maps[i]).transform(((Double)any.getValues()[i]).floatValue());
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else if(any.getValues()[i] instanceof Integer) {
				int[] bits = ((RealEncoder)encode_maps[i]).transform(((Integer)any.getValues()[i]).floatValue());
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else {
				int[] bits = ((CategoricalEncoder)encode_maps[i]).transform((String)any.getValues()[i]);
				encoded = ArrayUtils.addAll(encoded, bits);
			}					
		}
		
		return encoded;		
	}
	
	/**
	 * Transforms a predefined record
	 * @param val
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private int[] transform_record(V val) throws IllegalArgumentException, IllegalAccessException {
		
		List<Field> fields = getPrivateFields(val.getClass());
		
		int count = 0;
		int[] encoded = null;
		
		for(Field field : fields) {

			String type = field.getType().toString();
			if(type.contains("double")) {
							
				field.setAccessible(true);
				Float value = (float)field.getDouble(val);				
				int[] bits = ((RealEncoder)encode_maps[count]).transform(value);
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else if(type.contains("float")) {
				
				field.setAccessible(true);
				Float value = field.getFloat(val);				
				int[] bits = ((RealEncoder)encode_maps[count]).transform(value);
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else if(type.contains("int")) {
				
				field.setAccessible(true);
				Float value = (float)field.getInt(val);				
				int[] bits = ((RealEncoder)encode_maps[count]).transform(value);
				encoded = ArrayUtils.addAll(encoded, bits);
			}
			else if(type.contains("Temporal")) {
				
				field.setAccessible(true);
				Temporal value = (Temporal)field.get(val);				
				int[] bits = ((TimeEncoder)encode_maps[count]).transform(value);
				encoded = ArrayUtils.addAll(encoded, bits);	
			}
			else { 				
				field.setAccessible(true);
				String value = field.get(val).toString();
				int[] bits = ((CategoricalEncoder)encode_maps[count]).transform(value);
				encoded = ArrayUtils.addAll(encoded, bits);
			}	
			count++;
		}					
		return encoded;	

	}
	
	
	
	
	
}
