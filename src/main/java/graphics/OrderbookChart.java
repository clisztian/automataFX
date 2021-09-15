package graphics;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListMap;

import exchange.core2.core.common.L2MarketData;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.LinearGradient;

/**
 * Orderbook chart 
 * 
 * Takes an L2Orderbook, adds to the LinkedHashMap, and then converts to the graph
 * @author lisztian
 *
 */
public class OrderbookChart {


	
	private LinearGradient gradient;
	private Background back;
	
	private BarChart<Number, Number> orderbook_bar_chart;

	private XYChart.Series<Number, Number> bid_features;
	private XYChart.Series<Number, Number> ask_features;
	
	private ArrayList<XYChart.Data<Number, Number>> bid_size;
	private ArrayList<XYChart.Data<Number, Number>> ask_size;
	private NumberAxis xAxis;
	private NumberAxis yAxis;
	
	
	public OrderbookChart() {
		
		
		back = new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
		
		
		xAxis = new NumberAxis();
		xAxis.setLabel("Size");

		yAxis = new NumberAxis();
		yAxis.setLabel("Price");
		
		orderbook_bar_chart = new BarChart<Number, Number>(xAxis, yAxis);
		orderbook_bar_chart.setBackground(back);
		orderbook_bar_chart.setPrefSize(800, 1200);
		orderbook_bar_chart.setAnimated(false); 
		orderbook_bar_chart.setCategoryGap(5);
		
		bid_features = new XYChart.Series<Number, Number>();
		ask_features = new XYChart.Series<Number, Number>();
			
	}
	

	/**
	 * Initializes book with all current price levels and sizes at each price level
	 * @param book
	 */
	@SuppressWarnings("unchecked")
	public void initiateBook(long[] bid_prices, long[] ask_prices, long[] ask_orders, long[] bid_orders, long[] bid_vol, long[] ask_vol) {
		

		bid_size = new ArrayList<XYChart.Data<Number, Number>>();
		ask_size = new ArrayList<XYChart.Data<Number, Number>>();
		
		
		for(int i = 0; i < bid_prices.length; i++) {
			
			bid_size.add(new XYChart.Data<Number, Number>(bid_prices[i], bid_vol[i], bid_orders[i]));
			ask_size.add(new XYChart.Data<Number, Number>(bid_prices[i], 0, 0));
		}
		
		for(int i = 0; i < ask_prices.length; i++) {
			
			bid_size.add(new XYChart.Data<Number, Number>(ask_prices[i], 0, 0));
			ask_size.add(new XYChart.Data<Number, Number>(ask_prices[i], ask_vol[i], ask_orders[i]));
		}
		
		bid_features.getData().addAll(bid_size);
		ask_features.getData().addAll(ask_size);
		
		orderbook_bar_chart.getData().addAll(bid_features, ask_features);
	}
	
	public void update(long[] bid_prices, long[] ask_prices, long[] ask_orders, long[] bid_orders, long[] bid_vol, long[] ask_vol) {
		
		bid_features.getData().clear();
		ask_features.getData().clear();
		
		
		bid_size = new ArrayList<XYChart.Data<Number, Number>>();
		ask_size = new ArrayList<XYChart.Data<Number, Number>>();
		
		
		for(int i = 0; i < bid_prices.length; i++) {
			
			bid_size.add(new XYChart.Data<Number, Number>(bid_prices[i], bid_vol[i], bid_orders[i]));
			ask_size.add(new XYChart.Data<Number, Number>(bid_prices[i], 0, 0));
		}
		
		for(int i = 0; i < ask_prices.length; i++) {
			
			bid_size.add(new XYChart.Data<Number, Number>(ask_prices[i], 0, 0));
			ask_size.add(new XYChart.Data<Number, Number>(ask_prices[i], ask_vol[i], ask_orders[i]));
		}
		
		bid_features.getData().addAll(bid_size);
		ask_features.getData().addAll(ask_size);
		
		
	}
}
