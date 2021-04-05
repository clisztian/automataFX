package examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import graphics.ClauseScatterPane;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tagbio.umap.Umap;
import tagbio.umap.metric.Metric;

public class UMAPane extends Application {

	private Stage clause_stage;
    private ClauseScatterPane clause_pane;

    private Stage controller_stage;
    private StackPane controller;
    private ComboBox<String> metric_box;
    private Slider neibor_slider;
    private Slider min_dist_slider;
    private Umap umap;
    private TextField neibor_text;
    private TextField min_dist_text;
    private DataPair pair;

    public void buildController() {
    	
    	neibor_slider = new Slider();
    	neibor_slider.setMin(5);
    	neibor_slider.setMax(50);
    	
    	min_dist_slider = new Slider();
    	min_dist_slider.setMin(1);
    	min_dist_slider.setMax(5);
    	

    	ObservableList<String> options = 
    		    FXCollections.observableArrayList(
    		    		"euclidean",
    		    		"l2",
    		    		"manhattan",
    		    		"l1",
    		    		"taxicab",
    		    		"chebyshev",
    		    		"linfinity",
    		    		"linfty",
    		    		"linf",
    		    		"canberra",
    		    		"cosine",
    		    		"correlation",
    		    		"haversine",
    		    		"braycurtis",
    		    		"jaccard",
    		    		"dice",
    		    		"matching",
    		    		"kulsinski",
    		    		"rogerstanimoto",
    		    		"russellrao",
    		    		"sokalsneath",
    		    		"sokalmichener",
    		    		"yule"
    		    );
    	metric_box = new ComboBox<String>(options);
    	
    	metric_box.setOnAction((event) -> {
    	    
    	    String selectedItem = (String)metric_box.getSelectionModel().getSelectedItem();
    	    umap.setTargetMetric(Metric.getMetric(selectedItem));
    	    replot();
    	});


    	neibor_text = new TextField();
    	neibor_text.setPrefWidth(100);
    	neibor_slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    System.out.println(new_val.intValue());
                    umap.setNumberNearestNeighbours(new_val.intValue());
                    neibor_text.setText("" + new_val.intValue());
                    replot();
            }
        });
    	

    	min_dist_text = new TextField();
    	min_dist_text.setPrefWidth(100);
    	min_dist_slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
                        
            			umap.setMinDist(.1f*new_val.floatValue());
            			min_dist_text.setText("" + .1*new_val.doubleValue());
                        replot();
                }
        });

    	GridPane input_grid = new GridPane();
    	input_grid.setHgap(10);
        input_grid.setVgap(10);
        input_grid.add(metric_box, 0, 0);
        input_grid.add(neibor_text, 1, 0);
        input_grid.add(neibor_slider, 2, 0);
        input_grid.add(min_dist_text, 3, 0);
        input_grid.add(min_dist_slider, 4, 0);
    	
        controller = new StackPane();
        controller.getChildren().add(input_grid);
        

        controller_stage = new Stage();
        controller_stage.setTitle("UMAP Controller");
        Scene controller_scene = new Scene(controller);
        controller_scene.getStylesheets().add("css/WhiteOnBlack.css");
        controller_stage.setScene(controller_scene);
		
        controller_stage.show(); 
    }
    
    public void replot() {
    	

    	final float[][] result = umap.fitTransform(pair.getData());
		
		Platform.runLater(() -> {
			clause_pane.computeScatterPlot(result, pair.getLabels());
        });
    	
    }
    
    public void buildClauseScatterPane() {
		
    	umap = new Umap();
    	umap.setNumberComponents(3);         // number of dimensions in result
		umap.setNumberNearestNeighbours(15);
		umap.setThreads(20);
		
		clause_pane = new ClauseScatterPane();
		clause_stage = new Stage();
		clause_stage.setTitle("Clause plot");
		clause_pane.getClauseScene().getStylesheets().add("css/WhiteOnBlack.css");
		clause_stage.setScene(clause_pane.getClauseScene());
		
		clause_stage.show();
		
	}
	
	@Override
	public void start(Stage arg0) throws Exception {

		buildClauseScatterPane();	

		buildController();
		

		pair = getDigitData("/home/lisztian/AutomataFX/workspace/automataFX/src/main/resources/data/digits.tsv", 1000);
		

		createMap(pair);
		
	}

	
	public void createMap(DataPair pair) {
		
		
		
		final float[][] result = umap.fitTransform(pair.getData());
		
		Platform.runLater(() -> {
			clause_pane.computeScatterPlot(result, pair.getLabels());
        });
		
		
		
	}
	
	

	public static DataPair getDigitData(String fileName, int N) throws IOException {
		
		ArrayList<float[]> digits = new ArrayList<float[]>();	
		List<String> allLines = Files.readAllLines(Paths.get(fileName));
		float[][] my_data = new float[N][];
		int[] labels = new int[N];
		for(int i = 1; i <= N; i++) {
			
			String line = allLines.get(i);
			String[] obs = line.split("\t", -1);

			my_data[i-1] = new float[obs.length-1];
			
			labels[i-1] = Integer.parseInt(obs[0].split("[:]+")[0]);
			for(int k = 1; k < obs.length; k++) {
				my_data[i-1][k-1] = Float.parseFloat(obs[k]);
			}
		}
		
		return new DataPair(my_data, labels);
	}
	
	
	@Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
	
}

class DataPair {
	
	public DataPair(float[][] d, int[] l) {
		this.data = d;
		this.labels = l;
	}
	
	public float[][] getData() {
		return data;
	}
	public void setData(float[][] data) {
		this.data = data;
	}
	public int[] getLabels() {
		return labels;
	}
	public void setLabels(int[] labels) {
		this.labels = labels;
	}
	private float[][] data;
	private int[] labels;
	
}

//sMETRICS.put("euclidean", EuclideanMetric.SINGLETON);
//sMETRICS.put("l2", EuclideanMetric.SINGLETON);
//sMETRICS.put("manhattan", ManhattanMetric.SINGLETON);
//sMETRICS.put("l1", ManhattanMetric.SINGLETON);
//sMETRICS.put("taxicab", ManhattanMetric.SINGLETON);
//sMETRICS.put("chebyshev", ChebyshevMetric.SINGLETON);
//sMETRICS.put("linfinity", ChebyshevMetric.SINGLETON);
//sMETRICS.put("linfty", ChebyshevMetric.SINGLETON);
//sMETRICS.put("linf", ChebyshevMetric.SINGLETON);
//sMETRICS.put("canberra", CanberraMetric.SINGLETON);
//sMETRICS.put("cosine", CosineMetric.SINGLETON);
//sMETRICS.put("correlation", CorrelationMetric.SINGLETON);
//sMETRICS.put("haversine", HaversineMetric.SINGLETON);
//sMETRICS.put("braycurtis", BrayCurtisMetric.SINGLETON);
//sMETRICS.put("hamming", HammingMetric.SINGLETON);
//sMETRICS.put("jaccard", JaccardMetric.SINGLETON);
//sMETRICS.put("dice", DiceMetric.SINGLETON);
//sMETRICS.put("matching", MatchingMetric.SINGLETON);
//sMETRICS.put("kulsinski", KulsinskiMetric.SINGLETON);
//sMETRICS.put("rogerstanimoto", RogersTanimotoMetric.SINGLETON);
//sMETRICS.put("russellrao", RussellRaoMetric.SINGLETON);
//sMETRICS.put("sokalsneath", SokalSneathMetric.SINGLETON);
//sMETRICS.put("sokalmichener", SokalMichenerMetric.SINGLETON);
//sMETRICS.put("yule", YuleMetric.SINGLETON);
