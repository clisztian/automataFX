package examples;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.lang3.tuple.Pair;

import interpretability.GlobalRealFeatures;
import interpretability.Prediction;
import javafx.scene.chart.XYChart;
import records.AnomalySeriesObservation;
import records.OrderbookSignalUpdateRecord;
import records.OrderbookUpdateRecord;
import tsetlin.MultiAutomataLearning;

public class OrderbookInterfaceExample {

	
	
	
	
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
		
		double sigma = .00003;
		
		/**
		 * Model parameters
		 */
		int dim_x = 10;          //encoding feature dimension
		int window_size = 10;    //domain d'influence
		int patch_dim_y = 5;     //number of convolutional lags in time
		int threshold = 500;     
		int nClauses = 1000;
		float max_specificity = 2f;
		int nClasses = 2;
		
		DecimalFormat df = new DecimalFormat("#.######");
		
		//Pair<ArrayList<OrderbookUpdateRecord>, ArrayList<double[]>> data_pair = extractRegime(70, 0);
		
		double omega = Math.PI/5;
		double omega1 = Math.PI/12;
		double omega2 = Math.PI/16;
		
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
		
		int pred = 0;
		for(int i =  window; i < insamp; i++) {
			
			
			pred = automata.update(book_data.get(i), (int)label.get(i)[2]);
			int true_class = (int)label.get(i)[2];
			
			String error = pred != true_class ? "error" : "";
			
			System.out.println(i + " " + df.format(book_data.get(i).log_price()) + " " + df.format(book_data.get(i).volume()) + " " + df.format(book_data.get(i).sig_1()) + " " + df.format(book_data.get(i).sig_2()) + " " + true_class + " " + pred + " " + error);	
			
			if(true_class == 0) down_trend++;
			else if(true_class == 1) up_trend++;
			else sideways++;
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
		double perf = 0;
        double sell_price = 0;
        double buy_price = 0;
		int prev_pred = pred;
		int pred_class = 0;
		
        double sell_price_tm = 0;
        double buy_price_tm = 0;
		
		
		ArrayList<double[]> pnl_perf = new ArrayList<double[]>();
		ArrayList<double[]> pnl_perf_tm = new ArrayList<double[]>();
		
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
				
				analyzePrediction(predict, patch_dim_y - 1);
				
				if(sell_price_tm > 0) {

                    double profit = (sell_price_tm - label.get(i)[1]) / sell_price_tm;
                    perf += profit;
                    //System.out.println(sell_price + " " + label.get(i)[1] + " " + profit + " " + perf);	
                    pnl_perf_tm.add(new double[] {profit, perf});
				}				
				buy_price_tm = label.get(i)[1];			
				
				
				
			}
			else if(pred_class == 0 && prev_pred == 1) {
				
				analyzePrediction(predict, patch_dim_y - 1);
				
				if(buy_price_tm > 0) {

					double profit = (label.get(i)[1] - buy_price_tm) / buy_price_tm;
	                perf += profit;
                    //System.out.println(sell_price + " " + label.get(i)[1] + " " + profit + " " + perf);	
                    pnl_perf_tm.add(new double[] {profit, perf});
				}				
				sell_price_tm = label.get(i)[1];				
			}
			
			
			
			
			
			
			if(book_data.get(i).sig_3() > 0 && book_data.get(i-1).sig_3() < 0) {
				
				if(sell_price > 0) {

                    double profit = (sell_price - label.get(i)[1]) / sell_price;
                    perf += profit;
                    System.out.println(sell_price + " " + label.get(i)[1] + " " + profit + " " + perf);	
                    pnl_perf.add(new double[] {profit, perf});
				}				
				buy_price = label.get(i)[1];			
				
			}
			else if(book_data.get(i).sig_3() < 0 && book_data.get(i-1).sig_3() > 0) {
				
				if(buy_price > 0) {

					double profit = (label.get(i)[1] - buy_price) / buy_price;
	                perf += profit;
                    System.out.println(sell_price + " " + label.get(i)[1] + " " + profit + " " + perf);	
                    pnl_perf.add(new double[] {profit, perf});
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
                pnl_perf.add(new double[] {profit, perf});
			}			
		}
		else if(book_data.get(book_data.size() - 1).sig_3() > 0) {
			
			if(buy_price > 0) {

				double profit = (label.get(label.size() - 1)[1] - buy_price) / buy_price;
                perf += profit;
                System.out.println(sell_price + " " + label.get(label.size() - 1)[1] + " " + profit + " " + perf);	
                pnl_perf.add(new double[] {profit, perf});
			}			
		}
		
		
		
