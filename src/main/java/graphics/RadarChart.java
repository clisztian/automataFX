package graphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import eu.hansolo.fx.charts.Category;
import eu.hansolo.fx.charts.ChartType;
import eu.hansolo.fx.charts.YChart;
import eu.hansolo.fx.charts.YPane;
import eu.hansolo.fx.charts.data.YChartItem;
import eu.hansolo.fx.charts.series.YSeries;
import interpretability.Prediction;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.util.Duration;

/**
 * generalized radar chart featuring at most 3 series
 * 
 * the three series can represent 3 lags of the features, 
 * where the lags are goverend by patch_y_dim of the convolution
 * 
 * @author lisztian
 *
 */
public class RadarChart {

	private final int MAX_LAGS = 3;
	private final int MAX_FEATURES = 28;
	private static final Random               RND        = new Random();
	
    private static final ChartType            CHART_TYPE = ChartType.SMOOTH_RADAR_POLYGON;
    private              YSeries<YChartItem>[] feature_series;

    private              YChart<YChartItem>  chart;
    private              Timeline            timeline;
	
	private Color[][] color_matrix;
	private ArrayList categories;
	private int n_features;
	private int n_lags;
    
    
	/**
	 * Initialize Radar Chart with a Prediction object
	 * @param predict
	 * @throws Exception
	 */
    @SuppressWarnings("unchecked")
	public RadarChart(Prediction predict) throws Exception {
    	
    	if(predict.getReal_features() == null) {
    		throw new Exception("A Prediction object needs to be defined first");		
    	}
    	
    	
    	initiateColorMatrix();
    	
    	n_features = predict.getReal_features()[0].length;
    	n_lags = predict.getReal_features().length;
    	
    	n_features = Math.min(n_features, MAX_FEATURES);
    	n_lags = Math.min(n_lags, MAX_LAGS);
    	
    	
    	float[] max_strengths = new float[n_lags];
    	List<YChartItem>[] items = new List[n_lags];
    	
    	YChartItem dataPoint;
    	for(int k = 0; k < n_lags; k++) {    		
    		
    		for (int i = 0 ; i < n_features ; i++) {
        		max_strengths[k] = Math.max(predict.getReal_features()[0][i].getStrength(), max_strengths[k]);
        	}
    		items[k] = new ArrayList<>(n_features);
    		
    	}
    	
    	for(int k = 0; k < n_lags; k++) {  
    		
    		for (int i = 0 ; i < n_features ; i++) {
    			
    			dataPoint = new YChartItem((predict.getReal_features()[k][i].getStrength()/max_strengths[k]) * 100, "F" + i);
    			items[k].add(dataPoint);   			
    		}
    		
    		feature_series[k] = new YSeries<YChartItem>(items[k], CHART_TYPE, 
    				new RadialGradient(0, 0, 0, 0, 1, true, CycleMethod.NO_CYCLE, 
    						new Stop(0.0, color_matrix[k][0]), 
    						new Stop(0.5, color_matrix[k][1]), 
    						new Stop(1.0, color_matrix[k][2])), Color.TRANSPARENT);
    	}
    	feature_series[0].setWithWrapping(true);
        
            

    	/*
    	 * due to size limiations, just use genereric labels, and reference full name of features somwhere else in a legend
    	 */
        categories = new ArrayList<>();
        for (int i = 0 ; i < n_features ; i++) {
            categories.add(new Category("F" + i));
        }

        chart = new YChart<YChartItem>(new YPane<YChartItem>(categories, feature_series));
        chart.setPrefSize(600, 600);

        RadialGradient gradient = new RadialGradient(0, 0, 0.5, 0.25, 0.45, true, CycleMethod.NO_CYCLE,

                new Stop(0, Color.BLACK),

                new Stop(1,Color.BLACK));
        chart.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
        
        timeline      = new Timeline();
        

        registerListener();
    	
    	
    }
    
    

