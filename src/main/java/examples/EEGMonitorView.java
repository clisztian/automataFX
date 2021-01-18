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
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import records.AnyRecord;
import tsetlin.AutomataLearning;

public class EEGMonitorView extends Application {
	
	private static final Random                   RND = new Random();
	private Monitor monitor;
	private ScheduledExecutorService executorService;
    private Runnable eegTask;
    
	private ArrayList<double[]> eeg_samples;	
	private CsvReader marketDataFeed;// = new CsvReader(new FileReader("/home/lisztian/Downloads/eeg_time.csv"), ',');	
	
	private ArrayList<AnyRecord> in_sample;
	
	AnyRecord any = new AnyRecord(new String[] {"value"}, new Double[] {0.0});
	
	int dim_x = 20;
	int dim_y = 7200;
	int patch_dim_y = 20;
	int n_samples = 200;
	int threshold = 160;
	int nClauses = 64;
	int eeg_t = 0;

	
	float max_specificity = 2f;
	int nClasses = 2;
	private double[] eeg_sig;
	private int signal_count = 0;
	
	
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
		
		executorService = Executors.newScheduledThreadPool(1);
		
		eegTask         = () -> {
            Platform.runLater(() -> {
            	if(eeg_t < eeg_sig.length) monitor.addDataPoint(eeg_sig[eeg_t]);
            });
            eeg_t++;        
            if(eeg_t == eeg_sig.length) {
            	
            	signal_count = RND.nextInt(eeg_samples.size());
            	plotEEGSignal(signal_count); 
            	eeg_t = 0;
            	
            	System.out.println("Signal: " + signal_count);
            }
        };
		
	}
	
	
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
		for(int i = 0; i < eeg_samples.get(0).length-1; i++) {	
			in_sample.add(new AnyRecord(new Double[] {eeg_samples.get(0)[i]}));
		}
		for(int i = 0; i < eeg_samples.get(19).length-1; i++) {
			in_sample.add(new AnyRecord(new Double[] {eeg_samples.get(19)[i]}));
		}		
	}
	

	public void buildAutomataModel() throws IllegalArgumentException, IllegalAccessException {
		
		AutomataLearning<AnyRecord> automata = new AutomataLearning<AnyRecord>(dim_y, patch_dim_y, dim_x, any, nClauses, threshold, max_specificity, nClasses);
		automata.add_fit(in_sample);
		
	}


	@Override
	public void start(Stage stage) {
		
		
		try {
			dataPull();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		buildInSample();

		buildMonitorAndService();
		plotEEGSignal(0);
		

        Label                 themeLabel       = new Label("Theme");
        ObservableList<Theme> themes           = FXCollections.observableArrayList(List.of(Theme.values()));
        ComboBox<Theme>       themesComboBox   = new ComboBox<>(themes);

        Label                    timespanLabel = new Label("Timespan");
        ObservableList<Timespan> timespans     = FXCollections.observableArrayList(List.of(Timespan.values()));
        ComboBox<Timespan> timespanComboBox    = new ComboBox<>(timespans);
        timespanComboBox.getSelectionModel().select(Timespan.FIVE_SECONDS);
        themesComboBox.getSelectionModel().select(Theme.GREEN);
		
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.add(new VBox(themeLabel, themesComboBox), 0, 0);
        grid.add(new VBox(timespanLabel, timespanComboBox), 0, 1);
        themesComboBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> monitor.setColorTheme(nv));
        timespanComboBox.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> monitor.setTimespan(nv));
        
		StackPane monitorPane = new StackPane(monitor);
		monitorPane.setPrefSize(800, 500);
        VBox pane = new VBox(10, monitorPane, grid);
        pane.setPadding(new Insets(10));
		System.out.println("Monitor set done");
        Scene scene = new Scene(pane);
        scene.getStylesheets().add("css/WhiteOnBlack.css");
//        scene.setOnKeyPressed(e -> monitor.addDataPoint(RND.nextInt(200) - 100));
//        scene.setOnKeyReleased(e -> monitor.addDataPoint(0));

        stage.setTitle("JavaFX Monitor");
        stage.setScene(scene);
        stage.show();
   
        launchService();
		
	}
	
    @Override public void stop() {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
	
}