		System.out.println("Incorrect: " + false_positive + " out of " + (total_samples - n_in_sample));		
		System.out.println("true_positives: " + (1.0*true_pred_up/up_trend) + " " + (1.0*true_pred_down/down_trend));
		
		for(int i = 0; i < pnl_perf.size(); i++) {
			System.out.println(i + " " + df.format(pnl_perf.get(i)[0]) + " " + df.format(pnl_perf.get(i)[1]));
		}
		
	}
	
	
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


    public static Pair<ArrayList<OrderbookUpdateRecord>, ArrayList<double[]>> extractRegime(int K, double alpha) {

        ArrayList<double[]> orders = readFile();
        ArrayList<double[]> regime_label = new ArrayList<>();
        ArrayList<OrderbookUpdateRecord> data = new ArrayList<>();

 
        
        double time_diff;
        float price;
        int volume;
        float max_spread;
        OrderbookUpdateRecord rec = null;
        
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
			else if(l_t < -alpha) label = 0;
            
            regime_label.add(new double[]{orders.get(j)[0] - orders.get(0)[0], orders.get(j)[1], label} );
            
            
            /**
             * Build the difference
             */
            
            
            time_diff = orders.get(j)[0] - orders.get(j-1)[0];
            float log_price = (float) (Math.log(orders.get(j)[1]) -  Math.log(orders.get(j-1)[1]));
            volume = (int)orders.get(j)[2];

            float spread = (float)orders.get(j)[3];
            
            float[] bid_diff = new float[10];
            float[] ask_diff = new float[10];
            
            for(int k = 0; k < 10; k++) {
            	bid_diff[k] = (float) Math.log(orders.get(j)[4+k] / orders.get(j-1)[4+k]);
            	ask_diff[k] = (float) Math.log(orders.get(j)[14+k] / orders.get(j-1)[14+k]);
            }
            
            
            rec = new OrderbookUpdateRecord(time_diff, spread, log_price, volume, bid_diff[0], 
            		                                                                                    bid_diff[1], 
            		                                                                                    bid_diff[2], 
            		                                                                                    bid_diff[3], 
            		                                                                                    bid_diff[4], 
            		                                                                                    bid_diff[5], 
            		                                                                                    bid_diff[6], 
            		                                                                                    bid_diff[7], 
            		                                                                                    bid_diff[8], 
            		                                                                                    bid_diff[9], 
            		                                                                                    ask_diff[0], 
            		                                                                                    ask_diff[1], 
            		                                                                                    ask_diff[2], 
            		                                                                                    ask_diff[3], 
            		                                                                                    ask_diff[4], 
            		                                                                                    ask_diff[5], 
            		                                                                                    ask_diff[6], 
            		                                                                                    ask_diff[7], 
            		                                                                                    ask_diff[8], 
            		                                                                                    ask_diff[9]);
            
            data.add(rec);
            
        }

        return Pair.of(data, regime_label);
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
	
    
    
    public static void analyzePrediction(Prediction pred, int lag_loc) {
    	
    	
    	GlobalRealFeatures[][] feats = pred.getReal_features();
    	

		ArrayList<GlobalRealFeatures> real = new ArrayList<GlobalRealFeatures>();
		for(int i = 0; i < feats[0].length; i++) {
			real.add(feats[lag_loc][i]);
		}
		Collections.sort(real, compare.reversed());
		
		
		for(int i = 0; i < real.size(); i++) {
			
			float pos_val = real.get(i).getBitRanges().getStrength();
			//float neg_val = real.get(i).getBitRanges().getNeg_strength();
			
			
			System.out.println(real.get(i).getFeatureName() + " " + pos_val);
		}    	
    	
    }
    
    private static final Comparator<GlobalRealFeatures> compare = new Comparator<GlobalRealFeatures>() {
        public int compare(GlobalRealFeatures o1, GlobalRealFeatures o2) {
             return o1.getStrength() + o1.getNegStrength() < o2.getStrength() + o2.getNegStrength() ? -1
                      : o1.getStrength() + o1.getNegStrength() > o2.getStrength() + o2.getNegStrength() ? 1
                      : 0;
        }
    };
    
    
    
    /**
     * 
     * 
     * 		int dim_x = 10;          //encoding feature dimension
		int window_size = 20;    //domain d'influence
		int patch_dim_y = 2;     //number of convolutional lags in time
		int threshold = 600;     
		int nClauses = 10000;
		float max_specificity = 1.6f;
     * 
		int dim_x = 10;          //encoding feature dimension
		int window_size = 10;    //domain d'influence
		int patch_dim_y = 3;     //number of convolutional lags in time
		int threshold = 500;     
		int nClauses = 1000;
		float max_specificity = 2f;
		int nClasses = 2;
     * 
     */
    
    
}
