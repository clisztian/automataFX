package examples;

import java.io.IOException;

import dataio.CSVInterface;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import records.AnyRecord;
import records.AnyRecordTableView;

public class TestExpenseRecords {

	public static void main(String[] args) throws IOException {
		
		CSVInterface csv = new CSVInterface("data/expense_data_records.csv");	
		AnyRecord anyrecord = csv.createRecord();
		
		String[] fields = anyrecord.getField_names();
		
		for(int i = 0; i < fields.length; i++) {
			System.out.println(fields[i]);
		}
		

		
		new JFXPanel();
	
		AnyRecordTableView table = new AnyRecordTableView(anyrecord);
		
		
		RadialGradient gradient = new RadialGradient(0, 0, 0.5, 0.25, 0.45, true, CycleMethod.NO_CYCLE,

                new Stop(0, Color.BLACK),

                new Stop(1,Color.BLACK));
		table.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
		table.setOpacity(.90);
		
        Image img = new Image(TestExpenseRecords.class.getResourceAsStream("/images/418137.png"));

      
        BackgroundImage myBI= new BackgroundImage(img, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        Region backgroundRegion = new Region();

        backgroundRegion.setBackground(new Background(myBI));

        backgroundRegion.setEffect(new BoxBlur());
        StackPane tablePane = new StackPane(backgroundRegion,table);

		Platform.runLater(() -> {
	        try {
	        	
	        	table.addRecords(csv.getAllRecords());
	        	
	        	tablePane.setPrefSize(1200, 800);
	            Scene scene = new Scene(tablePane);
	            scene.getStylesheets().add("css/anystyle.css");

	            Stage stage = new Stage();
	            stage.setTitle("Expense Records");
	            stage.setScene(scene);
	            stage.show();
	        	
	        }
	        catch (Exception ex) {
	            ex.printStackTrace();
	        }
		});
		
	}
	
}
