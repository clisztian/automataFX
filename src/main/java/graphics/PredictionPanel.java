package graphics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.lang3.math.NumberUtils;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.medusa.Gauge.SkinType;
import interpretability.GlobalRealFeatures;
import interpretability.Prediction;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import output.CategoryLabel;
import output.OutputLabel;
import output.RealLabel;
import records.AnyRecord;
import tsetlin.TsetlinMachine;
import utils.Styles;

/**
 * A graphics panel that contains many chart components that are updated 
 * with any new input value
 * @author lisztian
 *
 */
public class PredictionPanel {

	
	private Gauge probability;	

	private BarChart<String,Number> local_feature_importance;
	private BarChart<String,Number> class_probability;
	private AutomatonOutputPanel automata_panel;
	
	
	private StackPane local_feature_pane;
	private CategoryAxis xAxis;
	private NumberAxis yAxis;
	
	private CategoryAxis pxAxis;
	private NumberAxis pyAxis;
	
	private LinearGradient gradient;
	private Background back;
	private XYChart.Series<String, Number> pos_features;
	private XYChart.Series<String, Number> neg_features;
	
	private ArrayList<XYChart.Data<String, Number>> pos_points;
	private ArrayList<XYChart.Data<String, Number>> neg_points;
	
	private XYChart.Series<String, Number> prob_class;
	private ArrayList<XYChart.Data<String, Number>> prob_points;
	
	private ScrollPane my_scroll;
	
	private Prediction pred;
	private Button risky_features_updates;
	private Button select_update;
	
	private String[] output_labels;
	
	private boolean plot_risky_features = false;
	
	private OutputLabel any_output;
	private DecimalFormat df = new DecimalFormat("0.##");
	
