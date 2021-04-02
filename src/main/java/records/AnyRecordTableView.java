package records;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import dataio.CSVInterface;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Text;

public class AnyRecordTableView extends TableView<Map> {

	private ArrayList<TableColumn<Map, Object>> any_columns;
	private String[] field_names;
	
	public AnyRecordTableView(AnyRecord val) {
		
		any_columns = new ArrayList<TableColumn<Map, Object>>();
		field_names = val.getField_names();
		
		/**
		 * Setup table
		 */
		for(int i = 0; i < field_names.length; i++) {	

			TableColumn<Map, Object> column = new TableColumn<>(field_names[i]);		
			column.setCellValueFactory(new MapValueFactory<>(field_names[i]));			
			any_columns.add(column);
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
	    
	}
	
	
    public void autoResizeColumns() {

        setColumnResizePolicy( TableView.UNCONSTRAINED_RESIZE_POLICY);
        getColumns().stream().forEach( (column) -> {
            Text t = new Text( column.getText() );
            double max = t.getLayoutBounds().getWidth();
            column.setPrefWidth( max + max*.90);
        } );
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
		for(int i = 0; i < field_names.length; i++) {	
			item.put(field_names[i], any.getValues()[i]);
		}	
		getItems().add(item);
	}
	
	public void addRecords(ArrayList<AnyRecord> records) {
		
		ObservableList<Map<String, Object>> items =
			    FXCollections.<Map<String, Object>>observableArrayList();

		for(AnyRecord record : records) {
			
			Map<String, Object> item = new HashMap<>();
			for(int i = 0; i < field_names.length; i++) {	
				item.put(field_names[i], record.getValues()[i]);
			}	
			items.add(item);
		}
				
		getItems().addAll(items);
	}
	
	public void fillTableFromCSV(String filename) throws IOException {
		
		CSVInterface csv = new CSVInterface(filename);	
		AnyRecord anyrecord = csv.createRecord();
		
		addRecords(csv.getAllRecords());
		
	}
	
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

	
	
	
}
