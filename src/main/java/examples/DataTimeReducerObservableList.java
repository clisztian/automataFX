package examples;

import javafx.collections.ModifiableObservableListBase;
import javafx.scene.chart.XYChart.Data;
import java.time.LocalDateTime;

public class DataTimeReducerObservableList <X extends LocalDateTime, Y extends Number> extends ModifiableObservableListBase<Data<X, Y>> {

	@Override
	protected void doAdd(int arg0, Data<X, Y> arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Data<X, Y> doRemove(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Data<X, Y> doSet(int arg0, Data<X, Y> arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Data<X, Y> get(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

}
