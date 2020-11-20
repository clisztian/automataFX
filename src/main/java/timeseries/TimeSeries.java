package timeseries;

import java.util.ArrayList;
import java.util.Collection;



/**
 * The underlying time series class that holds a sequence of 
 * information bars. 
 * 
 * The TimeSeries extends the ArrayList class
 * @author Christian D. Blakely (christian.blakely@six-group.com)
 * 
 *
 */
public class TimeSeries<V> extends ArrayList<TimeSeriesEntry<V>> {
	
	private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    public static final TimeSeries EMPTY_SERIES = new TimeSeries<>(0);
	
	public TimeSeries() {
		super();
	}

	public TimeSeries(Collection<? extends TimeSeriesEntry<V>> c) {
		super(c);
	}

	public TimeSeries(int initialCapacity) {
		super(initialCapacity);
	}
	
	public void add(String timeStamp, V value) {
		add(new TimeSeriesEntry<V>(timeStamp, value));
	}

    @SuppressWarnings("unchecked")
    public static final <V> TimeSeries<V> empty() {
        return EMPTY_SERIES;
    }
    
    public TimeSeriesEntry<V> last() {
        return get(size() - 1);
    }


    
    

}

