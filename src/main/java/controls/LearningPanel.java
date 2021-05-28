package controls;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Gauge.SkinType;
import javafx.scene.chart.BarChart;
import javafx.scene.paint.Color;

public class LearningPanel {

	private Gauge percent_train_gauge;
	private Gauge accuracy_gauge;
	private BarChart<String, Float> kpis;
	
	
	public void buildLearningPanel() {
		
		
		
		percent_train_gauge = GaugeBuilder.create()
                .skinType(SkinType.BAR)
                .barColor(Color.rgb(237, 22, 72))
                .valueColor(Color.WHITE)
                .unitColor(Color.WHITE)
                .unit("%")
                .minValue(0)
                .maxValue(100)
                .animated(true)
                .build();
		
		
		percent_train_gauge.setBackgroundPaint(new Color(.02,.024,.03,.6));
		
	}
	
	
	
}