    /**
     * Initializes radar chart with n lags (patch_y_dim) and the n_features 
     * The values in the initilization will be set at random
     * 
     * @param patch_y_dim
     * @param n_features
     */
    public RadarChart(int patch_y_dim, int n_features)  {
    	 	
    	initiateColorMatrix();

    	this.n_features = Math.min(n_features, MAX_FEATURES);
    	n_lags = Math.min(patch_y_dim, MAX_LAGS);
    	
    	
    	float[] max_strengths = new float[n_lags];
    	List<YChartItem>[] items = new List[n_lags];
    	
    	YChartItem dataPoint;
    	for(int k = 0; k < n_lags; k++) {    		
    		
    		items[k] = new ArrayList<>(n_features);		
    	}
    	
    	for(int k = 0; k < n_lags; k++) {  
    		
    		for (int i = 0 ; i < n_features ; i++) {
    			
    			dataPoint = new YChartItem(RND.nextDouble() * 100, "F" + i);
    			items[k].add(dataPoint);   			
    		}
    		
    		feature_series[k] = new YSeries<YChartItem>(items[k], CHART_TYPE, 
    				new RadialGradient(0, 0, 0, 0, 1, true, CycleMethod.NO_CYCLE, 
    						new Stop(0.0, color_matrix[k][0]), 
    						new Stop(0.5, color_matrix[k][1]), 
    						new Stop(1.0, color_matrix[k][2])), Color.TRANSPARENT);
    	}
    	feature_series[0].setWithWrapping(true);

    	/*
    	 * due to size limiations, just use genereric labels, and reference full name of features somwhere else in a legend
    	 */
        categories = new ArrayList<>();
        for (int i = 0 ; i < n_features ; i++) {
            categories.add(new Category("F" + i));
        }

        chart = new YChart<YChartItem>(new YPane<YChartItem>(categories, feature_series));
        chart.setPrefSize(600, 600);

        RadialGradient gradient = new RadialGradient(0, 0, 0.5, 0.25, 0.45, true, CycleMethod.NO_CYCLE,

                new Stop(0, Color.BLACK),

                new Stop(1,Color.BLACK));
        chart.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
        
        timeline      = new Timeline();
        

        registerListener();
    	
    	
    }
    
    /**
     * Updates the plot with a new prediction
     * @param predict
     */
    public void updatePlot(Prediction predict) {
    	
    	if(predict.getReal_features() == null)
    		return;
    	
    	if(predict.getReal_features().length != n_lags || predict.getReal_features()[0].length != n_features) 
    		return;

    	float[] max_strengths = new float[n_lags];
    	float[][] vals = new float[n_lags][n_features];
    	
    	for(int k = 0; k < n_lags; k++) {    		
    		
    		for (int i = 0 ; i < n_features ; i++) {
        		max_strengths[k] = Math.max(predict.getReal_features()[0][i].getStrength(), max_strengths[k]);
        	}
    		
    		for (int i = 0 ; i < n_features ; i++) {
    			vals[k][i] = (predict.getReal_features()[k][i].getStrength()/max_strengths[k]);
    		}
    	}
    	
    	
    	List<KeyFrame> keyFrames = new ArrayList<>();
    	for(int k = 0; k < n_lags; k++) {  
    		animateSeries(feature_series[k], keyFrames, vals[k]);
    	}
    	timeline.getKeyFrames().setAll(keyFrames);
    	
    }
    


    /*
     * Paint and animates new values from the old ones. 
     * Requires a keyvalue keyframe pairing
     * 
     */
    private void animateSeries(final YSeries<YChartItem> SERIES, final List<KeyFrame> KEY_FRAMES, float[] v) {
    	
    	int count = 0;
    	
    	for(YChartItem item : SERIES.getItems()) {
    		
    		KeyValue kv0 = new KeyValue(item.yProperty(), item.getY());
            KeyValue kv1 = new KeyValue(item.yProperty(), v[count] * 100);
            KeyFrame kf0 = new KeyFrame(Duration.ZERO, kv0);
            KeyFrame kf1 = new KeyFrame(Duration.millis(500), kv1);
            KEY_FRAMES.add(kf0);
            KEY_FRAMES.add(kf1);
    		count++;
    	}
    }
    
    
    
    private void initiateColorMatrix() {
    	
    	color_matrix = new Color[3][3];
    	
    	color_matrix[0][0] = Color.rgb(0, 255, 255, 0.25);
    	color_matrix[0][1] = Color.rgb(255, 255, 0, 0.5);
    	color_matrix[0][2] = Color.rgb(255, 0, 255, 0.75);
    	
    	color_matrix[2][0] = Color.rgb(255, 255, 0, 0.25);
    	color_matrix[2][1] = Color.rgb(255, 0, 255, 0.5);
    	color_matrix[2][2] = Color.rgb(0, 255, 255, 0.75);
    	
    	color_matrix[3][0] = Color.rgb(0, 255, 255, 0.25);
    	color_matrix[3][1] = Color.rgb(255, 0, 255, 0.5);
    	color_matrix[3][2] = Color.rgb(255, 255, 0, 0.75);
    	
    }
    
    
    private void registerListener() {
        timeline.currentTimeProperty().addListener(o -> chart.refresh());
    }
    
}
