package examples;

import java.util.ArrayList;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import dynamics.Evolutionize;
import encoders.Temporal;
import records.AnyRecord;
import records.TimeTrois;
import tsetlin.MultivariateConvolutionalAutomatonMachine;

public class TimeSeriesClassification {


	private Random rng;
	private ArrayList<TimeTrois> input_data;
	private ArrayList<Integer> labels;
	private String date_time_format = "yyyy-MM-dd HH:mm:ss";
	
	private DateTimeFormatter date_formatter;
	private Evolutionize evolve;
	
	private int dim_x = 20; //the real-valued dimension
	private int dim_y = 30; //the global lag dimension
	private int patch_dim_y = 5; //the sliding window function dimension
	private int n_samples = 200;
	
	
	public TimeSeriesClassification() {
		rng = new Random();
		date_formatter = DateTimeFormat.forPattern(date_time_format);
	}
	
	
	
	public void sampleData(int n) {
		
		input_data = new ArrayList<TimeTrois>();
		labels = new ArrayList<Integer>();
		
		DateTime dt = new DateTime(2020, 12, 23, 1, 0);
		
		for(int t = 0; t < n; t++) {
			
			double val_1 = series_3(t);
			double val_2 = series_2(t);
			double val_3 = series_1(t);
			
			if(dt.getDayOfMonth() < 10) {				
				input_data.add(new TimeTrois(new Temporal(dt.toString(date_formatter)), val_1, val_2, val_3));
				labels.add(0);
			}
			else if(dt.getDayOfMonth() >= 10 && dt.getDayOfMonth() < 20) {
				input_data.add(new TimeTrois(new Temporal(dt.toString(date_formatter)), val_2, val_1, val_3));
				labels.add(1);
			}
			else {
				input_data.add(new TimeTrois(new Temporal(dt.toString(date_formatter)), val_3, val_2, val_1));
				labels.add(2);
			}			
			//System.out.println(input_data.get(t).time().getDate_time_string() + " " + input_data.get(t).val_1() + " " + labels.get(t));		
			dt = dt.plusHours(1);
		}				
	}
	
	
	
	public double series_1(int t) {
		return 2.0 + Math.sin(t*.10*Math.PI)*Math.cos(t*.02*Math.PI) + rng.nextGaussian()*.1;
		
	}
	
	public double series_2(int t) {
		return -2.0 + Math.sin(t * 0.02f * 2f * Math.PI) * .25f + Math.sin(t * .05f * 2f * Math.PI)*0.25f + rng.nextGaussian()*.6;
	}
	
	public double series_3(int t) {
		return rng.nextGaussian()*.5;
	}



	public ArrayList<TimeTrois> getInput_data() {
		return input_data;
	}



	public void setInput_data(ArrayList<TimeTrois> input_data) {
		this.input_data = input_data;
	}



	public ArrayList<Integer> getLabels() {
		return labels;
	}



	public void setLabels(ArrayList<Integer> labels) {
		this.labels = labels;
	}
	
	
	
	public void classification(int N) throws IllegalArgumentException, IllegalAccessException {
		
		
		sampleData(N);
		
		/**
		 * Initiate 
		 */
		evolve = new Evolutionize(patch_dim_y, dim_y, 1);		
		evolve.initiate(TimeTrois.class, dim_x);
		evolve.initiateConvolutionEncoder();
		
		
		/**
		 * Add some in sample values to learn bounds
		 */
		for(int i = 0; i < input_data.size()/4; i++) {	
			evolve.addValue(input_data.get(i));
		}
		evolve.fit();
		
		
		/**
		 * Define and setup automata machine 
		 */
		int threshold = 160;
		int nClauses = 32;
		float S = 10f;
		float max_specificity = 2f;
		int nClasses = 3;
		
		MultivariateConvolutionalAutomatonMachine conv = new MultivariateConvolutionalAutomatonMachine(evolve.getConv_encoder(), threshold, nClasses, nClauses, max_specificity, true); 
		
		
		int n_train = (int)(N*.80);
		for(int i = 0; i < n_train; i++) {
			
			TimeTrois input = input_data.get(i);
			int label = labels.get(i);
			evolve.add(input);
			
			
			int pred = conv.update(evolve.get_last_sample(), label);			
			System.out.println(i + " " + input.time().getDate_time_string() + " " + input.val_1() + " " + pred + " " + label);
		}
		long end = System.currentTimeMillis();
		
		System.out.println("Out-of-sample");
		int false_pred = 0;
		for(int i = n_train; i < N; i++) {
			
			TimeTrois input = input_data.get(i);
			int label = labels.get(i);
			
			evolve.add(input);
			int pred = conv.predict(evolve.get_last_sample());	
			System.out.println(i + " " + input.time().getDate_time_string() + " " + input.val_1() + " " + pred + " " + label);
			
			false_pred += (pred != label) ? 1 : 0;
		}
		
		
	}
	
	
	
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
		
		TimeSeriesClassification classify = new TimeSeriesClassification();		
		classify.classification(10000);
		
	}
	
	
	
}





