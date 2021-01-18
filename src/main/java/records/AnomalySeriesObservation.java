package records;

import encoders.Temporal;

public record AnomalySeriesObservation(Temporal timestamp, float value) {

}
