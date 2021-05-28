package graphics;

import java.util.HashMap;
import java.util.Map.Entry;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import utils.MutableInt;

public class SexyCategoryChart {

	private StackPane sexy_bar_pane;
	private Scene sexy_bar_scene;



	private Stage sexy_bar_stage;
	
	private BarChart<String, Number> sexy_bar_chart;
	
	private CategoryAxis xAxis;
	private NumberAxis yAxis;
	
	private ContextMenu hist_menu;
	private MenuItem delete_latest;
	
	private LinearGradient gradient;
	private Background back;

	
	
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
	
	
	public SexyCategoryChart() {
		
		hist_menu = new ContextMenu();
		delete_latest = new MenuItem("Delete Last");
		delete_latest.setOnAction(event -> {
			
			Platform.runLater(() -> {
				
			});
			event.consume();			
        });
		
		hist_menu.getItems().add(delete_latest);
		hist_menu.setStyle(contextSyle);
		

		gradient =new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, 
    			new Stop(0, Color.rgb(2, 40, 50).darker()),
                new Stop(1, Color.rgb(45, 0, 45).darker()) );
    	
    	back = new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
		
		xAxis = new CategoryAxis();
		xAxis.setLabel("Category");

		yAxis = new NumberAxis();
		yAxis.setLabel("Frequency");

		
		sexy_bar_chart = new BarChart<String, Number>(xAxis, yAxis);
		sexy_bar_chart.setBackground(back);
		sexy_bar_chart.setPrefSize(1200, 800);
		sexy_bar_chart.setAnimated(true); 
		sexy_bar_chart.setBarGap(15);
		sexy_bar_chart.setCategoryGap(20);
		
		
		sexy_bar_pane = new StackPane();
		sexy_bar_pane.getChildren().add(sexy_bar_chart);
		
		
		sexy_bar_scene = new Scene(sexy_bar_pane);
		
		sexy_bar_scene.getStylesheets().add(getClass().getClassLoader().getResource("css/TransactionChart.css").toExternalForm());
		sexy_bar_stage = new Stage();
		sexy_bar_stage.setTitle("Category Features");
		sexy_bar_stage.setScene(sexy_bar_scene);
		
//		sexy_bar_chart.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() { 
//            @Override
//            public void handle(ContextMenuEvent event) {
//            	hist_menu.show(sexy_bar_pane.getScene().getWindow(),event.getScreenX(), event.getScreenY());
//            }
//        });
		
		
	}
	
	public void show() {
		sexy_bar_stage.show();
	}
	
	public void removeLast() {
		
		
	}
	
	public void plot(HashMap<String, MutableInt> vals, String name) {
		
		sexy_bar_chart.getData().clear();
		
		XYChart.Series<String, Number> data_series = new XYChart.Series<String, Number>();
		data_series.setName(name);

		for(Entry<String, MutableInt> entry : vals.entrySet()) {
			data_series.getData().add(new XYChart.Data<String, Number>(entry.getKey(), entry.getValue().get()));
		}
		
		sexy_bar_chart.getData().add(data_series);
				
	}
	
	public Stage getSexy_bar_stage() {
		return sexy_bar_stage;
	}


	public void setSexy_bar_stage(Stage sexy_bar_stage) {
		this.sexy_bar_stage = sexy_bar_stage;
	}


	public BarChart<String, Number> getSexy_bar_chart() {
		return sexy_bar_chart;
	}


	public void setSexy_bar_chart(BarChart<String, Number> sexy_bar_chart) {
		this.sexy_bar_chart = sexy_bar_chart;
	}
}
