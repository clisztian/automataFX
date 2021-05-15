package graphics;

import java.text.DecimalFormat;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

public class SexyHistogramPlot {




	private StackPane sexy_area_pane;
	private Scene sexy_area_scene;
	private Stage sexy_area_stage;
	
	private AreaChart<Number, Number> sexy_area_chart;
	private NumberAxis xAxis;
	private NumberAxis yAxis;
	private DecimalFormat df = new DecimalFormat("#.######");
	
	private double min;
	private double max;



	private int resolution = 100;
	private double[] buckets;
	private int[] group;
	private RadialGradient gradient;
	private Background back;
	private int[] group_ask;

	private double scale = 1.0;
	
	
	public SexyHistogramPlot() {
		
		gradient = new RadialGradient(0, 0, 0.5, 0.25, 0.8, true, CycleMethod.NO_CYCLE,
    			new Stop(0, Color.DARKSLATEGRAY.darker().darker()),
                new Stop(1,Color.BLACK));
    	
    	back = new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
		
		xAxis = new NumberAxis();
		xAxis.setLabel("Value");

		yAxis = new NumberAxis();
		yAxis.setLabel("Frequency");

		sexy_area_chart = new AreaChart<Number, Number>(xAxis, yAxis);
		sexy_area_chart.setBackground(back);
		sexy_area_chart.setPrefSize(800, 500);
		sexy_area_chart.setCreateSymbols(false);
		sexy_area_chart.setAnimated(true); 
		
		sexy_area_pane = new StackPane();
		sexy_area_pane.getChildren().add(sexy_area_chart);
		
		sexy_area_scene = new Scene(sexy_area_pane);
		sexy_area_scene.getStylesheets().add(getClass().getClassLoader().getResource("css/TransactionChart.css").toExternalForm());
		sexy_area_stage = new Stage();
		sexy_area_stage.setTitle("SexyHistogram");
		sexy_area_stage.setScene(sexy_area_scene);
		
	}
	
	public void show() {
		sexy_area_stage.show();
	}
	
	
	public AreaChart<Number, Number> getSexy_area_chart() {
		return sexy_area_chart;
	}
	
	
	
	public void plot(DescriptiveStatistics stats, String name) {
		

		
        
       
    	computeHistogram(stats);
    	
    	XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
        series1.setName(name + " distribution");
        
        for(int k = 0; k < resolution; k++) {     
        	series1.getData().add(new XYChart.Data<Number, Number>(buckets[k], group[k]));	
        }
                 
        sexy_area_chart.getData().addAll(series1);
        
	
	}
	
	
	private void computeHistogram(DescriptiveStatistics stats) {
    	
    	int n_simulations = (int) stats.getN();
    	min = Double.MAX_VALUE;
    	max = -Double.MAX_VALUE;
    	
    	double[] kpi = stats.getValues();
    	
    	for(int i = 0; i < kpi.length; i++) {   		
    		min = Math.min(min, kpi[i]);   		
    		max = Math.max(max, kpi[i]);
    	}
		
    	
    	group = new int[resolution];        
    	buckets = new double[resolution];
    	double delta = (max - min)/(double)resolution;
    	
    	
    	buckets[0] = min;
    	for(int i = 1; i < resolution; i++) {
    		buckets[i] = buckets[i-1] + delta;
    	}
    	
        for(int i=0; i < n_simulations; i++) {	
          for(int k = 1; k < resolution; k++) {
        	  
        	  if(kpi[i] <= buckets[k]) {
        		  group[k-1]++;
        		  break;
        	  }  
          }
        }    
    }   
	
    
	//count data population in groups
    private void computeHistogram(DescriptiveStatistics stats, DescriptiveStatistics ask_stats) {
    	
    	int n_simulations = (int) stats.getN();
    	
    	
    	min = Double.MAX_VALUE;
    	max = -Double.MAX_VALUE;
    	
    	double[] kpi = stats.getValues();
    	double[] kpi_ask = ask_stats.getValues();
    	
    	for(int i = 0; i < kpi.length; i++) {   	
    		if(kpi[i] > 0 && kpi_ask[i] > 0) {
    			min = Math.min(min, kpi[i]);   		
        		max = Math.max(max, kpi[i]);
    		}
    	}
		
    	max = max*scale;
    	
    	
    	group = new int[resolution];       
    	group_ask = new int[resolution]; 
    	buckets = new double[resolution];
    	double delta = (max - min)/(double)resolution;
    	
    	
    	buckets[0] = min;
    	for(int i = 1; i < resolution; i++) {
    		buckets[i] = buckets[i-1] + delta;
    	}
    	
        for(int i=0; i < n_simulations; i++) {	
          for(int k = 1; k < resolution; k++) {
        	  
        	  if(kpi[i] <= buckets[k]) {
        		  group[k-1]++;
        		  break;
        	  }
        	  if(kpi_ask[i] <= buckets[k]) {
        		  group_ask[k-1]++;
        		  break;
        	  }
        	  
          }
        }    
    } 
	
    
	public int getResolution() {
		return resolution;
	}


	public void setResolution(int resolution) {
		this.resolution = resolution;
	}
	
	public StackPane getSexy_area_pane() {
		return sexy_area_pane;
	}


	public void setSexy_area_pane(StackPane sexy_area_pane) {
		this.sexy_area_pane = sexy_area_pane;
	}
	
}

