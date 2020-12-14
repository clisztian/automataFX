package output;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestRealLabel {

	@Test
	public void test() {
		
		RealLabel label = new RealLabel(10, 0, 30);
		
		assertEquals(0, label.getLabel(0f));
		assertEquals(1, label.getLabel(4f));
		assertEquals(9, label.getLabel(29f));
		assertEquals(30f, (float)label.decode((int)9), .8);
	}

}
