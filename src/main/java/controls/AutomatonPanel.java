package controls;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;

import com.jfoenix.controls.JFXSlider;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * An automaton panel for 
 * a one-or multi dimension Tsetlin machine
 * 
 * Parameterizations used
 * 
 * Clauses N
 * Specificity S
 * Feature binarization dimension DIM
 * Threshold T
 * Dropout rate D
 * 
 * @author lisztian
 *
 */
public class AutomatonPanel {

	

	private DecimalFormat df = new DecimalFormat("0.00");
	
	private JFXSlider n_clauses_slider;
	private JFXSlider specificty_slider;
	private JFXSlider threshold_slider;
	private JFXSlider dropout_rate_slider;
	private JFXSlider n_epochs_slider;
	
	private TextField n_clauses_text;
	private TextField specificty_text;
	private TextField threshold_text;
	private TextField dropout_rate_text;
	private TextField n_epochs_text;
	
	private StackPane control_stack;
	private Stage control_stage;
	

	private int n_clauses;
	private float specificity;
	private int threshold;
	private float dropout_rate;
	
	
	public void buildSliders() {
		
		
		n_clauses_slider = new JFXSlider(10, 1000, 50);
		specificty_slider = new JFXSlider(.1, 25.0, .5);
		threshold_slider = new JFXSlider(10, 5000, 20);
		dropout_rate_slider = new JFXSlider(0, 1, 0);
		n_epochs_slider = new JFXSlider(1, 50, 5);
		
		n_clauses_slider.setMajorTickUnit(1.0);
		specificty_slider.setMajorTickUnit(.01);
		threshold_slider.setMajorTickUnit(10);
		dropout_rate_slider.setMajorTickUnit(0.01);
		n_epochs_slider.setMajorTickUnit(1.0);
		
		n_clauses_slider.setShowTickMarks(false);
		specificty_slider.setShowTickMarks(false);
		threshold_slider.setShowTickMarks(false);
		dropout_rate_slider.setShowTickMarks(false);
		n_epochs_slider.setShowTickMarks(false);
	
		n_clauses_slider.setBlockIncrement(10);
		specificty_slider.setBlockIncrement(.1);
		threshold_slider.setBlockIncrement(10);
		dropout_rate_slider.setBlockIncrement(.1);
		n_epochs_slider.setBlockIncrement(1);
		
		//177, 235, 252
		n_clauses_text = new TextField("50");
		specificty_text = new TextField(".5");
		threshold_text = new TextField("20");
		dropout_rate_text = new TextField("0.0");
		n_epochs_text = new TextField("5");
		
		n_clauses_text.setPrefWidth(60);
		specificty_text.setPrefWidth(60);
		threshold_text.setPrefWidth(60);
		dropout_rate_text.setPrefWidth(60);
		n_epochs_text.setPrefWidth(60);
		
		
		n_clauses_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
			n_clauses_text.setText(newValue.intValue() + "");
			n_clauses = newValue.intValue();
		});
		
		specificty_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
			specificty_text.setText(df.format(newValue.floatValue()) + "");
			specificity = newValue.floatValue();
		});
		
		threshold_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
			threshold_text.setText(newValue.intValue() + "");
			threshold = newValue.intValue();
		});
		
		dropout_rate_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
			dropout_rate_text.setText(df.format(newValue.floatValue()) + "");
			dropout_rate = newValue.floatValue();
		});
		

		n_clauses_slider.setPrefWidth(250);
		specificty_slider.setPrefWidth(250);
		threshold_slider.setPrefWidth(250);
		dropout_rate_slider.setPrefWidth(250);
		
		Font bgFont = null;
		InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/static/Exo-Medium.ttf");
		if (fontStream != null) {
            bgFont = Font.loadFont(fontStream, 14);	 
            System.out.println("FoundFont");
		}
		else {
			bgFont = Font.font("Cambria", 14);
			System.out.println("Didn't find..");
		}
		
		
		final Text n_clauses_label = new Text("CLAUSES:");
		final Text thresh_label = new Text("THRESHOLD:");
		final Text spec_label = new Text("SPECIFICITY:");
		final Text dropout_label = new Text("DROPOUT:");
		

//		final Label n_clauses_label = new Label("Clauses:");
//		final Label thresh_label = new Label("Threshold:");
//		final Label spec_label = new Label("Specificity:");
//		final Label dropout_label = new Label("Dropout:");
		
		n_clauses_label.setFont(bgFont);
		thresh_label.setFont(bgFont);
		spec_label.setFont(bgFont);
		dropout_label.setFont(bgFont);
		
		n_clauses_label.setFill(Color.rgb(177, 235, 252));
		thresh_label.setFill(Color.rgb(177, 235, 252));
		spec_label.setFill(Color.rgb(177, 235, 252));
		dropout_label.setFill(Color.rgb(177, 235, 252));
		
		
		n_clauses_label.setEffect(new Glow(.5));
		thresh_label.setEffect(new Glow(.5));
		spec_label.setEffect(new Glow(.5));
		dropout_label.setEffect(new Glow(.5));
		
		
		GridPane clause_pane = new GridPane();
		clause_pane.setHgap(20);
		clause_pane.setVgap(20);
		clause_pane.add(n_clauses_label, 0, 0);
		clause_pane.add(n_clauses_slider, 1, 0);
		clause_pane.add(n_clauses_text, 2, 0);
	
		clause_pane.add(spec_label, 0, 1);
		clause_pane.add(specificty_slider, 1, 1);
		clause_pane.add(specificty_text, 2, 1);
		
		clause_pane.add(thresh_label, 0, 2);
		clause_pane.add(threshold_slider, 1, 2);
		clause_pane.add(threshold_text, 2, 2);
		
		clause_pane.add(dropout_label, 0, 3);
		clause_pane.add(dropout_rate_slider, 1, 3);
		clause_pane.add(dropout_rate_text, 2, 3);
		
		control_stack = new StackPane();
		control_stack.getChildren().add(clause_pane);
		
		Scene control_scene = new Scene(control_stack);
		control_scene.getStylesheets().add(getClass().getClassLoader().getResource("css/WhiteOnBlack.css").toExternalForm());
		
		//linear-gradient(to right, rgb(26, 235, 221), rgb(230, 28, 155));
