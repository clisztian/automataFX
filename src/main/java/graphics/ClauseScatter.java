package graphics;

import java.io.Serializable;
import java.util.ArrayList;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class ClauseScatter { //implements Runnable {

    // variables for mouse interaction
    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;
    private final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-45, Rotate.Y_AXIS);
	private Group grid;
	private double[][] hidden;
	private int myLayerNumber = 0;
    private boolean pianoMode = false;
    private Color[] colors;
    RotateTransition rt;
    private ArrayList<double[]> realtime;
    private ArrayList<Color> realtimeColors;
	private double[] mins;
	private double[] maxs;
	
	
	public ClauseScatter() {
		realtime = new ArrayList<double[]>();
		realtimeColors = new ArrayList<Color>();
	}


	private StackPane hiddenStatePane;
	

	
	public void setGraphSize(int graphSize) {
	}

	
	public void setObservations(double[][] hidden, Color[] colors) {
		
		realtime.clear();
		realtimeColors.clear();
		
		for(int i = 0; i < hidden.length; i++) {
			realtime.add(hidden[i]);
			realtimeColors.add(colors[i]);
		}
	}
	
	public void addObservation(double[] hidden, Color color) {
		realtime.add(hidden);
		realtimeColors.add(color);
	}
	
	public void sketchCanvas() {
		
		grid = new Group();
		hiddenStatePane.getChildren().add(grid);
	}
	
	
	public void createHiddenState(Scene scene) {
		

		grid = new Group();	    
		grid.getTransforms().addAll(rotateX, rotateY);
	    hiddenStatePane.getChildren().add(grid);
		
	  
        // scene
        //Scene scene = new Scene(hiddenStatePane, 1600, 900, true, SceneAntialiasing.BALANCED);
        scene.setCamera(new PerspectiveCamera());

        scene.setOnMousePressed(me -> {
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnMouseDragged(me -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            rotateX.setAngle(rotateX.getAngle() - (mousePosY - mouseOldY));
            rotateY.setAngle(rotateY.getAngle() + (mousePosX - mouseOldX));
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;

        });

	    makeZoomable(hiddenStatePane);

	    rt = new RotateTransition(Duration.millis(8000), grid);
	    rt.setAxis(Rotate.Y_AXIS);  
	    rt.setByAngle(360);
	    rt.setCycleCount(Animation.INDEFINITE);
	    rt.setAutoReverse(true);	 
	    rt.play();
		
	}
	
	public void rotateMe() {
		rotateY.setAngle(rotateY.getAngle() + 1);
		
	}
	
	
	
	public void updateGrid(double[][] hidden, double[] mins, double[] maxs, Color[] colors) {
		
		grid = new Group();
		grid.getTransforms().addAll(rotateX, rotateY);
		setObservations(hidden,colors);
		this.mins = mins;
		this.maxs = maxs;
		this.colors = colors;

		
		double yjump = maxs[1] - mins[1];
		double zjump = maxs[2] - mins[2];
		double xjump = maxs[0] - mins[0];
		
		for (int i = 0; i < hidden.length; i++) {
        	
	           
        	double[] vals = hidden[i];    
        	       	
            Sphere sphere = new Sphere(5f); 

            // color
            PhongMaterial mat = new PhongMaterial();
            mat.setDiffuseColor(colors[i]);
            mat.setSpecularColor(Color.WHITE);
            sphere.setMaterial(mat);
            
            
            PointLight pointlight = new PointLight(colors[i]); 
     
            // location
            
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
		          
        hiddenStatePane.getChildren().set(0,grid);
        
        
	}
	

	public void rotateAnimation() {
		
		rt = new RotateTransition(Duration.millis(8000), grid);
        rt.setAxis(Rotate.Y_AXIS);  
	    rt.setByAngle(360);
	    rt.setCycleCount(Animation.INDEFINITE);
	    rt.setAutoReverse(true);
	    rt.play();
	}

	public void updateGrid(double[] hidden, Color color) {
		
//		grid = new Group();
//		grid.getTransforms().addAll(rotateX, rotateY);

		addObservation(hidden, color);
		
		double yjump = maxs[1] - mins[1];
		double zjump = maxs[2] - mins[2];
		double xjump = maxs[0] - mins[0];
		
		//for (int i = 0; i < realtime.size(); i++) {
        	    
        	double[] vals = hidden;            
            Sphere sphere = new Sphere(5f); 

            // color
            PhongMaterial mat = new PhongMaterial();
            mat.setDiffuseColor(color);
            mat.setSpecularColor(Color.WHITE);
            sphere.setMaterial(mat);
            
            
            PointLight pointlight = new PointLight(color); 
     
            // location
            
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
        //}
		          
        //hiddenStatePane.getChildren().set(0,grid);
        
        rt = new RotateTransition(Duration.millis(8000), grid);
        rt.setAxis(Rotate.Y_AXIS);  
	    rt.setByAngle(360);
	    rt.setCycleCount(Animation.INDEFINITE);
	    rt.setAutoReverse(true);
	    rt.play();
	}
	
	
	
	public void makeZoomable(StackPane control) {
	
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
	
	

	
	public static double normalizeValue(double value, double min, double max, double newMin, double newMax) {
	
	    return (value - min) * (newMax - newMin) / (max - min) + newMin;
	
	}
	
	public static double clamp(double value, double min, double max) {
	
	    if (Double.compare(value, min) < 0)
	        return min;
	
	    if (Double.compare(value, max) > 0)
	        return max;
	
	    return value;
	}


	public void setSizeInfo(int3 sizeInfo) {
	}

	public int getLayerChoice() {
		return myLayerNumber;
	}


//	@Override
//	public void run() {
//		rotateMe();
//	}


	public void togglePianoMode(boolean pianoMode) {
		this.pianoMode = pianoMode;
	}
	
	public boolean isPianoMode() {
		return pianoMode;
	}


	
	class int3  implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public int x;
		public int y;
		public int z;
		
		public int3(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}




}
	
	



