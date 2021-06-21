package graphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import interpretability.GlobalRealFeatures;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.Glow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ClauseGraphicPanel {

	
	private Font myfont;
	private ScrollPane interval_pane;
	private StackPane clause_pane;

	final Text n_clauses_label = new Text("CLAUSE ");
	final Text class_label = new Text("CLASS ");
	final Text weight_label = new Text("WEIGHT ");
	final Text output = new Text("AUTOMATA OUTPUT");
	
	private TextField n_clauses_text;
	private TextField class_text;
	private TextField weight_text;
	
	private AutomatonOutputPanel[] out;
	private Text[] feature_titles;
	private Glow glow;
	
	private final Comparator<GlobalRealFeatures> compare = new Comparator<GlobalRealFeatures>() {
        public int compare(GlobalRealFeatures o1, GlobalRealFeatures o2) {
             return o1.getClauseStrength() + o1.getClauseNegStrength() < o2.getClauseStrength() + o2.getClauseNegStrength() ? -1
                      : o1.getClauseStrength() + o1.getClauseNegStrength() > o2.getClauseStrength() + o2.getClauseNegStrength() ? 1
                      : 0;
        }
    };
	
	
	public ClauseGraphicPanel(int number, int my_class, int weight, GlobalRealFeatures[] clause_features, Font font) {
	

		ArrayList<GlobalRealFeatures> features = new ArrayList<GlobalRealFeatures>();
		for(int i = 0; i < clause_features.length; i++) {
			features.add(clause_features[i]);
		}
		
		Collections.sort(features, compare.reversed());
		
		glow = new Glow(1.0);
		
		n_clauses_text = new TextField("" + number);
		class_text = new TextField("" + my_class);
		weight_text = new TextField("" + weight);
		
		n_clauses_label.setFill(Color.rgb(177, 235, 252));
		class_label.setFill(Color.rgb(177, 235, 252));
		weight_label.setFill(Color.rgb(177, 235, 252));
		output.setFill(Color.rgb(177, 235, 252));
		
		output.setFont(font);
		n_clauses_label.setFont(font);
		class_label.setFont(font);
		weight_label.setFont(font);
		
		
		n_clauses_text.setFont(font);
		class_text.setFont(font);
		weight_text.setFont(font);
		
		n_clauses_text.setPrefWidth(60);
		class_text.setPrefWidth(60);
		weight_text.setPrefWidth(60);
		
		GridPane infopane = new GridPane();
		infopane.setHgap(5);
		infopane.setVgap(5);
		
		infopane.add(n_clauses_label, 0, 0);
		infopane.add(n_clauses_text, 1, 0);
		infopane.add(class_label, 0, 1);
		infopane.add(class_text, 1, 1);
		infopane.add(weight_label, 0, 2);
		infopane.add(weight_text, 1, 2);
		infopane.add(new Label(" "), 0, 3);
		infopane.add(output, 0, 4);
		
		feature_titles = new Text[clause_features.length];
		
		out = new AutomatonOutputPanel[clause_features.length];
		
		GridPane mypane = new GridPane();
		mypane.setHgap(5);
		mypane.setVgap(5);
		
		for(int i = 0; i < Math.min(features.size(), 10); i++) {
			
			out[i] = new AutomatonOutputPanel(features.get(i).getBit_length());
			out[i].changeLights(features.get(i).getBitRanges().getPos_bits(), features.get(i).getBitRanges().getNeg_bits(), features.get(i).getFeature_ranges());
			feature_titles[i] = new Text(features.get(i).getFeatureName());
			feature_titles[i].setFont(font);
			feature_titles[i].setFill(Color.rgb(177, 235, 252));
			feature_titles[i].setEffect(glow);
			
			mypane.add(feature_titles[i], 0, i);
			mypane.add(out[i].getLights_pane(), 1, i);
			
		}
		
		VBox vbox = new VBox();
		vbox.setPadding(new Insets(40));
		
		vbox.getChildren().addAll(infopane, mypane);

		clause_pane = new StackPane();
		clause_pane.getChildren().add(vbox);
	}
	
	
	public void update(int number, int my_class, int weight, GlobalRealFeatures[] clause_features) {
		
		n_clauses_text.setText(number+"");
		class_text.setText("" + my_class);
		weight_text.setText("" + weight);
		
		ArrayList<GlobalRealFeatures> features = new ArrayList<GlobalRealFeatures>();
		for(int i = 0; i < clause_features.length; i++) {
			features.add(clause_features[i]);
		}
		
		Collections.sort(features, compare.reversed());


		
		for(int i = 0; i < Math.min(features.size(), 10); i++) {
					
			out[i].changeLights(features.get(i).getBitRanges().getPos_bits(), features.get(i).getBitRanges().getNeg_bits(), features.get(i).getFeature_ranges());
			feature_titles[i].setText(features.get(i).getFeatureName());
			
		}
		
	}
	
	
	public StackPane getClause_pane() {
		return clause_pane;
	}

	public void setClause_pane(StackPane clause_pane) {
		this.clause_pane = clause_pane;
	}
	
	
	
	
}
