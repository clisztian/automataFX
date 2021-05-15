package examples;


import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.github.signaflo.timeseries.TimeSeries;
import com.github.signaflo.timeseries.model.arima.ArimaCoefficients;
import com.github.signaflo.timeseries.model.arima.ArimaProcess;

import encoders.Temporal;
import interpretability.Prediction;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import plots.KPIPlot;
import records.AnomalySeriesObservation;
import tsetlin.AutomataLearning;
import tsetlin.MultiAutomataLearning;

public class TimeSeriesMultRegressionExample {

	private static DateTimeFormatter date_formatter;
	private static String date_time_format = "yyyy-MM-dd HH:mm:ss";
	
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
		
		int dim_x = 40;
		int dim_y = 2;
		int patch_dim_y = 2;
		int threshold = 80;
		int nClauses = 30;
		float max_specificity = 2f;
		int nClasses = 100; //regression
		
		
		date_formatter = DateTimeFormat.forPattern(date_time_format);
		
		int total_samples = 1500;
		int out_sample = 200;
		//ArrayList<AnomalySeriesObservation> in_sample = TimeSeriesMultRegressionExample.sampleData(0, total_samples);
		ArrayList<AnomalySeriesObservation> in_sample = TimeSeriesMultRegressionExample.sampleStochasticData(0, total_samples);
		
//		AutomataLearning<AnomalySeriesObservation> automata = new AutomataLearning<AnomalySeriesObservation>(dim_y, patch_dim_y, dim_x, in_sample.get(0), nClauses, threshold, max_specificity, nClasses,
//			 false, false, false, false);
		
		MultiAutomataLearning<AnomalySeriesObservation> automata = new MultiAutomataLearning<AnomalySeriesObservation>(dim_y, patch_dim_y, dim_x, 
				in_sample.get(0), nClauses, threshold, max_specificity, nClasses, false, false, false, false, 0f);
		
		automata.add_fit(in_sample);
		automata.buildRealRegressionDecoders();
		automata.add(in_sample.subList(0, dim_y));
		
		/**
		 * Update 
		 */
		List<Float> predictions = new ArrayList<Float>();

		int n_samples = total_samples - out_sample;
		float error_sum = 0;
		for(int i = dim_y; i < n_samples; i++) {
			
			float pred = automata.update_regression(in_sample.get(i), in_sample.get(i+1).value());
			//System.out.println(i + " " + in_sample.get(i).value() + " " + in_sample.get(i+1).value() + " " + pred);

		}
		

		float naive_sum = 0; //compute native error (y_t = y_{t+1})
		for(int i = n_samples; i < in_sample.size()-1; i++) {
			
			Prediction pred = automata.predict_regression(in_sample.get(i));
			//System.out.println(i + " " + in_sample.get(i).value() + " " + in_sample.get(i+1).value() + " " + pred.getPred_class() + " " + pred.getRegression_prediction());
			
			predictions.add(pred.getRegression_prediction());
			error_sum += (pred.getRegression_prediction() - in_sample.get(i+1).value())*(pred.getRegression_prediction() - in_sample.get(i+1).value());			
			naive_sum += (in_sample.get(i).value() - in_sample.get(i+1).value())*(in_sample.get(i).value()  - in_sample.get(i+1).value());
		}
		
		
		System.out.println("Regression error: " + Math.sqrt(error_sum/(in_sample.size()-1 - n_samples)));
		System.out.println("Naive error: " + Math.sqrt(naive_sum/(in_sample.size()-1 - n_samples)));

		KPIPlot<AnomalySeriesObservation> plot = new KPIPlot<AnomalySeriesObservation>(in_sample.get(0));
		
		new JFXPanel();
		
