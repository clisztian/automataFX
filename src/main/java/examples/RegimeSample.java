package examples;


import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.marker.Marker;
import de.gsi.chart.plugins.XRangeIndicator;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.LineStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.chart.samples.CssStylingSample;
import de.gsi.chart.samples.utils.TradeMarker;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.chart.utils.AxisSynchronizer;
import de.gsi.dataset.spi.DefaultDataSet;
import interpretability.GlobalRealFeatures;
import interpretability.Prediction;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import records.OrderbookSignalUpdateRecord;
import tsetlin.MultiAutomataLearning;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

public class RegimeSample extends Application {


    public static double[] computerFilterCoefficients(int L_filter, double freqCutoff) {

        double sum;
        double[] wn_filter = new double[L_filter];
        wn_filter[0] = freqCutoff/Math.PI;
        sum = wn_filter[0];
        for(int i = 1; i < L_filter; i++) {

            wn_filter[i] = (1.0/Math.PI)*Math.sin(freqCutoff*i)/i;
            sum = sum + wn_filter[i];
        }
        for(int l=0; l < L_filter; l++) {
            wn_filter[l] = wn_filter[l]/(sum-wn_filter[0]/2.0);
        }
        return wn_filter;
    }


    public static  ArrayList<double[]> extractRegime(int K) {

        ArrayList<double[]> orders = readFile();
        ArrayList<double[]> regime_label = new ArrayList<>();


        for(int j = K; j < orders.size() - K; j++) {

            double sum_m = 0;
            double sum_p = 0;

            for(int l = 0; l < K; l++) {
                sum_m += orders.get(j - l)[1];
            }
            for(int l = 0; l < K; l++) {
                sum_p += orders.get(j + l)[1];
            }
            sum_m = sum_m/(double)K;
            sum_p = sum_p/(double)K;


            double l_t = (sum_p - sum_m)/sum_m;
            regime_label.add(new double[]{orders.get(j)[0] - orders.get(0)[0], orders.get(j)[1], l_t} );
        }

        return regime_label;
    }

    public static  ArrayList<double[]> extractRegimeFilter(int K, double omega) {


        double[] w = computerFilterCoefficients(K, omega);


        ArrayList<double[]> orders = readFile();
        ArrayList<double[]> regime_label = new ArrayList<>();


        for(int j = K; j < orders.size() - K; j++) {

            double sum_m = 0;
            double sum_p = 0;

            for(int l = 0; l < K; l++) {
                sum_m += w[l] * (Math.log(orders.get(j - l)[1]) - Math.log(orders.get(j - l - 1)[1]));
            }
            for(int l = 1; l < K; l++) {
                sum_p +=  w[l] * (Math.log(orders.get(j + l)[1]) - Math.log(orders.get(j + l - 1)[1]));
            }



            double l_t = sum_m + sum_p;
            regime_label.add(new double[]{orders.get(j)[0] - orders.get(0)[0], orders.get(j)[1], l_t} );
        }

        return regime_label;
    }



