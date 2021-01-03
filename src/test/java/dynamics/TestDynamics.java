package dynamics;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import dataio.CSVInterface;
import encoders.RecordEncoder;
import records.AnyRecord;

public class TestDynamics {

	@Test
	public void testDynamics() throws IOException, IllegalArgumentException, IllegalAccessException {
		
		/**
		 * Get some data
		 */
		CSVInterface csv = new CSVInterface("data/test_data.csv");
		
		//create record from data
		AnyRecord anyrecord = csv.createRecord();
		
		//grab some more data
		ArrayList<AnyRecord> all = csv.getAllRecords();
		
		
		///initialize the evolution object with window (x-axis convolutional patct), sample size (number of lags to con
		int window_size = 2;
		int sample_size = 6;
		int historical_size = 1;	
		Evolutionize<AnyRecord> evolve = new Evolutionize<AnyRecord>(window_size, sample_size);	
		
		//initiate the internal encoder with a dataset
		evolve.initiate(anyrecord);
		
		//add data and fit
		for(AnyRecord any : all) {
			evolve.addValue(any);
		}		
		evolve.fit();		
		evolve.initiateConvolutionEncoder();
		
		System.out.println(evolve.getEncoderDimension());
		
		assertEquals(45, evolve.getEncoderDimension());
		
		int[] output = evolve.encode_add(all.get(0));
			
		assertEquals(30, output.length);

		
		
//		int[] partone = new int[]{1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//		int[] parttwo = new int[45*5];
//		int[] expected = ArrayUtils.addAll(partone, parttwo); 
//		
//		assertArrayEquals(expected, output);
		
//		output = evolve.encode_add(all.get(1));
//		
//		int[] partthree = new int[] {1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0};
//		int[] first =  ArrayUtils.addAll(partthree, partone);
//		expected = ArrayUtils.addAll(first, new int[45*4]);
//		assertArrayEquals(expected, output);
				
		
		
	}

}
