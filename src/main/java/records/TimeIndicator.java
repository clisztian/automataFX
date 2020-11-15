package records;

import encoders.Temporal;

public record TimeIndicator(String name, String type, Temporal timestamp, double value) {

}

