package timeseries;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;




/**
 * Object for the TimeSeries class that holds a String referencing a
 * time stamp and an Information Bar that holds daily information.
 * 
 * Time stampls are typically made with a standard DataTimeFormatter, for example
 * "yyyy-MM-dd HH:mm:ss" or "yyyy-MM_dd" or "dd-MM-yyyy" 
 * 
 * @author Christian D. Blakely (christian.blakely@six-group.com)
 * 
 *
 */
public class TimeSeriesEntry<V>  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String timeStamp = null;
	
	private V value = null;
		
	/**
	 * Instantiate a time series object with the date and 
	 * Information bar
	 * @param date
	 * @param bar
	 */
	public TimeSeriesEntry(String date, V bar) {
		this.timeStamp = date;
		this.value = bar;
	}
	
	/**
	 * For a standard string date pattern, will grab the 
	 * time stamp at a joda-time object 
	 * @param pattern
	 * @return
	 */
	public DateTime getDateTime(String pattern) {
		return DateTimeFormat.forPattern(pattern).parseDateTime(timeStamp);
	}
	
	/**
	 * Grab the InformationBar value
	 * @return
	 */
	public V getValue() {
		return value;
	}

	/**
	 * Access the timestamp
	 * @return
	 */
	public String getTimeStamp() {
		return timeStamp;
	}

	
}
