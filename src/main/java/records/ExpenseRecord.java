package records;

@SuppressWarnings("preview")
public record ExpenseRecord(int rank, String reason_code, String ref_number,String disclosure_group, 
		String title_en,String name,String purpose_en,String start_date,String end_date,String destination_en, 
		double airfare, double other_transport, double lodging,double meals, double other_expenses, double total, 
		String additional_comments_en, String additional_comments_fr,String owner_org,String owner_org_title, int num_days) {

}
