package examples;

import java.util.ArrayList;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.github.signaflo.timeseries.TimeSeries;
import com.github.signaflo.timeseries.model.arima.ArimaCoefficients;
import com.github.signaflo.timeseries.model.arima.ArimaProcess;
import org.apache.commons.math3.stat.StatUtils;
import dynamics.Evolutionize;
import encoders.Temporal;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import plots.KPIPlot;
import records.TimeTrois;
import tsetlin.MultivariateConvolutionalAutomatonMachine;

public class TimeSeriesClassification {

	private Random rng;
	private ArrayList<Regime> input_data;

	private String date_time_format = "yyyy-MM-dd HH:mm:ss";
	
	private DateTimeFormatter date_formatter;
	private Evolutionize evolve;
	
	private int dim_x; //the real-valued dimension
	private int dim_y; //the global lag dimension
	private int patch_dim_y; //the sliding window function dimension

	private double accuracy;
	
	private int threshold;
	private int nClauses;
	float max_specificity = 2f;
	
	public TimeSeriesClassification(int dim_x, int dim_y, int patch_dim_y, int nclauses, int thresh) {
		
		this.dim_x = dim_x;
		this.dim_y = dim_y;
		this.patch_dim_y = patch_dim_y;
		
		this.nClauses = nclauses;
		this.threshold = thresh;
		
		rng = new Random();
		date_formatter = DateTimeFormat.forPattern(date_time_format);
	}
	

	/**
	 * Grab n_samples of multivariate time series of length N
	 * @param n_samples
	 * @param N
	 */
	public void sampleStochasticData(int n_samples, int N) {
		
		input_data = new ArrayList<Regime>();
	
		for(int k = 0; k < n_samples; k++) {
			
			int regime = rng.nextInt(3);
			ArrayList<TimeTrois> series = new ArrayList<TimeTrois>();
			TimeSeries myseries = sampleMAModel(N);
			TimeSeries myseries2 = sampleARModel(N);
			TimeSeries myseries3 = sampleSeasonalARModel(N);
			DateTime dt = new DateTime(2020, 12, 23, 1, 0);
			
			for(int i = 0; i < myseries.size(); i++) {
				
				double val1 = myseries.at(i);
				double val2 = myseries2.at(i);
				double val3 = myseries3.at(i);
				
				if(regime == 0) {
					series.add(new TimeTrois(new Temporal(dt.toString(date_formatter)), val1, val2, val3));			
				}
				else if(regime == 1) {
					series.add(new TimeTrois(new Temporal(dt.toString(date_formatter)), val2, val1, val3));
				}
				else {
					series.add(new TimeTrois(new Temporal(dt.toString(date_formatter)), val3, val2, val1));
				}				
				dt = dt.plusHours(1);
			}
			input_data.add(new Regime(series, regime));
		}		
	}
	

	
	public void classification() throws IllegalArgumentException, IllegalAccessException {
		
		
		sampleStochasticData(400, dim_y);
		
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
			
			for(int k = 0; k < dim_y; k++) {
				evolve.addValue(input_data.get(i).getSeries().get(k));
			}	
		}
		evolve.fit();
		
		
		/**
		 * Define and setup automata machine 
		 */

		float max_specificity = 2f;
		int nClasses = 3;
		
		
		MultivariateConvolutionalAutomatonMachine conv = new MultivariateConvolutionalAutomatonMachine(evolve.getConv_encoder(), threshold, nClasses, nClauses, max_specificity, true); 
		
		
		int n_train = (int)(input_data.size()*.60);
		for(int i = 0; i < n_train; i++) {
			
			Regime input = input_data.get(i);
			int label = input.getLabel();
			
			for(int k = 0; k < input.getSeries().size(); k++) {
				evolve.add(input.getSeries().get(k));
			}
			

			int pred = conv.update(evolve.get_last_sample(), label);			
			//System.out.println(i + " " + input.getSeries().get(0).time().getDate_time_string()+ " " + input.getSeries().get(0).val_1() + " " + pred + " " + label);
		}
		long end = System.currentTimeMillis();

		int false_pred = 0;
		for(int i = n_train; i < input_data.size(); i++) {
			
			Regime input = input_data.get(i);
			int label = input.getLabel();
			
			for(int k = 0; k < input.getSeries().size(); k++) {
				evolve.add(input.getSeries().get(k));
			}
			
			int pred = conv.predict(evolve.get_last_sample());	
			//System.out.println(i + " " + input.getSeries().get(0).time().getDate_time_string()+ " " + input.getSeries().get(0).val_1() + " " + pred + " " + label);
			
			false_pred += (pred != label) ? 1 : 0;
		}
		
