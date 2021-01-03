package examples;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.csvreader.CsvReader;

import interpretability.Prediction;
import records.AnyRecord;
import tsetlin.AutomataLearning;

public class EEGInterfaceExample {


	
	public static void main(String[] args) throws IOException, IllegalArgumentException, IllegalAccessException {
		
		
		ArrayList<double[]> eeg_samples = new ArrayList<double[]>();	
		CsvReader marketDataFeed = new CsvReader(new FileReader("/home/lisztian/Downloads/eeg_time.csv"), ',');	
		marketDataFeed.readHeaders();
		
		while(marketDataFeed.readRecord()) {
							
			double[] doubleValues = Arrays.stream(marketDataFeed.getRawRecord().split("[,]+"))
                    .mapToDouble(Double::parseDouble)
                    .toArray();
			
			eeg_samples.add(doubleValues);
		}
		
		ArrayList<AnyRecord> in_sample = new ArrayList<AnyRecord>();
		for(int i = 0; i < eeg_samples.get(0).length-1; i++) {	
			in_sample.add(new AnyRecord(new Double[] {eeg_samples.get(0)[i]}));
		}
		for(int i = 0; i < eeg_samples.get(19).length-1; i++) {
			in_sample.add(new AnyRecord(new Double[] {eeg_samples.get(19)[i]}));
		}
		
		
		
		AnyRecord any = new AnyRecord(new String[] {"value"}, new Double[] {0.0});
		
		int dim_x = 20;
		int dim_y = 7200;
		int patch_dim_y = 20;
		int n_samples = 200;
		int threshold = 160;
		int nClauses = 32;
		float S = 10f;
		float max_specificity = 2f;
		int nClasses = 2;
		
			
		
		AutomataLearning<AnyRecord> automata = new AutomataLearning<AnyRecord>(dim_y, patch_dim_y, dim_x, any, nClauses, threshold, max_specificity, nClasses);
		automata.add_fit(in_sample);
		
		
		ArrayList<Integer> training_sample_list = new ArrayList<Integer>();
		for(int i = 0; i < n_samples; i++) training_sample_list.add(i);
		Collections.shuffle(training_sample_list);
		
		int n_train = 140;
		for(int i = 0; i < n_train; i++) {
			
			int samp = training_sample_list.get(i);
			int label = getLabel(eeg_samples.get(samp));
			int pred = automata.update(getRecord(eeg_samples.get(samp)), label);
			
			System.out.println(i + " " + samp + " " + pred + " " + label);
		}
		
		int false_pred = 0;
		for(int i = n_train; i < training_sample_list.size(); i++) {
			
			int samp = training_sample_list.get(i);
			int label = getLabel(eeg_samples.get(samp));
			Prediction pred = automata.predict(getRecord(eeg_samples.get(samp)));
			System.out.println(i + " " + samp + " " + pred.getPred_class() + " " + label);
			false_pred += (pred.getPred_class() != label) ? 1 : 0;
		}
		System.out.println("Accuracy: " + 1.0*(training_sample_list.size() - n_train - false_pred)/(1.0*training_sample_list.size() - 1.0*n_train) + " " + false_pred);
		
		
		
		
	}
	
	public static ArrayList<AnyRecord> getRecord(double[] vals) throws IllegalArgumentException, IllegalAccessException {
		
		ArrayList<AnyRecord> records = new ArrayList<AnyRecord>();
		for(int i = 0; i < vals.length-1; i++) {
			records.add(new AnyRecord(new Double[] {vals[i]}));
		}
		return records;
	}
	
	public static int getLabel(double[] vals) {		
		return (int)vals[vals.length - 1];	
	}
}
