package encoders;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import records.AnyRecord;
import records.RecordColumn.Type;

public class TestCSVEncoder {

	@Test
	public void test() throws IllegalArgumentException, IllegalAccessException {
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		DateTime dt = formatter.parseDateTime("2020-11-16 12:08:00");
		
		String[] name_cat = new String[] {"economic", "financial", "weather", "health"};
		String[] reach_cat = new String[] {"global", "cantonal", "city", "EU"};
		
		
		RecordEncoder<AnyRecord> rencoder = new RecordEncoder<AnyRecord>();
		AnyRecord any_rec = new AnyRecord();
		String[] fields = new String[5];
		fields[0] = "name";
		fields[1] = "type";
		fields[2] = "timestamp";
		fields[3] = "value";
		fields[4] = "meta";
		
		Type[] types = new Type[5];
		types[0] = Type.CATEGORY;
		types[1] = Type.CATEGORY;
		types[2] = Type.TIME;
		types[3] = Type.REAL;
		types[4] = Type.INFO;
		
		any_rec.setField_names(fields);
		any_rec.setType(types);
		
		rencoder.initiate(any_rec, "yyyy-MM-dd HH:mm:ss", 10, false, false, false, false);
		

		
		//---- 4 times economic and EU
		
		rencoder.addValue(new AnyRecord(new Object[] {"economic", "EU", dt.toString(formatter), 0f, "econ"} )); dt = dt.plusDays(1);
		rencoder.addValue(new AnyRecord(new Object[] {"economic", "EU", dt.toString(formatter), 1f, "econ"} )); dt = dt.plusDays(1);
		rencoder.addValue(new AnyRecord(new Object[] {"economic", "EU", dt.toString(formatter), 2f, "econ"} )); dt = dt.plusDays(1);
		rencoder.addValue(new AnyRecord(new Object[] {"economic", "EU", dt.toString(formatter), 3f, "econ"} )); dt = dt.plusDays(1);
		
		rencoder.addValue(new AnyRecord(new Object[] {"weather", "cantonal", dt.toString(formatter), 4f, "econ"} )); dt = dt.plusDays(1);
		rencoder.addValue(new AnyRecord(new Object[] {"weather", "cantonal", dt.toString(formatter), 5f, "econ"} )); dt = dt.plusDays(1);
		rencoder.addValue(new AnyRecord(new Object[] {"weather", "cantonal", dt.toString(formatter), 6f, "econ"} )); dt = dt.plusDays(1);
		
		rencoder.addValue(new AnyRecord(new Object[] {"financial", "city", dt.toString(formatter), 7f, "econ"} )); dt = dt.plusDays(1);
		rencoder.addValue(new AnyRecord(new Object[] {"financial", "city", dt.toString(formatter), 8f, "econ"} )); dt = dt.plusDays(1);
		
		rencoder.addValue(new AnyRecord(new Object[] {"health", "global",  dt.toString(formatter), 9f, "econ"} )); dt = dt.plusDays(1);
		

		rencoder.fit_dynamic();
		
		
		int dim = rencoder.getBitDimension();

		//4 + 4 + 10 + 7 = 25
		assertEquals(25, dim);
		
		int[] expected = new int[] {1,0,0,0,0,0,0,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1};
		int[] enc = rencoder.transform(new AnyRecord(new Object[] {"economic", "global", dt.toString(formatter), 10f, "econ"} ));
		

				
		assertArrayEquals(expected, enc);
		
		int[] expected2 = new int[] {0,0,0,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

		//-back to Monday
		dt = dt.plusDays(4);	
		
		int[] enc2 = rencoder.transform(new AnyRecord(new Object[] {"health", "EU", dt.toString(formatter), -1f, "econ"} ) );	
		assertArrayEquals(expected2, enc2);		
		
		
	}

}
