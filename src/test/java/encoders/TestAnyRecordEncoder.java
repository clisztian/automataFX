package encoders;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import dataio.CSVInterface;
import records.AnyRecord;
import records.TimeIndicator;

public class TestAnyRecordEncoder {

	@Test
	public void TestAnyRecord() throws IOException, IllegalArgumentException, IllegalAccessException {
		
		CSVInterface csv = new CSVInterface("data/test_data.csv");	
		AnyRecord anyrecord = csv.createRecord();
		
		RecordEncoder<AnyRecord> encoder = new RecordEncoder<AnyRecord>().initiate(anyrecord);
		
		ArrayList<AnyRecord> all = csv.getAllRecords();
		
		for(AnyRecord any : all) {
			encoder.addValue(any);
		}
		
		encoder.fit_dynamic();
		int dim = encoder.getBitDimension();
		assertEquals(45, dim);
		
		int[] expected = new int[] {1,1,1,1,1,1,0,0,0,0,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1,1,0,0};
		int[] enc = encoder.transform(all.get(all.size() - 1));
		
		assertArrayEquals(expected, enc);
			
		csv.close();
	}

}
