package encoders;

import static org.junit.Assert.*;

import org.junit.Test;

import encoders.TimeEncoder.TimeEncoderResult;
import encoders.TimeEncoder.Time_Encoder;

public class TestTimeEncoder {

	@Test
	public void testTimeDateEncoder() {
		
		

		TimeEncoder encoder = new TimeEncoder("yyyy-MM-dd HH:mm:ss");
		
		encoder.addTimeEncoder(Time_Encoder.HOUR_OF_DAY);
		encoder.addTimeEncoder(Time_Encoder.DAY_OF_MONTH);
		
		
		int[] time_enc = encoder.transform(new Temporal("2020-11-10 20:35:32"));		
		int dim = encoder.getBitDimension();		
		TimeEncoderResult[] decoded = encoder.decode(time_enc);
		
		assertEquals(62, dim);
		assertEquals(2, decoded[0].getValue());
		assertEquals(20, decoded[1].getValue());
		assertEquals(10, decoded[2].getValue());
		assertEquals(Time_Encoder.DAY_OF_WEEK, decoded[0].getTime_encoder());
		assertEquals(Time_Encoder.HOUR_OF_DAY, decoded[1].getTime_encoder());
		assertEquals(Time_Encoder.DAY_OF_MONTH, decoded[2].getTime_encoder());
		
	}

}
