
package DataRing;

public class RingNarrowPass extends RingBase{
	int sumHigh;
	int sumLow;
	double cutoffFrequencyHzLow;
	double cutoffFrequencyHzHigh;
	RingBase orgValuesHigh;
	RingBase orgValuesLow;
	
	public RingNarrowPass(int length,double cutoffFrequencyHzHigh,double cutoffFrequencyHzLow,double samplerateHz) {
		super(length);
		this.cutoffFrequencyHzHigh=cutoffFrequencyHzHigh;
		this.cutoffFrequencyHzLow=cutoffFrequencyHzLow;
		orgValuesHigh=new RingBase(1);
		orgValuesLow=new RingBase(1);
		sumHigh=0;
		sumLow=0;
		setSampleRate(samplerateHz);
	}
	
	public void insert(short value){
		orgValuesHigh.insert(value);
		sumHigh+=value-orgValuesHigh.getOldest();
		short resulthigh=(short)(value - sumHigh/orgValuesHigh.getLength());
				
		orgValuesLow.insert(resulthigh);
		sumLow+=resulthigh-orgValuesLow.getOldest();
		
		pos = (pos+1)%buffer.length;
		buffer[pos] = (short)(sumLow/orgValuesLow.getLength());
	}	
	
	public void setSampleRate(double samplerateHz){
		double filtertimeHigh=1/(1.1*cutoffFrequencyHzHigh);
		int filterlengthHigh=(int)(filtertimeHigh*samplerateHz);
		if(filterlengthHigh<4) filterlengthHigh=4;
		if(filterlengthHigh>10000) filterlengthHigh=10000;
		if(filterlengthHigh!=orgValuesHigh.getLength()) 
		{
			System.err.println(filterlengthHigh);
			sumHigh=0;
			orgValuesHigh=new RingBase(filterlengthHigh);
		}
		
		double filtertimeLow=1/(1.4*cutoffFrequencyHzLow);
		int filterlengthLow=(int)(filtertimeLow*samplerateHz);
		if(filterlengthLow<4) filterlengthLow=4;
		if(filterlengthLow>10000) filterlengthLow=10000;
		if(filterlengthLow!=orgValuesLow.getLength()) 
		{
			System.err.println(filterlengthLow);
			sumLow=0;
			orgValuesLow=new RingBase(filterlengthLow);
			delayInTicks=filterlengthLow/2;
		}	
	}
}


