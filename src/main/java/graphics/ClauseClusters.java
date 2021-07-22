package graphics;

import java.util.ArrayList;
import java.util.Random;

import interpretability.GlobalRealFeatures;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import records.AnyRecord;
import tsetlin.AutomataNode;
import tsetlin.MultivariateConvolutionalAutomatonMachine;
import tsetlin.TsetlinMachine;
import utils.Styles;

public class ClauseClusters {

	
	private ClauseGraphicPanel graphic_panel;
	private Scene clause_scene;
	private Stage clause_stage;
	
	private Font bgFont;
	private final NumberAxis xAxis = new NumberAxis();
    private final NumberAxis yAxis = new NumberAxis();
    
    private XYChart.Series<Number, Number>[] pin;
    private ArrayList<XYChart.Data<Number, Number>>[] data;

	private ScatterChart<Number,Number> sc; 
	
	private Random rng;
	private int n_clauses; 
    
	private GlobalRealFeatures[][][] clause_features;
	
	private int n_classes;
    
	
	private String[] my_colors = new String[] {
			"177, 235, 252",
			"245, 64, 57",
			"77, 240, 55",  
			"55, 197, 240" 
	};
	private DropShadow shadow;
	private Glow glow;
	
    public ClauseClusters() {
    	
  
    	
    	sc = new ScatterChart<Number,Number>(xAxis,yAxis);
    	
    	
	    sc.setAnimated(true);
	    sc.setLegendVisible(false);
	    sc.getXAxis().setTickLabelsVisible(false);
	    sc.getXAxis().setTickMarkVisible(false);
	    sc.getYAxis().setTickLabelsVisible(false);
	    sc.getYAxis().setTickMarkVisible(false);
    	
        
		RadialGradient gradient = new RadialGradient(0, 0, 0.5, 0.50, 0.50, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(50, 20, 26)),
                new Stop(1,Color.rgb(47, 60, 66)));
	    
		sc.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
		sc.setOpacity(.7);
	    
		shadow = new DropShadow();
		shadow.setOffsetX(5);
		shadow.setOffsetY(5);
		shadow.setColor(Color.BLACK);
		shadow.setBlurType(BlurType.GAUSSIAN);
    	
		glow = new Glow(1.0);
		
    	rng = new Random();
    }
    
    
    /** 
     * Initiates the clauses, sets randomly in plot
     * @param machine
     */
    public void initiateClauses(int n_clauses, int n_classes) {
    	
    	this.n_clauses = n_clauses;
    	this.n_classes = n_classes;
    	
    	if(sc.getData().size() > 0) {
    		sc.getData().clear();
    	}
    	
    	data = new ArrayList[n_classes];
    	pin = new XYChart.Series[n_classes];
    	
    	for(int i = 0; i < n_classes; i++) {
    		data[i] = new ArrayList<XYChart.Data<Number, Number>>();
    		pin[i] = new XYChart.Series<Number, Number>();
    	}
    	

    	
    	
    	for(int k = 0; k < n_classes; k++) {    		
    		for(int i = 0; i < n_clauses; i++) { 		
        		data[k].add(new XYChart.Data<Number, Number>(rng.nextInt(30), 0));
        	}  		
    		pin[k].getData().addAll(data[k]);
    	}
    	
    	
    	
    	sc.getData().addAll(pin);
    	setClauseInterface();
    }
    
    
    /**
     * Updates the clauses on the canvas 
     * Y-axis is governed by the weight of the clause
     * X-axis by the leading feature strength
     * 
     * 
     * @param machine
     * @param my_class
     */
    public void updateClauses(TsetlinMachine<AnyRecord> machine, int my_class) {
    	
    	
    	clause_features = machine.getClause_global_strength();
    	
  	
    	for(int k = 0; k < n_classes; k++) {
    		
    		int[] weights = machine.getAutomaton().getMachine(k).getClauseStrength();
    		for(int i = 0; i < n_clauses; i++) {
        		
        		
            	double max_val = 0;
            	int max_index = 0;
            	double neg_max_val = 0;
            	int neg_max_index = 0;
            	
            	int sum_strength = 0;
            	
            	for(int j = 0; j < clause_features[0][0].length; j++) {
            		
            		sum_strength += clause_features[k][i][j].getClauseStrength();
            		sum_strength += clause_features[k][i][j].getClauseNegStrength();
            		
//            		if(max_val < clause_features[k][i][j].getClauseStrength()) {
//            			
//            			max_val = clause_features[k][i][j].getClauseStrength();
//            			max_index = j;
//
//            		}
//            		
//            		if(neg_max_val < clause_features[k][i][j].getClauseNegStrength()) {
//            			
//            			neg_max_val = clause_features[k][i][j].getClauseNegStrength();
//            			neg_max_index = j;
//            		}
            		
            	}

        		data[k].get(i).setYValue(sum_strength);
        		data[k].get(i).setXValue(weights[i]);
        		//data[k].get(i).setXValue(Math.max(neg_max_index, max_index));
        		data[k].get(i).setExtraValue(new Object[] {k, i, clause_features[k][i]});

        		
        	}
    		
    	}

    	setClauseInterface();
    	
    }
    
    
    /**
     * Adds coloring, shading, shadow, and meta info on clause
     */
    public void setClauseInterface() {
    	
    	
    	
    	for(int c = 0; c < n_classes; c++) {
    		
    		
    		
    		for(XYChart.Data<Number, Number> d : data[c]) {
        		
    			String color = my_colors[c];
        		d.getNode().setStyle("-fx-background-color: rgb(" + color + "), transparent;\n"
                                    + "-fx-background-radius: 7px;\n"
                                    + "-fx-padding: 7px;");

        		d.getNode().setEffect(shadow);
    			
        		        		
        		
        		d.getNode().setOnMouseEntered(e -> d.getNode().setEffect(glow));
        		
        		d.getNode().setOnMouseExited(e -> d.getNode().setEffect(shadow));
        		      		
        		
        		Object[] extras = (Object[]) d.getExtraValue();
        		d.getNode().setOnMousePressed(e -> addClauseGraphicPanel((int)extras[1], (int)extras[0], d.getYValue().intValue(), (GlobalRealFeatures[])extras[2]));
        		
        	}
		
    	}
    	
    }
    
    /**
     * Add a clause
     * @param features
     */
    public void addClauseGraphicPanel(int clause_num, int myclass, int weight, GlobalRealFeatures[] feats) {
    	
    	/**
    	 * If null, make new one
    	 */
    	if(graphic_panel == null) {    		
    		
    		graphic_panel = new ClauseGraphicPanel(clause_num, myclass, weight, feats, bgFont);
    		clause_scene = new Scene(graphic_panel.getClause_pane());
    		clause_stage = new Stage();
    		clause_scene.getStylesheets().add("css/WhiteOnBlack.css");
    		clause_stage.setScene(clause_scene);
    		clause_stage.show();
    		
    	}
    	else {
    		
    		Platform.runLater(() -> {
    			graphic_panel.update(clause_num, myclass, weight, feats);
			});
    		
    		
    	}
    	
    	
    	
    	
    }
    
    
    
    public ScatterChart<Number, Number> getSc() {
		return sc;
	}




	public void setSc(ScatterChart<Number, Number> sc) {
		this.sc = sc;
	}


	public void setFont(Font bgFont) {
		this.bgFont = bgFont;
		
	}
    
    
}
