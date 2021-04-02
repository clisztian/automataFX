package graphics;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/**
 * A factory for <code>TableRow</code>s suitable for passing to
 * <code>TableView.setRowFactory(...)</code>. This row factory will
 * set a pseudoclass state to true for rows whose indices are contained in
 * <code>getStyledRowIndices</code>.
 * Modifications to the <code>ObservableList&lt;Integer&gt;</code>
 * returned from <code>getStyledRowIndices</code> will result in 
 * immediate updates to the pseudoclass state of rows added to or removed
 * from the list.
 * The pseudoclass is determined
 * by the <code>styleClass</code> parameter in the constructor.
 * <br/>
 * Usage example:
 * <br/>
 * <pre>
 * final TableView&lt;MyDomainObject&gt; table = ... ;
 * final StyleChangingRowFactory&lt;MyDomainObject&gt; rowFactory 
 *          = new StyleChangingRowFactory&lt;&gt;("highlighted");
 * table.setRowFactory(rowFactory);
 * 
 * Button highlightButton = new Button("Highlight selected rows");
 * highlightButton.setOnAction(new EventHandler&lt;ActionEvent&gt;() {
 *  &#0064;Override
 *  public void handle(ActionEvent event) {
 *      rowFactory().getStyledRowIndices()
 *          .setAll(table.getSelectionModel().getSelectedIndices());
 *  }
 * });
 * </pre>
 * 
 * @author James_D
 *
 * @param <T> The type of the data displayed in the table row.
 */

public class StyleChangingRowFactory<T> implements
        Callback<TableView<T>, TableRow<T>> {

    private final PseudoClass pseudoClass ;
    private final ObservableList<Integer> styledRowIndices ;
    private final Callback<TableView<T>, TableRow<T>> baseFactory ;
    
    /**
     * Construct a <code>StyleChangingRowFactory</code>,
     * specifying the name of the pseudoclass that will be set
     * for rows determined by <code>getStyledRowIndices</code>
     * and a base factory to create the <code>TableRow</code>. If <code>baseFactory</code>
     * is <code>null</code>, default table rows will be created.
     * @param styleClass The name of the pseudoclass that will be set for specified rows.
     * @param baseFactory A factory for creating the rows. If null, default 
     * <code>TableRow&lt;T&gt;</code>s will be created using the default <code>TableRow</code> constructor.
     */
    public StyleChangingRowFactory(String styleClass, Callback<TableView<T>, TableRow<T>> baseFactory) {
        this.pseudoClass = PseudoClass.getPseudoClass(styleClass);
        this.baseFactory = baseFactory ;
        this.styledRowIndices = FXCollections.observableArrayList();
    }
    
    /**
     * Construct a <code>StyleChangingRowFactory</code>,
     * which sets the pseudoclass <code>styleClass</code> to true for rows determined by
     * <code>getStyledRowIndices</code>, and using default <code>TableRow</code>s.
     * @param styleClass
     */
    public StyleChangingRowFactory(String styleClass) {
        this(styleClass, null);
    }
    
    @Override
    public TableRow<T> call(TableView<T> tableView) {
        
        final TableRow<T> row ;
        if (baseFactory == null) {
            row = new TableRow<>();
        } else {
            row = baseFactory.call(tableView);
        }

        styledRowIndices.addListener((Change<? extends Integer> change) 
                -> row.pseudoClassStateChanged(pseudoClass, styledRowIndices.contains(row.getIndex())) 
        );
        
        row.indexProperty().addListener((observable, oldValue, newValue) 
                -> row.pseudoClassStateChanged(pseudoClass, styledRowIndices.contains(row.getIndex()))
        );
        
        return row;
    }
    
    /**
     * 
     * @return The list of indices of the rows for which the pseudoclass <code>styleClass</code> will be set.
     * Changes to the content of this list will result in the pseudoclass state being immediately updated
     * on rows whose indices are either added to or removed from this list.
     */
    public ObservableList<Integer> getStyledRowIndices() {
        return styledRowIndices ;
    }

}	