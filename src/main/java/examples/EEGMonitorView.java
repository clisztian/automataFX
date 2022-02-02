package examples;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.csvreader.CsvReader;

import eu.hansolo.fx.monitor.Monitor;
import eu.hansolo.fx.monitor.MonitorBuilder;
import eu.hansolo.fx.monitor.tools.Theme;
import eu.hansolo.fx.monitor.tools.Timespan;
import interpretability.Prediction;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import plots.PredictionPanel;
import records.AnyRecord;
import tsetlin.AutomataLearning;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.Gauge.SkinType;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.LcdDesign;
import graphics.ClauseScatterPane;

public class EEGMonitorView extends Application {
	
	private static final Random                   RND = new Random();
	private Monitor monitor;
	private ScheduledExecutorService executorService;
    private Runnable eegTask;
    
    private Stage clause_stage;
    private ClauseScatterPane clause_pane;
    private AutomataLearning<AnyRecord> automata;
    private GridPane input_grid;
    private Gauge time_gauge;
    private Gauge value_gauge;
    
    private PredictionPanel pred_panel;
	private ArrayList<double[]> eeg_samples;	
	private CsvReader marketDataFeed;// = new CsvReader(new FileReader("/home/lisztian/Downloads/eeg_time.csv"), ',');	
	
	private ArrayList<AnyRecord> in_sample;
	
	AnyRecord any = new AnyRecord(new String[] {"value"}, new Double[] {0.0});
	
