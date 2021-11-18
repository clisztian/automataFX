package examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.csvreader.CsvReader;

import javafx.application.Platform;
import output.CategoryLabel;
import output.OutputLabel;
import output.OutputStats;
import output.RealLabel;
import records.AnyRecord;
import records.CSVTableView;
import records.RecordColumn;
import records.RecordColumn.Type;
import tsetlin.AutomataMap;
import tsetlin.TsetlinMachine;

/**
 * A testing interface for multi-class clause sharing using a simple data set
 * @author lisztian
 *
 */
public class TestRecordToClauseShare {

	private ArrayList<AutomataMap<AnyRecord>> automata;
	private CsvReader marketDataFeed = null;
	private String[] field_names;
	private int total_columns;
	private String[] raw_headers;
	private AnyRecord anyRecord;
	private boolean[] categorical;
	private ArrayList<OutputLabel> output_labels;
	
	private int N_CLASSES = 50;
	private int dim_y = 1;
	private int patch_size = 1;
	private int n_bits = 10;
	private int n_clauses = 100;
	private int n_threshold = 50;
	private float specificity = 2f;
	private float dropout = 0;
	private ArrayList<AnyRecord> records;
	private Random rng;
	
//	automaton_panel.getDim_y(), 
//	automaton_panel.getPatch_size(), 
//	automaton_panel.getN_bits(), 
//	data_table.getAnyRecord(),
//	automaton_panel.getN_clauses(), 
//	automaton_panel.getThreshold(),  
//	automaton_panel.getSpecificity(), 
//	n_classes, 
//	encode_hours.isSelected(), 
//	encode_day_month.isSelected(), 
//	encode_months.isSelected(), 
//	encode_weeks.isSelected(), 
//	automaton_panel.getDropout_rate(),
//	"yyyy-MM-dd");
	

