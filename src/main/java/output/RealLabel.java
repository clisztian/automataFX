package output;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import encoders.Encoder;
import encoders.RealEncoder;
import records.RecordColumn;

/**
 * The counterpart to the real encoder is the real label
 * @author lisztian
 *
 */
public class RealLabel implements OutputLabel<Float> {

	private RealEncoder encoder;


	private int target_resolution;
	private float target_min;
	private float target_max;
	
	private DescriptiveStatistics stats;
	private RecordColumn recordColumn;
	
	public RealLabel(int nbits, float min, float max) {
		
		this.target_resolution = nbits;
		this.target_min = min;
		this.target_max = max;
	}
	
	public float[] getLabels() {
		
		float[] targets = new float[target_resolution];
		
		float delta = (target_max - target_min) / (1f*target_resolution );
		
		targets[0] = target_min;
		for(int i = 1; i < target_resolution; i++) {
			targets[i] = targets[i-1] + delta;
		}
		return targets;
	}
	
	@Override
	public void setLabel(Object obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getLabel(Float valf) {
		int val = Math.max(0, (int)((valf - target_min)/(target_max - target_min) * (target_resolution - 1) + .5f));
		val = Math.min(val, target_resolution-1);
		return (Math.max(0, val));
	}

	@Override
	public void setEncoder(Encoder enoder) {
		this.encoder = (RealEncoder)encoder;
	}

	@Override
	public Float decode(Object val) {
		return ((Integer)val) / (float)(target_resolution - 1) * (target_max - target_min) + target_min;
	}

	public DescriptiveStatistics getStats() {
		return stats;
	}

	public void setStats(DescriptiveStatistics stats) {
		this.stats = stats;
	}


	public void setRecordColumn(RecordColumn recordColumn) {
		this.recordColumn = recordColumn;
		
	}
	
	public RecordColumn getRecordColumn() {
		return recordColumn;
	}
	
	public int getTarget_resolution() {
		return target_resolution;
	}

	public void setTarget_resolution(int target_resolution) {
		this.target_resolution = target_resolution;
	}

	public float getTarget_min() {
		return target_min;
	}

	public void setTarget_min(float target_min) {
		this.target_min = target_min;
	}

	public float getTarget_max() {
		return target_max;
	}

	public void setTarget_max(float target_max) {
		this.target_max = target_max;
	}
	

}