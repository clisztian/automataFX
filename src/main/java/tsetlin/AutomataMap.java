package tsetlin;

import java.util.HashMap;
import java.util.Map;

import output.CategoryLabel;
import output.OutputLabel;
import output.OutputStats;
import output.RealLabel;

/**
 * An automata -> output pair
 * 
 * Examples
 * 
 * Automata -> Real(regression) 
 * Automata -> Categorical (classification)
 * 
 * @author lisztian
 *
 * @param <V>
 */
public class AutomataMap<V> {

	private TsetlinMachine<V> automata;
	private OutputLabel output;
	private HashMap<Integer, OutputStats> in_sample_results;
	private int n_classes;
	
	public AutomataMap(TsetlinMachine<V> automata, OutputLabel output) {
		super();
		this.automata = automata;
		this.output = output;
		setIn_sample_results(new HashMap<Integer, OutputStats>());
		
		n_classes = 0;
		if(output instanceof CategoryLabel) {
			n_classes = ((CategoryLabel)output).getNumber_of_classes();
		}
		else if(output instanceof RealLabel) {
			n_classes = ((RealLabel)output).getTarget_resolution();
		}
		
		for(int i = 0; i < n_classes; i++) {
			in_sample_results.put(i, new OutputStats());
		}
	}
	
	public TsetlinMachine<V> getAutomata() {
		return automata;
	}
	public void setAutomata(TsetlinMachine<V> automata) {
		this.automata = automata;
	}
	public OutputLabel getOutput() {
		return output;
	}
	public void setOutput(OutputLabel output) {
		this.output = output;
	}

	public HashMap<Integer, OutputStats> getIn_sample_results() {
		return in_sample_results;
	}
	
	public void printInSampleResults() {
		
		for(Map.Entry<Integer, OutputStats> entry : in_sample_results.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue().getTrue_output() + " " + entry.getValue().getPred_output_correct() + " " + entry.getValue().getFalse_positive());
		}
		
	}
	

	public void setIn_sample_results(HashMap<Integer, OutputStats> in_sample_results) {
		this.in_sample_results = in_sample_results;
	}

	public void getClearResults() {
		
		for(Map.Entry<Integer, OutputStats> entry : in_sample_results.entrySet()) {
			entry.getValue().clear();
		}
		
	}
	
	
	
	
}
