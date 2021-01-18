package plots;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.format.DateTimeFormat;

import encoders.CategoricalEncoder;
import encoders.RealEncoder;
import encoders.Temporal;
import encoders.TimeEncoder;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Popup;
import javafx.util.StringConverter;
import timeseries.TimeSeries;

public class KPIPlot<V> {
	
	private final DecimalFormat df2 = new DecimalFormat("###,###,##0.00");
	private final DecimalFormat df3 = new DecimalFormat("###,###,##0.000");
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final DateTimeFormatter   DTF = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");
	public static final Background TRANSPARENT_BACKGROUND = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
    private Popup                            popup;
	
    private double series_min, series_max;
    private double adversaire_min, adversaire_max;

    private String master_bo_name;

    
    private RadialGradient gradient;
    private Background back;
    private DropShadow dropShadow;

	private double bo_start_second;
	private double bo_end_second;
	private Color startColor;
	private Color endColor;
	private ArrayList<Field> plot_fields;
	private int numerical_field_count;
	private Field temporal_field;
    
    public KPIPlot(V val) throws IllegalArgumentException, IllegalAccessException {
    	
    	transform_record(val);
    	
    	startColor = Color.DARKRED.darker().darker();
    	endColor = Color.DARKSLATEGRAY.darker();
    	 
    	gradient = new RadialGradient(0, 0, 0.5, 0.25, 0.8, true, CycleMethod.NO_CYCLE,
    			new Stop(0, Color.DARKSLATEGRAY.darker()),
                new Stop(1,Color.BLACK));
    	
    	back = new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
    	
    	dropShadow = new DropShadow();
        dropShadow.setOffsetX(5);
        dropShadow.setOffsetY(5);
        dropShadow.setColor(Color.BLACK);
        dropShadow.setBlurType(BlurType.GAUSSIAN);
        
    }
    
    

	
    public LineChart<LocalDateTime, Number> plotKPI(String name, ArrayList<V> anySeries) throws IllegalArgumentException, IllegalAccessException {
    	
    	LineChart<LocalDateTime, Number> lineChart;
    	
    	final StringConverter<LocalDateTime> STRING_CONVERTER = new StringConverter<LocalDateTime>() {
            @Override public String toString(LocalDateTime localDateTime) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yy\nHH:mm:ss");
                return dtf.format(localDateTime);
            }
            @Override public LocalDateTime fromString(String s) {
                return LocalDateTime.parse(s);
            }
        };        

        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(name);

        DateAxis310 xAxis = new DateAxis310();
        xAxis.setTickLabelFormatter(STRING_CONVERTER);
        
        XYChart.Series<LocalDateTime, Number>[] all_series = new XYChart.Series[numerical_field_count];
        List<XYChart.Data<LocalDateTime, Number>>[] data = new ArrayList[numerical_field_count];
        
        for(int i = 0; i < numerical_field_count; i++) {
        	all_series[i] = new XYChart.Series<LocalDateTime, Number>();
        	all_series[i].setName(plot_fields.get(i).getName());
        	data[i] = new ArrayList<>();
        }

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("KPI Chart for " + name);
        
    
        lineChart.setAnimated(false);
        xAxis.setTickLabelFormatter(STRING_CONVERTER);
          
        
        if(anySeries != null && anySeries.size() > 0 && all_series.length > 0) {
        	
        	lineChart.getData().addAll(all_series);
            
        	series_min = Double.MAX_VALUE;
        	series_max = -Double.MAX_VALUE;
        	
        	for(int i = 0; i < anySeries.size(); i++) {
        		
        		Temporal time = (Temporal)temporal_field.get(anySeries.get(i));	
        		
        		for(int k = 0; k < plot_fields.size(); k++) {
        		
        			double value = (double)plot_fields.get(k).get(anySeries.get(i));		
        			data[k].add(new XYChart.Data<>(LocalDateTime.parse(time.getDate_time_string(), formatter), value));     	
        			
        			if(value < series_min) series_min = value;
                	else if(value > series_max) series_max = value;
        			
        		}	
        	}
        	
        	for(int k = 0; k < plot_fields.size(); k++) {        		
        		all_series[k].getData().setAll(data[k]); 		
        	}
            	
        }
        
        
        
        lineChart.setBackground(back);
        
