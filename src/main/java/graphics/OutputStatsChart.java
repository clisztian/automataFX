package graphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import interpretability.GlobalRealFeatures;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.LinearGradient;
import javafx.stage.Stage;
import output.OutputStats;

/**
 * 
 * OutputStatsChart
 * 
 * This chart collects the frequency for each class out-of-sample (non-training data)
 * 
 * During training runtime the chart will update with three values for each class output
 * 
 * 1) Number of true-positives (true predictions of class) (solid blue color)
 * 2) Difference between total frequency of class and number of true-positives (for accuracy) (transparent)
 * 3) Number of false positive for this class (red color) 
 * 
 * A perfect model will feature 1) solidly colored and no red
 * 
 * 
 * @author lisztian
 *
 */
public class OutputStatsChart {

	private StackedBarChart<String, Number> sexy_bar_chart;
	private ScrollPane my_scroll;
	
	private XYChart.Series<String, Number> correctly_classified_data;
	private XYChart.Series<String, Number> discrepency_data;
	private XYChart.Series<String, Number> incorrectly_classified_data;
	
	private ArrayList<XYChart.Data<String, Number>> correctly_classified_points;
	private ArrayList<XYChart.Data<String, Number>> discrepency_points;
	private ArrayList<XYChart.Data<String, Number>> incorrectly_classified_points;
	
	private CategoryAxis xAxis;
	private NumberAxis yAxis;
	
	private ContextMenu hist_menu;
	private MenuItem delete_latest;
	
	private LinearGradient gradient;
	private Background back;
	private Stage prediction_stage;
	
	public OutputStatsChart() {
		
		
		back = new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
		
		
		
		xAxis = new CategoryAxis();
		xAxis.setLabel("Output Value");

		yAxis = new NumberAxis();
		yAxis.setLabel("Frequency");
		
		sexy_bar_chart = new StackedBarChart<String, Number>(xAxis, yAxis);
		sexy_bar_chart.setBackground(back);
		sexy_bar_chart.setPrefSize(1200, 500);
		sexy_bar_chart.setAnimated(false); 
		sexy_bar_chart.setCategoryGap(15);
		
		correctly_classified_data = new XYChart.Series<String, Number>();
		discrepency_data = new XYChart.Series<String, Number>();
		incorrectly_classified_data = new XYChart.Series<String, Number>();
		
		my_scroll = new ScrollPane();
		my_scroll.setMinSize(400,500);
		my_scroll.setPrefSize(400,500);
		my_scroll.pannableProperty().set(true);
		my_scroll.fitToHeightProperty().set(true);
		my_scroll.setContent(sexy_bar_chart);
		
		sexy_bar_chart.setOnScroll(new EventHandler<ScrollEvent>() {

			@Override
			public void handle(ScrollEvent event) {
			
				event.consume();
				if(event.getDeltaY() == 0) {
					return;
				}
				
				double scaleFactor = event.getDeltaY();
				double w = sexy_bar_chart.getWidth();
				
				if(w + scaleFactor > 1200) {
					sexy_bar_chart.setMinWidth(w + scaleFactor);
				}				
			}		
		});
		
		
		Scene pred_scene = new Scene(my_scroll);
		pred_scene.getStylesheets().add(getClass().getClassLoader().getResource("css/outputOnBlack.css").toExternalForm());
		prediction_stage = new Stage();
		prediction_stage.setScene(pred_scene);

			
	}
	
	
	public void initialize(int n_real) {
		
		sexy_bar_chart.setPrefSize(n_real*70, 500);
		
		correctly_classified_points = new ArrayList<XYChart.Data<String, Number>>();
		discrepency_points = new ArrayList<XYChart.Data<String, Number>>();
		incorrectly_classified_points = new ArrayList<XYChart.Data<String, Number>>();
		
		for(int i = 0; i < n_real; i++) {
			
			discrepency_points.add(new XYChart.Data<String, Number>("" + i, 0));
			correctly_classified_points.add(new XYChart.Data<String, Number>("" + i, 0));
			incorrectly_classified_points.add(new XYChart.Data<String, Number>("" + i, 0));
	
			
		}
		correctly_classified_data.getData().addAll(correctly_classified_points);
		discrepency_data.getData().addAll(discrepency_points);
		incorrectly_classified_data.getData().addAll(incorrectly_classified_points);
		
		sexy_bar_chart.getData().addAll(correctly_classified_data, discrepency_data, incorrectly_classified_data);
		
	}
	
	
	public void show() {
		prediction_stage.show();
	}
	
	
	public void computeChart_nonanimated(HashMap<Integer, OutputStats> out_sample_results) {
		
		int n_real = out_sample_results.size();
		correctly_classified_data.getData().clear();
		discrepency_data.getData().clear();
		incorrectly_classified_data.getData().clear();
		
		sexy_bar_chart.getData().clear();
		sexy_bar_chart.layout();
		
		
		correctly_classified_points = new ArrayList<XYChart.Data<String, Number>>();
		discrepency_points = new ArrayList<XYChart.Data<String, Number>>();
		incorrectly_classified_points = new ArrayList<XYChart.Data<String, Number>>();
		
		
		for(int i = 0; i < n_real; i++) {
			
			//System.out.println(real.get(i).getFeatureName() + " " + (real.get(i).getBitRanges().getStrength() + real.get(i).getBitRanges().getNeg_strength()));
			OutputStats stats = out_sample_results.get(i);
			
			int discrepency = stats.getTrue_output() - stats.getPred_output_correct();
			
			discrepency_points.add(new XYChart.Data<String, Number>("" + stats.getLabel_class(), discrepency));
			correctly_classified_points.add(new XYChart.Data<String, Number>("" + stats.getLabel_class(), stats.getPred_output_correct()));
			incorrectly_classified_points.add(new XYChart.Data<String, Number>("" + stats.getLabel_class(), stats.getFalse_positive()));
	
			
		}
		correctly_classified_data.getData().addAll(correctly_classified_points);
		discrepency_data.getData().addAll(discrepency_points);
		incorrectly_classified_data.getData().addAll(incorrectly_classified_points);
		
		sexy_bar_chart.getData().addAll(correctly_classified_data, discrepency_data, incorrectly_classified_data);
		
		
	}
	
