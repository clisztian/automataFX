package controls;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;

import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.glyphfont.FontAwesome;

import com.jfoenix.controls.JFXSlider;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Gauge.SkinType;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.Styles;

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
	private JFXSlider n_feature_bits_slider;
	private JFXSlider patch_size_slider;
	private JFXSlider dim_y_slider;
	
	private TextField n_clauses_text;
	private TextField specificty_text;
	private TextField threshold_text;
	private TextField dropout_rate_text;
	
	private TextField n_epochs_text;	
	private TextField n_bits_text;
	private TextField patch_size_text;
	private TextField dim_y_text;
	
	private RadialGradient gradient1;
	private Color start_color = Color.rgb(177, 235, 252);
	private Color end_color = Color.rgb(210, 83, 200);





	private EmbeddingPanel embedding_panel;
	private StackPane control_stack;
	private Stage control_stage;
	







	private Gauge in_sample_percent_gauge;


	private Button build_button;
	private Button learn_button;
	private Button real_time_updates;
	private boolean real_time = false;

	/**
	 * Parameters for automata model
	 */
	private int n_clauses = 50;
	private float specificity = .5f;
	private int threshold = 20;
	private float dropout_rate = 0f;

	/*
	 * Parameters for learning architecture
	 */
	private int n_epochs = 5;
	private int dim_y = 1;
	private int patch_size = 1;
	private int n_feature_bits = 10;

	private BorderPane insamp;



	private Font bgFont;
	
	


	//private Tile fireSmokeTile;




	public void buildEmbeddingPanel() {
		
		embedding_panel = new EmbeddingPanel();
	    embedding_panel.buildController();
	    embedding_panel.buildClauseScatterPane();
		
	}
	
	
	
	public void buildSliders() {
		
		
		
		
		

		
		InputStream logoStream = getClass().getClassLoader().getResourceAsStream("images/dragon.png");
		Image dragLogo = new Image(logoStream);
		ImageView imgView = new ImageView(dragLogo);
		imgView.setFitHeight(160);
		imgView.setFitWidth(160);
		imgView.setPreserveRatio(true);  
	
		
		Label firelabel = new Label();
		firelabel.setGraphic(imgView);
		firelabel.setPrefSize(160, 160);
		

		

		StackPane firepane = new StackPane();
		firepane.setBackground(new Background(new BackgroundFill(Color.rgb(177, 235, 252), new CornerRadii(100), Insets.EMPTY)));
		firepane.setStyle("-fx-effect: dropshadow( gaussian , rgb(177, 235, 252) , 25 , .4 , 0, 0 )");
		
		
		final Transition animation = new Transition() {

            {
                setCycleDuration(Duration.millis(10000));
                setCycleCount(Animation.INDEFINITE);
                setInterpolator(Interpolator.EASE_BOTH);
            }

            @Override
            protected void interpolate(double frac) {
    
                
                Color vColor = end_color.interpolate(start_color, Math.abs(1.0 - 2*frac));
                
                gradient1 = new RadialGradient(0,
        	            .1,
        	            70,
        	            100,
        	            10 + 100.0*frac,
        	            false,
        	            CycleMethod.REFLECT,
        	            new Stop(0, start_color),
        	            new Stop(1, vColor));
                
                firepane.setBackground(new Background(new BackgroundFill(gradient1, new CornerRadii(100), Insets.EMPTY)));
            }
        };

		
		
		
		StackPane logo = new StackPane();
		logo.getChildren().addAll(firepane, firelabel);
		logo.setPrefSize(130, 160);

		
		build_button = new Button("BUILD MACHINE");
		learn_button = new Button("LEARN MACHINE");
		real_time_updates = new Button("BATCH MODE");
				
		build_button.setStyle(Styles.IDLE_BUTTON_STYLE);
		build_button.setOnMouseEntered(e -> build_button.setStyle(Styles.HOVERED_BUTTON_STYLE));
		build_button.setOnMouseExited(e -> build_button.setStyle(Styles.IDLE_BUTTON_STYLE));
		build_button.setOnMousePressed(e -> build_button.setStyle(Styles.DOWN_BUTTON_STYLE));

		
		
		learn_button.setStyle(Styles.IDLE_BUTTON_STYLE);
		learn_button.setOnMouseEntered(e -> learn_button.setStyle(Styles.HOVERED_BUTTON_STYLE));
		learn_button.setOnMouseExited(e -> learn_button.setStyle(Styles.IDLE_BUTTON_STYLE));
		learn_button.setOnMousePressed(e -> learn_button.setStyle(Styles.DOWN_BUTTON_STYLE));
		
		real_time_updates.setStyle(Styles.IDLE_BUTTON_STYLE);
		real_time_updates.setOnMouseEntered(e -> real_time_updates.setStyle(Styles.HOVERED_BUTTON_STYLE));
		real_time_updates.setOnMouseExited(e -> real_time_updates.setStyle(Styles.IDLE_BUTTON_STYLE));

		real_time_updates.setOnMouseReleased(e -> {
			real_time_updates.setStyle(Styles.HOVERED_BUTTON_STYLE);
		});
		
		
		
		n_clauses_slider = new JFXSlider(10, 1000, 50);
		specificty_slider = new JFXSlider(.1, 25.0, .5);
		threshold_slider = new JFXSlider(10, 5000, 20);
		dropout_rate_slider = new JFXSlider(0, 1, 0);
		n_epochs_slider = new JFXSlider(1, 50, 5);
		
		n_feature_bits_slider = new JFXSlider(10, 50, 10);
		patch_size_slider = new JFXSlider(1, 50, 1);
		dim_y_slider = new JFXSlider(1, 50, 1);
		
		n_feature_bits_slider.setMajorTickUnit(1.0);
		patch_size_slider.setMajorTickUnit(1.0);
		dim_y_slider.setMajorTickUnit(1.0);
		
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
		n_feature_bits_slider.setShowTickMarks(false);
		patch_size_slider.setShowTickMarks(false);
		dim_y_slider.setShowTickMarks(false);
		
		n_clauses_slider.setBlockIncrement(10);
		specificty_slider.setBlockIncrement(.1);
		threshold_slider.setBlockIncrement(10);
		dropout_rate_slider.setBlockIncrement(.1);
		n_epochs_slider.setBlockIncrement(1);
		n_feature_bits_slider.setBlockIncrement(1);
		patch_size_slider.setBlockIncrement(1);
		dim_y_slider.setBlockIncrement(1);
		
		//177, 235, 252
		n_clauses_text = new TextField("50");
		specificty_text = new TextField(".5");
		threshold_text = new TextField("20");
		dropout_rate_text = new TextField("0.0");
		n_epochs_text = new TextField("5");
		n_bits_text = new TextField("10");
		patch_size_text = new TextField("1");
		dim_y_text = new TextField("1");
		
		
		n_clauses_text.setPrefWidth(60);
		specificty_text.setPrefWidth(60);
		threshold_text.setPrefWidth(60);
		dropout_rate_text.setPrefWidth(60);
		n_epochs_text.setPrefWidth(60);
		n_bits_text.setPrefWidth(60);
		patch_size_text.setPrefWidth(60);
		dim_y_text.setPrefWidth(60);
		
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
		

		n_epochs_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
			n_epochs_text.setText(newValue.intValue() + "");
			n_epochs = newValue.intValue();
		});
		n_feature_bits_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
			n_bits_text.setText(newValue.intValue() + "");
			n_feature_bits = newValue.intValue();
		});
		patch_size_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
			patch_size_text.setText(newValue.intValue() + "");
			patch_size = newValue.intValue();
		});
		dim_y_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
			dim_y_text.setText(newValue.intValue() + "");
			dim_y = newValue.intValue();
		});
		
		
		n_clauses_slider.setPrefWidth(250);
		specificty_slider.setPrefWidth(250);
		threshold_slider.setPrefWidth(250);
		dropout_rate_slider.setPrefWidth(250);
		
		n_epochs_slider.setPrefWidth(250);
		n_feature_bits_slider.setPrefWidth(250);
		patch_size_slider.setPrefWidth(250);
		dim_y_slider.setPrefWidth(250);
		
