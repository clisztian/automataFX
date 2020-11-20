package dataio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import com.csvreader.CsvReader;

import records.AnyRecord;

/**
 * Util for managing reading from csv files
 * 
 * 	 * 1) Anything with 'time' or 'date' in the string will be a String later converted to a Temporal TimeEncoder
	 * 2) Anything with 'category_' will be categorical string value
	 * 3) All else will be a floating point value (float, double, integer)
 * 
 * @author lisztian
 *
 */
public class CSVInterface {

	private CsvReader marketDataFeed;
	private AnyRecord anyRecord;
	private String[] headers;
	
	/**
	 * Instantiates a CSV reader and for a given file name and produces the 
	 * record template for the record encoder
	 * assertArrayEquals(expected, enc);
	 * @param file_name
	 * @throws IOException 
	 */
	public CSVInterface(String file_name) throws IOException {
		
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(file_name).getFile());

		marketDataFeed = new CsvReader(file.getAbsolutePath());		
		marketDataFeed.readHeaders();
		headers = marketDataFeed.getHeaders();

		anyRecord = createRecord();
	}

	
	/**
	 * Creates a record template given a string of headers. The default rules are as follow:
	 * 
	 * 1) Anything with 'time' or 'date' in the string will be a String later converted to a Temporal TimeEncoder
	 * 2) Anything with 'category_' will be categorical string value
	 * 3) All else will be a floating point value (float, double, integer)
	 * 
	 * @param headers
	 * @return
	 */
	public AnyRecord createRecord() {
		
		AnyRecord record = new AnyRecord();
		
		String[] fields = new String[headers.length];
		Object[] values = new Object[headers.length];
		
		for(int i = 0; i < headers.length; i++) {
			
			if(headers[i].contains("category_") || headers[i].contains("cat_")) {
				fields[i] = new String(headers[i].split("[_]+")[1]);
				values[i] = new String("categorical");
			}
			else if(headers[i].contains("time") || headers[i].contains("date")) {
				fields[i] = headers[i];
				values[i] = new String("time");
			}
			else {
				fields[i] = headers[i];
				values[i] = (Float)0f;		
			}
		}
		record.setField_names(fields);
		record.setValues(values);
		
		return record;
			
	}
	
	/**
	 * Get the next n records in csv, if they exist
	 * If less than n exist the final records will be null
	 * @param n
	 * @return
	 * @throws IOException
	 */
	public AnyRecord[] getNextRecords(int n) throws IOException {
		
		AnyRecord[] records = new AnyRecord[n];
		
		int count = 0;
		while (marketDataFeed.readRecord()) {
		
			records[count] = getOneRecord();
			count++;
			
			if(count == n) break;
		}
		
		return records;			 		
	}
	
	/**
	 * Gets all the records in the csv file as an arraylist
	 * @return
	 * @throws IOException
	 */
	public ArrayList<AnyRecord> getAllRecords() throws IOException {
		
		ArrayList<AnyRecord> all = new ArrayList<AnyRecord>();
		while (marketDataFeed.readRecord()) {		
			all.add(getOneRecord());			
		}
		return all;	
	}
	

	/**
	 * Gets the next record in the file
	 * @return
	 * @throws IOException
	 */
	public AnyRecord getRecord() throws IOException {
		
		if(headers == null) {
			return null;
		}
		
		if(!marketDataFeed.readRecord()) {
			return null;
		}
		
		AnyRecord anyRecord = new AnyRecord();
		Object[] values = new Object[headers.length];
		

		for(int i = 0; i < headers.length; i++) {
			
			if(headers[i].contains("category_") || headers[i].contains("cat_")) {
				values[i] = marketDataFeed.get(headers[i]);
			}
			else if(headers[i].contains("time") || headers[i].contains("date")) {
				values[i] = marketDataFeed.get(headers[i]);
			}
			else {
				values[i] = Float.parseFloat(marketDataFeed.get(headers[i]));	
			}
		}
		anyRecord.setValues(values);
		
		
		return anyRecord;	
	}
	
	private AnyRecord getOneRecord() throws IOException {
		
		if(headers == null) {
			return null;
		}
				
		AnyRecord anyRecord = new AnyRecord();
		Object[] values = new Object[headers.length];
		

		for(int i = 0; i < headers.length; i++) {
			
			if(headers[i].contains("category_") || headers[i].contains("cat_")) {
				values[i] = marketDataFeed.get(headers[i]);
			}
			else if(headers[i].contains("time") || headers[i].contains("date")) {
				values[i] = marketDataFeed.get(headers[i]);
			}
			else {
				values[i] = Float.parseFloat(marketDataFeed.get(headers[i]));	
			}
		}
		anyRecord.setValues(values);
		
		
		return anyRecord;	
	}
	
	
	public AnyRecord getAnyRecord() {
		return anyRecord;
	}

	public void setAnyRecord(AnyRecord anyRecord) {
		this.anyRecord = anyRecord;
	}

	public CsvReader getMarketDataFeed() {
		return marketDataFeed;
	}

	public void setMarketDataFeed(CsvReader marketDataFeed) {
		this.marketDataFeed = marketDataFeed;
	}
	
	public void close() {
		marketDataFeed.close();
	}

	
	public static void main(String[] args) throws IOException {
		
		
		CSVInterface csv = new CSVInterface("data/test_data.csv");	
		AnyRecord anyrecord = csv.createRecord();
		
		ArrayList<AnyRecord> records = csv.getAllRecords();
		
		for(AnyRecord record : records) {
			System.out.println(record.toString());
		}
		
		csv.close();
		
		
		
	}
	
	
}
