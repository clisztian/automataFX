package examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import tradingeconomics.Candle;
import tradingeconomics.JsonParseCandle;

public class TradingEconomicsPull {
	
	  protected final DateTimeFormatter SIMPLE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	  protected final DateTimeFormatter DATE_WITH_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	  protected final DateTimeFormatter DATE_WITH_SIMPLE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	
	private static com.google.gson.JsonParser PARSER = new com.google.gson.JsonParser();
	public static void main(String[] args) throws IOException, CsvValidationException {
		
//		getHistoricalSymbol();
//		getHistoricalMultiSymbol();
//		getHistoricalByDate();
		getHistoricalBetweenDates();
//		getHistoricalCountryIndicator();
		
	}

	public static void getHistoricalCountryIndicator() throws IOException, CsvValidationException {
	    //put country name here
	    String params = "switzerland" + "/"; 
	    params = params.replaceAll("\\s","%20");
	    //put indicator name here
	    String params1 = "gdp"; 
	    params1 = params1.replaceAll("\\s","%20");
	    //set the path
	    String path = "/historical/country" + "/" + params + "indicator/" + params1;   
	    System.out.println("Get historical data by country and indicator");
	    constructUrl (path);
	}
	
	public static void getIndicatorsByIndicator() throws IOException, CsvValidationException {
	    //put indicator name here
	    String params = "gdp"; 
	    params = params.replaceAll("\\s","%20"); 
	    //set the path
	    String path = "/country/all" + "/" + params;   
	    System.out.println("Get a list of all countries with a specific indicator");
	    constructUrl (path);
	}
	
	public static void getIndicatorsByCountry() throws IOException, CsvValidationException {
	    //put country name here
	    String params = "united states"; 
	    params = params.replaceAll("\\s","%20"); 
	    //set the path
	    String path = "/country" + "/" + params;   
	    System.out.println("Get a list of indicators by country");
	    constructUrl (path);
	}
	
	public static void constructUrl(String path) throws IOException, CsvValidationException {
		String _clientKey = "guest:guest";
		String base_url = "http://api.tradingeconomics.com";
		String auth;
		if (path.contains("?"))
		    auth = base_url + path + "&c=" + _clientKey;
		else
		    auth = base_url + path + "?c=" + _clientKey;
		
		URL obj = new URL(auth);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
		
		while ((inputLine = in.readLine()) != null) {
		    response.append(inputLine);
		}

		String json = response.toString();
//		JsonElement jsonElement = PARSER.parse(json);
//	    JsonObject rootObject = jsonElement.getAsJsonObject();
		List<Candle> inpList;
		Type type = new TypeToken<List<Candle>>(){}.getType();
        inpList = new Gson().fromJson(json, type);
        System.out.println("Size: " + inpList.size());
        
        for(Candle candle : inpList) {
        	System.out.println(candle.toString());
        }
        	      
	     System.out.println(json);
	}
	
	
//	public static  void getHistoricalSymbol() throws IOException {
//		
//		//put symbol here
//		String params = "aapl:us";
//		params = params.replaceAll("\\s","%20");
//		//set the path for the query
//		String path = "/markets/historical" + "/" + params;
//		
//		System.out.println("--------Historical markets by symbol--------");
//		constructUrl(path);
//	
//	}
	
	public static void getHistoricalMultiSymbol() throws IOException, CsvValidationException {
	
		//put symbols here
		String params = "aapl:us,indu:ind";
		params = params.replaceAll("\\s","%20");
		//set the path for the query
		String path = "/markets/historical" + "/" + params;
		
		System.out.println("--------Historical markets by multiple symbols--------");
		constructUrl(path);
	
	}
	
//	public static void getHistoricalByDate() throws IOException {
//	
//		//put symbol here
//		String params = "aapl:us";
//		params = params.replaceAll("\\s","%20");
//		//put date here (date format: yyyy-mm-dd)
//		String date = "2017-08-01";
//		//set the path for the query
//		String path = "/markets/historical" + "/" + params + "?d1=" + date;
//		
//		System.out.println("--------Historical markets by start date--------");
//		constructUrl(path);
//	
//	}
//	
	public static void getHistoricalBetweenDates() throws IOException, CsvValidationException {
	
		//put indicators name here
		String params = "SIKA:VX,GIVN:VX,UHR:VX";
		params = params.replaceAll("\\s","%20");
		//put start date here
		String start_date = "2013-08-01";
		//put end date here
		String end_date = "2020-12-22";
		//set the path for the query
		String path = "/markets/historical" + "/" + params + "?d1=" + start_date + "&d2=" + end_date;
		
		System.out.println("--------Historical markets between dates--------");
		constructUrl(path);
	
	}


}