	public void computeChart_(int[] labels, int[][] vals) {
		
		int n_real = labels.length;
		correctly_classified_data.getData().clear();
		discrepency_data.getData().clear();
		incorrectly_classified_data.getData().clear();
		
		sexy_bar_chart.getData().clear();
		sexy_bar_chart.layout();
		
		
		correctly_classified_points = new ArrayList<XYChart.Data<String, Number>>();
		discrepency_points = new ArrayList<XYChart.Data<String, Number>>();
		incorrectly_classified_points = new ArrayList<XYChart.Data<String, Number>>();
		
		
		for(int i = 0; i < n_real; i++) {
						
			
			
			discrepency_points.add(new XYChart.Data<String, Number>("" + labels[i], vals[i][0]));
			correctly_classified_points.add(new XYChart.Data<String, Number>("" + labels[i], vals[i][1]));
			incorrectly_classified_points.add(new XYChart.Data<String, Number>("" + labels[i], vals[i][2]));
	
			
		}
		correctly_classified_data.getData().addAll(correctly_classified_points);
		discrepency_data.getData().addAll(discrepency_points);
		incorrectly_classified_data.getData().addAll(incorrectly_classified_points);
		
		sexy_bar_chart.getData().addAll(correctly_classified_data, discrepency_data, incorrectly_classified_data);
		
		
	}
	
	

	public void computeChart(HashMap<Integer, OutputStats> out_sample_results) {
		
		int n_real = out_sample_results.size();

			
		for(int i = 0; i < n_real; i++) {
			
			//System.out.println(real.get(i).getFeatureName() + " " + (real.get(i).getBitRanges().getStrength() + real.get(i).getBitRanges().getNeg_strength()));
			OutputStats stats = out_sample_results.get(i);
			
			int discrepency = stats.getTrue_output() - stats.getPred_output_correct();
			
			discrepency_points.add(new XYChart.Data<String, Number>("" + stats.getLabel_class(), discrepency));
			correctly_classified_points.add(new XYChart.Data<String, Number>("" + stats.getLabel_class(), stats.getPred_output_correct()));
			incorrectly_classified_points.add(new XYChart.Data<String, Number>("" + stats.getLabel_class(), stats.getFalse_positive()));
	
			
			discrepency_data.getData().get(i).setYValue(discrepency);
			correctly_classified_data.getData().get(i).setYValue(stats.getPred_output_correct());
			incorrectly_classified_data.getData().get(i).setYValue(stats.getFalse_positive());
			
		}
		//sexy_bar_chart.layout();
	}
	
	
}
