package examples;

import java.util.List;
import java.util.Map;

import org.patriques.AlphaVantageConnector;
import org.patriques.SectorPerformances;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.sectorperformances.Sectors;
import org.patriques.output.sectorperformances.data.SectorData;
import org.patriques.output.timeseries.Daily;
import org.patriques.output.timeseries.DailyAdjusted;
import org.patriques.output.timeseries.data.StockData;

public class MySectorPerformances {
	  public static void main(String[] args) {
	    String apiKey = "2DCO8OV5X9H26HG3";
	    int timeout = 3000;
	    AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
	    SectorPerformances sectorPerformances = new SectorPerformances(apiConnector);

	    try {
	      Sectors response = sectorPerformances.sector();
	      Map<String, String> metaData = response.getMetaData();
	      System.out.println("Information: " + metaData.get("Information"));
	      System.out.println("Last Refreshed: " + metaData.get("Last Refreshed"));

	      List<SectorData> sectors = response.getSectors();
	      sectors.forEach(data -> {
	        System.out.println("key:           " + data.getKey());
	        System.out.println("Consumer Discretionary: " + data.getConsumerDiscretionary());
	        System.out.println("Consumer Staples:       " + data.getConsumerStaples());
	        System.out.println("Energy:                 " + data.getEnergy());
	        System.out.println("Financials:             " + data.getFinancials());
	        System.out.println("Health Care:            " + data.getHealthCare());
	        System.out.println("Industrials:            " + data.getIndustrials());
	        System.out.println("Information Technology: " + data.getInformationTechnology());
	        System.out.println("Materials:              " + data.getMaterials());
	        System.out.println("Real Estate:            " + data.getRealEstate());
	      });
	    } catch (AlphaVantageException e) {
	      System.out.println("something went wrong");
	    }
	  }
}