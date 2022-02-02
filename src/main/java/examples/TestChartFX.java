package examples;



import cern.extjfx.chart.NumericAxis;
import cern.extjfx.chart.XYChartPane;
import cern.extjfx.chart.data.DataReducingObservableList;
import cern.extjfx.chart.plugins.CrosshairIndicator;
import cern.extjfx.chart.plugins.DataPointTooltip;
import cern.extjfx.chart.plugins.Panner;
import cern.extjfx.chart.plugins.Zoomer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import utils.RandomDataGenerator;

public class TestChartFX extends Application {

	
	final int MAX_NUMBER_OF_POINTS = 500000;
	private AreaChart<Number, Number> lineChart;
	
	private StackPane data_reducing_pane;
	private Scene data_scene;
	private Color startColor;
	private Color endColor;
	private RadialGradient gradient;
	private Background back;


	
	@SuppressWarnings("unchecked")
	public TestChartFX() {
		
		
		startColor = Color.DARKRED.darker().darker();
    	endColor = Color.BLACK;
    	 
    	gradient = new RadialGradient(0, 0, 0.5, 0.25, 0.8, true, CycleMethod.NO_CYCLE,
    			new Stop(0, startColor),
                new Stop(1,endColor));
    	
    	back = new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
		
		
		
		NumericAxis xAxis = new NumericAxis();
		xAxis.setAnimated(false);
		NumericAxis yAxis = new NumericAxis();
		yAxis.setAnimated(false);
		

		lineChart = new AreaChart<>(xAxis, yAxis);
		lineChart.setTitle("Test data");
		//lineChart.setCreateSymbols(false);
		 
		DataReducingObservableList<Number, Number> reducedData = new DataReducingObservableList<>(xAxis, RandomDataGenerator.generateData(0, 1, MAX_NUMBER_OF_POINTS));
		DataReducingObservableList<Number, Number> secondData = new DataReducingObservableList<>(xAxis, RandomDataGenerator.generateData(0, 1, MAX_NUMBER_OF_POINTS));
		
		lineChart.getData().addAll(new Series<>("Random data", reducedData), new Series<>("Random data2", secondData));
		


		XYChartPane<Number, Number> chartPane = new XYChartPane<>(lineChart);
		chartPane.getPlugins().addAll(new Zoomer(), new Panner(), new DataPointTooltip(), new CrosshairIndicator<>());

		data_reducing_pane = new StackPane();
		data_reducing_pane.getChildren().add(chartPane);
		
		data_reducing_pane.setMinSize(1000, 800);
		
		data_scene = new Scene(data_reducing_pane);
		data_scene.getStylesheets().add("css/WhiteOnBlack.css");

		lineChart.setBackground(back);
		
		
		
	}
	
	@Override
	public void start(Stage arg0) throws Exception {
		

		TestChartFX chart = new TestChartFX();
		
		
		
		arg0.setTitle("DataChart");
		arg0.setScene(chart.getData_scene());
		arg0.show();
		
	}
	
    public static void main(String[] args) {
        launch(args);
    }
	


	public StackPane getData_reducing_pane() {
		return data_reducing_pane;
	}

	public void setData_reducing_pane(StackPane data_reducing_pane) {
		this.data_reducing_pane = data_reducing_pane;
	}

	public Scene getData_scene() {
		return data_scene;
	}

	public void setData_scene(Scene data_scene) {
		this.data_scene = data_scene;
	}



	public int getMAX_NUMBER_OF_POINTS() {
		return MAX_NUMBER_OF_POINTS;
	}



	
	
}
