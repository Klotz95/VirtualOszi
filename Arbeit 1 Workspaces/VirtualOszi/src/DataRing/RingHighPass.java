package DataRing;

public class RingHighPass extends RingBase{
	int sum;
	double cutoffFrequencyHz;
	RingBase orgValues;
	
	public RingHighPass(int length,double cutoffFrequencyHz,double samplerateHz) {
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
		buffer[pos] = (short)(value - sum/orgValues.getLength());
	}	
	
	public void setSampleRate(double samplerateHz){
		double filtertime=1/(1.1*cutoffFrequencyHz);
		int filterlength=(int)(filtertime*samplerateHz);
		if(filterlength<4) filterlength=4;
		if(filterlength>10000) filterlength=10000;
		if(filterlength!=orgValues.getLength()) 
		{
			System.err.println(filterlength);
			sum=0;
			orgValues=new RingBase(filterlength);
			delayInTicks=0;
		}
	}
}
