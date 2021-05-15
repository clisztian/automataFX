package examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.github.signaflo.data.visualization.Plots;
import com.github.signaflo.timeseries.TimeSeries;
import com.github.signaflo.timeseries.model.arima.ArimaCoefficients;
import com.github.signaflo.timeseries.model.arima.ArimaProcess;

import dynamics.Evolutionize;
import encoders.Temporal;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import plots.KPIPlot;
import records.AnyRecord;
import records.TimeTrois;
import tsetlin.MultivariateConvolutionalAutomatonMachine;


/**
 * An example showing real-time (one example at a time) learning and classification 
 * of a multivariate time series governed by three independent stochastic series wit 
 * @author lisztian
 *
 */
public class DynamicTimeSeriesClassification {


	private Random rng;
	private ArrayList<TimeTrois> input_data;
	private ArrayList<Integer> labels;
	private String date_time_format = "yyyy-MM-dd HH:mm:ss";
	
	private DateTimeFormatter date_formatter;
	private Evolutionize evolve;
	
	private int dim_x = 30; //the real-valued dimension
	private int dim_y = 24; //the global lag dimension
	private int patch_dim_y = 4; //the sliding window function dimension

	
	
	public DynamicTimeSeriesClassification() {
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
	
	
	public void sampleStochasticData(int N) {
		
		input_data = new ArrayList<TimeTrois>();
		labels = new ArrayList<Integer>();
		
		DateTime dt = new DateTime(2020, 12, 23, 1, 0);
		
		TimeSeries myseries = sampleMAModel(N);
		TimeSeries myseries2 = sampleARModel(N);
		TimeSeries myseries3 = sampleSeasonalARModel(N);
		
		for(int i = 0; i < myseries.size(); i++) {
			
			double val1 = myseries.at(i);
			double val2 = myseries2.at(i);
			double val3 = myseries3.at(i);
			
			if(dt.getDayOfMonth() < 10) {
				input_data.add(new TimeTrois(new Temporal(dt.toString(date_formatter)), val1, val2, val3));
				labels.add(0);
			}
			else if(dt.getDayOfMonth() >= 10 && dt.getDayOfMonth() < 20) {
				input_data.add(new TimeTrois(new Temporal(dt.toString(date_formatter)), val2, val1, val3));
				labels.add(1);
			}
			else {
				input_data.add(new TimeTrois(new Temporal(dt.toString(date_formatter)), val3, val2, val1));
				labels.add(2);
			}		
					
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
		
		//sampleData(N);
		sampleStochasticData(N);
		
		/**
		 * Initiate 
		 */
		evolve = new Evolutionize(patch_dim_y, dim_y);		
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
		int nClauses = 80;
		float max_specificity = 2f;
		int nClasses = 3;
		
		
		MultivariateConvolutionalAutomatonMachine conv = new MultivariateConvolutionalAutomatonMachine(evolve.getConv_encoder(), threshold, nClasses, nClauses, max_specificity, true, 0f); 
		
		
		int n_train = (int)(input_data.size()*.60);
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
		for(int i = n_train; i < input_data.size(); i++) {
			
			TimeTrois input = input_data.get(i);
			int label = labels.get(i);
			
			evolve.add(input);
			int pred = conv.predict(evolve.get_last_sample());	
			System.out.println(i + " " + input.time().getDate_time_string() + " " + input.val_1() + " " + pred + " " + label);
			
			false_pred += (pred != label) ? 1 : 0;
		}
		
		System.out.println("Accuracy: " + 1.0*(input_data.size() - n_train - false_pred)/(1.0*input_data.size() - 1.0*n_train) + " " + false_pred);
		
		
		
	}
	
	
	
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
		
		DynamicTimeSeriesClassification classify = new DynamicTimeSeriesClassification();		
		classify.classification(10000);
		
		//classify.testTimeSeries(400);
	}
	
	
	public TimeSeries sampleMAModel(int N) {
		
		ArimaCoefficients.Builder builder = ArimaCoefficients.builder();
		ArimaCoefficients coefficients = builder.setMACoeffs(-0.2)
                .setARCoeffs(0.1, 0.5)
                .setDrift(.22)
                .build();

		ArimaProcess process = ArimaProcess.builder()
           .setCoefficients(coefficients)
           .build();
		
		TimeSeries myseries = process.simulate(N);
		
		return myseries.slice(100, N-1);
	}
	
	

	public TimeSeries sampleARModel(int N) {
		
		ArimaCoefficients.Builder builder = ArimaCoefficients.builder();
		ArimaCoefficients coefficients = builder.setMACoeffs(0.2)
                .setARCoeffs(0.95)
                .build();

		ArimaProcess process = ArimaProcess.builder()
           .setCoefficients(coefficients)
           .build();
		
		TimeSeries myseries = process.simulate(N);
		
		return myseries.slice(100, N-1);
	}
	

	public TimeSeries sampleSeasonalARModel(int N) {
		
		ArimaCoefficients.Builder builder = ArimaCoefficients.builder();
		ArimaCoefficients coefficients = builder.setMACoeffs(-0.5)
                .setARCoeffs(0.5)
                .setSeasonalARCoeffs(0.951)
                .setSeasonalFrequency(12)
                .build();
		
		ArimaProcess process = ArimaProcess.builder()
           .setCoefficients(coefficients)
           .build();
		
		TimeSeries myseries = process.simulate(N);
		
		return myseries.slice(100, N-1);
	}
	
	
	
	public void testTimeSeries(int N) throws IllegalArgumentException, IllegalAccessException {
		

		TimeSeries myseries = sampleMAModel(N);
		TimeSeries myseries2 = sampleARModel(N);
		TimeSeries myseries3 = sampleSeasonalARModel(N);
		

		
		ArrayList<TimeTrois> series = new ArrayList<TimeTrois>(); 
		DateTime dt = new DateTime(2020, 12, 23, 1, 0);
		for(int i = 0; i < myseries.size(); i++) {
			
			double val1 = myseries.at(i);
			double val2 = myseries2.at(i);
			double val3 = myseries3.at(i);
			
			series.add(new TimeTrois(new Temporal(dt.toString(date_formatter)), val1, val2, val3));
			
			dt = dt.plusHours(1);
		}
		
		KPIPlot<TimeTrois> plot = new KPIPlot<TimeTrois>(series.get(0));
		
		new JFXPanel();
		
		Platform.runLater(() -> {
	        try {
	            //an event with a button maybe
	            System.out.println("button is clicked");
	            
	            StackPane pane = new StackPane(plot.plotKPI("Testing", series));

	            pane.setPrefSize(1200, 600);
	            Scene     scene = new Scene(pane);
	            scene.getStylesheets().add("css/WhiteOnBlack.css");

	            Stage stage = new Stage();
	            stage.setTitle("RadarChart");
	            stage.setScene(scene);
	            stage.show();
	            
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    });
		
		
        
		
		
		
	}
	
	
	
}


/**
 * Parameters 
 * 36/12
 * 				int threshold = 160;
		int nClauses = 80;
		float max_specificity = 2f; 
		
		
		
		
 * 
 * 
 */


