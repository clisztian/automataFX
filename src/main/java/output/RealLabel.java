package output;

import encoders.Encoder;
import encoders.RealEncoder;

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
	
	public RealLabel(int nbits, float min, float max) {
		
		this.target_resolution = nbits;
		this.target_min = min;
		this.target_max = max;
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

	

}