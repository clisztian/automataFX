package encoders;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import records.TimeIndicator;

public class TestRecordEncoder {

	@Test
	public void testRecordEncoder() throws IllegalArgumentException, IllegalAccessException {
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		DateTime dt = formatter.parseDateTime("2020-11-16 12:08:00");
		
		String[] name_cat = new String[] {"economic", "financial", "weather", "health"};
		String[] reach_cat = new String[] {"global", "cantonal", "city", "EU"};
		
		
		RecordEncoder<TimeIndicator> rencoder = new RecordEncoder<TimeIndicator>();
		
		rencoder.initiate(TimeIndicator.class);
		
		//---- 4 times economic and EU
		rencoder.addValue(new TimeIndicator("economic", "EU", new Temporal(dt.toString(formatter)), 0)); dt = dt.plusDays(1);
		rencoder.addValue(new TimeIndicator("economic", "EU", new Temporal(dt.toString(formatter)), 1)); dt = dt.plusDays(1);
		rencoder.addValue(new TimeIndicator("economic", "EU", new Temporal(dt.toString(formatter)), 2)); dt = dt.plusDays(1);
		rencoder.addValue(new TimeIndicator("economic", "EU", new Temporal(dt.toString(formatter)), 3)); dt = dt.plusDays(1);
		
		//---- 3 times weather and cantonal
		rencoder.addValue(new TimeIndicator("weather", "cantonal", new Temporal(dt.toString(formatter)), 4)); dt = dt.plusDays(1);
		rencoder.addValue(new TimeIndicator("weather", "cantonal", new Temporal(dt.toString(formatter)), 5)); dt = dt.plusDays(1);
		rencoder.addValue(new TimeIndicator("weather", "cantonal", new Temporal(dt.toString(formatter)), 6)); dt = dt.plusDays(1);
		
		//---- 2 times financial and cantonal
		rencoder.addValue(new TimeIndicator("financial", "city", new Temporal(dt.toString(formatter)), 7)); dt = dt.plusDays(1);
		rencoder.addValue(new TimeIndicator("financial", "city", new Temporal(dt.toString(formatter)), 8)); dt = dt.plusDays(1);
		
		//---- 1 times financial and cantonal
		rencoder.addValue(new TimeIndicator("health", "global", new Temporal(dt.toString(formatter)), 9)); dt = dt.plusDays(1);	
		rencoder.fit_dynamic();
		
		
		int dim = rencoder.getBitDimension();

		//4 + 4 + 10 + 7 = 25
		assertEquals(25, dim);
		
		int[] expected = new int[] {1,0,0,0,0,0,0,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1};
		int[] enc = rencoder.transform(new TimeIndicator("economic", "global", new Temporal(dt.toString(formatter)), 10));
		

				
		assertArrayEquals(expected, enc);
		
		int[] expected2 = new int[] {0,0,0,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

		//-back to Monday
		dt = dt.plusDays(4);	
		
		int[] enc2 = rencoder.transform(new TimeIndicator("health", "EU", new Temporal(dt.toString(formatter)), -1));		
		assertArrayEquals(expected2, enc2);		
		
	}

}
