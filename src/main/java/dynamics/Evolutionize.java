package dynamics;

import java.util.ArrayList;

import encoders.RecordEncoder;

/**
 * Interface from records to input for clause learning
 * @author lisztian
 *
 */
public class Evolutionize<V> {

	private int window;
	private int sample_length;

	private Object[] historical_data;
	private RecordEncoder<V> encoder;
	
	
	/**
	 * Evolutionize instantiates a learning framework using a window and a lag_length
	 * 
	 * @param window total size of the learning period
	 * @param lag_length 
	 */
	public Evolutionize(int window, int sample_length) {
		
		this.window = window;
		this.sample_length = sample_length;
		
		encoder = new RecordEncoder<V>();
		
	}
	
	public void initiate(Class<?> val) {
		
		encoder.initiate(val);
	}
	
	public void addValue(V val) throws IllegalArgumentException, IllegalAccessException {		
		encoder.addValue(val);
	}
	
	public ArrayList<int[]> encodeSample() {
		
		ArrayList<int[]> sample = new ArrayList<int[]>();
		
		
		
		return sample;		
	}
	
//
//	public TimeSeries<int[]> evolutionize(ArrayList<EncodedExperiment> experiments) {
//		
//		TimeSeries<int[]> encoded = new TimeSeries<int[]>();
//		
//		int new_experiment_day = 0;
//		int global_count = window_size;
//		
//		System.out.println("Experiments size: " + experiments.size());
//		while(global_count < experiments.size() - 1) {
//							
//			if(experiments.get(global_count).getTimestamp() < experiments.get(global_count+1).getTimestamp()) {
//				new_experiment_day++;
//			}
//			
//			if(new_experiment_day == n_days) {
//				
//				int[] flattend_evolution = experiments.get(global_count - (window_size - 1)).getEncoded_experiment();							
//				for(int k = 1; k < window_size; k++) {
//					flattend_evolution = ArrayUtils.addAll(flattend_evolution, experiments.get(global_count - (window_size - 1) + k).getEncoded_experiment());
//				}
//						
//				encoded.add("" + experiments.get(global_count).getTimestamp(), flattend_evolution);
//				new_experiment_day = 0;
//			}
//			global_count++;	
//		}
//		return encoded;		
//	}

	
	
	
	public int getWindow() {
		return window;
	}

	public void setWindow(int window_lag) {
		this.window = window_lag;
	}

	public RecordEncoder getEncoder() {
		return encoder;
	}

	public void setEncoder(RecordEncoder encoder) {
		this.encoder = encoder;
	}
	
}
