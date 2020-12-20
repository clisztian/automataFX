package examples;

import java.util.List;
import java.util.Map;

import org.patriques.AlphaVantageConnector;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.timeseries.Daily;
import org.patriques.output.timeseries.DailyAdjusted;
import org.patriques.output.timeseries.data.StockData;

public class AlphaDailyPull {

	private static String[] pharm_chem = new String[] {"ROG.SW", "NOVN.SW", "GIVN.SW", "DESN.SW", "SIKA.SW" };
	private static String[] construct = new String[] {"FORN.SW", "ZUGN.SW", "ZWM.SW"};
	private static String[] agric = new String[] {	"CON.SW", "ISN.SW"};
	private static String[] finance = new String[] { };
	private static String[] insur = new String[] {"SREN.SW", "ZURN.SW"};
	private static String[] food = new String[] {"BELL.SW", "LISN.SW"};
	private static String[] tourism = new String[] {"JFN.SW", "FHZN.SW", "HLEE.SW"};
	private static String[] retail = new String[] {"OFN.SW", "MOZN.SW", "VALN.SW", "DUFN.SW", "GALE.SW"};
	
	private String[] cantonal = new String[] {"BCJ.SW", "BEKN.SW", "BSKP.SW", "BLKB.SW", "BCVN.SW", "BCGE.SW", "GRKP.SW", "GLKBN.SW", "LUKN.SW", "SGKN.SW", "TKBP.SW", "WKBN.SW"};
	private String[] cantonal_names = new String[] {"Jura","Bern", "Basel", "Baselland", "Vaud", "Geneva", "Graubunden", "Glarus", "Luzern", "StGallen", "Thurgau", "Valais"};
	
	
	
	public static void main(String[] args) {
	    String apiKey = "2DCO8OV5X9H26HG3";
	    int timeout = 3000;
	    AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
	    TimeSeries stockTimeSeries = new TimeSeries(apiConnector);
	    
	    try {
	      	
	      Daily response = stockTimeSeries.daily("NOVN.SW", OutputSize.FULL);
	      Map<String, String> metaData = response.getMetaData();
	      System.out.println("Information: " + metaData.get("1. Information"));
	      System.out.println("Stock: " + metaData.get("2. Symbol"));
	      
	      List<StockData> stockData = response.getStockData();
	      stockData.forEach(stock -> {
	        System.out.println("date:   " + stock.getDateTime());
	        System.out.println("open:   " + stock.getOpen());
	        System.out.println("high:   " + stock.getHigh());
	        System.out.println("low:    " + stock.getLow());
	        System.out.println("close:  " + stock.getClose());
	        System.out.println("volume: " + stock.getVolume());
	      });
	    } catch (AlphaVantageException e) {
	      System.out.println("something went wrong");
	    }
	  }
	
}
