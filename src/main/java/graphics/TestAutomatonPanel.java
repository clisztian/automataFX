package graphics;

import controls.AutomatonPanel;
import javafx.application.Application;
import javafx.stage.Stage;

public class TestAutomatonPanel extends Application {

	@Override
	public void start(Stage arg0) throws Exception {
		
		AutomatonPanel panel = new AutomatonPanel();
		panel.buildEmbeddingPanel();
		panel.buildSliders();
		
		
	}
	
    public static void main(String[] args) {
        launch(args);
    }
	
	
}
