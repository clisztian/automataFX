package controls;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import encoders.RealEncoder;
import interpretability.Prediction;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import output.CategoryLabel;
import output.OutputLabel;
import output.RealLabel;
import records.AnyRecord;
import records.CSVTableView;
import tsetlin.AutomataMap;
import tsetlin.TsetlinMachine;
import utils.Styles;

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
	
	private ArrayList<AutomataMap<AnyRecord>> automata;
	private Font myFont;
	
	private Text drop_data;
	private Font myFontBig;
	
	private AutomatonPanel automaton_panel;
	private ImageView imgView;
	private RotateTransition rt;
	private boolean real_time;
	private int n_classes; 
	
		
	
	public void buildCentralPane() {
		
		automaton_panel = new AutomatonPanel();
		automaton_panel.buildEmbeddingPanel();
		automaton_panel.buildSliders();
		
		automaton_panel.getLearn_button().setOnMouseReleased(e -> {
			automaton_panel.getLearn_button().setStyle(Styles.HOVERED_BUTTON_STYLE);
			try {
				learnMachine();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		
		automaton_panel.getBuild_button().setOnMouseReleased(e -> {
			automaton_panel.getBuild_button().setStyle(Styles.HOVERED_BUTTON_STYLE);
			buildMachine();
		});
		
		automaton_panel.getReal_time_updates().setOnMousePressed(e -> {
			automaton_panel.getReal_time_updates().setStyle(Styles.DOWN_BUTTON_STYLE);
			real_time = !real_time;
			if(real_time) automaton_panel.getReal_time_updates().setText("REALTIME UPDATES");
			else automaton_panel.getReal_time_updates().setText("BATCH LEARN");			
		});
		
		
		data_table = new CSVTableView();
		data_table.setEmbeddingPanel(automaton_panel.getEmbedding_panel());
		addPredictModelListener();

		
		
		drop_data = new Text("DROP DATA HERE");
		drop_data.setFont(myFontBig);
		drop_data.setFill(Color.rgb(177, 235, 252));
		
		buildIntroPane();
		
		
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
		
		Group lightgroup = new Group();
		PointLight pointlight = new PointLight(Color.rgb(184, 20, 56)); 
		lightgroup.getChildren().addAll(pointlight, imgView);
		
		
		central_pane.getChildren().add(lightgroup);
		
		rt.play();
		controller_stage.show();
		
		
	}
	
	
	
	
	/**
	 * 
	 * Learn machine can occur after an automata has been instantiated. 
	 * 
	 * Assuming instantiated:
	 * 
	 * 1) Grab any rows from the table (preferably around 50-70% of the total amount of rows
	 * 
	 * 2) Selected rows are built into AnyRecords arraylist and sent to the add_fit functions of the automata
	 * 
	 * 3) Selected rows are then sent to the update routine individually
	 * 
	 * 4) After each row "learned", all training panels being show will be updated in "realtime" to see the evolution
	 * 
	 * 5) After number of epochs complete rows that were trained on have been painted a certain color
	 * 
	 * 6) Any row selection thereafter envokes predict (regardless if in insample or outsample
	 * 
	 * 7) If timeseries data involved, prediction/replay up to select row will be envoked 
	 * @throws Exception 
	 * 
	 * 
	 */
	
	private void learnMachine() throws Exception {
		
		if(data_table == null) {
			throw new Exception("Need data table first");
		}
		
		if(automata == null || automata.size() == 0) {
			throw new Exception("Please build model first");
		}
		
		if(data_table.getSelectionModel().getSelectedItems().size() == 0) {
			throw new Exception("Please select data first");
		}
		
		ArrayList<AnyRecord> records = data_table.selectedRecords();
		
		for(AutomataMap<AnyRecord> map : automata) {			
			map.getAutomata().add_fit(records);
		}
		
		
		int n_epochs = automaton_panel.getN_epochs();
		
		System.out.println("Here: Spin off progress if batch otherwise, update visualization cues");
		for(int i = 0; i < n_epochs; i++) {
			
			System.out.println("Epoch " + i);
			
			
			if(automaton_panel.getDropout_rate() > 0) {
				
				System.out.println("Applying drop clause");
				for(AutomataMap<AnyRecord> auto : automata) {
					auto.getAutomata().drop_clauses();
				}
			}
			
			for(AnyRecord record : records) {
				
				for(AutomataMap<AnyRecord> auto : automata) {
					
					int label = 0;
					if(auto.getOutput() instanceof RealLabel) {					
						label = (int)((RealLabel)auto.getOutput()).getLabel((float)record.getMap().get(((RealLabel)auto.getOutput()).getRecordColumn().getName()));
					}
					else if(auto.getOutput() instanceof CategoryLabel) {
						
						String name = ((CategoryLabel)auto.getOutput()).getRecordColumn().getName();							
						label = (int)((CategoryLabel)auto.getOutput()).getLabel(record.getMap().get(name).toString());
					}
					int out = auto.getAutomata().update(record, label);		
					
					System.out.println(label + " " + out);					
				}				
			}	
		}
				
		
	}

	
	public void addPredictModelListener() {
		
		
		data_table.getSelectionModel().getSelectedItems().addListener(
	            new ListChangeListener<Map>() {

				public void onChanged( 
	               ListChangeListener.Change<? extends Map> c) {
					
					if(data_table.getSelectionModel().getSelectedItems().size() > 0) {
						
						float perc =(1f*data_table.getSelectionModel().getSelectedItems().size())/(1f*data_table.getItems().size());
						automaton_panel.getIn_sample_percent_gauge().setValue(100f*perc);
					}
					
					
					if(automata != null && automata.size() > 0 && data_table.getSelectionModel().getSelectedItems().size() == 1) {
						AnyRecord record = data_table.mapToRecord(data_table.getSelectionModel().getSelectedItems().get(0)); 
						
						for(AutomataMap<AnyRecord> auto : automata) {
							try {
								Prediction pred = auto.getAutomata().predict(record);
								System.out.println(pred.getPred_class());
								
								float perc =(1f*data_table.getSelectionModel().getSelectedItems().size())/(1f*data_table.getItems().size());
								automaton_panel.getIn_sample_percent_gauge().setValue(100f*perc);
								
							} catch (IllegalArgumentException | IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
	    });
		
		
		
		
	}
	
	

	/**
	 * 
	 * The build machine interface handles many prerequisists before learning can happen
	 * 
	 * 1) Commits the current table structure map for an AnyRecord object that will be used for the learning
	 * 
	 * 2) Finds any output/labels and creates an interface 
	 * 
	 * 3) Commits the hyperparameters for the machine model building
	 * 
	 * 4) Creates a machine that maps to each output
	 * 
	 * 5) Creates visualization and summary tools for the model and the clause canvas'
	 * 
	 */
	private void buildMachine() {
		
		
		
		if(data_table != null && data_table.getItems().size() > 0) {
			
			data_table.commitRecord();		
			data_table.buildAutomataSystem();
			
			ArrayList<OutputLabel> outputs = data_table.getOutput_labels();
			
			if(outputs.size() == 0) {
				System.out.println("Throw a popup message here");
			}
			
			automata = new ArrayList<AutomataMap<AnyRecord>>();
			
			for(OutputLabel out : outputs) {
				
				
				if(out instanceof CategoryLabel) {
					n_classes = ((CategoryLabel)out).getLabel_encoder().size();		
				}
				else if (out instanceof RealLabel) {
					n_classes = automaton_panel.getThreshold();
					((RealLabel)out).setTarget_resolution(n_classes);
				}
				
				TsetlinMachine<AnyRecord> automaton = new TsetlinMachine<AnyRecord>(
						automaton_panel.getDim_y(), 
						automaton_panel.getPatch_size(), 
						automaton_panel.getN_bits(), 
						data_table.getAnyRecord(),
						automaton_panel.getN_clauses(), 
						automaton_panel.getThreshold(),  
						automaton_panel.getSpecificity(), 
						n_classes, 
						encode_hours.isSelected(), 
						encode_day_month.isSelected(), 
						encode_months.isSelected(), 
						encode_weeks.isSelected(), 
						automaton_panel.getDropout_rate(),
						"yyyy-MM-dd");
				
				
				automata.add(new AutomataMap<AnyRecord>(automaton, out));
				
			}

			/**
			 * Create machine summary view
			 */
			
		}

		
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
		
		rt.stop();
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
	 * Builds the rotating logo
	 */
	private void buildIntroPane() {
		
		central_pane = new StackPane();
		central_pane.setPrefSize(1400, 800);
		
		RadialGradient gradient = new RadialGradient(0, 0, 0.5, 0.50, 0.50, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.BLACK),
                new Stop(1,Color.rgb(47, 60, 66)));
		
		central_pane.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
		InputStream logoStream = getClass().getClassLoader().getResourceAsStream("images/dragon.png");
		Image dragLogo = new Image(logoStream);
		imgView = new ImageView(dragLogo);
		
 
		
		
	    rt = new RotateTransition(Duration.millis(8000), imgView);
	    rt.setAxis(Rotate.Y_AXIS);  
	    rt.setByAngle(360);
	    rt.setCycleCount(Animation.INDEFINITE);
	    
	    central_pane.getChildren().add(imgView);
	   
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
