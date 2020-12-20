package examples;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import com.csvreader.CsvReader;

import dynamics.Evolutionize;
import records.AnyRecord;
import tsetlin.MultivariateConvolutionalAutomatonMachine;

public class EEGRawExample {
	
	
	private ArrayList<double[]> eeg_samples;
	private Evolutionize evolve;
	private CsvReader marketDataFeed;
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws IOException, IllegalArgumentException, IllegalAccessException {
		
		EEGRawExample raw = new EEGRawExample();
		raw.testEEGRawExample();
		
	}

	
	public void testEEGRawExample() throws IllegalArgumentException, IllegalAccessException {
		
		EEGRawExample eeg = new EEGRawExample();
		try {
			eeg.readEEGData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Random rng = new Random();
		int dim_x = 20;
		int dim_y = 7200;
		int patch_dim_y = 40;
		int n_samples = 200;
		
	
		/**
		 * Initiate encoder
		 */
		AnyRecord any = new AnyRecord(new String[] {"value"}, new Double[] {0.0});
				
		evolve = new Evolutionize(patch_dim_y, dim_y, 1);		
		evolve.initiate(any, dim_x);
		evolve.initiateConvolutionEncoder();
		
		
		/**
		 * Add some data and fit
		 */
		ArrayList<double[]> eeg_samples = eeg.getEEGData();
		
		for(int i = 0; i < eeg_samples.get(0).length-1; i++) {	
			evolve.addValue(new AnyRecord(new Double[] {eeg_samples.get(0)[i]}));
		}
		for(int i = 0; i < eeg_samples.get(19).length-1; i++) {
			evolve.addValue(new AnyRecord(new Double[] {eeg_samples.get(19)[i]}));
		}
		evolve.fit();
			
	
		int threshold = 160;
		int nClauses = 32;
		float S = 10f;
		float max_specificity = 2f;
		int nClasses = 2;

		MultivariateConvolutionalAutomatonMachine conv = new MultivariateConvolutionalAutomatonMachine(evolve.getConv_encoder(), threshold, nClasses, nClauses, max_specificity, true); 
		ArrayList<Integer> training_sample_list = new ArrayList<Integer>();
		for(int i = 0; i < n_samples; i++) training_sample_list.add(i);
		Collections.shuffle(training_sample_list);
		
		long start = System.currentTimeMillis();
		int n_train = 140;
		for(int i = 0; i < n_train; i++) {
			
			int samp = training_sample_list.get(i);
			int label = getLabel(eeg_samples.get(samp));
			int pred = conv.update(getRecord(eeg_samples.get(samp)), label);
			
			System.out.println(i + " " + samp + " " + pred + " " + label);
		}
		long end = System.currentTimeMillis();
		
		
		System.out.println("Time training: " + (end - start));
		int false_pred = 0;
		for(int i = n_train; i < training_sample_list.size(); i++) {
			
			int samp = training_sample_list.get(i);
			int label = getLabel(eeg_samples.get(samp));
			int pred = conv.predict(getRecord(eeg_samples.get(samp)));	
			System.out.println(i + " " + samp + " " + pred + " " + label);
			false_pred += (pred != label) ? 1 : 0;
		}
		System.out.println("Accuracy: " + 1.0*(training_sample_list.size() - n_train - false_pred)/(1.0*training_sample_list.size() - 1.0*n_train) + " " + false_pred);
		System.out.println("Top Patterns for class 0");
		conv.getTopClausePatterns(0);
		System.out.println("Top Patterns for class 1");
		conv.getTopClausePatterns(1);
		
		
		
		
	}
	
	

	
	public int[] getRecord(double[] vals) throws IllegalArgumentException, IllegalAccessException {
		for(int i = 0; i < vals.length-1; i++) {
			evolve.add(new AnyRecord(new Double[] {vals[i]}));
		}
		return evolve.get_last_sample();
	}
	
	
	public int getLabel(double[] vals) {
		return (int)vals[vals.length - 1];
	}
	
		
	public void readEEGData() throws IOException {
		
		eeg_samples = new ArrayList<double[]>();
		
		marketDataFeed = new CsvReader(new FileReader("/home/lisztian/Downloads/eeg_time.csv"), ',');
		
		marketDataFeed.readHeaders();
		
		while(marketDataFeed.readRecord()) {
							
			double[] doubleValues = Arrays.stream(marketDataFeed.getRawRecord().split("[,]+"))
                    .mapToDouble(Double::parseDouble)
                    .toArray();
			
			eeg_samples.add(doubleValues);
		}
		
	}
	
	public ArrayList<double[]> getEEGData() {
		return eeg_samples;
	}
	
	
}