        return lineChart;
    }
    
    

    public LineChart<LocalDateTime, Number> plotKPI(String name, List<V> anySeries, List<Float> predictions) throws IllegalArgumentException, IllegalAccessException {
    	
    	LineChart<LocalDateTime, Number> lineChart;
    	
    	final StringConverter<LocalDateTime> STRING_CONVERTER = new StringConverter<LocalDateTime>() {
            @Override public String toString(LocalDateTime localDateTime) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yy\nHH:mm:ss");
                return dtf.format(localDateTime);
            }
            @Override public LocalDateTime fromString(String s) {
                return LocalDateTime.parse(s);
            }
        };        

        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(name);

        DateAxis310 xAxis = new DateAxis310();
        xAxis.setTickLabelFormatter(STRING_CONVERTER);
        
        XYChart.Series<LocalDateTime, Number>[] all_series = new XYChart.Series[numerical_field_count];
        XYChart.Series<LocalDateTime, Number> pred_ict = new XYChart.Series();
        
        List<XYChart.Data<LocalDateTime, Number>>[] data = new ArrayList[numerical_field_count];
        List<XYChart.Data<LocalDateTime, Number>> output = new ArrayList();
        
        for(int i = 0; i < numerical_field_count; i++) {
        	all_series[i] = new XYChart.Series<LocalDateTime, Number>();
        	all_series[i].setName(plot_fields.get(i).getName());
        	data[i] = new ArrayList<>();
        }

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("KPI Chart for " + name);
        
    
        lineChart.setAnimated(false);
        xAxis.setTickLabelFormatter(STRING_CONVERTER);
          
        
        if(anySeries != null && anySeries.size() > 0 && all_series.length > 0) {
        	
        	lineChart.getData().addAll(all_series);
        	lineChart.getData().add(pred_ict);
            
        	series_min = Double.MAX_VALUE;
        	series_max = -Double.MAX_VALUE;
        	
        	for(int i = 0; i < anySeries.size(); i++) {
        		
        		Temporal time = (Temporal)temporal_field.get(anySeries.get(i));	
        		
        		for(int k = 0; k < plot_fields.size(); k++) {
        		
        			float value = (float)plot_fields.get(k).get(anySeries.get(i));		
        			data[k].add(new XYChart.Data<>(LocalDateTime.parse(time.getDate_time_string(), formatter), value));     	
        			
        			if(value < series_min) series_min = value;
                	else if(value > series_max) series_max = value;
        			
        		}
        		output.add(new XYChart.Data<>(LocalDateTime.parse(time.getDate_time_string(), formatter), predictions.get(i)));
        	}
        	
        	
        	
        	for(int k = 0; k < plot_fields.size(); k++) {        		
        		all_series[k].getData().setAll(data[k]); 		
        	}
        	pred_ict.getData().setAll(output);
            	
        }
        lineChart.setBackground(back);
        
        return lineChart;
    }
    
    
    
    
    private void applyDataPointMouseEvents(final XYChart.Series SERIES) {                
        Platform.runLater(new Runnable() {
            @Override public void run() {                                                
                Label label = new Label("");
                label.getStyleClass().add("value-label");
                StackPane popupPane = new StackPane(label);
                popupPane.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
                popup = new Popup();                
                popup.getContent().add(popupPane);                
                
                for (Object data : SERIES.getData()) {                                    
                    XYChart.Data<LocalDateTime, Number> dataPoint = (XYChart.Data<LocalDateTime, Number>) data;
                    final Node node = dataPoint.getNode();
                                                                            
                    node.setOnMouseEntered(mouseEvent -> {
                    	
                    	String yval = df2.format(dataPoint.getYValue());
                    	if(!SERIES.getName().equals(master_bo_name)) {
                    		
                    		//translate to 0 and 1
                    		double t = ((double)dataPoint.getYValue() - series_min)/(series_max - series_min);	
                    		double adval = (1.0 - t)*adversaire_min + t*adversaire_max;
                    		yval = df2.format(adval);
                    	}
                    	
                        label.setText(DTF.format(dataPoint.getXValue()) + "\n" + yval);
                        label.setTextFill(Color.CYAN);
                        popup.setX(mouseEvent.getScreenX() + 20);
                        popup.setY(mouseEvent.getScreenY() - 20);
                        popup.show(SERIES.getNode().getScene().getWindow());
                    });
            
                    node.setOnMouseExited(mouseEvent -> {
                        popup.hide();
                    });
                }
            }
        });
    }
    
    

    private void transform_record(V val) throws IllegalArgumentException, IllegalAccessException {
		
		List<Field> fields = getPrivateFields(val.getClass());
		plot_fields = new ArrayList<Field>();
		
		numerical_field_count = 0;
		
		for(Field field : fields) {

			String type = field.getType().toString();
			if(type.contains("double")) {
							
				field.setAccessible(true);	
				plot_fields.add(field);
				numerical_field_count++;
			}
			else if(type.contains("float")) {
				
				field.setAccessible(true);			
				plot_fields.add(field);
				numerical_field_count++;
			}
			else if(type.contains("int")) {
				
				field.setAccessible(true);		
				plot_fields.add(field);
				numerical_field_count++;
			}
			else if(type.contains("Temporal")) {
				
				field.setAccessible(true);			
				temporal_field = field;
			}	
		}					
	}
    
	private  List<Field> getPrivateFields(Class<?> theClass){
        LinkedList<Field> privateFields = new LinkedList<Field>();

        Field[] fields = theClass.getDeclaredFields();

        for(Field field:fields){
        	privateFields.add(field);
        }
        return privateFields;
    }
    
    
}
