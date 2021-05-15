package examples;


import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import records.AnyRecord;
import tsetlin.AutomataLearning;

public class EEGMonitorViewSimple  {
	
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
	
	
	public void showcase() {
		
		try {
			dataPull();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Pull done");
		buildInSample();
		System.out.println("in sample");
		buildMonitorAndService();
		System.out.println("Build monitor done");
		plotEEGSignal(0);
		System.out.println("Build done");
		
		StackPane monitorPane = new StackPane(monitor);
//        VBox pane = new VBox(10, monitorPane, grid);
//        pane.setPadding(new Insets(10));
		System.out.println("Monitor set done");

   
        
		
        new JFXPanel();
		
		Platform.runLater(() -> {
	        try {


	        	monitorPane .setPrefSize(1200, 600);
	            Scene scene = new Scene(monitorPane);

	            scene.setOnKeyPressed(e -> monitor.addDataPoint(RND.nextInt(200) - 100));
	            scene.setOnKeyReleased(e -> monitor.addDataPoint(0));
	            scene.getStylesheets().add("css/WhiteOnBlack.css");

	            Stage stage = new Stage();
	            stage.setTitle("RadarChart");
	            stage.setScene(scene);
	            stage.show();
	            
	            
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    });
		
		launchService();
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
                .timespan(Timespan.FIVE_SECONDS)
                .colorTheme(Theme.GREEN)
                .speedFactor(1)
                //.noOfSegments(150)
                //.scaleFactorY(0.4)
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
                monitor.addDataPoint(eeg_sig[eeg_t]);
            });
            eeg_t++;        
            if(eeg_t == eeg_sig.length) eeg_t = 0;
        };
		
	}
	
	
	public void plotEEGSignal(int sig) {
		
		eeg_sig = new double[eeg_samples.get(sig).length-1];
		
		for(int i = 0; i < eeg_samples.get(sig).length-1; i++) {	
			eeg_sig[i] = eeg_samples.get(sig)[i];
		}
		
		
		
	}
	
	public void launchService() {
		executorService.scheduleAtFixedRate(eegTask, 0, 5, TimeUnit.MILLISECONDS);
	}
	
	
	
	public void dataPull() throws IOException {
		
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
		
		AutomataLearning<AnyRecord> automata = new AutomataLearning<AnyRecord>(dim_y, patch_dim_y, dim_x, any, nClauses, threshold, max_specificity, nClasses, 0f);
		automata.add_fit(in_sample);
		
	}

	
	public static void main(String[] args) {
		
		EEGMonitorViewSimple view = new EEGMonitorViewSimple();
		
		view.showcase();
		
	}

//	@Override
//	public void start(Stage stage) {
//		
//		
//		try {
//			dataPull();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		System.out.println("Pull done");
//		buildInSample();
//		System.out.println("in sample");
//		buildMonitorAndService();
//		System.out.println("Build monitor done");
//		plotEEGSignal(0);
//		System.out.println("Build done");
//		
//		StackPane monitorPane = new StackPane(monitor);
////        VBox pane = new VBox(10, monitorPane, grid);
////        pane.setPadding(new Insets(10));
//		System.out.println("Monitor set done");
//        Scene scene = new Scene(monitorPane);
//
//        scene.setOnKeyPressed(e -> monitor.addDataPoint(RND.nextInt(200) - 100));
//        scene.setOnKeyReleased(e -> monitor.addDataPoint(0));
//
//        stage.setTitle("JavaFX Monitor");
//        stage.setScene(scene);
//        stage.show();
//   
//        launchService();
//		
//	}
	

	
}
