package output;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestCategoryLabel {

	@Test
	public void testCategoryLabel() {
		
		CategoryLabel cat_lab = new CategoryLabel(4);
		
		String[] cats = new String[] {"very cold", "cold" , "warm", "hot"};
		
		int lab = (int) cat_lab.getLabel(cats[0]);
		int lab1 = (int) cat_lab.getLabel(cats[0]);
		
		int lab2 = (int) cat_lab.getLabel(cats[1]);
		int lab3 = (int) cat_lab.getLabel(cats[2]);
		int lab4 = (int) cat_lab.getLabel(cats[3]);
		
		assertEquals(0, lab);
		assertEquals(0, lab1);
		
		assertEquals(1, lab2);
		assertEquals(2, lab3);
		assertEquals(3, lab4);
		
	
		assertEquals(0, (int) cat_lab.getLabel(cats[0]));
		
		assertEquals("cold", cat_lab.decode(1));
		assertEquals("warm", cat_lab.decode(2));
		assertEquals("hot", cat_lab.decode(3));
		
	}

}
