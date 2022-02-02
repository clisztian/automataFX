package utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * A helper function to grab microseconds from a localDateTime object
 * @author lisztian
 *
 */
public class Microseconds {

	
	public static long toMicroseconds(LocalDateTime dt) {		
		Instant inst = dt.atZone(ZoneId.of("CET")).toInstant();   
		return inst.getEpochSecond();
    	//return TimeUnit.SECONDS.toMicros(inst.getEpochSecond()) + TimeUnit.NANOSECONDS.toMicros(inst.getNano());
	}
	
	
}
