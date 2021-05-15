package examples;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 *
 * @author jKaufmann
 */
public class TableCellTextColorExample extends Application {
	
	private TableView<TableData> myTable;
	
public static class TableData {
    SimpleStringProperty one,two,three;
    public TableData(String one, String two, String three) {
        this.one = new SimpleStringProperty(one);
        this.two = new SimpleStringProperty(two);
        this.three = new SimpleStringProperty(three);
    }
    public String getOne() {
        return one.get();
    }

    public void setOne(String one) {
        this.one.set(one);
    }

    public String getThree() {
        return three.get();
    }

    public void setThree(String three) {
        this.three.set(three);
    }

    public String getTwo() {
        return two.get();
    }

    public void setTwo(String two) {
        this.two.set(two);
    }

} 
/**
 * @param args the command line arguments
 */
public static void main(String[] args) {
    Application.launch(args);
}

@Override
public void start(Stage stage) {
    VBox vbox = new VBox();
    Scene scene = new Scene(vbox, 200, 200);
    stage.setTitle("Table View - Change color of a particular column");
    stage.setWidth(400);
    stage.setHeight(500);


    myTable = new TableView<TableData>();
    ObservableList<TableData> myTableData = FXCollections.observableArrayList(
            new TableData("data", "data", "data"),
            new TableData("data", "data", "data"),
            new TableData("Name the song","867-5309","SomeEmail@gmail.com"));  

    TableColumn firstColumn = new TableColumn("First Column"); 
    firstColumn.setCellValueFactory(new PropertyValueFactory<TableData,String>("one"));

    TableColumn secondColumn = new TableColumn("Second Column"); 
    secondColumn.setCellValueFactory(new PropertyValueFactory<TableData,String>("two"));

    TableColumn thirdColumn = new TableColumn("Third Column");  
    thirdColumn.setCellValueFactory(new PropertyValueFactory<TableData,String>("three"));

    // ** The TableCell class has the method setTextFill(Paint p) that you 
    // ** need to override the text color
    //   To obtain the TableCell we need to replace the Default CellFactory 
    //   with one that returns a new TableCell instance, 
    //   and @Override the updateItem(String item, boolean empty) method.
    //
    
    Callback<TableColumn, TableCell> call = new Callback<TableColumn, TableCell>() {
        public TableCell call(TableColumn param) {
            return new TableCell<TableData, String>() {

                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!isEmpty()) {
                        this.setTextFill(Color.RED);
                        // Get fancy and change color based on data
                        if(item.contains("@")) 
                            this.setTextFill(Color.BLUEVIOLET);
                        setText(item);
                    }
                }
            };
        }
    };
    
    thirdColumn.setCellFactory(call);
    firstColumn.setCellFactory(call);
    
    

    myTable.setItems(myTableData); 
    myTable.getColumns().addAll(firstColumn, secondColumn, thirdColumn);

    myTable.getStylesheets().add(getClass().getClassLoader().getResource("css/TransactionChart.css").toExternalForm());
    
    
    
    vbox.getChildren().addAll(myTable);
    VBox.setVgrow(myTable, Priority.ALWAYS);

    stage.setScene(scene);
    stage.show();
    
    setSelectionListener();
}

public void tableRefresh() {
	
	
}

public void setSelectionListener() {
	
	myTable.getSelectionModel().getSelectedItems().addListener(
            new ListChangeListener<TableData>() {

			public void onChanged( 
               ListChangeListener.Change<? extends TableData> c) {
				tableRefresh();
			}
    });
	
}


}
