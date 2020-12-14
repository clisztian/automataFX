package dynamics;

import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import encoders.RecordEncoder;
import records.AnyRecord;
import timeseries.TimeSeries;
import tsetlin.ConvolutionEncoder;
import utils.int2;

/**
 * Interface from records to input for clause learning
 * 
 * Includes a record encoder and a hierarchical encoder to map from 
 * 
 * record sequence -> bit sequence -> convolution encoder for convolutional learning
 * 
 * Sample length is the minimum in-sample sequence length for training
 * Window is the time-dependent patch size on which learning is achieved
 * 
 * @author lisztian
 *
 */
public class Evolutionize<V> {

	private int window;
	private int sample_length;

	private ArrayList<int[]> encoded_records;

	private RecordEncoder<V> encoder;
	private ConvolutionEncoder conv_encoder;
	private int dim_x;
	private Histories historical;
	private int historical_length;
	
	/**
	 * Evolutionize instantiates a learning framework using a window and a lag_length
	 * 
	 * @param window total size of the learning period
	 * @param lag_length 
	 */
	public Evolutionize(int window, int sample_length, int historical_length) {
		
		this.window = window;
		this.sample_length = sample_length;
		this.historical_length = historical_length;
		encoded_records = new ArrayList<int[]>();
		encoder = new RecordEncoder<V>();		
	}
	
	/**
	 * Initiate the evolutionizer with any record, a datetime format and real encoder dimension
	 * @param record
	 * @param datetime_format
	 * @param dim
	 */
	public void initiate(AnyRecord record, String datetime_format, int dim) {		
		encoder.initiate(record, datetime_format, dim);		
	}
	
	/**
	 * Initiate the internal encoder with an anyrecord
	 * @param record
	 */
	public void initiate(AnyRecord record) {
		encoder.initiate(record);
	}
	
	public void initiate(Class<?> val) {		
		encoder.initiate(val);
	}
	
	public void fit() {
		encoder.fit_dynamic();
	}
	
	public void initiateConvolutionEncoder() {
		
		dim_x = encoder.getBitDimension();
		conv_encoder = new ConvolutionEncoder(dim_x, sample_length, window);
		historical = new Histories(sample_length, dim_x);
	}
	
	
	public void addValue(V val) throws IllegalArgumentException, IllegalAccessException {		
		encoder.addValue(val);
	}
	

		
	public TimeSeries<int[]> encode(ArrayList<V> records) throws IllegalArgumentException, IllegalAccessException {
		
		TimeSeries<int[]> series = new TimeSeries<int[]>();
		
		for(int i = 0; i < records.size(); i++) {
			series.add(""+i, encoder.transform(records.get(i)));;	
		}
			
		return series;	
	}
	

	/**
	 * Adds a new value and updates the historical data to reflect new observation value
	 * Returns the latest sample ready to be inputed into covolutional model
	 * @param val
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public int[] encode_add(V val) throws IllegalArgumentException, IllegalAccessException {
		
		int[] encoded_val = encoder.transform(val);
		encoded_records.add(encoded_val);		
		historical.addHistory(encoded_val);
		
		return flatten(historical.getHistories());			
	}
	
	
	
	public int getWindow() {
		return window;
	}

	public void setWindow(int window_lag) {
		this.window = window_lag;
	}

	public RecordEncoder<V> getEncoder() {
		return encoder;
	}

	public void setEncoder(RecordEncoder<V> encoder) {
		this.encoder = encoder;
	}

	public ConvolutionEncoder getConv_encoder() {
		return conv_encoder;
	}

	public void setConv_encoder(ConvolutionEncoder conv_encoder) {
		this.conv_encoder = conv_encoder;
	}

	public Histories getHistorical() {
		return historical;
	}

	public void setHistorical(Histories historical) {
		this.historical = historical;
	}
	
	private static int[] flatten(int[][] data) {
	    return Stream.of(data)
                .flatMapToInt(IntStream::of)
                .toArray();
	}
	
	public int getEncoderDimension() {
		return dim_x;
	}
}
