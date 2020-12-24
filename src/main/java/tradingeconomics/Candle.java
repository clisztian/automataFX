package tradingeconomics;

import com.google.gson.annotations.SerializedName;

public class Candle {

	@SerializedName("Symbol")
	private String symbol;
	
	@SerializedName("Date")
	private String date;
	
	@SerializedName("Open")
	private double open;
	
	@SerializedName("High")
	private double high;
	
	@SerializedName("Low")
	private double low;
	
	@SerializedName("Close")
	private double close;
	
	public Candle(String symbol, String date, double open, double high, double low, double close) {
		
		this.symbol = symbol;
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
	}
	
	
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public double getClose() {
		return close;
	}
	public void setClose(double close) {
		this.close = close;
	}
	

	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append(symbol + " date: " + date + ", open: " + open);
		return sb.toString();
	}
 	
	
}
