package records;

public record OrderbookSignalUpdateRecord(double time_diff, 
									float spread, 
									float log_price, 
									int volume, 
									float bid_vol, 
									float ask_vol,
									float sig_1, 
									float sig_2, 
									float sig_3) {

}
