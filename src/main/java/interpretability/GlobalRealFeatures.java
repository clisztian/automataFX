package interpretability;

public class GlobalRealFeatures {

	private String feature_name;
	private BitRanges global_features;
	private float[] feature_ranges;
	
	public GlobalRealFeatures(String feature_name) {
		
		this.feature_name = feature_name;		
		global_features = new BitRanges();

	}
	
	public void setValues(String name, float strength, float neg_strength, int[] pos_bits, int[] neg_bits, int[] max_range, int[] zero_range) {
		
			global_features.setStrength(strength)
			   .setNegStrength(neg_strength)
			   .setPos_bits(pos_bits)
			   .setNeg_bits(neg_bits)
			   .setMax_range(max_range)
			   .setZero_range(zero_range);
		
	}
		
	class BitRanges {
		
		private float strength;
		private float neg_strength;
		private int[] pos_bits;
		private int[] neg_bits;
		private int[] max_range;
		private int[] zero_range;
				
		
		public int[] getPos_bits() {
			return pos_bits;
		}
		public BitRanges setNegStrength(float strength2) {
			this.setNeg_strength(strength2);
			return this;
		}
		public BitRanges setPos_bits(int[] pos_bits) {
			this.pos_bits = pos_bits;
			return this;
		}
		public int[] getNeg_bits() {
			return neg_bits;
		}
		public BitRanges setNeg_bits(int[] neg_bits) {
			this.neg_bits = neg_bits;
			return this;
		}
		public int[] getMax_range() {
			return max_range;
		}
		public BitRanges setMax_range(int[] max_range) {
			this.max_range = max_range;
			return this;
		}
		public int[] getZero_range() {
			return zero_range;
		}
		public BitRanges setZero_range(int[] zero_range) {
			this.zero_range = zero_range;
			return this;
		}
		public float getStrength() {
			return strength;
		}
		public BitRanges setStrength(float strength) {
			this.strength = strength;
			return this;
		}
		public float getNeg_strength() {
			return neg_strength;
		}
		public void setNeg_strength(float neg_strength) {
			this.neg_strength = neg_strength;
		}
		
	}


	public float getStrength() {
		return global_features.getStrength();
	}

	public BitRanges getBitRanges() {
		return global_features;
	}

	public float[] getFeature_ranges() {
		return feature_ranges;
	}

	public void setFeature_ranges(float[] feature_ranges) {
		this.feature_ranges = feature_ranges;
	}

	public String getFeatureName() {
		return feature_name;
	}

	public void setName(String name) {
		this.feature_name = name;
	}

}