//		n_clauses_slider.setStyle("-fx-control-inner-background: rgb(24, 217, 185);");
//		specificty_slider.setStyle("-fx-control-inner-background: rgb(24, 217, 185);");
//		threshold_slider.setStyle("-fx-control-inner-background: rgb(24, 217, 185);");
//		dropout_rate_slider.setStyle("-fx-control-inner-background: rgb(24, 217, 185);");
	
		LinearGradient gradient =new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, 
    			new Stop(0, Color.rgb(24, 217, 185)),
                new Stop(1, Color.rgb(217, 24, 185)) );
    	
		Background back = new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
		
    		
		//n_clauses_slider.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
		
		
//		n_clauses_slider.setOnMouseEntered(e -> {
//			n_clauses_slider.setBackground(back);
//		});
//		
//		n_clauses_slider.setOnMouseExited(e -> {
//			n_clauses_slider.setBackground(null);
//		});
//		
//		
//		specificty_slider.setOnMouseEntered(e -> {
//			specificty_slider.setBackground(back);
//		});
//		
//		specificty_slider.setOnMouseExited(e -> {
//			specificty_slider.setBackground(null);
//		});
//		
//		threshold_slider.setOnMouseEntered(e -> {
//			threshold_slider.setBackground(back);
//		});
//		
//		threshold_slider.setOnMouseExited(e -> {
//			threshold_slider.setBackground(null);
//		});
//		
//		dropout_rate_slider.setOnMouseEntered(e -> {
//			dropout_rate_slider.setBackground(back);
//		});
//		
//		dropout_rate_slider.setOnMouseExited(e -> {
//			dropout_rate_slider.setBackground(null);
//		});
		
		
		
		control_stage = new Stage();
		control_stage.setScene(control_scene);
		
		
		
		control_stage.show();
	}
	
	
	
	public JFXSlider getN_clauses_slider() {
		return n_clauses_slider;
	}
	public void setN_clauses_slider(JFXSlider n_clauses_slider) {
		this.n_clauses_slider = n_clauses_slider;
	}
	public JFXSlider getSpecificty_slider() {
		return specificty_slider;
	}
	public void setSpecificty_slider(JFXSlider specificty_slider) {
		this.specificty_slider = specificty_slider;
	}
	public JFXSlider getThreshold_slider() {
		return threshold_slider;
	}
	public void setThreshold_slider(JFXSlider threshold_slider) {
		this.threshold_slider = threshold_slider;
	}
	public JFXSlider getDropout_rate_slider() {
		return dropout_rate_slider;
	}
	public void setDropout_rate_slider(JFXSlider dropout_rate_slider) {
		this.dropout_rate_slider = dropout_rate_slider;
	}
	public TextField getN_clauses_text() {
		return n_clauses_text;
	}
	public void setN_clauses_text(TextField n_clauses_text) {
		this.n_clauses_text = n_clauses_text;
	}
	public TextField getSpecificty_text() {
		return specificty_text;
	}
	public void setSpecificty_text(TextField specificty_text) {
		this.specificty_text = specificty_text;
	}

	public TextField getThreshold_text() {
		return threshold_text;
	}
	public void setThreshold_text(TextField threshold_text) {
		this.threshold_text = threshold_text;
	}
	public TextField getDropout_rate_text() {
		return dropout_rate_text;
	}
	public void setDropout_rate_text(TextField dropout_rate_text) {
		this.dropout_rate_text = dropout_rate_text;
	}
	public StackPane getControl_stack() {
		return control_stack;
	}
	public void setControl_stack(StackPane control_stack) {
		this.control_stack = control_stack;
	}
	public Stage getControl_stage() {
		return control_stage;
	}
	public void setControl_stage(Stage control_stage) {
		this.control_stage = control_stage;
	}
	
	public int getN_clauses() {
		return n_clauses;
	}



	public void setN_clauses(int n_clauses) {
		this.n_clauses = n_clauses;
	}



	public float getSpecificity() {
		return specificity;
	}



	public void setSpecificity(float specificity) {
		this.specificity = specificity;
	}



	public int getThreshold() {
		return threshold;
	}



	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}



	public float getDropout_rate() {
		return dropout_rate;
	}



	public void setDropout_rate(float dropout_rate) {
		this.dropout_rate = dropout_rate;
	}
}
