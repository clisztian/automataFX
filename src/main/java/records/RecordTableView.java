package records;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dataio.CSVInterface;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

public class RecordTableView<V> extends TableView<V> {

	private ArrayList<TableColumn<V, Object>> any_columns;
	
	public RecordTableView(V val) {
		
		any_columns = new ArrayList<TableColumn<V, Object>>();
		List<Field> any_fields = getPrivateFields(val.getClass());
		
		/**
		 * Setup table
		 */
		for(Field field : any_fields) {
			
			TableColumn<V, Object> column = new TableColumn<V, Object>(field.getName());
			column.setCellValueFactory(new PropertyValueFactory<>(field.getName()));
			any_columns.add(column);
		}
		
		
		getColumns().addAll(any_columns);
		setEditable(false);
		
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		getStylesheets().add(getClass().getClassLoader().getResource("anystyle.css").toExternalForm());
		
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
	
	
	public void fillTableFromCSV(String filename) throws IOException {
		
		CSVInterface csv = new CSVInterface(filename);	
		AnyRecord anyrecord = csv.createRecord();
		
	}
	
	public static void main(String[] args) throws IOException {
		
		CSVInterface csv = new CSVInterface("data/expense_data_records.csv");	
		AnyRecord anyrecord = csv.createRecord();
		
		RecordTableView<AnyRecord> table = new RecordTableView<AnyRecord>(anyrecord);
		
	}
	
	
}
