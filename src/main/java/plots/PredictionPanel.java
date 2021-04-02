package plots;

import interpretability.Prediction;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import output.OutputLabel;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.colors.Bright;
import eu.hansolo.tilesfx.colors.Dark;
import eu.hansolo.tilesfx.tools.Helper;

public class PredictionPanel {

	final int TILE_SIZE = 240;

	private String output_title;
	private OutputLabel<?> output_decoder;
	private int target;
	private GridPane pred_pane;
	
	private Tile target_class;
	private Tile probability_output;
	final private Stop[] myStops = new Stop[] {new Stop(0.00000, Color.TRANSPARENT),
            new Stop(0.00001, Color.web("#3552a0")),
            new Stop(0.09090, Color.web("#456acf")),
            new Stop(0.27272, Color.web("#45a1cf")),
            new Stop(0.36363, Color.web("#30c8c9")),
            new Stop(0.45454, Color.web("#30c9af")),
            new Stop(0.50909, Color.web("#56d483")),
            new Stop(0.72727, Color.web("#9adb49")),
            new Stop(0.81818, Color.web("#efd750")),
            new Stop(0.90909, Color.web("#ef9850")),
            new Stop(1.00000, Color.web("#ef6050"))};

	
	
	public PredictionPanel(int nClasses, String output_title, int target) {
		
		this.target = target;
		this.setOutput_title(output_title);
		
		initializePanels();
		
        pred_pane = new GridPane();
		
		pred_pane.setHgap(5);
		pred_pane.setVgap(5);
		pred_pane.setPadding(new Insets(5));
		pred_pane.setBackground(new Background(new BackgroundFill(new Color(.01,.015,.02,.2), CornerRadii.EMPTY, Insets.EMPTY)));
       
		probability_output.setBackgroundColor(new Color(.02,.02,.03,.99));

		pred_pane.add(target_class, 0, 0);
		pred_pane.add(probability_output, 1, 0);
	}
	
	
	

	
	public void initializePanels() {
		
		probability_output = TileBuilder.create()
		        .skinType(Tile.SkinType.BAR_GAUGE)
		        .prefSize(TILE_SIZE , TILE_SIZE+40)
		        .title(output_title)
		        .unit("%")
		        .startFromZero(true)
		        .threshold(50)
		        .thresholdVisible(true)
		        .gradientStops(new Stop(0, Bright.BLUE),
		                       new Stop(0.1, Bright.BLUE_GREEN),
		                       new Stop(0.2, Bright.GREEN),
		                       new Stop(0.3, Bright.GREEN_YELLOW),
		                       new Stop(0.4, Bright.YELLOW),
		                       new Stop(0.5, Bright.YELLOW_ORANGE),
		                       new Stop(0.6, Bright.ORANGE),
		                       new Stop(0.7, Bright.ORANGE_RED),
		                       new Stop(0.8, Bright.RED),
		                       new Stop(1.0, Dark.RED))
		        .strokeWithGradient(true)
		        .animated(true)
		        .value(0.0)
		        .build();

		
		target_class = TileBuilder.create()
                .skinType(SkinType.LED)
                .prefSize(TILE_SIZE, TILE_SIZE)
                .title("Prediction")
                .description("Mortality High")
                .text("")
                .build();
	}
	
	public void updatePanel(Prediction prediction) {
		
		probability_output.setValue(100*prediction.getProbability());
		target_class.setActive(prediction.getPred_class() == target);
		if(prediction.getPred_class() == target) target_class.setDescription("Mortality High");
		else target_class.setDescription("Mortality Low");
	}
	
	
	public OutputLabel<?> getOutput_decoder() {
		return output_decoder;
	}
	public void setOutput_decoder(OutputLabel<?> output_decoder) {
		this.output_decoder = output_decoder;
	}



	public String getOutput_title() {
		return output_title;
	}



	public void setOutput_title(String output_title) {
		this.output_title = output_title;
	}
	
	public GridPane getPred_pane() {
		return pred_pane;
	}
}
