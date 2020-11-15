package records;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class TestSimpleIndicator {

	@Test
	public void test() {
		
		String name = "anyname";
		String type = "anytype";
		String date = "2020-10-17";
		double val  = 2.3;
		
		SimpleIndicator ind = new SimpleIndicator(name, type, date, val);
		
		assertEquals(name, ind.name());
	    assertEquals(date, ind.timestamp());
		assertEquals(val, ind.value(),.0001);
				
	}
	

	
	public static List<Field> getPrivateFields(Class<?> theClass){
        LinkedList<Field> privateFields = new LinkedList<Field>();

        Field[] fields = theClass.getDeclaredFields();

        for(Field field:fields){
        	privateFields.add(field);
        }
        return privateFields;
    }
	

}