	/**
	 * Opens a market data feed from a standard csv file
	 * Reads the headers, so requires a header
	 * @param file
	 * @throws IOException
	 */
	public TestRecordToClauseShare(File file) throws IOException  {

		try {
			marketDataFeed = new CsvReader(file.getAbsolutePath());
			marketDataFeed.readHeaders();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		rng = new Random(2334);
	}
	
	
	/**
	 * Creates a record out of the first record in the file
	 * @throws IOException
	 */
	public void createRecord() throws IOException {
		
		if(marketDataFeed != null)  {
			
			
			field_names = marketDataFeed.getHeaders();
			
			
			
			total_columns = field_names.length;
			raw_headers = field_names;
			
			categorical = new boolean[total_columns];

			if(marketDataFeed.readRecord()) {
			
				String[] raw_vals = marketDataFeed.getValues();
				for(int i = 0; i < total_columns; i++) {
					
					System.out.println(field_names[i] + " " + raw_vals[i].trim());
					
					if(NumberUtils.isCreatable(raw_vals[i].trim())) {
						categorical[i] = false;
					}
					else {
						categorical[i] = true;
					}
				}
							
				anyRecord = createRecord(field_names, categorical);
			}	
		}
	}
	
	/**
	 * Sets all indices in record to Real Values except for the indices given in the passed array
	 * Sets label_ind indices in anyRecord to real labels (regression)
	 * @param label_ind
	 */
	public void setLabelIndex(int[] label_ind) {
		
		Type[] anyTypes = new Type[total_columns];
		
		//by default, use a real label
		for(int i = 0; i < total_columns; i++) anyTypes[i] = Type.REAL;
		
		for(int i = 0; i < label_ind.length; i++) {
			
			if(label_ind[i] < total_columns) {
				anyTypes[label_ind[i]] = Type.REAL_LABEL;
			}
		}
		
		anyRecord.setType(anyTypes);
		
	}
	

	
	
	public void buildMachine() throws IOException, IllegalArgumentException, IllegalAccessException {
		
		records = getAllRecords();
		output_labels = new ArrayList<OutputLabel>();
		
		/*
		 * Discretize the outputs
		 */
		
		for(int i = 0; i < anyRecord.getType().length; i++) {
			
			if(anyRecord.getType()[i].equals(Type.REAL_LABEL)) {
				
				DescriptiveStatistics columnData = new DescriptiveStatistics();	
				for(AnyRecord record : records) {
					
					Float val = (Float)(record.getValues()[i]);
					columnData.addValue(val.doubleValue());
				}
								
				RealLabel real_label = new RealLabel(N_CLASSES, (float)columnData.getMin(), (float)columnData.getMax());
				real_label.setStats(columnData);
				real_label.setName(anyRecord.getField_names()[i]);
				output_labels.add(real_label);
			
			}			
		}
		
		
		automata = new ArrayList<AutomataMap<AnyRecord>>();
		
		for(OutputLabel out : output_labels) {
						
			
			TsetlinMachine<AnyRecord> automaton = new TsetlinMachine<AnyRecord>(
					dim_y, 
					patch_size, 
					n_bits, 
					anyRecord,
					n_clauses, 
					n_threshold,  
					specificity, 
					N_CLASSES, 
					false, 
					false, 
					false, 
					false, 
					dropout,
					"yyyy-MM-dd");
			
			
			automata.add(new AutomataMap<AnyRecord>(automaton, out));
			
		}
		
		
		for(AutomataMap<AnyRecord> map : automata) {			
			map.getAutomata().add_fit(records);
		}
		
		
	}
	
	public void train(int n_epochs, float split) throws IllegalArgumentException, IllegalAccessException {
		
		
		ArrayList<AnyRecord> train_set = new ArrayList<AnyRecord>();
		ArrayList<AnyRecord> test_set = new ArrayList<AnyRecord>();
		
		for(AnyRecord rec : records) {
			
			if(rng.nextFloat() < split) train_set.add(rec);
			else test_set.add(rec);
			
		}
		
		
		for(int i = 0; i < n_epochs; i++) {
			
			System.out.println("Epoch " + i);
			
			
			System.out.println("Applying drop clause");
			for(AutomataMap<AnyRecord> auto : automata) {
				auto.getAutomata().drop_clauses();
				auto.getClearResults();
			}
			
			for(AnyRecord record : train_set) {
				
				for(AutomataMap<AnyRecord> auto : automata) {
					
					int label = 0;
					if(auto.getOutput() instanceof RealLabel) {					
						label = (int)((RealLabel)auto.getOutput()).getLabel((float)record.getMap().get(((RealLabel)auto.getOutput()).getName()));
					}
					else if(auto.getOutput() instanceof CategoryLabel) {
						
						String name = ((CategoryLabel)auto.getOutput()).getRecordColumn().getName();							
						label = (int)((CategoryLabel)auto.getOutput()).getLabel(record.getMap().get(name).toString());
					}
					int out = auto.getAutomata().update(record, label);		
									
				}						
			}	
			
			System.out.println("Finished Epoch " + i + ": computing clause importance");
			automata.get(0).getAutomata().computeClauseImportance();
			automata.get(0).getAutomata().computeClauseFeatureImportance();
						
			evaluateModel(test_set);
		}
		
		
		
		
	}
	
	
	public static void main(String[] args) throws IOException, IllegalArgumentException, IllegalAccessException {
		
		File file = new File("src/main/resources/data/ENB2012_data.csv");		
		TestRecordToClauseShare auto = new TestRecordToClauseShare(file);
		
		
		auto.createRecord();
		auto.setLabelIndex(new int[] {8,9});		
		auto.buildMachine();
				
		auto.train(10, .7f);
	}
	
	
	
	
	/**
	 * Gets all the records in the csv file as an arraylist
	 * @return
	 * @throws IOException
	 */
	private ArrayList<AnyRecord> getAllRecords() throws IOException {
		
		ArrayList<AnyRecord> all = new ArrayList<AnyRecord>();
		while (marketDataFeed.readRecord()) {
			
			AnyRecord rec = getOneRecord();
			all.add(rec);			
		}
		return all;	
	}
	
	
	
	private AnyRecord getOneRecord() throws IOException {
		
		if(raw_headers == null) {
			return null;
		}
				
		AnyRecord anyRecord = new AnyRecord();
		Object[] values = new Object[raw_headers.length];
		Map<String, Object> label_map = new HashMap<String, Object>();

		for(int i = 0; i < raw_headers.length; i++) {
			
			String val = marketDataFeed.get(i);
			if(NumberUtils.isCreatable(val.trim())) {
				values[i] = Float.parseFloat(val.trim());
			}
			else values[i] = val;
			
			if(this.anyRecord.getType()[i] == Type.REAL_LABEL || this.anyRecord.getType()[i] == Type.CLASS_LABEL) {
				label_map.put(this.anyRecord.getField_names()[i], values[i]);
				System.out.println(this.anyRecord.getField_names()[i] + " " + values[i]);
			}
			
		}
		
		anyRecord.setMap(label_map);
		anyRecord.setType(this.anyRecord.getType());
		anyRecord.setValues(values);
		
		return anyRecord;	
	}
	
	public static AnyRecord createRecord(String[] headers, boolean[] categorical) {
		
		AnyRecord record = new AnyRecord();
		
		String[] fields = new String[headers.length];
		Object[] values = new Object[headers.length];
		
		Type[] types = new Type[headers.length];
		
		for(int i = 0; i < headers.length; i++) {
			
			if(categorical[i]) {
				fields[i] = headers[i];
				values[i] = new String("categorical");
				types[i] = Type.CATEGORY;
			}
			else {
				fields[i] = headers[i];
				values[i] = (Float)0f;		
				types[i] = Type.REAL;
			}
		}
		record.setField_names(fields);
		record.setValues(values);
		record.setType(types);
		
		return record;		
	}	
	
	
	public void evaluateModel(ArrayList<AnyRecord> test_set) throws IllegalArgumentException, IllegalAccessException {
		

		for(AnyRecord record : test_set) {
			
			for(AutomataMap<AnyRecord> auto : automata) {
				
				int label = 0;
				if(auto.getOutput() instanceof RealLabel) {					
					label = (int)((RealLabel)auto.getOutput()).getLabel((float)record.getMap().get(((RealLabel)auto.getOutput()).getName()));
				}
				else if(auto.getOutput() instanceof CategoryLabel) {
					
					String name = ((CategoryLabel)auto.getOutput()).getName();							
					label = (int)((CategoryLabel)auto.getOutput()).getLabel(record.getMap().get(name).toString());
				}
				int out = auto.getAutomata().fast_predict(record);	
					
				OutputStats stat = auto.getIn_sample_results().get(label);
				stat.true_output_inc();
				if(label == out) {
					stat.pred_output_correct_inc();
				}
				else {
					auto.getIn_sample_results().get(out).false_positive_inc();
				}
				
				System.out.println(label + " " + out);
				
			}				
		}
		automata.get(1).printInSampleResults();
		automata.get(0).getAutomata().computeFeatureImportance();
		
	
		
		int n_real = automata.get(0).getIn_sample_results().size();
		int[] labels = new int[n_real];
		int[][] vals = new int[n_real][3];
		
		for(int i = 0; i < n_real; i++) {
			
			OutputStats stats = automata.get(0).getIn_sample_results().get(i);
			int discrepency = stats.getTrue_output() - stats.getPred_output_correct();
			
			labels[i] = stats.getLabel_class();
			vals[i][0] = discrepency;
			vals[i][1] = stats.getPred_output_correct();
			vals[i][2] = stats.getFalse_positive();
		}
			
		
	}
	
	
	
	
}
