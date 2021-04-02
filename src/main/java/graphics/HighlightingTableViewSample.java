package graphics;

import java.util.Arrays;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
 
public class HighlightingTableViewSample extends Application {
 
    private final TableView<Person> table = new TableView<>();
    private final ObservableList<Person> data =
        FXCollections.observableArrayList(
            new Person("Jacob", "Smith", "jacob.smith@example.com"),
            new Person("Isabella", "Johnson", "isabella.johnson@example.com"),
            new Person("Ethan", "Williams", "ethan.williams@example.com"),
            new Person("Emma", "Jones", "emma.jones@example.com"),
            new Person("Michael", "Brown", "michael.brown@example.com")
        );
   
    public static void main(String[] args) {
        launch(args);
    }
 
    @Override
    public void start(Stage stage) {
 
        final Label label = new Label("Address Book");
        label.setFont(new Font("Arial", 20));
 
        table.setEditable(true);
 
        TableColumn<Person, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setMinWidth(100);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<>("firstName"));
 
        TableColumn<Person, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setMinWidth(100);
        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<>("lastName"));
 
        TableColumn<Person, String> emailCol = new TableColumn<>("Email");
        emailCol.setMinWidth(200);
        emailCol.setCellValueFactory(
                new PropertyValueFactory<>("email"));
 
        table.setItems(data);
        table.getColumns().addAll(Arrays.asList(firstNameCol, lastNameCol, emailCol));
        
        final StyleChangingRowFactory<Person> rowFactory = new StyleChangingRowFactory<>("highlighted");
        table.setRowFactory(rowFactory);
        
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        final Button highlightButton = new Button("Highlight");
        highlightButton.disableProperty().bind(Bindings.isEmpty(table.getSelectionModel().getSelectedIndices()));
        highlightButton.setOnAction(event -> rowFactory.getStyledRowIndices().setAll(table.getSelectionModel().getSelectedIndices()) );
        
        final Button clearHighlightButton = new Button("Clear Highlights");
        clearHighlightButton.disableProperty().bind(Bindings.isEmpty(rowFactory.getStyledRowIndices()));
        clearHighlightButton.setOnAction(event -> rowFactory.getStyledRowIndices().clear() );
        
        final HBox buttons = new HBox(5);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(highlightButton, clearHighlightButton);
        
 
        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, table, buttons);
 
        Scene scene = new Scene(vbox, 450, 500);
        stage.setTitle("Highlighting Table View Sample");
        //scene.getStylesheets().add(getClass().getResource("css/WhiteOnBlack.css").toExternalForm());
        scene.getStylesheets().add("css/WhiteOnBlack.css");
        stage.setScene(scene);
        stage.show();
    }
 
    public static class Person {
 
        private final StringProperty firstName;
        private final StringProperty lastName;
        private final StringProperty email;
 
        private Person(String fName, String lName, String email) {
            this.firstName = new SimpleStringProperty(this, "firstName", fName);
            this.lastName = new SimpleStringProperty(this, "lastName", lName);
            this.email = new SimpleStringProperty(this, "email", email);
        }
 
        public final String getFirstName() {
            return firstName.get();
        }
 
        public final void setFirstName(String fName) {
            firstName.set(fName);
        }
        
        public StringProperty firstNameProperty() {
            return firstName ;
        }
 
        public final String getLastName() {
            return lastName.get();
        }
 
        public final void setLastName(String fName) {
            lastName.set(fName);
        }
        
        public final StringProperty lastNameProperty() {
            return lastName ;
        }
 
        public final String getEmail() {
            return email.get();
        }
 
        public final void setEmail(String fName) {
            email.set(fName);
        }
        
        public final StringProperty emailProperty() {
            return email ;
        }
    }
} 