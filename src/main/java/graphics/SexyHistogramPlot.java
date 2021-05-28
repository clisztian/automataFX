package graphics;

import java.text.DecimalFormat;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import records.RecordColumn;
import smoothchart.SmoothedChart;
import smoothchart.SmoothedChart.ChartType;

public class SexyHistogramPlot {




	private StackPane sexy_area_pane;
	private Scene sexy_area_scene;
	private Stage sexy_area_stage;
	
	private SmoothedChart<Number, Number> sexy_area_chart;
	private NumberAxis xAxis;
	private NumberAxis yAxis;
	private DecimalFormat df = new DecimalFormat("#.######");
	
	private double min;
	private double max;


	private ContextMenu hist_menu;
	private MenuItem delete_latest;
	

	private int resolution = 100;
	private double[] buckets;
	private int[] group;
	private LinearGradient gradient;
	private Background back;

	/**
	 * Contains reference to data in table
	 */
	private ObservableList<Map> underyling_data;
	



	private final String contextSyle = ".root {\n" + 
			"  -fx-background-color: rgb(35,25,25,.8); \n" + 
			"  -fx-padding: 3;\n" + 
			"}\n" + 
			"\n" + 
			".context-menu {\n" + 
			"  -fx-background-color: rgb(15,15,15,.6);\n" + 
			"  -fx-text-fill: white;\n" + 
			"}\n" + 
			"\n" + 
			".menu-item .label {\n" + 
			"  -fx-text-fill: yellow;\n" + 
			"}\n" + 
			"\n" + 
			".menu-item:focused .label {\n" + 
			"  -fx-text-fill: white;\n" + 
			"}";
	
	
	public SexyHistogramPlot() {
		
		hist_menu = new ContextMenu();
		delete_latest = new MenuItem("Delete Last");
		delete_latest.setOnAction(event -> {
			
			Platform.runLater(() -> {
				removeLast();
			});
			event.consume();			
        });
		
		hist_menu.getItems().add(delete_latest);
		hist_menu.setStyle(contextSyle);
		

		gradient =new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, 
    			new Stop(0, Color.rgb(2, 40, 50).darker()),
                new Stop(1, Color.rgb(45, 0, 45).darker()) );
    	
    	back = new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
		
		xAxis = new NumberAxis();
		xAxis.setLabel("Value");
		xAxis.setForceZeroInRange(false);
		
		
		yAxis = new NumberAxis();
		yAxis.setLabel("Frequency");

		sexy_area_chart = new SmoothedChart<Number, Number>(xAxis, yAxis);
		sexy_area_chart.setBackground(back);
		sexy_area_chart.setPrefSize(1200, 800);
		sexy_area_chart.setCreateSymbols(false);
		sexy_area_chart.setAnimated(true); 
		sexy_area_chart.setSmoothed(true);
		sexy_area_chart.setChartType(ChartType.AREA);
		sexy_area_chart.setInteractive(true);
		sexy_area_chart.getVerticalZeroLine().setStroke(Color.TRANSPARENT);
		
		
		sexy_area_pane = new StackPane();
		sexy_area_pane.getChildren().add(sexy_area_chart);
		
		
		sexy_area_scene = new Scene(sexy_area_pane);
		
		sexy_area_scene.getStylesheets().add(getClass().getClassLoader().getResource("css/TransactionChart.css").toExternalForm());
		sexy_area_stage = new Stage();
		sexy_area_stage.setTitle("Real Features Histogram");
		sexy_area_stage.setScene(sexy_area_scene);
		
		sexy_area_chart.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() { 
            @Override
            public void handle(ContextMenuEvent event) {
            	hist_menu.show(sexy_area_pane.getScene().getWindow(),event.getScreenX(), event.getScreenY());
            }
        });
		
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
        series1.setName(name);
        
        series1.getData().add(new XYChart.Data<Number, Number>(buckets[0] - Double.MIN_VALUE, 0));
        for(int k = 0; k < resolution; k++) {     
        	series1.getData().add(new XYChart.Data<Number, Number>(buckets[k], group[k]));	
        }
        series1.getData().add(new XYChart.Data<Number, Number>(buckets[buckets.length - 1] + Double.MIN_VALUE, 0));
                 
        sexy_area_chart.getData().addAll(series1);
        
        
	}
	
	
	/**
	 * Removes the last plot
	 */
	public void removeLast() {		
		if(sexy_area_chart.getData().size() > 0) {
			sexy_area_chart.getData().remove(sexy_area_chart.getData().size() - 1);
		}		
	}
	
	
	/**
	 * Given a field name from the features, grab all values from table that are
	 * EQUAL to the value 
	 * 
	 * Uses the latest real feature histogram 
	 * 
	 * @param field_name feature_name (most often a category)
	 * @param value the conditional value
	 */
	public void conditionalDataExtractEquals(String field_name, String value) {
		
		if(sexy_area_chart.getData().size() > 0) {
			
			int size = sexy_area_chart.getData().size();
			
			String target_feature_name = sexy_area_chart.getData().get(size-1).getName();
			
			DescriptiveStatistics columnData = new DescriptiveStatistics();
			
			for (Map item : underyling_data) {
				
				if(item.get(field_name).toString().equals(value) && NumberUtils.isCreatable(item.get(target_feature_name).toString())) {
					Float myval = (Float)item.get(target_feature_name);
					columnData.addValue(myval);
				}
			}
			
			plot(columnData, target_feature_name + " | " + value);
					
		}
		
		
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
	

	public void setUnderyling_data(ObservableList<Map> underyling_data) {
		this.underyling_data = underyling_data;
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

