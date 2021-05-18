package records;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.csvreader.CsvReader;

import dataio.CSVInterface;
import examples.TableCellTextColorExample.TableData;
import graphics.SexyHistogramPlot;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Callback;
import records.RecordColumn.Type;


/**
 * An interactive table view that allows a user to 
 * control which columns are configurable into a record
 * 
 * Some example applications to be built:
 * 
 * Change which values are used for a record
 * Which value(s) are labels, and which ones are categorical/real/timestamps
 * Change the column/feature name
 * 
 * 
 * Many application ideas: 
 * Dimension reduction to learning new dimensions with tsetlin machines
 * 
 * Predict with just a click of a row
 * highlight which values used for training
 * highlight outliers
 * 
 * Basic statistics for each column with descriptiveStatistics
 * 
 * @author lisztian
 *
 */

public class CSVTableView extends TableView<Map> {

	
	private ArrayList<RecordColumn> any_columns;
	private CsvReader marketDataFeed;
	private SexyHistogramPlot analysis_view;
	
	/**
	 * The record that governs the table
	 */
	private AnyRecord anyRecord;

	/*
	 * total raw amount of columns in table (not the amount used for analytical purposes)
	 */
	private int total_columns;
	/*
	 * the raw header names to initate table if no header exists 
	 */
	private String[] raw_headers;

	/*
	 * the field names used for the record, this is a subset of raw_headers 
	 */
	private String[] field_names;
	private boolean with_header;

	/**
	 * A menu to change the state of a column
	 */
	private final String contextSyle = ".root {\n" + 
			"  -fx-background-color: rgb(35,25,25,.8); \n" + 
			"  -fx-padding: 3;\n" + 
			"}\n" + 
			"\n" + 
			".context-menu {\n" + 
			"  -fx-background-color: rgb(15,15,15,.6);\n" + 
			"  -fx-text-fill: white;\n" + 
			"}\n" + 
			"\n" + 
			".menu-item .label {\n" + 
			"  -fx-text-fill: yellow;\n" + 
			"}\n" + 
			"\n" + 
			".menu-item:focused .label {\n" + 
			"  -fx-text-fill: white;\n" + 
			"}";


	/**
	 * If .csv file already has a usable descriptive header, simply 
	 * create anyRecord with from a csv instance and build table
	 * @param file_name
	 * @throws IOException
	 */
	public CSVTableView(String file_name) throws IOException {
		
		

		any_columns = new ArrayList<RecordColumn>();
		
		this.with_header = false;
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(file_name).getFile());

		marketDataFeed = new CsvReader(file.getAbsolutePath());		
		marketDataFeed.readHeaders();
		
		//find out how many values total
	
		field_names = marketDataFeed.getHeaders();
		total_columns = field_names.length;
		raw_headers = field_names;
		
//		for(int i = 0; i < total_columns; i++) {
//			System.out.println(field_names[i]);
//		}
		

