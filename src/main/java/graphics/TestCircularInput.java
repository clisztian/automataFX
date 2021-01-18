package graphics;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.GaugeBuilder;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TestCircularInput extends Application {

	private double global_position = 0;
	
	@Override
	public void start(Stage stage) throws Exception {
	
		String valueIn = "2019-02-19T23:28:04.434410+0800";
		String formatIn = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ";
		String formatOut = "yyyy-MM-dd HH:mm:ss";
		String formatOut2 = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ";
		
		Date date= new SimpleDateFormat(formatIn).parse(valueIn);
		String out1= new SimpleDateFormat(formatOut).format(date);
		System.out.println("SimpleDateFormat out:" + out1 +", will be a few more minutes");
		

		LocalDateTime ldt = LocalDateTime.parse(valueIn, DateTimeFormatter.ofPattern(formatIn));
		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());        
		String out2 = DateTimeFormatter.ofPattern(formatOut2).format(zdt);
		System.out.println("LocalDateTime out:" + out2);
				
		
		Gauge ui = GaugeBuilder.create()
                .skinType(SkinType.BAR)
                .barColor(Color.rgb(237, 22, 72))
                .valueColor(Color.WHITE)
                .unitColor(Color.WHITE)
                .unit("KPH")
                .minValue(0)
                .maxValue(100)
                .animated(true)
                .build();
		
		ui.setBackgroundPaint(new Color(.02,.024,.03,.6));
	
		ui.setOnMouseDragged(new EventHandler<MouseEvent>() {
		      @Override public void handle(MouseEvent event) {
//		    	  System.out.println("(x: "       + event.getSceneX()      + ", y: "       + event.getSceneY());
		    	  double position =  Math.max(0,event.getSceneY() - global_position);
		    	  System.out.println(position);
		    	  ui.setValue(Math.min(position, ui.getMaxValue()));
		      }
		});
		
		ui.setOnMousePressed(new EventHandler<MouseEvent>() {
		      @Override public void handle(MouseEvent event) {
		    	  global_position = event.getSceneY();
		      }
		});
		
		
		StackPane pane  = new StackPane(ui);
		pane.setBackground(new Background(new BackgroundFill(new Color(.01,.015,.02,.9), CornerRadii.EMPTY, Insets.EMPTY)));
        Scene     scene = new Scene(pane);

        
        
        
        
        stage.setTitle("MyChart");
        stage.setScene(scene);
        stage.show();
		
		
	}

	
	
    public static void main(String[] args) {
        launch(args);
    }
}