//		n_clauses_slider.setPrefHeight(50);
//		specificty_slider.setPrefHeight(50);
//		threshold_slider.setPrefHeight(50);
//		dropout_rate_slider.setPrefHeight(50);
//		
//		n_epochs_slider.setPrefHeight(50);
//		n_feature_bits_slider.setPrefHeight(50);
//		patch_size_slider.setPrefHeight(50);
//		dim_y_slider.setPrefHeight(50);
		
		bgFont = null;
		InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/static/Exo-Medium.ttf");
		if (fontStream != null) {
            bgFont = Font.loadFont(fontStream, 16);	 
		}
		else {
			bgFont = Font.font("Cambria", 14);
		}
		
		build_button.setFont(bgFont);
		learn_button.setFont(bgFont);
		real_time_updates.setFont(bgFont);
		build_button.setPrefSize(180, 45);
		learn_button.setPrefSize(180, 45);
		real_time_updates.setPrefSize(180, 45);
		
		final Text n_clauses_label = new Text("CLAUSES:");
		final Text thresh_label = new Text("THRESHOLD:");
		final Text spec_label = new Text("SPECIFICITY:");
		final Text dropout_label = new Text("DROPOUT:");
		final Text n_feature_bits_label = new Text("N_BITS:");
		final Text dim_y_label = new Text("TIME WINDOW:");
		final Text patch_label = new Text("TIME LAG:");
		final Text epochs_label = new Text("EPOCHS:");