	private Stage prediction_stage;

	
	private final Comparator<GlobalRealFeatures> compare = new Comparator<GlobalRealFeatures>() {
        public int compare(GlobalRealFeatures o1, GlobalRealFeatures o2) {
             return o1.getStrength() + o1.getNegStrength() < o2.getStrength() + o2.getNegStrength() ? -1
                      : o1.getStrength() + o1.getNegStrength() > o2.getStrength() + o2.getNegStrength() ? 1
                      : 0;
        }
    };
	private int n_real_features;
	private Gauge prediction_type;
	private float[] real_val_outputs;
	
	
	public PredictionPanel(TsetlinMachine<AnyRecord> machine, OutputLabel output, int nbits) {
		
		n_real_features = machine.getN_real_features();
		any_output = output;
		
		buildBarChar();
			
		
		if(output instanceof RealLabel) {
			
			RealLabel out = (RealLabel)output;
			
			real_val_outputs = out.getLabels();
			
			output_labels = new String[real_val_outputs.length];
			
			for(int i = 0; i < output_labels.length; i++) output_labels[i] = df.format(real_val_outputs[i]);
		}
		else if(output instanceof CategoryLabel) {
			
			CategoryLabel out = (CategoryLabel)output;
			ArrayList<String> temp = new ArrayList<String>();
			
			temp.addAll(out.getRecordColumn().getCategory_map().keySet());
			output_labels = new String[temp.size()];
			
			for(int i = 0; i < output_labels.length; i++) output_labels[i] = temp.get(i);
			
		}
		
		automata_panel = new AutomatonOutputPanel(nbits);
		
		select_update = new Button("PREDICTIVE MODE");
		select_update.setStyle(Styles.IDLE_BUTTON_STYLE);
		select_update.setOnMouseEntered(e -> select_update.setStyle(Styles.HOVERED_BUTTON_STYLE));
		select_update.setOnMouseExited(e -> select_update.setStyle(Styles.IDLE_BUTTON_STYLE));

		select_update.setOnMouseReleased(e -> {
			select_update.setStyle(Styles.HOVERED_BUTTON_STYLE);
		});
		
		
		risky_features_updates = new Button("PREDICTIVE MODE");
		risky_features_updates.setStyle(Styles.IDLE_BUTTON_STYLE);
		risky_features_updates.setOnMouseEntered(e -> risky_features_updates.setStyle(Styles.HOVERED_BUTTON_STYLE));
		risky_features_updates.setOnMouseExited(e -> risky_features_updates.setStyle(Styles.IDLE_BUTTON_STYLE));

		risky_features_updates.setOnMouseReleased(e -> {
			risky_features_updates.setStyle(Styles.HOVERED_BUTTON_STYLE);
		});
		
		risky_features_updates.setOnMousePressed(e -> {
			risky_features_updates.setStyle(Styles.DOWN_BUTTON_STYLE);
			plot_risky_features = !plot_risky_features;
			if(plot_risky_features) risky_features_updates.setText("RISK MODE");
			else risky_features_updates.setText("PREDICTIVE MODE");
			
			Platform.runLater(() -> {
				try {
					updateFeatureChart();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			});
			
		});
		
		
		risky_features_updates.setFont(Styles.bgFont);
		risky_features_updates.setPrefSize(180, 45);
		
		select_update.setFont(Styles.bgFont);
		select_update.setPrefSize(180, 45);
		
		
		GridPane button_pane = new GridPane();
		button_pane.setHgap(30);
		button_pane.setVgap(30); 
		button_pane.add(risky_features_updates, 0, 0);
		button_pane.add(select_update, 0, 1);
		
		BorderPane.setAlignment(button_pane,Pos.CENTER);
		BorderPane embed = new BorderPane(button_pane);	
		
        // Set the Size of the VBox
		embed.setPrefSize(300, 300);     
        // Set the Style-properties of the BorderPane
		embed.setStyle("-fx-padding: 20;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 1;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: rgb(177, 235, 252);" +
                "-fx-border-effect: dropshadow( gaussian , rgb(177, 235, 252) , 25 , .4 , 0, 0 )");
		
		
		
		
		
		
		probability = GaugeBuilder.create()
		        .skinType(SkinType.DIGITAL)
		        .foregroundBaseColor(Color.rgb(177, 235, 252))
		        .barColor(Color.rgb(177, 235, 252))
		        .title("STRENGTH")
		        .unit("%")
		        .maxValue(100.0)
		        .animated(true)
		        .build();
		
		prediction_type = GaugeBuilder.create()
		        .skinType(SkinType.DIGITAL)
		        .foregroundBaseColor(Color.rgb(177, 235, 252))
		        .barColor(Color.rgb(177, 235, 252))
		        .title("PREDICTION")
		        .unit("")
		        .maxValue(100.0)
		        .animated(true)
		        .build();
		
		if(any_output instanceof RealLabel) {
			prediction_type.setMinValue(real_val_outputs[0]);
			prediction_type.setMaxValue(real_val_outputs[real_val_outputs.length  - 1]);			
		}
		if(any_output instanceof CategoryLabel) {
			prediction_type.setMinValue(0);
			prediction_type.setMaxValue(output_labels.length);			
		}
		
				
		
		
		GridPane top_pane =  new GridPane();
		top_pane.setHgap(30);
		top_pane.setVgap(30);
		top_pane.add(probability, 0, 0);
		top_pane.add(prediction_type, 1, 0);
		top_pane.add(embed, 2, 0);
		top_pane.add(class_probability, 3, 0);
		
		VBox box = new VBox(); 		
		box.setPadding(new Insets(40));
		box.getChildren().addAll(top_pane, my_scroll);
		box.setPrefSize(1400, 1000);
		
		Scene pred_scene = new Scene(box);
		pred_scene.getStylesheets().add(getClass().getClassLoader().getResource("css/WhiteOnBlack.css").toExternalForm());
		prediction_stage = new Stage();
		prediction_stage.setScene(pred_scene);
		prediction_stage.show();
		
	}
	
	
	public boolean isShowing() {
		return prediction_stage.isShowing();
	}
	
	
	/**
	 * Update all the prediction components
	 * @param pred
	 * @throws Exception 
	 */
	public void update(Prediction pred) throws Exception {
		
		this.pred = pred;
		
		probability.setValue(pred.getProbability()*100);
		
		String val = "";
		if(any_output instanceof CategoryLabel) {
			val = ((CategoryLabel)any_output).decode(pred.getPred_class());
			prediction_type.setTitle(val);
			if(NumberUtils.isCreatable(val)) {
				prediction_type.setValue(Double.parseDouble(val));
			}
		}
		else if(any_output instanceof RealLabel) {
			prediction_type.setValue(((RealLabel)any_output).decode(pred.getPred_class()));
			
		}
		
		
		
		updateFeatureChart();
		updateProbabilityChart();
	}
	
	
	
	public void buildBarChar() {
		
		
		gradient =new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, 
    			new Stop(0, Color.rgb(2, 40, 50).darker()),
                new Stop(1, Color.rgb(45, 0, 45).darker()) );
    	
    	back = new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
		
		xAxis = new CategoryAxis();
		xAxis.setLabel("Numerical Feature");

		yAxis = new NumberAxis();
		yAxis.setLabel("Importance");
		
		
		local_feature_importance = new BarChart<String, Number>(xAxis, yAxis);
		local_feature_importance.setBackground(back);
		local_feature_importance.setPrefSize(n_real_features*70, 500);
		local_feature_importance.setAnimated(true); 
		local_feature_importance.setBarGap(2);
		local_feature_importance.setCategoryGap(15);
		
		
		my_scroll = new ScrollPane();
		my_scroll.setMinSize(1000,500);
		my_scroll.pannableProperty().set(true);
		my_scroll.fitToHeightProperty().set(true);
		my_scroll.setContent(local_feature_importance);
		
		local_feature_importance.setOnScroll(new EventHandler<ScrollEvent>() {

			@Override
			public void handle(ScrollEvent event) {
			
				event.consume();
				if(event.getDeltaY() == 0) {
					return;
				}
				
				double scaleFactor = event.getDeltaY();
				double w = local_feature_importance.getWidth();
				
				if(w + scaleFactor > 1200) {
					local_feature_importance.setMinWidth(w + scaleFactor);
				}				
			}		
		});
		
		
		local_feature_pane = new StackPane();
		local_feature_pane.getChildren().add(my_scroll);
		
		pos_features = new XYChart.Series<String, Number>();
		neg_features = new XYChart.Series<String, Number>();
		
		pos_features.setName("Inclusion");
		neg_features.setName("Negation");
		
		
		pxAxis = new CategoryAxis();
		pxAxis.setLabel("Output");

		pyAxis = new NumberAxis();
		pyAxis.setLabel("Strength");
		
		class_probability = new BarChart<String, Number>(pxAxis, pyAxis);
		class_probability.setBackground(back);
		class_probability.setPrefSize(800, 400);
		class_probability.setAnimated(true); 
		class_probability.setBarGap(2);
		class_probability.setCategoryGap(3);
		
		prob_class = new XYChart.Series<String, Number>();
		
		
	}
	
	
	
	
	public void updateFeatureChart() throws Exception {
		
		if(pred == null) {
			throw new Exception("No prediction found yet");
		}
		
		GlobalRealFeatures[][] feats = plot_risky_features ? pred.getRisk_real_features() : pred.getReal_features();
	
		/*
		 * Initiate and redesign from scratch
		 */
		if(pos_points == null || pos_points.size() == 0) {
				
			clearPlots();
			
			pos_points = new ArrayList<XYChart.Data<String, Number>>();
			neg_points = new ArrayList<XYChart.Data<String, Number>>();
			

			ArrayList<GlobalRealFeatures> real = new ArrayList<GlobalRealFeatures>();
			for(int i = 0; i < feats[0].length; i++) {
				real.add(feats[0][i]);
			}
			Collections.sort(real, compare.reversed());
			
			
			for(int i = 0; i < real.size(); i++) {
				
				float pos_val = real.get(i).getBitRanges().getStrength();
				float neg_val = real.get(i).getBitRanges().getNeg_strength();
				
				pos_points.add(new XYChart.Data<String, Number>(real.get(i).getFeatureName(), pos_val));
				neg_points.add(new XYChart.Data<String, Number>(real.get(i).getFeatureName(), neg_val));
				
			}
			pos_features.getData().addAll(pos_points);
			neg_features.getData().addAll(neg_points);
			
			local_feature_importance.getData().addAll(pos_features, neg_features);
			
		}
		
		/**
		 * Aint my first rodeo
		 */
		else if(feats[0].length == pos_points.size()) {
			
			ArrayList<GlobalRealFeatures> real = new ArrayList<GlobalRealFeatures>();
			for(int i = 0; i < feats[0].length; i++) {
				real.add(feats[0][i]);
			}
			Collections.sort(real, compare.reversed());
			
			for(int i = 0; i < pos_features.getData().size(); i++) {
				
				float pos_val = real.get(i).getBitRanges().getStrength();
				float neg_val = real.get(i).getBitRanges().getNeg_strength();
				
				pos_features.getData().get(i).setXValue(real.get(i).getFeatureName());
				neg_features.getData().get(i).setXValue(real.get(i).getFeatureName());
				
				pos_features.getData().get(i).setYValue(pos_val);
				neg_features.getData().get(i).setYValue(neg_val);
				
			}
			
		}
		
		
	}
	
	
	public void updateProbabilityChart() {
		
		float[] probs = pred.getPred_probabilities();
		
		if(prob_points == null || prob_points.size() == 0) {
			
			prob_points = new ArrayList<XYChart.Data<String, Number>>();
			
			for(int i = 0; i < output_labels.length; i++) {
				prob_points.add(new XYChart.Data<String, Number>(output_labels[i], probs[i]));
			}
			prob_class.getData().addAll(prob_points);
			class_probability.getData().add(prob_class);
			
			if(prob_class.getNode() != null) {
				prob_class.getNode().setStyle("-fx-bar-fill: rgb(46, 91, 255);");
			}
			
		}
		else {			
			for(int i = 0; i < output_labels.length; i++) {
				prob_points.get(i).setYValue(probs[i]);
			}
		}
	}


	public void clearPlots() {
		
		pos_features.getData().clear();
		neg_features.getData().clear();
		pos_points = new ArrayList<XYChart.Data<String, Number>>();
		neg_points = new ArrayList<XYChart.Data<String, Number>>();
		
		local_feature_importance.getData().clear();
		
		
		prob_class.getData().clear();
		prob_points = new ArrayList<XYChart.Data<String, Number>>();
		class_probability.getData().clear();
		
	}
	
	
	
	
	
	
}
