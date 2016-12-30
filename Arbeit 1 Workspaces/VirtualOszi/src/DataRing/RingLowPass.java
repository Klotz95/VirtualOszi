package DataRing;

public class RingLowPass extends RingBase{
	int sum;
	double cutoffFrequencyHz;
	RingBase orgValues;
	
	public RingLowPass(int length,double cutoffFrequencyHz,double samplerateHz) {
		super(length);
		this.cutoffFrequencyHz=cutoffFrequencyHz;
		orgValues=new RingBase(length);
		sum=0;
		setSampleRate(samplerateHz);
	}
	
	public void insert(short value){
		orgValues.insert(value);
		sum+=value-orgValues.getOldest();
		
		pos = (pos+1)%buffer.length;
		buffer[pos] = (short)(sum/orgValues.getLength());
	}	
	
	public void setSampleRate(double samplerateHz){
		double filtertime=1/(1.4*cutoffFrequencyHz);
		int filterlength=(int)(filtertime*samplerateHz);
		if(filterlength<4) filterlength=4;
		if(filterlength!=orgValues.getLength()) 
		{
			sum=0;
			orgValues=new RingBase(filterlength);
			delayInTicks=filterlength/2;
		}
	}
}