//		final Label n_clauses_label = new Label("Clauses:");
//		final Label thresh_label = new Label("Threshold:");
//		final Label spec_label = new Label("Specificity:");
//		final Label dropout_label = new Label("Dropout:");
		
		n_clauses_label.setFont(bgFont);
		thresh_label.setFont(bgFont);
		spec_label.setFont(bgFont);
		dropout_label.setFont(bgFont);
		
		n_feature_bits_label.setFont(bgFont);
		dim_y_label.setFont(bgFont);
		patch_label.setFont(bgFont);
		epochs_label.setFont(bgFont);
		
		n_clauses_label.setFill(Color.rgb(177, 235, 252));
		thresh_label.setFill(Color.rgb(177, 235, 252));
		spec_label.setFill(Color.rgb(177, 235, 252));
		dropout_label.setFill(Color.rgb(177, 235, 252));
		
		n_feature_bits_label.setFill(Color.rgb(177, 235, 252));
		dim_y_label.setFill(Color.rgb(177, 235, 252));
		patch_label.setFill(Color.rgb(177, 235, 252));
		epochs_label.setFill(Color.rgb(177, 235, 252));
		
		n_feature_bits_label.setEffect(new Glow(.5));
		dim_y_label.setEffect(new Glow(.5));
		patch_label.setEffect(new Glow(.5));
		epochs_label.setEffect(new Glow(.5));
		n_clauses_label.setEffect(new Glow(.5));
		thresh_label.setEffect(new Glow(.5));
		spec_label.setEffect(new Glow(.5));
		dropout_label.setEffect(new Glow(.5));
		
		GridPane button_pane = new GridPane();
		button_pane.setHgap(30);
		button_pane.setVgap(30);
		button_pane.add(build_button, 0, 0);
		button_pane.add(learn_button, 0, 1);
		button_pane.add(real_time_updates, 0, 2);
		
		GridPane clause_pane = new GridPane();
		clause_pane.setHgap(30);
		clause_pane.setVgap(30);
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
		
		
		GridPane learn_pane = new GridPane();
		learn_pane.setHgap(30);
		learn_pane.setVgap(30);
		learn_pane.add(epochs_label, 0, 0);
		learn_pane.add(n_epochs_slider, 1, 0);
		learn_pane.add(n_epochs_text, 2, 0);
	
		learn_pane.add(n_feature_bits_label, 0, 1);
		learn_pane.add(n_feature_bits_slider, 1, 1);
		learn_pane.add(n_bits_text, 2, 1);
		
		learn_pane.add(dim_y_label, 0, 2);
		learn_pane.add(dim_y_slider, 1, 2);
		learn_pane.add(dim_y_text, 2, 2);
		
		learn_pane.add(patch_label, 0, 3);
		learn_pane.add(patch_size_slider, 1, 3);
		learn_pane.add(patch_size_text, 2, 3);
		
		Label title = new Label("Border Title");
        title.setStyle("-fx-text-fill: white;" + 
        			   "-fx-translate-y: -3;" + 
        			   "-fx-background-color: black;");
        
        BorderPane.setAlignment(title, Pos.TOP_LEFT);
		
		BorderPane.setAlignment(embedding_panel.getInput_grid(),Pos.CENTER);
		BorderPane embed = new BorderPane(embedding_panel.getInput_grid());
		
		
		
        // Set the Size of the VBox
		embed.setPrefSize(460, 200);     
        // Set the Style-properties of the BorderPane
		embed.setStyle("-fx-padding: 20;" +
                "-fx-border-style: solid inside;" +
                "-fx-border-width: 1;" +
                "-fx-border-insets: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: rgb(177, 235, 252);" +
                "-fx-border-effect: dropshadow( gaussian , rgb(177, 235, 252) , 25 , .4 , 0, 0 )");
		
		
		
		
		
		in_sample_percent_gauge = GaugeBuilder.create()
	        .skinType(SkinType.DIGITAL)
	        .foregroundBaseColor(Color.rgb(177, 235, 252))
	        .barColor(Color.rgb(177, 235, 252))
	        .title("IN-SAMPLE")
	        .unit("%")
	        .maxValue(100.0)
	        .animated(true)
	        .build();
		

		
		BorderPane.setAlignment(in_sample_percent_gauge,Pos.CENTER);
		BorderPane insamp = new BorderPane(in_sample_percent_gauge);
		insamp.setPrefSize(200, 200);   
		
		HBox hbox = new HBox(40, logo, clause_pane, learn_pane, button_pane, embed, insamp);
		hbox.setPadding(new Insets(40));
		
		control_stack = new StackPane();
		control_stack.getChildren().add(hbox);
		
		Scene control_scene = new Scene(control_stack);
		control_scene.getStylesheets().add(getClass().getClassLoader().getResource("css/WhiteOnBlack.css").toExternalForm());
			
		LinearGradient gradient =new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, 
    			new Stop(0, Color.rgb(24, 217, 185)),
                new Stop(1, Color.rgb(217, 24, 185)) );
    	
		Background back = new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
		
		
		control_stage = new Stage();
		control_stage.setScene(control_scene);
		
		animation.play();
		
		control_stage.show();
	}
	
	
	public void buildMachine() {
		
		System.out.println("Build machine");
		
	}
	
	
	public void learnMachine() {
		
		System.out.println("Learn machine");
		
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
	
	
	public EmbeddingPanel getEmbedding_panel() {
		return embedding_panel;
	}



	public void setEmbedding_panel(EmbeddingPanel embedding_panel) {
		this.embedding_panel = embedding_panel;
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
	
	public int getN_epochs() {
		return n_epochs;
	}



	public void setN_epochs(int n_epochs) {
		this.n_epochs = n_epochs;
	}



	public int getDim_y() {
		return dim_y;
	}



	public void setDim_y(int dim_y) {
		this.dim_y = dim_y;
	}



	public int getPatch_size() {
		return patch_size;
	}



	public void setPatch_size(int patch_size) {
		this.patch_size = patch_size;
	}



	public int getN_bits() {
		return n_feature_bits;
	}



	public void setN_bits(int n_bits) {
		this.n_feature_bits = n_bits;
	}
	
	
	public Button getBuild_button() {
		return build_button;
	}



	public void setBuild_button(Button build_button) {
		this.build_button = build_button;
	}



	public Button getLearn_button() {
		return learn_button;
	}



	public void setLearn_button(Button learn_button) {
		this.learn_button = learn_button;
	}



	public Button getReal_time_updates() {
		return real_time_updates;
	}



	public void setReal_time_updates(Button real_time_updates) {
		this.real_time_updates = real_time_updates;
	}



	public boolean isReal_time() {
		return real_time;
	}



	public void setReal_time(boolean real_time) {
		this.real_time = real_time;
	}

	public Gauge getIn_sample_percent_gauge() {
		return in_sample_percent_gauge;
	}
	
	public Font getBgFont() {
		return bgFont;
	}



	public void setBgFont(Font bgFont) {
		this.bgFont = bgFont;
	}
	
	
}
