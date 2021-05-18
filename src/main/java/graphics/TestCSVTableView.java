package graphics;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import records.CSVTableView;

public class TestCSVTableView extends Application {

	@Override
	public void start(Stage arg0) throws Exception {
		
		CSVTableView mytable = new CSVTableView("data/breast-cancer.csv");
		
		RadialGradient gradient = new RadialGradient(0, 0, 0.5, 0.25, 0.45, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(2, 58, 87)),
                new Stop(1,Color.rgb(105, 0, 105)));

		
		StackPane mypane = new StackPane();
		
		mypane.getChildren().add(mytable);
		
		mypane.setPrefSize(1400, 800);
		
		Scene scene = new Scene(mypane);		
		mypane.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
		arg0.setTitle("MyTable");
		arg0.setScene(scene);
		arg0.show();
		
		
		
		
		mytable.fillTable();
		mytable.setSelectionListener();
	}

    public static void main(String[] args) {
        launch(args);
    }
	
}
