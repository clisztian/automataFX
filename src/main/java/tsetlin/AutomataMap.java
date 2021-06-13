package tsetlin;

import output.OutputLabel;

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
	
	
	public AutomataMap(TsetlinMachine<V> automata, OutputLabel output) {
		super();
		this.automata = automata;
		this.output = output;
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
	
	
	
	
}