		if(marketDataFeed.readRecord()) {
			String[] raw_vals = marketDataFeed.getValues();
		
		
//			for(int i = 0; i < raw_vals.length; i++) {
//				System.out.println(raw_vals[i]);
//			}
			
					
			boolean[] categorical = new boolean[total_columns];
			for(int i = 0; i < total_columns; i++) {
				
				if(NumberUtils.isCreatable(raw_vals[i].trim())) {
					categorical[i] = false;
				}
				else {
					categorical[i] = true;
				}
			}
	
			/**
			 * Now create a default record with the raw headers
			 */
			anyRecord = createRecord(field_names, categorical);
	
			
			/**
			 * Setup the table
			 */		
			for(int i = 0; i < raw_vals.length; i++) {	
	
				RecordColumn column = new RecordColumn(field_names[i], categorical[i]);		
				column.setCellValueFactory(new MapValueFactory<>(field_names[i]));			
				any_columns.add(column);
			}
			
				
			getColumns().addAll(any_columns);
			setEditable(false);
			
			getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			//getStylesheets().add("css/TransactionChart.css");
			getStylesheets().add(getClass().getClassLoader().getResource("css/TransactionChart.css").toExternalForm());
			setPlaceholder(new Text("Loading expenses..."));
			
		    final KeyCodeCombination keyCodeCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
		    setOnKeyPressed(event -> {	
		            if (keyCodeCopy.match(event)) {	
		                copySelectionToClipboard();	
		            }	
		    });
		    autoResizeColumns();
	    
		    Callback<RecordColumn, TableCell<Map, Object>> call = new Callback<RecordColumn, TableCell<Map, Object>>() {
		        public TableCell<Map, Object> call(RecordColumn param) {
		            return new TableCell<Map, Object>() {
	
		                @Override
		                public void updateItem(Object item, boolean empty) {
		                    super.updateItem(item, empty);
		                    if (!isEmpty() && item != null) {
		                    	
		                    	if(param.getColumn_type() == Type.REAL) this.setTextFill(Color.rgb(0, 237, 229));
		                    	else if(param.getColumn_type() == Type.CATEGORY) this.setTextFill(Color.rgb(0, 237, 8));
		                    	else if(param.getColumn_type() == Type.TIME) this.setTextFill(Color.GOLDENROD.brighter().brighter());
		                    	else if(param.getColumn_type() == Type.INFO) this.setTextFill(Color.LIGHTSLATEGREY.brighter());
		                    	else if(param.getColumn_type() == Type.REAL_LABEL) this.setTextFill(Color.CORNFLOWERBLUE);
		                    	else if(param.getColumn_type() == Type.CLASS_LABEL) this.setTextFill(Color.rgb(255, 0, 60));
		                    	
		                        setText(item.toString());
		                    }
		                }
		            };
		        }
		    };
		    
		    
		    for(int i = 0; i < any_columns.size(); i++) {
		    	any_columns.get(i).buildColumnFactory(call);
		    	any_columns.get(i).setContextMenu(new RecordContextMenu(any_columns.get(i), this));
		    }
		    
		    analysis_view = new SexyHistogramPlot();
		}
	}
	
	/**
	 * Instantiate a new table given a file (.csv)
	 * Every row in file is assumed to be comma deliminated (will open for tab or semicolon later)
	 * With_header if true, first line in file is column names, else, no header exists and a default header will be build
	 * 
	 * @param file_name
	 * @param with_header 
	 * @throws IOException
	 */
	public CSVTableView(String file_name, boolean with_header) throws IOException {
		
		any_columns = new ArrayList<RecordColumn>();
		
		this.with_header = false;
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(file_name).getFile());

		marketDataFeed = new CsvReader(file.getAbsolutePath());		
		marketDataFeed.readHeaders();
		
		//find out how many values total
	
		String[] _headers = marketDataFeed.getHeaders();
		total_columns = _headers.length;

		raw_headers = new String[total_columns];
		for(int i = 0; i < total_columns; i++) {
			
			//System.out.print(_headers[i] + " ");
			//if a numerical value, make it Fi
			if(NumberUtils.isCreatable(_headers[i].trim())) {
				raw_headers[i] = "F" + i;
			}
			else {
				raw_headers[i] = "cat_C" + i;
			}
		}
		System.out.println();
		
		/**
		 * Now create a default record with the raw headers
		 */
		anyRecord = createRecord(raw_headers);
		field_names = anyRecord.getField_names();
		
		/**
		 * Setup the table
		 */
		
		for(int i = 0; i < raw_headers.length; i++) {	

			RecordColumn column = new RecordColumn(raw_headers[i]);		
			column.setCellValueFactory(new MapValueFactory<>(raw_headers[i]));			
			any_columns.add(column);
		}
		
			
		getColumns().addAll(any_columns);
		setEditable(false);
		
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		//getStylesheets().add("css/TransactionChart.css");
		getStylesheets().add(getClass().getClassLoader().getResource("css/TransactionChart.css").toExternalForm());
		setPlaceholder(new Text("Loading expenses..."));
		
	    final KeyCodeCombination keyCodeCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
	    setOnKeyPressed(event -> {	
	            if (keyCodeCopy.match(event)) {	
	                copySelectionToClipboard();	
	            }	
	    });
	    autoResizeColumns();
    
	    Callback<RecordColumn, TableCell<Map, Object>> call = new Callback<RecordColumn, TableCell<Map, Object>>() {
	        public TableCell<Map, Object> call(RecordColumn param) {
	            return new TableCell<Map, Object>() {

	                @Override
	                public void updateItem(Object item, boolean empty) {
	                    super.updateItem(item, empty);
	                    if (!isEmpty()) {
	                    	
	                    	if(param.getColumn_type() == Type.REAL) this.setTextFill(Color.rgb(0, 237, 229));
	                    	else if(param.getColumn_type() == Type.CATEGORY) this.setTextFill(Color.rgb(0, 237, 8));
	                    	else if(param.getColumn_type() == Type.TIME) this.setTextFill(Color.GOLDENROD.brighter().brighter());
	                    	else if(param.getColumn_type() == Type.INFO) this.setTextFill(Color.LIGHTSLATEGREY.brighter());
	                    	else if(param.getColumn_type() == Type.REAL_LABEL) this.setTextFill(Color.CORNFLOWERBLUE);
	                    	else if(param.getColumn_type() == Type.CLASS_LABEL) this.setTextFill(Color.rgb(255, 0, 60));
	                    	
	                        setText(item.toString());
	                    }
	                }
	            };
	        }
	    };
	    
	    
	    for(int i = 0; i < any_columns.size(); i++) {
	    	any_columns.get(i).buildColumnFactory(call);
	    	any_columns.get(i).setContextMenu(new RecordContextMenu(any_columns.get(i), this));
	    }
	    
	    analysis_view = new SexyHistogramPlot();
	}
	


	public void setSelectionListener() {
		
		getSelectionModel().getSelectedItems().addListener(
	            new ListChangeListener<Map>() {

				public void onChanged( 
	               ListChangeListener.Change<? extends Map> c) {
					AnyRecord record = mapToRecord(getSelectionModel().getSelectedItems().get(0)); 
					
					System.out.println(record.toString());
				}
	    });
		
	}
	
	
	private AnyRecord mapToRecord(Map<String, Object> map) {
		
		AnyRecord myrec = new AnyRecord();
		
		Object[] vals = new Object[anyRecord.getField_names().length];
		for(int i = 0; i < anyRecord.getField_names().length; i++) {
			vals[i] = map.get(anyRecord.getField_names()[i]);
		}
		myrec.setValues(vals);
		myrec.setType(anyRecord.getType());
				
		return myrec;
		
	}
	
	
	/**
	 * Once the columns have been selected with their appropriate types, 
	 * a record will be created that will govern what an observation record
	 * will look like
	 */
	public void commitRecord() {
		
		anyRecord = new AnyRecord();
		
		Type[] anyTypes = new Type[any_columns.size()];
		String[] field_names = new String[any_columns.size()];
		
		for(int i = 0; i < any_columns.size(); i++) {
			
			anyTypes[i] = any_columns.get(i).getColumn_type();
			field_names[i] = any_columns.get(i).getName();
		}
		
		anyRecord.setType(anyTypes);
		anyRecord.setField_names(field_names);
				
	}
	
	
		
	
	
	public void fillTable() throws IOException {
	    addRecords(getAllRecords());		
	}
	
	
	/**
	 * Create a record given the following headers
	 * @param headers
	 * @return
	 */
	public static AnyRecord createRecord(String[] headers) {
		
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
	 * Create a record given the following headers
	 * @param headers
	 * @return
	 */
	public static AnyRecord createRecord(String[] headers, boolean[] categorical) {
		
		AnyRecord record = new AnyRecord();
		
		String[] fields = new String[headers.length];
		Object[] values = new Object[headers.length];
		
		Type[] types = new Type[headers.length];
		
		for(int i = 0; i < headers.length; i++) {
			
			if(categorical[i]) {
				fields[i] = headers[i];
				values[i] = new String("categorical");
				types[i] = Type.CATEGORY;
			}
			else {
				fields[i] = headers[i];
				values[i] = (Float)0f;		
				types[i] = Type.REAL;
			}
		}
		record.setField_names(fields);
		record.setValues(values);
		record.setType(types);
		
		return record;		
	}	
	

	
	
	public  List<Field> getPrivateFields(Class<?> theClass){
        LinkedList<Field> privateFields = new LinkedList<Field>();

        Field[] fields = theClass.getDeclaredFields();

        for(Field field:fields){
        	privateFields.add(field);
        }
        return privateFields;
    }
	
	public void addRecord(AnyRecord any) {
		
		Map<String, Object> item = new HashMap<>();
		for(int i = 0; i < raw_headers.length; i++) {	
			item.put(raw_headers[i], any.getValues()[i]);
		}	
		getItems().add(item);
	}
	
	public void addRecords(ArrayList<AnyRecord> records) {
		
		ObservableList<Map<String, Object>> items =
			    FXCollections.<Map<String, Object>>observableArrayList();

		for(AnyRecord record : records) {
			
			Map<String, Object> item = new HashMap<>();
			for(int i = 0; i < raw_headers.length; i++) {	
				item.put(raw_headers[i], record.getValues()[i]);
			}	
			items.add(item);
		}
				
		getItems().addAll(items);
	}
	
	
	/**
	 * Gets all the records in the csv file as an arraylist
	 * @return
	 * @throws IOException
	 */
	public ArrayList<AnyRecord> getAllRecords() throws IOException {
		
		ArrayList<AnyRecord> all = new ArrayList<AnyRecord>();
		while (marketDataFeed.readRecord()) {
			
			AnyRecord rec = getOneRecord();
			all.add(rec);			
		}
		return all;	
	}
	
	
	
	private AnyRecord getOneRecord() throws IOException {
		
		if(raw_headers == null) {
			return null;
		}
				
		AnyRecord anyRecord = new AnyRecord();
		Object[] values = new Object[raw_headers.length];
		

		for(int i = 0; i < raw_headers.length; i++) {
			
			String val = marketDataFeed.get(i);
			if(NumberUtils.isCreatable(val.trim())) {
				values[i] = Float.parseFloat(val.trim());
			}
			else {
				values[i] = val;
			}
		}
		
		
		anyRecord.setValues(values);
		return anyRecord;	
	}
	
	


	
	
	
	/**
	 * Copies any selected rows to clipboard for export to another file
	 */
	public void copySelectionToClipboard() {

        final Set<Integer> rows = new TreeSet<>();

        for (final TablePosition tablePosition : getSelectionModel().getSelectedCells()) {
            rows.add(tablePosition.getRow());
        }

        final StringBuilder strb = new StringBuilder();
        boolean firstRow = true;
        for (final Integer row : rows) {
              if(firstRow) {  
                    boolean firstCol = true;
                    for (final TableColumn<?, ?> column : getColumns()) {
                    if (!firstCol) {
                        strb.append('\t');
                    }
                    firstCol = false;
                    String headername = column.getText();
                    strb.append(headername == null ? "" : headername);
                }
                     strb.append('\n');
              }
           
            if (!firstRow) {
                strb.append('\n');
            }
            firstRow = false;
            boolean firstCol = true;
            for (final TableColumn<?, ?> column : getColumns()) {

                if (!firstCol) {
                    strb.append('\t');
                }
                firstCol = false;
                final Object cellData = column.getCellObservableValue(row).getValue();
                if(cellData == null) {
                     strb.append("");
                }
                else {
                     if (cellData instanceof Double) {
                           strb.append(cellData);
                     }
                     else if (cellData instanceof Integer) {
                           strb.append(cellData);
                     }
                     else {
                           strb.append(cellData.toString());
                     }           
                }
            }
        }

        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(strb.toString());
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }
	
	public void autoResizeColumns() {

        setColumnResizePolicy( TableView.UNCONSTRAINED_RESIZE_POLICY);
        getColumns().stream().forEach( (column) -> {
            Text t = new Text( column.getText() );
            double max = t.getLayoutBounds().getWidth();
            column.setPrefWidth( max + max*.90);
        } );
    }
	
	
	
	class RecordContextMenu extends ContextMenu {

        public RecordContextMenu(RecordColumn record_column, CSVTableView my_table) {

        	MenuItem change_to_info = new MenuItem("Set to Info");
        	MenuItem change_to_real = new MenuItem("Set to Numerical");
        	MenuItem change_to_time = new MenuItem("Set to DateTime");
        	MenuItem change_to_cat = new MenuItem("Set to Categorical");
        	MenuItem change_to_class = new MenuItem("Set to Class Label");
        	MenuItem change_to_num_label = new MenuItem("Set Numerical Label");
        	MenuItem analyze_column = new MenuItem("Analyze Column");
        	
    		change_to_info.setOnAction(event -> {
    			record_column.setColumn_type(RecordColumn.Type.INFO);    		
    			commitRecord();
    			event.consume();
    			my_table.refresh();
            });
    		change_to_real.setOnAction(event -> {
    			record_column.setColumn_type(RecordColumn.Type.REAL);
    			commitRecord();
    			event.consume();
    			my_table.refresh();
            });
    		change_to_time.setOnAction(event -> {
    			record_column.setColumn_type(RecordColumn.Type.TIME);
    			commitRecord();
    			event.consume();
    			my_table.refresh();
            });
    		change_to_cat.setOnAction(event -> {
    			record_column.setColumn_type(RecordColumn.Type.CATEGORY);
    			commitRecord();
    			event.consume();
    			my_table.refresh();
    		});
    		change_to_class.setOnAction(event -> {
    			record_column.setColumn_type(RecordColumn.Type.CLASS_LABEL);
    			commitRecord();
    			event.consume();
    			my_table.refresh();
    		});
    		change_to_num_label.setOnAction(event -> {
    			record_column.setColumn_type(RecordColumn.Type.REAL_LABEL);
    			commitRecord();
    			event.consume();
    			my_table.refresh();
    		});
    		analyze_column.setOnAction(event -> {
    			
    			if(record_column.getColumn_type() == RecordColumn.Type.REAL || record_column.getColumn_type() == RecordColumn.Type.REAL_LABEL) {
    				
    				DescriptiveStatistics columnData = new DescriptiveStatistics();	
    				
    				for (Map item : my_table.getItems()) {
    					
    					if(NumberUtils.isCreatable(record_column.getCellObservableValue(item).getValue().toString())) {
    						Float myval = (Float)record_column.getCellObservableValue(item).getValue();
    						columnData.addValue(myval);
    					}
    				}
    				analysis_view.show();
    				
    				Platform.runLater(() -> {
    					analysis_view.plot(columnData, record_column.getName());
    				});
    				
    			}
    			
    		});
    		    		
    		getItems().addAll(change_to_info, change_to_real, change_to_time, change_to_cat, change_to_class, change_to_num_label, analyze_column);  	
        	setStyle(contextSyle);
        }

    }
	
	
}