	int dim_x = 20;
	int dim_y = 7200;
	int patch_dim_y = 20;
	int n_samples = 200;
	int threshold = 400;
	int nClauses = 128;
	int eeg_t = 0;

	
	float max_specificity = 2f;
	int nClasses = 2;
	private double[] eeg_sig;
	private int signal_count = 0;
	
	
	public void buildGauges() {
		
		value_gauge = GaugeBuilder.create()
                .skinType(SkinType.LCD)
                .animated(true)
                .title("EEG Signal")
                .subTitle("")
                .unit("")
                .lcdDesign(LcdDesign.YOCTOPUCE)
                .thresholdVisible(true)
                .threshold(65)
                .build();
		
		value_gauge.setBackgroundPaint(Color.BLACK);
		value_gauge.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));


		time_gauge = GaugeBuilder.create()
                .skinType(SkinType.BAR)
                .barColor(Color.rgb(0, 190, 200))
                .valueColor(Color.WHITE)
                .unitColor(Color.WHITE)
                .unit("ms")
                .minValue(0)
                .maxValue(72.0)
                .animated(true)
                .build();
		
		time_gauge.setBackgroundPaint(Color.BLACK);
		time_gauge.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

	}
	
	public void buildClauseScatterPane() {
		
		clause_pane = new ClauseScatterPane();
		clause_stage = new Stage();
		clause_stage.setTitle("Clause plot");
		clause_stage.setScene(clause_pane.getClauseScene());
		clause_stage.show();
		
	}

	public void buildPrecitionPanel() {
		pred_panel = new PredictionPanel(2, "Class probability", 1);
	}
	
	public void buildMonitorAndService() {
		
		monitor = MonitorBuilder.create()
                //.lineColor(Color.BLUE)
                //.backgroundColor(Color.rgb(51, 51, 51))
                //.rasterColor(Color.RED)
                //.textColor(Color.YELLOW)
                .lineWidth(2)
                .dotSize(4)
                .rasterVisible(true)
                .textVisible(true)
                .glowVisible(true)
                .crystalOverlayVisible(true)
                .lineFading(true)
                .timespan(Timespan.THREE_SECONDS)
                .colorTheme(Theme.GREEN)
                .speedFactor(1)
               
                //.noOfSegments(150)
                .scaleFactorY(1)
                //.data(EcgData.ECG_DATA)
                .build();


				
		monitor.setOnMouseClicked(e -> {
			if (monitor.isRunning()) {
			monitor.stop();
			} else {
			monitor.start();
			}
		});
		time_gauge.setValue(eeg_t/100.0);
		
		
		eegTask         = () -> {
            Platform.runLater(() -> {
            	if(eeg_t < eeg_sig.length) {
            		monitor.addDataPoint(eeg_sig[eeg_t]);
            		value_gauge.setValue(eeg_samples.get(signal_count)[eeg_t]);
            		time_gauge.setValue(eeg_t/100.0);
            	}            	
            });
            
//            Platform.runLater(() -> {
//            	if(eeg_t < eeg_sig.length) {
//            		
//            	}            	
//            });
            
            eeg_t++;        
            if(eeg_t == eeg_sig.length) {
            	
            	signal_count = RND.nextInt(eeg_samples.size());
            	plotEEGSignal(signal_count); 
            	eeg_t = 0;
            	
            	try {
            		Prediction pred = predict();
            		if(pred.getPred_class() == 0) {
            			monitor.setColorTheme(Theme.BLUE_BLACK);
            		}
            		else {
            			monitor.setColorTheme(Theme.GREEN_RED);
            		}
            		Platform.runLater(() -> {
            			updateInterpretabilityCharts(pred);    
            			
            			//clause_pane.computeClauseScatterPlot(automata.getAutomaton());
                    });
            		
            		
				} catch (IllegalArgumentException | IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        };
        executorService = Executors.newScheduledThreadPool(1);
	}
	
	
	/**
	 * Pulls a sample from the EEG deck, normalizes between -100 and 100 for the monitor
	 * @param sig
	 */
	public void plotEEGSignal(int sig) {
		
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		
		eeg_sig = new double[eeg_samples.get(sig).length-1];
		
		for(int i = 0; i < eeg_samples.get(sig).length-1; i++) {	
			eeg_sig[i] = eeg_samples.get(sig)[i];
	        max = Math.max(eeg_sig[i],max);
	        min = Math.min(eeg_sig[i],min);
		}
		
		for(int i = 0; i < eeg_sig.length; i++) {
			eeg_sig[i] = (2*((eeg_sig[i] - min)/(max-min)) - 1.0)*95.0;
		}
	}
	
	
	public void update() throws IllegalArgumentException, IllegalAccessException {
		
		int label = getLabel(eeg_samples.get(signal_count));
		int pred = automata.update(getRecord(eeg_samples.get(signal_count)), label);
		
		System.out.println(signal_count + " " + pred + " " + label);	
	}
	
	public void learn_insample(int n_sample) throws IllegalArgumentException, IllegalAccessException {
		
		for(int i = 0; i < n_sample; i++) {
			signal_count = RND.nextInt(eeg_samples.size());
			update();
		}	
	}
	
	
	public Prediction predict() throws IllegalArgumentException, IllegalAccessException {
		
		int label = getLabel(eeg_samples.get(signal_count));
		Prediction pred = automata.predict(getRecord(eeg_samples.get(signal_count)));
		
		System.out.println(signal_count + " " + pred.getPred_class() + " " + label);	
		return pred;
	}
	
	public void updateInterpretabilityCharts(Prediction pred) {		
		pred_panel.updatePanel(pred);
	}
	
	
	public void launchService() {
		executorService.scheduleAtFixedRate(eegTask, 0, 2, TimeUnit.MILLISECONDS);
	}
	
	
	
	public void dataPull() throws IOException {
		
		eeg_samples = new ArrayList<double[]>();
		marketDataFeed = new CsvReader(new FileReader("/home/lisztian/Downloads/eeg_time.csv"), ',');	
		marketDataFeed.readHeaders();
		
		while(marketDataFeed.readRecord()) {
							
			double[] doubleValues = Arrays.stream(marketDataFeed.getRawRecord().split("[,]+"))
                    .mapToDouble(Double::parseDouble)
                    .toArray();
			
			eeg_samples.add(doubleValues);
		}		
	}
	
	public void buildInSample() {
		
		in_sample = new ArrayList<AnyRecord>();
		
		for(int samp = 0; samp < eeg_samples.size(); samp++) {
			for(int i = 0; i < eeg_samples.get(samp).length-1; i++) {	
				in_sample.add(new AnyRecord(new Double[] {eeg_samples.get(samp)[i]}));
			}	
		}
	}
	

	public void buildAutomataModel() throws IllegalArgumentException, IllegalAccessException {
		
		automata = new AutomataLearning<AnyRecord>(dim_y, patch_dim_y, dim_x, any, nClauses, threshold, max_specificity, nClasses, 0);
		automata.add_fit(in_sample);
		
	}


	@Override
	public void start(Stage stage) throws IllegalArgumentException, IllegalAccessException {
		
		
		try {
			dataPull();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		buildGauges();
		buildPrecitionPanel();
		buildInSample();
		buildAutomataModel();
		buildMonitorAndService();
		plotEEGSignal(0);
		buildClauseScatterPane();
		learn_insample(50);
		

        Label                 themeLabel       = new Label("Theme");
        ObservableList<Theme> themes           = FXCollections.observableArrayList(List.of(Theme.values()));
        ComboBox<Theme>       themesComboBox   = new ComboBox<>(themes);

        Label                    timespanLabel = new Label("Timespan");
        ObservableList<Timespan> timespans     = FXCollections.observableArrayList(List.of(Timespan.values()));
        ComboBox<Timespan> timespanComboBox    = new ComboBox<>(timespans);
        timespanComboBox.getSelectionModel().select(Timespan.FIVE_SECONDS);
        themesComboBox.getSelectionModel().select(Theme.GREEN);
		
        input_grid = new GridPane();
        input_grid.setHgap(10);
        input_grid.setVgap(10);
        input_grid.add(time_gauge, 0, 0);
        input_grid.add(value_gauge, 1, 0);
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.add(new VBox(themeLabel, themesComboBox), 0, 0);
        grid.add(new VBox(timespanLabel, timespanComboBox), 0, 1);
        themesComboBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> monitor.setColorTheme(nv));
        timespanComboBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> monitor.setTimespan(nv));
        
        HBox hpane = new HBox(10, grid, input_grid);
        hpane.setPadding(new Insets(10));
        
        Scene pred_scene = new Scene(new StackPane(pred_panel.getPred_pane()));
        pred_scene.getStylesheets().add("css/WhiteOnBlack.css");
        
        Stage pred_stage = new Stage();
        pred_stage.setTitle("Prediction Panel");
        pred_stage.setScene(pred_scene);
        
        
		StackPane monitorPane = new StackPane(monitor);
		monitorPane.setPrefSize(800, 500);
        VBox pane = new VBox(10, monitorPane, hpane);
        pane.setPadding(new Insets(10));

        Scene scene = new Scene(pane);
        scene.getStylesheets().add("css/WhiteOnBlack.css");
//        scene.setOnKeyPressed(e -> monitor.addDataPoint(RND.nextInt(200) - 100));
//        scene.setOnKeyReleased(e -> monitor.addDataPoint(0));

        stage.setTitle("EEG Monitor");
        stage.setScene(scene);
        stage.show();
        
        pred_stage.show();
   
        launchService();
		
	}
	
    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
	
    
	private static ArrayList<AnyRecord> getRecord(double[] vals) throws IllegalArgumentException, IllegalAccessException {
		
		ArrayList<AnyRecord> records = new ArrayList<AnyRecord>();
		for(int i = 0; i < vals.length-1; i++) {
			records.add(new AnyRecord(new Double[] {vals[i]}));
		}
		return records;
	}
	
	private static int getLabel(double[] vals) {		
		return (int)vals[vals.length - 1];	
	}
    
    
}
