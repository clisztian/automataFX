package graphics;

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class AutomatonOutputPanel {
	
	private int n_lights;
	
	private Circle[] my_circles;
	private Rectangle[] my_region;
	private Group[] output_group;
	
	private LinearGradient gradient =new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, 
			new Stop(0, Color.rgb(24, 24, 36)),
            new Stop(1, Color.rgb(36, 36, 46)) );
	
	
	private HBox[] borders = new HBox[3];
    private String border_styles[] = {"-fx-border-width:0 1 1 0; -fx-border-color: white",
                                      "-fx-border-width:1; -fx-border-color:grey",
                                      "-fx-border-width:1 0 0 1; -fx-border-color:white"};


	private StackPane lights_pane;

	private DropShadow shadow;
	private DropShadow out_shadow;
	private Glow glow;
	
	
	public AutomatonOutputPanel(int n) {
		

		glow = new Glow(1.0);
		shadow = new DropShadow();
		shadow.setColor(Color.BLACK);
		shadow.setOffsetX(5);
		shadow.setOffsetY(5);
		shadow.setBlurType(BlurType.GAUSSIAN);
		
		out_shadow = new DropShadow();
		out_shadow.setRadius(10);
		out_shadow.setColor(Color.rgb(177, 235, 252));
		out_shadow.setBlurType(BlurType.GAUSSIAN);
		
		this.n_lights = n;
		my_region = new Rectangle[n_lights];
		my_circles = new Circle[n_lights];
		output_group = new Group[n_lights];
		

		
		GridPane mypane = new GridPane();
		mypane.setHgap(2);
		mypane.setVgap(2);
		
		for(int i = 0; i < n; i++) {
			
			my_region[i] = new Rectangle();
			my_region[i].setX(0);
			my_region[i].setY(0);
			my_region[i].setWidth(30);
			my_region[i].setHeight(30);
			my_region[i].setArcWidth(10); 
			my_region[i].setArcHeight(10);
			my_region[i].setFill(gradient);
			my_region[i].setEffect(shadow);
			
			my_circles[i] = new Circle();
			my_circles[i].setRadius(10);
			my_circles[i].setCenterX(15);
			my_circles[i].setCenterY(15);
			my_circles[i].setStroke(Color.LIGHTSLATEGRAY);
			my_circles[i].setStrokeWidth(1);
			my_circles[i].setFill(Color.rgb(10, 14, 11));
			
			output_group[i] = new Group();
			output_group[i].getChildren().addAll(my_region[i], my_circles[i]);
			my_circles[i].toFront();
			
			mypane.add(output_group[i], i, 0);
		}
						
		
//		for(int i = 0; i < borders.length; i++) {
//            borders[i] = new HBox();
//            borders[i].setStyle(border_styles[i]);
//
//            //decrement of border-size for inner-border, prevents from the overlapping of border
//            borders[i].setMaxSize(width - (1.5 *i), height - (1.5 * i));
//            borders[i].setMinSize(width - (1.5 *i), height - (1.5 * i));
//
//            borders[i].setSpacing(0);
//        }

//        this.borders[1].getChildren().add(borders[2]);
//        this.borders[0].getChildren().add(borders[1]);
//        mypane.getChildren().get(0).setGraphic(borders[0]);
		
		LinearGradient gradient2 =new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, 
    			new Stop(0, Color.SLATEGRAY.darker().darker()),
                new Stop(1, Color.SLATEGRAY) );
    	
		Background back = new Background(new BackgroundFill(gradient2, CornerRadii.EMPTY, Insets.EMPTY));
		
		
		lights_pane = new StackPane();
		lights_pane.setBackground(back);
		lights_pane.getChildren().add(mypane);
		
	}
	
	public void changeLights(boolean[] isLit) {

		for(int i = 0; i < isLit.length; i++) {
			
			if(isLit[i]) {
				my_circles[i].setFill(Color.rgb(177, 235, 252));
				//my_circles[i].setEffect(glow);
			}
			else {
				my_circles[i].setFill(Color.rgb(20, 14, 31));
				//my_circles[i].setEffect(null);
			}
		}
	}
	
	
	public void changeLights(int[] pos, int[] neg, float[] fs) {

		for(int i = 0; i < pos.length; i++) {
			
			if(pos[i] == 1 && neg[i] == 1) {
				my_circles[i].setFill(Color.CYAN);
				//my_circles[i].setEffect(glow);
			}
			else if(pos[i] == 1) {
				my_circles[i].setFill(Color.rgb(177, 235, 252));
			}
			else if(neg[i] == 1) {
				my_circles[i].setFill(Color.rgb(210, 83, 200));
			}
			else {
				my_circles[i].setFill(Color.rgb(20, 14, 31));
			}
			
			Tooltip mytooltip = new Tooltip();
			
			if(i == 0) mytooltip.setText(" < " + fs[i]);
			else if(i == pos.length-1) mytooltip.setText(" > " + fs[fs.length-1]);
			else if(i < fs.length) mytooltip.setText(" <  " + fs[i]);
			Tooltip.install(my_circles[i], mytooltip);
		}
	}
	
	
	public StackPane getLights_pane() {
		return lights_pane;
	}

	public void setLights_pane(StackPane lights_pane) {
		this.lights_pane = lights_pane;
	}
	
}
