package controls;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import records.AnyRecord;
import records.CSVTableView;
import tsetlin.MultiAutomataLearning;

public class CentralAutomataController extends Application {

	
	private StackPane central_pane;
	private Stage controller_stage;
	
	private CSVTableView data_table;
	private ObservableList<Map> backingList;
	
	private CheckBox encode_day_month;
	private CheckBox encode_hours;
	private CheckBox encode_weeks;
	private CheckBox encode_months;
	private TextField filter_field;
	
	private MultiAutomataLearning<AnyRecord> automata;
	private Font myFont;
	
	private Text drop_data;
	private Font myFontBig;
	
		
	
	public void buildCentralPane() {
		
		data_table = new CSVTableView();
		
		RadialGradient gradient = new RadialGradient(0, 0, 0.5, 0.25, 0.45, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(2, 58, 87)),
                new Stop(1,Color.rgb(105, 0, 105)));

		
		
		drop_data = new Text("DROP DATA HERE");
		drop_data.setFont(myFontBig);
		drop_data.setFill(Color.rgb(177, 235, 252));
		
		central_pane = new StackPane();
		central_pane.setPrefSize(1400, 800);
		central_pane.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
		
		central_pane.getChildren().add(drop_data);
		
		
		encode_hours = new CheckBox("HOUR");
		encode_day_month = new CheckBox("DAY");
		encode_weeks = new CheckBox("WEEK");
		encode_months = new CheckBox("MONTH");
		
		encode_hours.setFont(myFont);
		encode_day_month.setFont(myFont);
		encode_weeks.setFont(myFont);
		encode_months.setFont(myFont);
		
		filter_field = new TextField();
		Text filter_label = new Text("Filter:");
		filter_label.setFont(myFont);
		filter_label.setFill(Color.rgb(177, 235, 252));
		
		
		filter_field.setStyle("-fx-control-inner-background: black;");
		filter_field.setStyle("-fx-text-fill: white;");
		filter_field.setPrefWidth(80);
		filter_field.setMaxWidth(80);
		
		
		GridPane table_pane = new GridPane();
		table_pane.setHgap(20);
		table_pane.setVgap(20);
		table_pane.add(filter_label, 0, 0);
		table_pane.add(filter_field, 1, 0);
		table_pane.add(encode_day_month, 2, 0);
		table_pane.add(encode_hours, 3, 0);
		table_pane.add(encode_weeks, 4, 0);
		table_pane.add(encode_months, 5, 0);
		
		
		VBox vbox = new VBox(10);
		vbox.setPadding(new Insets(5,5,5,10));
		vbox.getChildren().addAll(table_pane, central_pane);
		vbox.getStylesheets().add("css/WhiteOnBlack.css");
		
		
		Scene control_scene = new Scene(vbox);
		controller_stage = new Stage();
		controller_stage.setScene(control_scene);
		
		controller_stage.show();
		
		
	}
	
	
	
	
	
	
	
	
	private void loadFont() {

		InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/static/Exo-Medium.ttf");
		myFont = Font.loadFont(fontStream, 14);	
		myFontBig = Font.loadFont(fontStream, 18);	
	}
	
	public void buildDropBox() {
		
		
		central_pane.setOnDragOver(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                if (event.getGestureSource() != central_pane
                        && event.getDragboard().hasFiles()) {
                	
                	
                	central_pane.setStyle("-fx-border-color: cyan;"
                			+ "-fx-border-width: 5;");
                	
                    /* allow for both copying and moving, whatever user chooses */
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            }
        });

		central_pane.setOnDragDropped(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    
                	
                	File file = db.getFiles().get(0);

                	System.out.println("My file: " + file.toString());
                	try {
                		addData(file);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                	
                	
                    success = true;
                }
                /* let the source know whether the string was successfully 
                 * transferred and used */
                event.setDropCompleted(success);

                event.consume();
            }
        });
		
		central_pane.setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(final DragEvent event) {
            	central_pane.setStyle("-fx-border-color: #C6C6C6;");
            }
        });
		
	}

	
	private void addData(File file) throws IOException {
		
		data_table.createTable(file);
		central_pane.getChildren().set(0, data_table);
	
		GridPane.setHgrow(data_table, Priority.ALWAYS);
		GridPane.setVgrow(data_table, Priority.ALWAYS);
		
		data_table.fillTable();
		data_table.setSelectionListener();
		
		backingList = data_table.getItems();
		

		filter_field.textProperty().addListener((observable, oldValue, newValue) -> {
			
			FilteredList<Map> filteredData2 = new FilteredList<Map>(backingList, p -> true); 

            filteredData2.setPredicate(anyMap -> {

                  // If filter text is empty, display all persons.

                  if (newValue == null || newValue.isEmpty()) {

                         return true;

                  }                        

                  return isMatched(anyMap, newValue.toLowerCase());

            });


            SortedList<Map> sortedData = new SortedList<>(filteredData2);
            sortedData.comparatorProperty().bind(data_table.comparatorProperty());
            data_table.setItems(sortedData);
			
			
		});
		
	}
	
	
	/**
	 * Searches over all fields in Map that is a category
	 * @param anyMap
	 * @param lowerCase
	 * @return
	 */
	private boolean isMatched(Map anyMap, String lowerCase) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void start(Stage arg0) throws Exception {
		loadFont();
		buildCentralPane();
		buildDropBox();
	}
	
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
