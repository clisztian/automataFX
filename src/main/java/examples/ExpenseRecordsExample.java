package examples;

import dataio.CSVInterface;
import javafx.application.Application;
import javafx.stage.Stage;
import records.AnyRecord;

public class ExpenseRecordsExample extends Application {

	@Override
	public void start(Stage arg0) throws Exception {
		

		CSVInterface csv = new CSVInterface("data/expense_data_records.csv");	
		AnyRecord anyrecord = csv.createRecord();
		
		
	}
	
	@Override 
	public void stop() {
	        System.exit(0);
	}

    public static void main(String[] args) {
        launch(args);
    }
	
}