		Platform.runLater(() -> {
	        try {
	            //an event with a button maybe
	            
	            StackPane pane = new StackPane(plot.plotKPI("Testing", in_sample.subList(n_samples, in_sample.size() - 1), predictions));

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
	
	
	public static ArrayList<AnomalySeriesObservation> sampleData(int start, int T) {
		
		ArrayList<AnomalySeriesObservation> data = new ArrayList<AnomalySeriesObservation>();		
		DateTime dt = new DateTime(2012, 12, 23, 1, 0);
		
		
		for(int i = start; i < T + start; i++) {
			
			double val1 = timeFive(dt);
			data.add(new AnomalySeriesObservation(new Temporal(dt.toString(date_formatter)), (float)val1));
			
			dt = dt.plusHours(1);
		}
		
		return data;
		
	}
	

	public static ArrayList<AnomalySeriesObservation> sampleStochasticData(int start, int T) {
		
		TimeSeries myseries = sampleMAModel(T);
		TimeSeries myseries2 = sampleARModel(T);
		TimeSeries myseries3 = sampleSeasonalARModel(T);
		
		ArrayList<AnomalySeriesObservation> data = new ArrayList<AnomalySeriesObservation>();		
		DateTime dt = new DateTime(2012, 12, 23, 1, 0);
		
		
		for(int i = start; i < T + start; i++) {
			
			double val1 = myseries.at(i);
			double val2 = myseries2.at(i);
			double val3 = myseries3.at(i);
			data.add(new AnomalySeriesObservation(new Temporal(dt.toString(date_formatter)), (float)val2));
//			if(dt.getDayOfMonth() > 15) {
//				data.add(new AnomalySeriesObservation(new Temporal(dt.toString(date_formatter)), (float)val2));
//			}
//			else {
//				data.add(new AnomalySeriesObservation(new Temporal(dt.toString(date_formatter)), (float)val3));
//			}			
			dt = dt.plusHours(1);
		}
		
		return data;
	}
	
	
	public static ArrayList<AnomalySeriesObservation> sampleARIMAData(int start, int T) {
		
		TimeSeries myseries = sampleMAModel(T);
		TimeSeries myseries2 = sampleARModel(T);
		TimeSeries myseries3 = sampleSeasonalARModel(T);
		
		ArrayList<AnomalySeriesObservation> data = new ArrayList<AnomalySeriesObservation>();		
		DateTime dt = new DateTime(2012, 12, 23, 1, 0);
		
		
		for(int i = start; i < T + start; i++) {
			
			double val1 = myseries.at(i);
			double val2 = myseries2.at(i);
			double val3 = myseries3.at(i);
			data.add(new AnomalySeriesObservation(new Temporal(dt.toString(date_formatter)), (float)val2));
//			if(dt.getDayOfMonth() > 15) {
//				data.add(new AnomalySeriesObservation(new Temporal(dt.toString(date_formatter)), (float)val2));
//			}
//			else {
//				data.add(new AnomalySeriesObservation(new Temporal(dt.toString(date_formatter)), (float)val3));
//			}			
			dt = dt.plusHours(1);
		}
		
		return data;
	}
	
	
	
	
	
	public static double series_2(int t) {
		return Math.sin(t * 0.02f * 2f * Math.PI) * .25f + Math.sin(t * .09f * 2f * Math.PI)*0.65f;
	}
	
	public static float waveOne(float t) {
		return (float) (Math.sin(t * 0.02f * 2f * Math.PI) * .25f + Math.sin(t * .05f * 2f * Math.PI)*0.25f + (((t * .01f) % 1.25f)*2f - 1.0) * .3f);
	}
	
	public static float waveTwo(float t) {
		return (float) (Math.sin(t * 0.02f * 2f * Math.PI) * .55f + Math.cos((((t * .01f) % 0.75f)*2f - 1.0) * .3f * .05f * 2f * Math.PI)*0.35f);
	}
	
	public static float waveThree(float t) {
		return (float) (Math.sin(t * 0.04f * 2f * Math.PI) * .15f + Math.cos(t * .09f * 2f * Math.PI)*0.35f + (((t * .01f) % 1.0f)*2f - 1.0) * .40f);
	}
	
	public static float waveFour(float t) {
		return (float) (Math.cos(t * 0.01f * 2f * Math.PI) * .35f + Math.sin(t * .11f * 2f * Math.PI)*0.25f + (((t * .01f) % .55f)*2f - 1.0) * .35f);
	}
	
	public static float waveFive(float t) {
		return (float) (Math.cos(t * 0.04f * 2f * Math.PI) * .35f + Math.sin(t * .09f * 2f * Math.PI)*0.15f + (((t * .01f) % 1.50f)*2f - 1.0) * .25f);
	}
	
	public static float timeFive(DateTime dt) {
		
		int t = (dt.getDayOfWeek()-1)*24 + dt.getHourOfDay();
		
		return (float) (Math.cos(t * 0.24f * 2f * Math.PI) * .35f + Math.sin(t * .79f * 2f * Math.PI)*0.15f + (((t * .01f) % 1.1f)*2f - 1.0) * .25f);
		
	}
	
	public static TimeSeries sampleMAModel(int N) {
		
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
	
	

	public static TimeSeries sampleARModel(int N) {
		
		ArimaCoefficients.Builder builder = ArimaCoefficients.builder();
		ArimaCoefficients coefficients = builder .setARCoeffs(0.7)
                .build();

		ArimaProcess process = ArimaProcess.builder()
           .setCoefficients(coefficients)
           .build();
		
		TimeSeries myseries = process.simulate(N+100);
		
		return myseries.slice(100, myseries.size()-1);
	}
	

	public static TimeSeries sampleSeasonalARModel(int N) {
		
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
}