    public static ArrayList<double[]> readFile() {

        ArrayList<double[]> order = new ArrayList<double[]>();

        try (BufferedReader br = new BufferedReader(new FileReader("/home/lisztian/Downloads/new.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                double[] vals = Arrays.stream(line.split("[,]+")).mapToDouble(Double::parseDouble).toArray();
                order.add(vals);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return order;
    }



    @Override
    public void start(Stage primaryStage) throws Exception {

        Stop[] stop = {new Stop(0, Color.rgb(150, 1, 21, .4).darker().darker()),
                new Stop(1, Color.rgb(130, 178, 255, .3).darker().darker())};

        // create a Linear gradient object
        LinearGradient linear_gradient = new LinearGradient(0, 0,
                0, 1, true, CycleMethod.NO_CYCLE, stop);


        Triple<ArrayList<double[]>,ArrayList<double[]>,ArrayList<double[]>> past_per= getPerformance();

        ArrayList<double[]> sig_perf = past_per.getLeft();
        ArrayList<double[]> sig_perf2 = past_per.getMiddle();
        ArrayList<double[]> sig_metrics = past_per.getRight();
        
        Background back = new Background(new BackgroundFill(linear_gradient, CornerRadii.EMPTY, Insets.EMPTY));

        final DefaultNumericAxis timeAxis = new DefaultNumericAxis("Timestamp");
        final DefaultNumericAxis timeAxis2 = new DefaultNumericAxis("Timestamp");
        final DefaultNumericAxis priceAxis = new DefaultNumericAxis("CHF");
        final DefaultNumericAxis pnlAxis = new DefaultNumericAxis("PnL");
        final DefaultNumericAxis strengthAxis = new DefaultNumericAxis("PnL");

        priceAxis.setAutoUnitScaling(true);
        priceAxis.setAutoRanging(true);
        priceAxis.setForceZeroInRange(false);
        priceAxis.setSide(Side.LEFT);

        pnlAxis.setAutoUnitScaling(true);
        pnlAxis.setAutoRanging(true);
        pnlAxis.setForceZeroInRange(false);
        pnlAxis.setSide(Side.RIGHT);
        
        strengthAxis.setAutoUnitScaling(true);
        strengthAxis.setAutoRanging(true);
        strengthAxis.setForceZeroInRange(false);
        strengthAxis.setSide(Side.RIGHT);

        XYChart chart = new XYChart(timeAxis, priceAxis);
        chart.legendVisibleProperty().set(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);

        XYChart schart = new XYChart(timeAxis2, strengthAxis);
        schart.legendVisibleProperty().set(true);
        schart.setHorizontalGridLinesVisible(false);
        schart.setVerticalGridLinesVisible(false);
        
//        timeAxis.setTickLabelFormatter(new StringConverter<Number>() {
//            @Override
//            public String toString(Number object) {
//                LocalDateTime dt = LocalDateTime.ofInstant(Instant.EPOCH.plus(object.longValue(), ChronoUnit.MICROS), ZoneId.systemDefault());
//                return dt.format(shortt);
//            }
//            @Override
//            public Number fromString(String string) {
//                return 0;
//            }
//        });


        AxisSynchronizer sync = new AxisSynchronizer();
        sync.add(timeAxis);
        sync.add(timeAxis2);


        DefaultDataSet price = new DefaultDataSet("PRICE");
        DefaultDataSet pnl = new DefaultDataSet("P&L");
        DefaultDataSet pnl2 = new DefaultDataSet("P&LTM");
        
        DefaultDataSet bidstrength = new DefaultDataSet("LogPrice");
        DefaultDataSet askstrength = new DefaultDataSet("AskVolume");
        DefaultDataSet pricestrength = new DefaultDataSet("BidVolume");
        

        //ArrayList<double[]> plot = extractRegimeFilter(50, Math.PI/22.0);
        ArrayList<double[]> plot = extractRegime(100);

        ArrayList<XRangeIndicator> ranges = new ArrayList<>();
        ArrayList<XRangeIndicator> ranges_buy = new ArrayList<>();
        ArrayList<double[]> cum_profit = new ArrayList<>();
        //DefaultRenderColorScheme.fillStylesProperty().
        Color mycol = Color.rgb(52,177,215);
        price.setStyle("strokeColor=rgb(223, 223, 200); markerSize=1; markerColor=rgb(223, 223, 225); markerType=circle;");
        pnl.setStyle("strokeColor=rgb(52,177,215); markerSize=1; markerColor=rgb(52,177,215); markerType=circle;");
        pnl2.setStyle("strokeColor=rgb(52,177,215); markerSize=1; markerColor=rgb(52,177,215); markerType=circle;");
        
        bidstrength.setStyle("strokeColor=rgba(58, 232, 191,.6);");
        askstrength.setStyle("strokeColor=rgba(232, 58, 203,.6); ");
        pricestrength.setStyle("strokeColor=rgba(78, 186, 107,.6); ");
        
        
        double[] v1 = plot.get(0);
        double xmin=0,xmax=0;
        if(v1[2] > 0) xmin = v1[0];
        double alpha = 0.0001;

        double perf = 0;
        double sell_price = 0;
        double buy_price = 0;
        for(int i = 1; i < plot.size(); i++) {

            double[] v = plot.get(i);
            price.add(v[0], v[1]);

            /**
             * sell regime
             */
            if(v[2] < -alpha && plot.get(i-1)[2] > -alpha) {
                xmin = v[0];

                sell_price = v[1];
            }

            if(v[2] > -alpha && plot.get(i-1)[2] < -alpha) {
                xmax = v[0];

                XRangeIndicator xRange = new XRangeIndicator(timeAxis, xmin, xmax);

                ranges.add(xRange);

                if(sell_price > 0) {

                    double profit = (sell_price - v[1]) / sell_price;
                    perf += profit;

                    cum_profit.add(new double[]{xmax, perf});
//                    pnl.add(xmax, perf);
//                    System.out.println(pnl.getDataCount() + " " + sell_price + v[1] + " " + profit + " " + perf);
                }
            }

            /**
             * buy regime
             */
            if(v[2] > alpha && plot.get(i-1)[2] < alpha) {
                xmin = v[0];
                buy_price = v[1];
            }

            if(v[2] < alpha && plot.get(i-1)[2] > alpha) {
                xmax = v[0];

                XRangeIndicator xRange = new XRangeIndicator(timeAxis, xmin, xmax);

                ranges_buy.add(xRange);

                if(buy_price > 0) {

                    double profit = (v[1] - buy_price) / buy_price;
                    perf += profit;
                    cum_profit.add(new double[]{xmax, perf});
//                    pnl.add(xmax, perf);
//                    System.out.println(pnl.getDataCount() + " " + v[1] + " " + buy_price + " " + profit + " " + perf);
                }
            }


        }


        for(int i = 0; i < sig_perf.size(); i++) {
        	
        	double[] d = sig_perf.get(i);
        	pnl.add(d[0], d[2]);
        	
        	
        }

        for(int i = 0; i < sig_perf2.size(); i++) {
        	
        	double[] d = sig_perf2.get(i);
        	pnl2.add(d[0], d[2]);
        	
        	
        }

        for(int i = 0; i < sig_metrics.size(); i++) {
        	
        	double[] d = sig_metrics.get(i);
        	bidstrength.add(d[0], d[1]);
        	askstrength.add(d[0], d[2]);
        	pricestrength.add(d[0], d[3]);
        }
        
        
        

        chart.getPlugins().add(new Zoomer());
        //chart.getPlugins().addAll(ranges_buy);

        schart.getPlugins().add(new Zoomer());

        final ErrorDataSetRenderer tradeRenderer = new ErrorDataSetRenderer();
        tradeRenderer.setMarkerSize(2);
        tradeRenderer.setMarker(TradeMarker.CIRCLE);
        tradeRenderer.setPolyLineStyle(LineStyle.NORMAL);
        tradeRenderer.setErrorType(ErrorStyle.NONE);
        tradeRenderer.setDrawMarker(true);
        tradeRenderer.setDrawBubbles(false);
        tradeRenderer.setAssumeSortedData(true); // !! important since DS is likely unsorted
        tradeRenderer.setPointReduction(true);
        tradeRenderer.getAxes().addAll(timeAxis,priceAxis);

        final ErrorDataSetRenderer pnlRenderer = new ErrorDataSetRenderer();
        pnlRenderer.setDrawMarker(false);
        pnlRenderer.setPolyLineStyle(LineStyle.AREA);
        pnlRenderer.setErrorType(ErrorStyle.NONE);
        pnlRenderer.setDrawBubbles(false);
        pnlRenderer.setAssumeSortedData(true); // !! important since DS is likely unsorted
        pnlRenderer.setPointReduction(true);
        pnlRenderer.getAxes().add(pnlAxis);
        
        final ErrorDataSetRenderer strengthRenderer = new ErrorDataSetRenderer();
        strengthRenderer.setPolyLineStyle(LineStyle.STAIR_CASE);
        strengthRenderer.setErrorType(ErrorStyle.NONE);
        strengthRenderer.setDrawMarker(false);
        strengthRenderer.setDrawBubbles(false);
        strengthRenderer.setAssumeSortedData(true); // !! important since DS is likely unsorted
        strengthRenderer.setPointReduction(true);
        //strengthRenderer.getAxes().add(strengthAxis);
        

        chart.getRenderers().setAll(tradeRenderer, pnlRenderer);
        schart.getRenderers().setAll(strengthRenderer);
        
        tradeRenderer.getDatasets().addAll(price);
        pnlRenderer.getDatasets().addAll(pnl2);
        strengthRenderer.getDatasets().addAll(bidstrength, askstrength, pricestrength);

        chart.setPrefSize(1000, 800);
        schart.setPrefSize(100,400);
        
        chart.setBackground(back);
        schart.setBackground(back);
        BorderPane pane = new BorderPane();
        pane.setCenter(chart);
        pane.setBottom(schart);

        Scene myscene = new Scene(pane, 1000, 800);
        myscene.getStylesheets().setAll(Objects.requireNonNull(CssStylingSample.class.getResource("WhiteOnBlack.css"), "could not load css file: " + "WhiteOnBlack.css").toExternalForm());
        primaryStage.setTitle(this.getClass().getSimpleName());
        primaryStage.setScene(myscene);
        primaryStage.setOnCloseRequest(evt -> Platform.exit());
        primaryStage.show();
    }

    
    
    public Triple<ArrayList<double[]>,ArrayList<double[]>,ArrayList<double[]>>  getPerformance() throws IllegalArgumentException, IllegalAccessException {
    	
    	
		double sigma = .00003;
		
		/**
		 * Model parameters
		 */
		int dim_x = 10;          //encoding feature dimension
		int window_size = 15;    //domain d'influence
		int patch_dim_y = 5;     //number of convolutional lags in time
		int threshold = 500;     
		int nClauses = 1000;
		float max_specificity = 2f;
		int nClasses = 2;
		
		DecimalFormat df = new DecimalFormat("#.######");
		
		//Pair<ArrayList<OrderbookUpdateRecord>, ArrayList<double[]>> data_pair = extractRegime(70, 0);
		
		double omega = Math.PI/5;
		double omega1 = Math.PI/12;
		double omega2 = Math.PI/22;
		
		Pair<ArrayList<OrderbookSignalUpdateRecord>, ArrayList<double[]>> data_pair = extractSignalRegime(70, 0, omega, omega1, omega2);
		
		ArrayList<OrderbookSignalUpdateRecord> book_data = data_pair.getKey();
		ArrayList<double[]> label = data_pair.getValue();
		
		
		
		int total_samples = book_data.size();
		int n_in_sample = (int)(1.0*book_data.size()*.40);
		
		System.out.println(book_data.size() + " " + label.size() + " " + n_in_sample);
		
		
		int down_trend = 0;
		int up_trend = 0;
		
		int count = 0; 
		for(OrderbookSignalUpdateRecord rec : book_data) {			
			
			if(count > 600) {
				if(label.get(count)[2] == 0) down_trend++;
				else if(label.get(count)[2] == 1) up_trend++;
				
				
				
				System.out.println(count + " " + (down_trend - up_trend)); 
			}
			

			count++;
		}
		
		
		/**
		 * Initiate model
		 */
		int start = 600;
		int window = start + window_size;
		int insamp = 5024;
		
		MultiAutomataLearning<OrderbookSignalUpdateRecord> automata = new MultiAutomataLearning<OrderbookSignalUpdateRecord>(window_size, patch_dim_y, dim_x, 
				book_data.get(0), nClauses, threshold, max_specificity, nClasses, false, false, false, false, .5f);
		
		automata.add_fit(book_data);
		automata.add(book_data.subList(start, window));
		
		
		down_trend = 0;
		up_trend = 0;
		int sideways = 0;
		
		double perf = 0;
        double sell_price = 0;
        double buy_price = 0;
		
		int pred = 0;
		ArrayList<double[]> pnl_perf = new ArrayList<double[]>();
		ArrayList<double[]> pnl_perf_tm = new ArrayList<double[]>();
		ArrayList<double[]> local_metrics = new ArrayList<double[]>();
		
		
		for(int i =  window; i < insamp; i++) {
			
			
			pred = automata.update(book_data.get(i), (int)label.get(i)[2]);
			int true_class = (int)label.get(i)[2];
			
			String error = pred != true_class ? "error" : "";
			
			System.out.println(i + " " + df.format(book_data.get(i).log_price()) + " " + df.format(book_data.get(i).volume()) + " " + df.format(book_data.get(i).sig_1()) + " " + df.format(book_data.get(i).sig_2()) + " " + true_class + " " + pred + " " + error);	
			
			if(true_class == 0) down_trend++;
			else if(true_class == 1) up_trend++;
			else sideways++;
			
			
			
			if(book_data.get(i).sig_3() > 0 && book_data.get(i-1).sig_3() < 0) {
				
				if(sell_price > 0) {

                    double profit = (sell_price - label.get(i)[1]) / sell_price;
                    perf += profit;
                    System.out.println(sell_price + " " + label.get(i)[1] + " " + profit + " " + perf);	
                    pnl_perf.add(new double[] {label.get(i)[0], profit, perf});
				}				
				buy_price = label.get(i)[1];			
				
			}
			else if(book_data.get(i).sig_3() < 0 && book_data.get(i-1).sig_3() > 0) {
				
				if(buy_price > 0) {

					double profit = (label.get(i)[1] - buy_price) / buy_price;
	                perf += profit;
                    System.out.println(sell_price + " " + label.get(i)[1] + " " + profit + " " + perf);	
                    pnl_perf.add(new double[] {label.get(i)[0], profit, perf});
				}				
				sell_price = label.get(i)[1];				
			}
			
			
		}
		System.out.println(down_trend + " " + up_trend + " " + sideways);
		System.out.println("Out of sample"); 
		
		
		down_trend = 0;
		up_trend = 0;
		sideways = 0;
		int false_positive = 0;
		
		int pred_up = 0;
		int pred_down = 0;
		
		int true_pred_down = 0;
		int true_pred_up = 0;
		/**
		 * Run out of sample
		 */

        double sell_price_tm = 0;
        double buy_price_tm = 0;
		int prev_pred = pred;
		int pred_class = 0;
		
		
		
		for(int i = insamp; i < total_samples; i++) {
			
			Prediction predict = automata.predict(book_data.get(i));
			
			int true_class = (int)label.get(i)[2];
			pred_class = predict.getPred_class();
			
			String error = pred_class != true_class ? "error" : "";
			if(error.equals("error")) {
				false_positive++;
			}
			
			if(pred_class == 0) pred_down++;
			if(pred_class == 1) pred_up++;
			
			if(pred_class == 0 && true_class == 0) true_pred_down++;
			if(pred_class == 1 && true_class == 1) true_pred_up++;
			
			System.out.println(i 
//					df.format(book_data.get(i).log_price()) 
//					+ " " + book_data.get(i).volume() 
//					+ " " + df.format(book_data.get(i).sig_1()) 
					+ " " + df.format(book_data.get(i).sig_3()) 
					+ " " + true_class 
					+ " " + pred_class  
					+ " " + predict.getProbability() 
					+ " " + error);
			
			
			if(true_class == 0) down_trend++;
			else if(true_class == 1) up_trend++;
			else sideways++;
			
			if(pred_class == 1 && prev_pred == 0) {
				
				double[] local_int = analyzePrediction(predict, patch_dim_y - 1);
				
				local_metrics.add(new double[] {label.get(i)[0], local_int[0], local_int[1], local_int[2]});
				
				if(sell_price_tm > 0) {

                    double profit = (sell_price_tm - label.get(i)[1]) / sell_price_tm;
                    perf += profit;
                    //System.out.println(sell_price + " " + label.get(i)[1] + " " + profit + " " + perf);	
                    pnl_perf_tm.add(new double[] {label.get(i)[0], profit, perf});
				}				
				buy_price_tm = label.get(i)[1];			
				
				
				
			}
			else if(pred_class == 0 && prev_pred == 1) {
				
				double[] local_int = analyzePrediction(predict, patch_dim_y - 1);
				
				local_metrics.add(new double[] {label.get(i)[0], local_int[0], local_int[1], local_int[2]});
				
				if(buy_price_tm > 0) {

					double profit = (label.get(i)[1] - buy_price_tm) / buy_price_tm;
	                perf += profit;
                    //System.out.println(sell_price + " " + label.get(i)[1] + " " + profit + " " + perf);	
                    pnl_perf_tm.add(new double[] {label.get(i)[0], profit, perf});
				}				
				sell_price_tm = label.get(i)[1];				
			}
			
		
			
			
			if(book_data.get(i).sig_3() > 0 && book_data.get(i-1).sig_3() < 0) {
				
				if(sell_price > 0) {

                    double profit = (sell_price - label.get(i)[1]) / sell_price;
                    perf += profit;
                    System.out.println(sell_price + " " + label.get(i)[1] + " " + profit + " " + perf);	
                    pnl_perf.add(new double[] {label.get(i)[0], profit, perf});
				}				
				buy_price = label.get(i)[1];			
				
			}
			else if(book_data.get(i).sig_3() < 0 && book_data.get(i-1).sig_3() > 0) {
				
				if(buy_price > 0) {

					double profit = (label.get(i)[1] - buy_price) / buy_price;
	                perf += profit;
                    System.out.println(sell_price + " " + label.get(i)[1] + " " + profit + " " + perf);	
                    pnl_perf.add(new double[] {label.get(i)[0], profit, perf});
				}				
				sell_price = label.get(i)[1];				
			}
			
			
			prev_pred = pred_class;
			
		}
		
		/**
		 * Close position
		 */
		if(book_data.get(book_data.size() - 1).sig_3() < 0) {
			
			if(sell_price > 0) {

                double profit = (sell_price - label.get(label.size() - 1)[1]) / sell_price;
                perf += profit;
                System.out.println(sell_price + " " + label.get(label.size() - 1)[1] + " " + profit + " " + perf);	
                pnl_perf.add(new double[] {label.get(label.size() - 1)[0], profit, perf});
			}			
		}
		else if(book_data.get(book_data.size() - 1).sig_3() > 0) {
			
			if(buy_price > 0) {

				double profit = (label.get(label.size() - 1)[1] - buy_price) / buy_price;
                perf += profit;
                System.out.println(sell_price + " " + label.get(label.size() - 1)[1] + " " + profit + " " + perf);	
                pnl_perf.add(new double[] {label.get(label.size() - 1)[0], profit, perf});
			}			
		}
		
		
		
		System.out.println("Incorrect: " + false_positive + " out of " + (total_samples - n_in_sample));		
		System.out.println("true_positives: " + (1.0*true_pred_up/up_trend) + " " + (1.0*true_pred_down/down_trend));
		
		
		double mean = 0; 
		double std = 0;
		int count2 = 0;
		for(int i = 0; i < pnl_perf.size(); i++) {
			System.out.println(i + " " + df.format(pnl_perf.get(i)[0]) + " " + df.format(pnl_perf.get(i)[1]));
			
			if(pnl_perf.get(i)[1] != 0) {
				mean += pnl_perf.get(i)[1];
				count2++;
			}
			
		}
		mean = mean/count2;
		
		for(int i = 0; i < pnl_perf.size(); i++) {
			
			
			if(pnl_perf.get(i)[1] != 0) {
				std = (pnl_perf.get(i)[1] - mean)*(pnl_perf.get(i)[1] - mean);
			}
			
		}
		std = Math.sqrt(mean/count2);
		
		System.out.println("Sharpe: " + (Math.sqrt(255)*mean/std) + " " + mean);
		
//		System.out.println("Global features");
//		
//		
//		int[] feats = automata.getAutomaton().getGlobalFeatureImportance(0);
//		GlobalRealFeatures[][] reg1 = automata.localFeatureImportance(feats);
//		
//		int[] feats2 = automata.getAutomaton().getGlobalFeatureImportance(1);
//		GlobalRealFeatures[][] reg2 = automata.localFeatureImportance(feats2);
//		
//		ArrayList<GlobalRealFeatures> real = new ArrayList<GlobalRealFeatures>();
//		for(int i = 0; i < reg1[0].length; i++) {
//			real.add(reg1[patch_dim_y-1][i]);
//		}
//		Collections.sort(real, compare.reversed());
//		
//
//		for(int i = 0; i < real.size(); i++) {
//			
//			float pos_val = real.get(i).getBitRanges().getStrength();			
//			System.out.println(real.get(i).getFeatureName() + " " + pos_val);
//		}    
//		
//		
//		ArrayList<GlobalRealFeatures> real2 = new ArrayList<GlobalRealFeatures>();
//		for(int i = 0; i < reg2[0].length; i++) {
//			real2.add(reg2[patch_dim_y-1][i]);
//		}
//		Collections.sort(real2, compare.reversed());
//
//		for(int i = 0; i < real2.size(); i++) {
//			
//			float pos_val = real2.get(i).getBitRanges().getStrength();			
//			System.out.println(real2.get(i).getFeatureName() + " " + pos_val);
//		}
		
		
		
		return Triple.of(pnl_perf, pnl_perf_tm, local_metrics);
    	
    	
    }
    
    
    
    
    public static Pair<ArrayList<OrderbookSignalUpdateRecord>, ArrayList<double[]>> extractSignalRegime(int K, double alpha, double omega, double omega_1, double omega_2) {

    	
    	double[] w_1 = computerFilterCoefficients(K, omega);
    	double[] w_2 = computerFilterCoefficients(K, omega_1);
    	double[] w_3 = computerFilterCoefficients(K, omega_2);
    	
    	
        ArrayList<double[]> orders = readFile();
        ArrayList<double[]> regime_label = new ArrayList<>();
        ArrayList<OrderbookSignalUpdateRecord> data = new ArrayList<>();

 
        
        double time_diff;
        float price;
        int volume;
        float max_spread;
        OrderbookSignalUpdateRecord rec = null;
        
        for(int j = K; j < orders.size() - K; j++) {

            double sum_m = 0;
            double sum_p = 0;

            for(int l = 0; l < K; l++) {
                sum_m += orders.get(j - l)[1];
            }
            for(int l = 0; l < K; l++) {
                sum_p += orders.get(j + l)[1];
            }
            sum_m = sum_m/(double)K;
            sum_p = sum_p/(double)K;


            double l_t = (sum_p - sum_m)/sum_m;
            int label = 2;
            
			if(l_t > alpha) label = 1;
			else if(l_t <= alpha) label = 0;
            
            regime_label.add(new double[]{orders.get(j)[0] - orders.get(0)[0], orders.get(j)[1], label} );
            
            double sig_1 = 0;
            double sig_2 = 0;
            double sig_3 = 0;
            for(int l = 0; l < K; l++) {
            	
            	double p = Math.log(orders.get(j - l)[1]) - Math.log(orders.get(j - l - 1)[1]);
            	sig_1 += w_1[l] * p;
            	sig_2 += w_2[l] * p;
            	sig_3 += w_3[l] * p;
            }
            
            
            /**
             * Build the difference
             */
            
            
            time_diff = orders.get(j)[0] - orders.get(j-1)[0];
            float log_price = (float) (Math.log(orders.get(j)[1]) -  Math.log(orders.get(j-1)[1]));
            volume = (int)orders.get(j)[2];

            float spread = (float)orders.get(j)[3];
            
            float bid_diff;
            float ask_diff;
            
            double bid_sum_lag = 0;
            double ask_sum_lag = 0;
            double bid_sum = 0;
            double ask_sum = 0;
            
            
            
            for(int k = 0; k < 10; k++) {
            	
            	bid_sum_lag += orders.get(j-1)[4+k];
            	ask_sum_lag += orders.get(j-1)[14+k];
            	
            	bid_sum += orders.get(j)[4+k];
            	ask_sum += orders.get(j)[14+k];
            	
            }
            
            bid_diff = (float) Math.log(bid_sum/ bid_sum_lag);
        	ask_diff = (float) Math.log(ask_sum / ask_sum_lag);
            
            
            
            rec = new OrderbookSignalUpdateRecord(time_diff, spread, log_price, volume, bid_diff, ask_diff, (float)sig_1, (float)sig_2, (float)sig_3);
            
            data.add(rec);
            
        }

        return Pair.of(data, regime_label);
    }
    
    
    public static double[] analyzePrediction(Prediction pred, int lag_loc) {
    	
    	
    	GlobalRealFeatures[][] feats = pred.getReal_features();
    	

		ArrayList<GlobalRealFeatures> real = new ArrayList<GlobalRealFeatures>();
		for(int i = 0; i < feats[0].length; i++) {
			real.add(feats[lag_loc][i]);
		}
		Collections.sort(real, compare.reversed());
		
		double[] metrics = new double[3];
		for(int i = 0; i < real.size(); i++) {
			
			float pos_val = real.get(i).getBitRanges().getStrength();
			
			System.out.print(real.get(i).getFeatureName() + " " + pos_val + " | ");
			
			if(real.get(i).getFeatureName().equals("sig_1")) {
				metrics[0] = pos_val;
			}
			else if(real.get(i).getFeatureName().equals("ask_vol")) {
				metrics[1] = pos_val;
			}
			else if(real.get(i).getFeatureName().equals("bid_vol")) {
				metrics[2] = pos_val;
			}
			
			
			
		}    	
		System.out.println("");
		
		
		return metrics;
    }
    
    private static final Comparator<GlobalRealFeatures> compare = new Comparator<GlobalRealFeatures>() {
        public int compare(GlobalRealFeatures o1, GlobalRealFeatures o2) {
             return o1.getStrength() + o1.getNegStrength() < o2.getStrength() + o2.getNegStrength() ? -1
                      : o1.getStrength() + o1.getNegStrength() > o2.getStrength() + o2.getNegStrength() ? 1
                      : 0;
        }
    };
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }
}
