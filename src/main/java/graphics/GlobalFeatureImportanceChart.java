package graphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import interpretability.GlobalRealFeatures;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.LinearGradient;
import javafx.stage.Stage;
import javafx.stage.Window;

public class GlobalFeatureImportanceChart {


	
	private StackedBarChart<String, Number> sexy_bar_chart;
	private ScrollPane my_scroll;
	private XYChart.Series<String, Number> pos_features;
	private XYChart.Series<String, Number> neg_features;
	
	private ArrayList<XYChart.Data<String, Number>> pos_points;
	private ArrayList<XYChart.Data<String, Number>> neg_points;
	
	private CategoryAxis xAxis;
	private NumberAxis yAxis;
	
	private ContextMenu hist_menu;
	private MenuItem delete_latest;
	
	private LinearGradient gradient;
	private Background back;
	private Stage prediction_stage;
	
	public GlobalFeatureImportanceChart() {
		
		
		back = new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
		
		
		
		xAxis = new CategoryAxis();
		xAxis.setLabel("Numerical Feature");

		yAxis = new NumberAxis();
		yAxis.setLabel("Importance");
		
		sexy_bar_chart = new StackedBarChart<String, Number>(xAxis, yAxis);
		sexy_bar_chart.setBackground(back);
		sexy_bar_chart.setPrefSize(1200, 500);
		sexy_bar_chart.setAnimated(false); 
		sexy_bar_chart.setCategoryGap(15);
		
		pos_features = new XYChart.Series<String, Number>();
		neg_features = new XYChart.Series<String, Number>();
		
		my_scroll = new ScrollPane();
		my_scroll.setMinSize(1000,500);
		my_scroll.setPrefSize(1200,500);
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
		pred_scene.getStylesheets().add(getClass().getClassLoader().getResource("css/WhiteOnBlack.css").toExternalForm());
		prediction_stage = new Stage();
		prediction_stage.setScene(pred_scene);

			
	}
	
	
	public void initialize(int n_real) {
		
		sexy_bar_chart.setPrefSize(n_real*70, 500);
	}
	
	
	public void show() {
		prediction_stage.show();
	}
	
	
	public void computeChart(GlobalRealFeatures[][] feats, int my_class) {
		
		int n_real_features = feats[0].length;
		pos_features.getData().clear();
		neg_features.getData().clear();
		sexy_bar_chart.getData().clear();
		sexy_bar_chart.layout();
		
		pos_points = new ArrayList<XYChart.Data<String, Number>>();
		neg_points = new ArrayList<XYChart.Data<String, Number>>();
		
		ArrayList<GlobalRealFeatures> real = new ArrayList<GlobalRealFeatures>();
		for(int i = 0; i < feats[my_class].length; i++) {
			real.add(feats[my_class][i]);
		}
		Collections.sort(real, compare.reversed());
		
		
		for(int i = 0; i < real.size(); i++) {
			
			//System.out.println(real.get(i).getFeatureName() + " " + (real.get(i).getBitRanges().getStrength() + real.get(i).getBitRanges().getNeg_strength()));
			
			float pos_val = real.get(i).getBitRanges().getStrength();
			float neg_val = real.get(i).getBitRanges().getNeg_strength();			
			pos_points.add(new XYChart.Data<String, Number>(real.get(i).getFeatureName(), pos_val));		
			neg_points.add(new XYChart.Data<String, Number>(real.get(i).getFeatureName(), neg_val));		
			
		}
		pos_features.getData().addAll(pos_points);
		neg_features.getData().addAll(neg_points);
		sexy_bar_chart.getData().addAll(pos_features, neg_features);
		
		
	}
	
	private final Comparator<GlobalRealFeatures> compare = new Comparator<GlobalRealFeatures>() {
        public int compare(GlobalRealFeatures o1, GlobalRealFeatures o2) {
             return o1.getStrength() + o1.getNegStrength() < o2.getStrength() + o2.getNegStrength() ? -1
                      : o1.getStrength() + o1.getNegStrength() > o2.getStrength() + o2.getNegStrength() ? 1
                      : 0;
        }
    };
	
}
