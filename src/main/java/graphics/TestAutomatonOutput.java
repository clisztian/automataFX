package graphics;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestAutomatonOutput extends Application {

	@Override
	public void start(Stage arg0) throws Exception {
		

		AutomatonOutputPanel out = new AutomatonOutputPanel(10);
		
		boolean[] lights = new boolean[10];
		lights[0] = true;
		lights[1] = true;
		lights[2] = true;
		
		out.changeLights(lights);
		
		Scene myscene = new Scene(out.getLights_pane());
		arg0.setScene(myscene);
		
		arg0.show();
		
	}

	
    public static void main(String[] args) {
        launch(args);
    }
	
}
