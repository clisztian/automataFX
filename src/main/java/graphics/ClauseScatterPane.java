package graphics;

import java.util.ArrayList;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import tsetlin.MultivariateConvolutionalAutomatonMachine;

public class ClauseScatterPane {

	private Group grid;
	private StackPane clause_pane;
	private Scene clause_scene;
	private final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-45, Rotate.Y_AXIS);
    
    private RotateTransition rt;
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;
    
    private Color[] colors;
    private Color[] pre_colors;
    
    private double[][] clauses;
	private double[] mins;
	private double[] maxs;
	private Color color_polarity1, color_polarity2;
	
	
	
    public ClauseScatterPane() {
		
		grid = new Group();	    
		grid.getTransforms().addAll(rotateX, rotateY);
	    
		clause_pane = new StackPane();
		clause_pane.getChildren().add(grid);
		makeZoomable(clause_pane);
		
		clause_pane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
		clause_pane.setPrefSize(1200, 1400);
		clause_scene = new Scene(clause_pane);
		clause_scene.getStylesheets().add("css/WhiteOnBlack.css");
        clause_scene.setCamera(new PerspectiveCamera());

        clause_scene.setOnMousePressed(me -> {
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        clause_scene.setOnMouseDragged(me -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            rotateX.setAngle(rotateX.getAngle() - (mousePosY - mouseOldY));
            rotateY.setAngle(rotateY.getAngle() + (mousePosX - mouseOldX));
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;

        });

        color_polarity1 = Color.rgb(200, 10, 50);
        color_polarity2 = Color.rgb(100, 0, 200);
        
        
	    rt = new RotateTransition(Duration.millis(8000), grid);
	    rt.setAxis(Rotate.Y_AXIS);  
	    rt.setByAngle(360);
	    rt.setCycleCount(Animation.INDEFINITE);
	    rt.setAutoReverse(true);	 
	    rt.play();
		
	    pre_colors = new Color[] {Color.CORNFLOWERBLUE, Color.AQUA, Color.DARKMAGENTA, Color.VIOLET, Color.TOMATO, Color.DARKSALMON, Color.SPRINGGREEN, Color.STEELBLUE, Color.TAN, Color.SADDLEBROWN};
	    
	}
    
    
    public void computeScatterPlot(float[][] vals, int[] labels) {
    	
    	grid.getChildren().clear();
    	
    	mins = new double[3];		
		maxs = new double[3];
		
		for(int i = 0; i < 3; i++) {
			mins[i] = Double.MAX_VALUE;
			maxs[i] = -Double.MAX_VALUE;
		}
		
		
		for(int i = 0; i < vals.length; i++) {
			
			for(int j = 0; j < vals[0].length; j++) {
												
				mins[0] = Math.min(mins[0], vals[i][0]);
				mins[1] = Math.min(mins[1], vals[i][1]);
				mins[2] = Math.min(mins[2], vals[i][2]);
				
				maxs[0] = Math.max(maxs[0], vals[i][0]);
				maxs[1] = Math.max(maxs[1], vals[i][1]);
				maxs[2] = Math.max(maxs[2], vals[i][2]);	
			}	
		}
		
		double yjump = maxs[1] - mins[1];
		double zjump = maxs[2] - mins[2];
		double xjump = maxs[0] - mins[0];
		
		for (int i = 0; i < vals.length; i++) {
        	          
       	
            Sphere sphere = new Sphere(5f); 

            // color
            PhongMaterial mat = new PhongMaterial();
            mat.setDiffuseColor(pre_colors[labels[i]]);
            mat.setSpecularColor(Color.WHITE);
            sphere.setMaterial(mat);
            
            
            PointLight pointlight = new PointLight(pre_colors[labels[i]]); 
    
            double yloc = 400.0*(vals[i][1] - mins[1])/yjump - 200;
            double xloc = 400.0*(vals[i][0] - mins[0])/xjump - 200;
            double zloc = 400.0*(vals[i][2] - mins[2])/zjump - 200;

            sphere.setLayoutY(yloc);
            sphere.setTranslateX(xloc);
            sphere.setTranslateZ(zloc);
                   
            pointlight.setTranslateZ(zloc); 
            pointlight.setTranslateX(xloc); 
            pointlight.setLayoutY(yloc); 
            
            
            grid.getChildren().addAll(sphere,pointlight);
        }
    	
    	
    	
    }
    
    
	public void computeClauseScatterPlot(MultivariateConvolutionalAutomatonMachine machine) {
		
		grid.getChildren().clear();
		
		int nClauses = machine.getNumberClauses();
		int nClasses = machine.getNumberClasses();
		
		colors = new Color[nClauses];
		clauses = new double[nClauses][3];
		
		mins = new double[3];		
		maxs = new double[3];
		
		for(int i = 0; i < 3; i++) {
			mins[i] = Double.MAX_VALUE;
			maxs[i] = -Double.MAX_VALUE;
		}
		
		
		for(int i = 0; i < nClauses; i++) {
			
			for(int j = 0; j < nClasses; j++) {
				
				clauses[i][0] = (double)machine.getMachine(j).getClauseStrength()[i];
				clauses[i][1] = (double)machine.getMachine(j).getClause_patch_coverage()[i];
				clauses[i][2] = 50.0*j;
				
				colors[i] = i % 2 == 0 ? color_polarity1 : color_polarity2;
				
				mins[0] = Math.min(mins[0], clauses[i][0]);
				mins[1] = Math.min(mins[1], clauses[i][1]);
				mins[2] = Math.min(mins[2], clauses[i][2]);
				
				maxs[0] = Math.max(maxs[0], clauses[i][0]);
				maxs[1] = Math.max(maxs[1], clauses[i][1]);
				maxs[2] = Math.max(maxs[2], clauses[i][2]);	
			}	
		}
		
		double yjump = maxs[1] - mins[1];
		double zjump = maxs[2] - mins[2];
		double xjump = maxs[0] - mins[0];
		
		for (int i = 0; i < clauses.length; i++) {
        	          
        	double[] vals = clauses[i];    
        	       	
            Sphere sphere = new Sphere(5f); 

            // color
            PhongMaterial mat = new PhongMaterial();
            mat.setDiffuseColor(colors[i]);
            mat.setSpecularColor(Color.WHITE);
            sphere.setMaterial(mat);
            
            
            PointLight pointlight = new PointLight(colors[i]); 
    
            double yloc = 400.0*(vals[1] - mins[1])/yjump - 200;
            double xloc = 400.0*(vals[0] - mins[0])/xjump - 200;
            double zloc = 400.0*(vals[2] - mins[2])/zjump - 200;

            sphere.setLayoutY(yloc);
            sphere.setTranslateX(xloc);
            sphere.setTranslateZ(zloc);
                   
            pointlight.setTranslateZ(zloc); 
            pointlight.setTranslateX(xloc); 
            pointlight.setLayoutY(yloc); 
            
            
            grid.getChildren().addAll(sphere,pointlight);
        }
		
	}
	
	
	private void makeZoomable(StackPane control) {
		
	    final double MAX_SCALE = 20.0;
	    final double MIN_SCALE = 0.1;
	
	    control.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
	
	        @Override
	        public void handle(ScrollEvent event) {
	
	            double delta = 1.2;
	
	            double scale = control.getScaleX();
	
	            if (event.getDeltaY() < 0) {
	                scale /= delta;
	            } else {
	                scale *= delta;
	            }
	
	            scale = clamp(scale, MIN_SCALE, MAX_SCALE);
	
	            control.setScaleX(scale);
	            control.setScaleY(scale);
	
	            event.consume();	
	        }	
	    });
	}
	
	public static double clamp(double value, double min, double max) {
		
	    if (Double.compare(value, min) < 0)
	        return min;
	
	    if (Double.compare(value, max) > 0)
	        return max;
	
	    return value;
	}
	
	
	public Scene getClauseScene() {
		return clause_scene;
	}
	
	
}
