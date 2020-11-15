package records;

import encoders.Temporal;

public record EconTimeSeries(double europeGDP, float norwayGDP, float swissGDP, Temporal time, String covidColor) {

}
