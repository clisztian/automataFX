package records;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;


import com.csvreader.CsvReader;

import controls.EmbeddingPanel;
import graphics.SexyCategoryChart;
import graphics.SexyHistogramPlot;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.chart.XYChart;
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
import output.CategoryLabel;
import output.OutputLabel;
import output.RealLabel;
import records.RecordColumn.Type;
import utils.DataPair;
import utils.MutableInt;

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

	
	private EmbeddingPanel embedding_panel;
	
	private ArrayList<RecordColumn> any_columns;
	private CsvReader marketDataFeed;
	private SexyHistogramPlot analysis_view;
	private SexyCategoryChart category_analysis_view;
	
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

	private final Color start = Color.rgb(177, 235, 252);
	private final Color end = Color.rgb(217, 24, 185);
	
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
	
	private MenuItem embed_data;
	
	private ContextMenu data_context_menu;
	
	private HashMap<String, RecordColumn> column_map;



	private ArrayList<OutputLabel> output_labels;



	
	
	public CSVTableView() {
		
	}

	public void createTable(File file) throws IOException {
		
		with_header = false;
		try {
			with_header = checkIfHeaderExists(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		any_columns = new ArrayList<RecordColumn>();
		marketDataFeed = new CsvReader(file.getAbsolutePath());		
		marketDataFeed.readHeaders();
		
		
		if(with_header) {
			
			field_names = marketDataFeed.getHeaders();
			total_columns = field_names.length;
			raw_headers = field_names;
			
			if(marketDataFeed.readRecord()) {
				
				String[] raw_vals = marketDataFeed.getValues();
			
						
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
			
			}
					
		}
		else {
			
			String[] _headers = marketDataFeed.getHeaders();
			total_columns = _headers.length;

			raw_headers = new String[total_columns];
			for(int i = 0; i < total_columns; i++) {
				
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
			
			for(int i = 0; i < raw_headers.length; i++) {	

				RecordColumn column = new RecordColumn(raw_headers[i]);		
				column.setCellValueFactory(new MapValueFactory<>(raw_headers[i]));			
				any_columns.add(column);
			}
			
		}
		
		getColumns().addAll(any_columns);
		setEditable(false);
		
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		getStylesheets().add(getClass().getClassLoader().getResource("css/TransactionChart.css").toExternalForm());
		setPlaceholder(new Text("Loading expenses..."));
		
	    final KeyCodeCombination keyCodeCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
	    setOnKeyPressed(event -> {	
	            if (keyCodeCopy.match(event)) {	
	                copySelectionToClipboard();	
	            }	
	    });
	  
    
	    Callback<RecordColumn, TableCell<Map, Object>> call = new Callback<RecordColumn, TableCell<Map, Object>>() {
	        public TableCell<Map, Object> call(RecordColumn param) {
	            return new TableCell<Map, Object>() {

	                @Override
	                public void updateItem(Object item, boolean empty) {
	                    super.updateItem(item, empty);
	                    if (!isEmpty() && item != null) {
	                    	
	                    	if(param.getColumn_type() == Type.REAL) this.setTextFill(start);
	                    	else if(param.getColumn_type() == Type.CATEGORY) this.setTextFill(start.interpolate(end, .3f));
	                    	else if(param.getColumn_type() == Type.TIME) this.setTextFill(start.interpolate(end, .6f));
	                    	else if(param.getColumn_type() == Type.INFO) this.setTextFill(Color.LIGHTSLATEGREY.brighter());
	                    	else if(param.getColumn_type() == Type.REAL_LABEL) this.setTextFill(start.interpolate(end, 1f));
	                    	else if(param.getColumn_type() == Type.CLASS_LABEL) this.setTextFill(start.interpolate(end, .9f));
	                    	
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
	    
	    
	    buildAnalyticsTables();
		
	}
	
	/**
	 * Builds an assortment of tables used for analytics
	 */
	private void buildAnalyticsTables() {
		
		
		analysis_view = new SexyHistogramPlot();
	    analysis_view.setUnderyling_data(getItems());
	    category_analysis_view = new SexyCategoryChart();
	    installMenu();
	    commitRecord();
		
	}
	
	public void setEmbeddingPanel(EmbeddingPanel embedding_panel) {
		this.embedding_panel = embedding_panel;		
	}
	
	
	/**
	 * Check if file has header or not
	 * If there is a numerical value in the first line of the file, 
	 * assumes that there is no header
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	private boolean checkIfHeaderExists(File file) throws IOException {
		
		System.out.println(file.toString());
		BufferedReader br = new BufferedReader(new FileReader(file));  //creates a buffering character input stream  
		
		StringBuffer sb=new StringBuffer();    //constructs a string buffer with no characters  
		String[] line = br.readLine().split("[,]+");
	
		
		for(int i = 0; i < line.length; i++) {			
			if(NumberUtils.isCreatable(line[i].trim())) {
				br.close();
				return false;
			}			
		}
		br.close();
		return true;
	}
	

	/**
	 * Create and install menu on datatable
	 */
	private void installMenu() {
		
		
		embed_data = new MenuItem("Create Embedding");
		data_context_menu = new ContextMenu();
		
		embed_data.setOnAction(event -> {
			
			DataPair data = getDataPairFromRecords();	
			System.out.println("Length: " + data.getData().length);
			
			if(data != null) {
				embedding_panel.createMap(data, getSelectionModel().getSelectedItems());
			}			
			event.consume();			
        });
		
		data_context_menu.getItems().add(embed_data);
		data_context_menu.setStyle(contextSyle);
		
		setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() { 
            @Override
            public void handle(ContextMenuEvent event) {
            	data_context_menu.show(getScene().getWindow(),event.getScreenX(), event.getScreenY());
            }
        });
	}
	
	

	/**
	 * Creates a record for any change in selection in the table
	 * Can be used for prediction, viewing analytics, or any other output
	 */
	public void setSelectionListener() {
		
		getSelectionModel().getSelectedItems().addListener(
	            new ListChangeListener<Map>() {

				public void onChanged( 
	               ListChangeListener.Change<? extends Map> c) {
					
					if(getSelectionModel().getSelectedItems().size() > 0) {
						AnyRecord record = mapToRecord(getSelectionModel().getSelectedItems().get(0)); 
					}
				}
	    });
		
	}
	
	
	/**
	 * Returns the currently selected rows into records 
	 * @return
	 */
	public ArrayList<AnyRecord> selectedRecords() {
		
		ObservableList<Map> mymap = getSelectionModel().getSelectedItems();
		ArrayList<AnyRecord> record_list = new ArrayList<AnyRecord>();
			
		if(mymap.size() > 0) {
			for(Map map : mymap) record_list.add(mapToRecord(map));
		}		
		return record_list;
	}
	
	/**
	 * Returns the current non selected rows into records
	 * @return
	 */
	public ArrayList<AnyRecord> getNonSelectedRecords() {
		
		Collection<Map> mymap = CollectionUtils.subtract(getItems(), getSelectionModel().getSelectedItems());
		ArrayList<AnyRecord> record_list = new ArrayList<AnyRecord>();
		
		if(mymap.size() > 0) {
			for(Map map : mymap) record_list.add(mapToRecord(map));
		}		
		return record_list;
	}
	
	
	/**
	 * Maps a generic map into a record given the AnyRecord structure
	 * 
	 * 
	 * TODO: Method to handle null/empty values
	 * Options are randomly sample from existing
	 * Take median/mean
	 * 
	 * @param map
	 * @return
	 */
	public AnyRecord mapToRecord(Map<String, Object> map) {
		
		AnyRecord myrec = new AnyRecord();
		
		Map<String, Object> label_map = new HashMap<String, Object>();
		
		Object[] vals = new Object[anyRecord.getField_names().length];
		for(int i = 0; i < anyRecord.getField_names().length; i++) {
			vals[i] = map.get(anyRecord.getField_names()[i]);
			
			if(anyRecord.getType()[i] == Type.REAL_LABEL || anyRecord.getType()[i] == Type.CLASS_LABEL) {
				label_map.put(anyRecord.getField_names()[i], vals[i]);
			}		
		}
		myrec.setValues(vals);
		myrec.setType(anyRecord.getType());
		myrec.setMap(label_map);
				
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
	
	
	
	/**
	 * Grabs data for the embedding algorithm. Uses the current committed data structure
	 * @return
	 */
	public DataPair getDataPairFromRecords() {
		
		encodeCategoryColumns();
			
		int n_feat = 0; 
		for(int i = 0; i < anyRecord.getType().length; i++) {
			
			if(anyRecord.getType()[i] == Type.CATEGORY) {
				n_feat++;
			}
			else if(anyRecord.getType()[i] == Type.REAL) {
				n_feat++;
			}
			else if(anyRecord.getType()[i] == Type.TIME) {
				n_feat++;
			}
		}
		

		ObservableList<Map> mymap = getSelectionModel().getSelectedItems();	
		
		if(mymap.size() > 0) {
		
			float[][] umap_vals = new float[mymap.size()][];
			
			int count = 0;
			for(Map map : mymap) {
				
				AnyRecord record = mapToRecord(map);
				umap_vals[count] = new float[n_feat];
	
				int i_maps = 0;
				for(int i = 0; i < anyRecord.getType().length; i++) {
					
					if(record.getType()[i] == Type.CATEGORY) {
						umap_vals[count][i_maps] = 1f*column_map.get(anyRecord.getField_names()[i]).getCategory_map().get(record.getValues()[i].toString());
						i_maps++;
					}
					else if(record.getType()[i] == Type.REAL) {
						umap_vals[count][i_maps] = (Float)record.getValues()[i];
						i_maps++;
					}
					else if(record.getType()[i] == Type.TIME) {
						
					}
				}	
				count++;
			}
			int[] labels = new int[mymap.size()];
			return new DataPair(umap_vals, labels);
		}
		
		return null;
		
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
    			else if(record_column.getColumn_type() == RecordColumn.Type.CATEGORY || record_column.getColumn_type() == RecordColumn.Type.CLASS_LABEL) {
    				
    				HashMap<String, MutableInt> category_map = new HashMap<String, MutableInt>();
    				
    				for (Map item : my_table.getItems()) {
    					
    					String myval = record_column.getCellObservableValue(item).getValue().toString();
    					
    					MutableInt mi = category_map.get(myval);
    					if(mi == null) {
    						category_map.put(myval, new MutableInt());
    					}
    					else mi.increment();
    				}
    				
    				category_analysis_view.show();
    				Platform.runLater(() -> {
    					category_analysis_view.plot(category_map, record_column.getName());   
    					setMouseInteraction();
    				});
    				
    			}
    			
    		});
    		    		
    		getItems().addAll(change_to_info, change_to_real, change_to_time, change_to_cat, change_to_class, change_to_num_label, analyze_column);  	
        	setStyle(contextSyle);
        }

    }
	
	/**
	 * Find all the label columns and create an Automata System for the record -> label pairing
	 */
	public void buildAutomataSystem() {
		
		output_labels = new ArrayList<OutputLabel>();
		
		for(TableColumn record_column : getColumns()) {
			
			if(record_column instanceof RecordColumn)  {
				
				if(((RecordColumn)record_column).getColumn_type() == RecordColumn.Type.REAL_LABEL) {
					
					DescriptiveStatistics columnData = new DescriptiveStatistics();	
					
					for (Map item : getItems()) {
						
						if(NumberUtils.isCreatable(record_column.getCellObservableValue(item).getValue().toString())) {
							Float myval = (Float)record_column.getCellObservableValue(item).getValue();
							columnData.addValue(myval);
						}
					}
					
					RealLabel real_label = new RealLabel(10, (float)columnData.getMin(), (float)columnData.getMax());
					real_label.setStats(columnData);
					real_label.setRecordColumn(((RecordColumn)record_column));
					
					output_labels.add(real_label);
				}
				else if(((RecordColumn)record_column).getColumn_type() == RecordColumn.Type.CLASS_LABEL) {
					
					
					HashMap<String, MutableInt> category_map = new HashMap<String, MutableInt>();
    				
    				for (Map item : getItems()) {
    					
    					String myval = ((RecordColumn)record_column).getCellObservableValue(item).getValue().toString();
    					
    					MutableInt mi = category_map.get(myval);
    					if(mi == null) {
    						category_map.put(myval, new MutableInt());
    					}
    					else mi.increment();
    				}
    				
    				SortedSet<Map.Entry<String, MutableInt>> sort_cats = entriesSortedByValues(category_map);
    				HashMap<String, Integer> mymap = new HashMap<String, Integer>();
    				
    				int count = 0;
    				for(Map.Entry<String, MutableInt> ent : sort_cats) {
    					mymap.put(ent.getKey(), count);
    					count++;
    				}
    				((RecordColumn)record_column).setCategory_map(mymap);
					
    				CategoryLabel cat_label = new CategoryLabel(mymap);
    				cat_label.setRecordColumn(((RecordColumn)record_column));
    				
    				output_labels.add(cat_label);
    				
				}
				
			}
		}
		
		
		
		
	}
	
	
	
	/**
	 * Sets the mouse interaction for the categories to plot conditionally
	 */
	private void setMouseInteraction() {
		
		for(XYChart.Data<String, Number> d : category_analysis_view.getSexy_bar_chart().getData().get(0).getData()) {
			
			
			d.getNode().setOnMouseEntered(e -> {
				d.getNode().setStyle("-fx-background-color: rgb(77, 135, 152, .6);");
				analysis_view.conditionalDataExtractEquals(category_analysis_view.getSexy_bar_chart().getData().get(0).getName(), d.getXValue());
				
				embedding_panel.enlightenBulbs(category_analysis_view.getSexy_bar_chart().getData().get(0).getName(), d.getXValue());
				
			});
			
			d.getNode().setOnMouseExited(e -> {
				d.getNode().setStyle("-fx-background-color: black;");
				analysis_view.removeLast();
				
				embedding_panel.delightenBulbs();
				
			});			
		}
	}
	
	
	/**
	 * Encode all the category columns into integers
	 */
	private void encodeCategoryColumns() {
		
		column_map = new HashMap<String, RecordColumn>();
		
		for(TableColumn record_column : getColumns()) {
			
			column_map.put(((RecordColumn)record_column).getName(), ((RecordColumn)record_column));
			if(record_column instanceof RecordColumn)  {
				
				if(((RecordColumn)record_column).getColumn_type() == RecordColumn.Type.CATEGORY || ((RecordColumn)record_column).getColumn_type() == RecordColumn.Type.CLASS_LABEL) {
    				
    				HashMap<String, MutableInt> category_map = new HashMap<String, MutableInt>();
    			    				
    				for (Map item : getItems()) {
    					
    					String myval = ((RecordColumn)record_column).getCellObservableValue(item).getValue().toString();
    					
    					MutableInt mi = category_map.get(myval);
    					if(mi == null) {
    						category_map.put(myval, new MutableInt());
    					}
    					else mi.increment();
    				}
    				
    				SortedSet<Map.Entry<String, MutableInt>> sort_cats = entriesSortedByValues(category_map);
    				HashMap<String, Integer> mymap = new HashMap<String, Integer>();
    				
    				int count = 0;
    				for(Map.Entry<String, MutableInt> ent : sort_cats) {
    					mymap.put(ent.getKey(), count);
    					count++;
    				}
    				((RecordColumn)record_column).setCategory_map(mymap);

    			}
			}
		}
	}
	
	
	static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
	        new Comparator<Map.Entry<K,V>>() {
	            @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
	                int res = e1.getValue().compareTo(e2.getValue());
	                return res != 0 ? res : 1;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
	
	public ArrayList<OutputLabel> getOutput_labels() {
		return output_labels;
	}

	public void setOutput_labels(ArrayList<OutputLabel> output_labels) {
		this.output_labels = output_labels;
	}

	public AnyRecord getAnyRecord() {
		return anyRecord;
	}

	public void setAnyRecord(AnyRecord anyRecord) {
		this.anyRecord = anyRecord;
	}
	
}
