package controls;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Map;

import com.jfoenix.controls.JFXSlider;


import graphics.ClauseScatterPane;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.effect.Glow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tagbio.umap.Umap;
import tagbio.umap.metric.Metric;
import utils.DataPair;

public class EmbeddingPanel {

	private Stage clause_stage;
    private ClauseScatterPane clause_pane;

    private Stage controller_stage;
    private StackPane controller;

	private ComboBox<String> metric_box;
    private Umap umap;

    private DataPair pair;
	
    private JFXSlider neibor_slider;
    private JFXSlider min_dist_slider;
    
    private TextField neibor_text;
    private TextField min_dist_text;
    
    private GridPane input_grid;
    
    private DecimalFormat df = new DecimalFormat("0.00");


	/**
     * ObservationalList of the full records
     */
    private ObservableList<Map> underlying_data;
    
    public EmbeddingPanel() { }
    
    public void buildController() {
    	
    	neibor_slider = new JFXSlider(5, 50, 5);    	
    	min_dist_slider = new JFXSlider(1, 5, 1);
    	neibor_slider.setPrefWidth(250);
    	min_dist_slider.setPrefWidth(250);
    	
    	
    	Font bgFont = null;
		InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/static/Exo-Medium.ttf");
		if (fontStream != null) {
            bgFont = Font.loadFont(fontStream, 16);	 
		}
		else {
			bgFont = Font.font("Cambria", 14);
			System.out.println("Didn't find..");
		}
    	
    	Text neiibor_label = new Text("NEIGHBORS");
    	Text min_dist_label = new Text("DISTANCE");
    	Text metric_label = new Text("METRIC");
    	
    	neiibor_label.setFont(bgFont);
    	min_dist_label.setFont(bgFont);
    	metric_label.setFont(bgFont);

		
    	neiibor_label.setFill(Color.rgb(177, 235, 252));
    	min_dist_label.setFill(Color.rgb(177, 235, 252));
    	metric_label.setFill(Color.rgb(177, 235, 252));

				
		neiibor_label.setEffect(new Glow(.5));
		min_dist_label.setEffect(new Glow(.5));
		metric_label.setEffect(new Glow(.5));

    	

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
    	
    	options.stream().forEach(e -> e.toUpperCase());
    	metric_box = new ComboBox<String>(options);
    	metric_box.getSelectionModel().select(0);
    	metric_box.setEffect(new Glow(1.0));
    	
    	
    	metric_box.setOnAction((event) -> {
    	    
    	    String selectedItem = (String)metric_box.getSelectionModel().getSelectedItem();
    	    umap.setTargetMetric(Metric.getMetric(selectedItem));
    	    replot();
    	});


    	neibor_text = new TextField("5");
    	neibor_text.setPrefWidth(100);
    	neibor_slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                Number old_val, Number new_val) {
                    umap.setNumberNearestNeighbours(new_val.intValue());
                    neibor_text.setText("" + new_val.intValue());
                    replot();
            }
        });

    	min_dist_text = new TextField(".1");
    	min_dist_text.setPrefWidth(100);
    	min_dist_slider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
                        
            			umap.setMinDist(.1f*new_val.floatValue());
            			min_dist_text.setText("" + df.format(.1f*new_val.floatValue()));
                        replot();
                }
        });

    	
    	
    	input_grid = new GridPane();
    	input_grid.setHgap(30);
        input_grid.setVgap(30);
        
        input_grid.add(neiibor_label, 0, 0);
        input_grid.add(neibor_slider, 1, 0);
        input_grid.add(neibor_text, 2, 0);
        
        input_grid.add(min_dist_label, 0, 1);
        input_grid.add(min_dist_slider, 1, 1);
        input_grid.add(min_dist_text, 2, 1);
        
        input_grid.add(metric_label, 0, 2);
        input_grid.add(metric_box, 1, 2);
        

        
    	
//        controller = new StackPane();
//        controller.getChildren().add(input_grid);
//        
//
//        controller_stage = new Stage();
//        controller_stage.setTitle("UMAP Embedding Controller");
//        Scene controller_scene = new Scene(controller);
//        controller_scene.getStylesheets().add("css/WhiteOnBlack.css");
//        controller_stage.setScene(controller_scene);
//		
//        controller_stage.show(); 
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
		
		
		
	}
    
    public void replot() {
    	

    	if(pair != null) {
    		
    		final float[][] result = umap.fitTransform(pair.getData());
    		
    		Platform.runLater(() -> {
    			clause_pane.computeScatterPlot(result, pair.getLabels());
            });
    	}
    	
    	
    }
	
    
    
	public void createMap(DataPair pair, ObservableList<Map> items) {
		
		System.out.println("Items: " + items.size());
		
		this.pair = pair;
		this.underlying_data = items;
		
		final float[][] result = umap.fitTransform(pair.getData());
		
		Platform.runLater(() -> {
			clause_pane.computeScatterPlot(result, pair.getLabels());
        });
		
		clause_stage.show();
		
	}

	public void enlightenBulbs(String name, String xValue) {
				
		if(pair != null && pair.getData().length == underlying_data.size()) {
			
			Platform.runLater(() -> {
				clause_pane.enlightenBalls(underlying_data, name, xValue);
			});	
		}		
	}
	
	public void delightenBulbs() {
		
		if(pair != null) {
			
			Platform.runLater(() -> {
				clause_pane.delightenBalls();
			});	
		}		
	}
	
    public GridPane getInput_grid() {
		return input_grid;
	}

	public void setInput_grid(GridPane input_grid) {
		this.input_grid = input_grid;
	}
    
}
