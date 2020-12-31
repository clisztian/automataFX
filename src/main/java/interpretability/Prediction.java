package interpretability;

public class Prediction {

	private float[] local_feature_importance;
	private float[] risk_features;
	private int[] top_temporal;
	private int[] top_categories;
	
	private GlobalRealFeatures local_exp;
	private double probability;
	private int pred_class;
	

	
	public Prediction(float[] local, float[] risks, int[] top_fingerbits, int[] top_riskfingerbits, int pred_class, double probability) {
		
		this.risk_features = risks;
		this.setLocal_feature_importance(local);
		this.pred_class = pred_class;
		this.probability = probability;
	}
	
	
	public double getProbability() {
		return probability;
	}
	
	public void setProbability(double probability) {
		this.probability = probability;
	}

	public int getPred_class() {
		return pred_class;
	}

	public void setPred_class(int pred_class) {
		this.pred_class = pred_class;
	}

	public float[] getRisk_features() {
		return risk_features;
	}

	public void setRisk_features(float[] risk_features) {
		this.risk_features = risk_features;
	}




	public float[] getLocal_feature_importance() {
		return local_feature_importance;
	}

	public void setLocal_feature_importance(float[] local_feature_importance) {
		this.local_feature_importance = local_feature_importance;
	}



	public GlobalRealFeatures getLocal_exp() {
		return local_exp;
	}


	public void setLocal_exp(GlobalRealFeatures local_exp) {
		this.local_exp = local_exp;
	}


	public int[] getTop_temporal() {
		return top_temporal;
	}


	public void setTop_temporal(int[] top_temporal) {
		this.top_temporal = top_temporal;
	}


	public int[] getTop_categories() {
		return top_categories;
	}


	public void setTop_categories(int[] top_categories) {
		this.top_categories = top_categories;
	}
	
	
	
}
