package encoders;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestCategoricalEncoder {

	@Test
	public void testBasicCategoricalEncoder() {
		
		CategoricalEncoder catenc = new CategoricalEncoder("Weather");
		
		catenc.addValue("Sunny");
		catenc.addValue("Sunny");
		catenc.addValue("Sunny");
		catenc.addValue("Sunny");
		catenc.addValue("Sunny");
		catenc.addValue("Cloudy");
		catenc.addValue("Cloudy");
		catenc.addValue("Cloudy");
		catenc.addValue("Cloudy");
		catenc.addValue("Cloudy");
		catenc.addValue("Cloudy");
		catenc.addValue("Cloudy");
		catenc.addValue("Rainy");
		catenc.addValue("Rainy");
		catenc.addValue("Rainy");
		catenc.addValue("Snow");
		catenc.addValue("Snow");
		catenc.addValue("Snow");
		catenc.addValue("Snow");
		catenc.addValue("Windy");
		
		catenc.fit_uniform();

		
		int[] transcloudy = catenc.transform("Cloudy");
		
		assertEquals(1, transcloudy[0]);
		assertEquals(0, transcloudy[1]);
		assertEquals(0, transcloudy[2]);
		assertEquals(0, transcloudy[3]);
		assertEquals(0, transcloudy[4]);
		
		int[] transrainy = catenc.transform("Rainy");
		
		assertEquals(0, transrainy[0]);
		assertEquals(0, transrainy[1]);
		assertEquals(0, transrainy[2]);
		assertEquals(1, transrainy[3]);
		assertEquals(0, transrainy[4]);
		
		
		

		
	}

}