		accuracy = 1.0*(input_data.size() - n_train - false_pred)/(1.0*input_data.size() - 1.0*n_train);
		System.out.println("Accuracy: " + accuracy + " " + false_pred);
		
		
		
	}
	
	
	
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
		
		int dim_x = 30;
		int dim_y = 120;
		int patch_dim_y = 3;
		
		int n_clauses = 80;
		int threshold = n_clauses + (int)(n_clauses/4);
		
		int samps = 30;
		
		for(int k = 0; k < 15; k++) {
			
			double[] stats = new double[samps];
			for(int i = 0; i < samps; i++) {
				
				TimeSeriesClassification classify = new TimeSeriesClassification(dim_x, dim_y, patch_dim_y, n_clauses, threshold);		
				classify.classification();	
				stats[i] = classify.getAccuracy();
				
			}
			System.out.println(n_clauses + " " + threshold + ": " + StatUtils.mean(stats) + " " + StatUtils.variance(stats));
			
			
			n_clauses += 50;
			threshold = n_clauses + (int)(n_clauses/4);
		}
		
		

		TimeSeriesClassification classify = new TimeSeriesClassification(dim_x, dim_y, patch_dim_y, n_clauses, threshold);
		classify.testTimeSeries(400);
		
		
	}
	
	
	private Double getAccuracy() {
		return accuracy;
	}


	public TimeSeries sampleMAModel(int N) {
		
		ArimaCoefficients.Builder builder = ArimaCoefficients.builder();
		ArimaCoefficients coefficients = builder.setMACoeffs(-0.2)
                .setARCoeffs(0.1)
                .build();

		ArimaProcess process = ArimaProcess.builder()
           .setCoefficients(coefficients)
           .build();
		
		TimeSeries myseries = process.simulate(N+100);

		return myseries.slice(100, myseries.size()-1);
	}
	
	

	public TimeSeries sampleARModel(int N) {
		
		ArimaCoefficients.Builder builder = ArimaCoefficients.builder();
		ArimaCoefficients coefficients = builder.setMACoeffs(0.1)
                .setARCoeffs(0.7)
                .build();

		ArimaProcess process = ArimaProcess.builder()
           .setCoefficients(coefficients)
           .build();
		
		TimeSeries myseries = process.simulate(N+100);
		
		return myseries.slice(100, myseries.size()-1);
	}
	

	public TimeSeries sampleSeasonalARModel(int N) {
		
		ArimaCoefficients.Builder builder = ArimaCoefficients.builder();
		ArimaCoefficients coefficients = builder.setARCoeffs(0.5)
                .setSeasonalARCoeffs(0.11)
                .setSeasonalFrequency(12)
                .build();
		
		ArimaProcess process = ArimaProcess.builder()
           .setCoefficients(coefficients)
           .build();
		
		TimeSeries myseries = process.simulate(N+100);
		
		return myseries.slice(100, myseries.size()-1);
	}
	
	
	
	public void testTimeSeries(int N) throws IllegalArgumentException, IllegalAccessException {
		

		TimeSeries myseries = sampleMAModel(N);
		TimeSeries myseries2 = sampleARModel(N);
		TimeSeries myseries3 = sampleSeasonalARModel(N);
		

		System.out.println("Series 1 - mean: " + myseries.mean() + ", stdev: " + myseries.stdDeviation());
		System.out.println("Series 2 - mean: " + myseries2.mean() + ", stdev: " + myseries2.stdDeviation());
		System.out.println("Series 3 - mean: " + myseries3.mean() + ", stdev: " + myseries3.stdDeviation());
		
		
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
	
	public int getThreshold() {
		return threshold;
	}


	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public int getnClauses() {
		return nClauses;
	}


	public void setnClauses(int nClauses) {
		this.nClauses = nClauses;
	}

	class Regime {
		
		private ArrayList<TimeTrois> series;
		private int label;
		
		Regime(ArrayList<TimeTrois> series, int label) {
			this.series = series;
			this.label = label;
		}
		
		public ArrayList<TimeTrois> getSeries() {
			return series;
		}
		public void setSeries(ArrayList<TimeTrois> series) {
			this.series = series;
		}
		public int getLabel() {
			return label;
		}
		public void setLabel(int label) {
			this.label = label;
		}
		
		
	}
	
}
