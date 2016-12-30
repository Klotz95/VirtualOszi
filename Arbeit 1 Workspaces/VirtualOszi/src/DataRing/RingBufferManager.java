package DataRing;

import java.util.Hashtable;

import Communication.AD_Parameter;
import Settings.Settings;

public class RingBufferManager {

	static Settings[] settingsCoupling={Settings.COUPLING_CHANNEL_1,Settings.COUPLING_CHANNEL_2,Settings.COUPLING_CHANNEL_3,Settings.COUPLING_CHANNEL_4};	
	static Settings[] settingsBWLimit={Settings.BW_LIMIT_1,Settings.BW_LIMIT_2,Settings.BW_LIMIT_3,Settings.BW_LIMIT_4};	
	
	
	final int count=4;
	final int size=1000000;
	protected  RingBase  ring[]=new RingBase[count];

	/** InstanceHandler holds all Instances of RingBuffer with a name*/
	protected static Hashtable<String, RingBufferManager> InstanceHandler = new Hashtable<String, RingBufferManager>();

	protected AD_Parameter adPara;

	public void setParameter(AD_Parameter adPara){
		this.adPara=adPara;
		for(int i=0;i<count;i++)
			ring[i].setSampleRate(adPara.sampleRate);
	}
	
	public void updateFilter(){
		double sampleRate;
		if(adPara!=null) sampleRate=adPara.sampleRate;
		else sampleRate=AD_Parameter.get_samplerateOszi();
		for(int i=0;i<count;i++){
			switch (settingsCoupling[i].get()){
			case "AC":
				if(settingsBWLimit[i].getBool()){
					ring[i]=(RingBase)new RingNarrowPass(size,10,20000000f,sampleRate);					
				
				} else {
					ring[i]=(RingBase)new RingHighPass(size,10,sampleRate);					
				}
				break;
			default:
				if(settingsBWLimit[i].getBool()){
					ring[i]=(RingBase)new RingLowPass(size,20000000f,sampleRate);					
				
				} else {
					ring[i]=new RingBase(size);
				}
				break;
			}			
		}
		
	}

	public AD_Parameter getParameter(){
		return adPara;
	}

	public static RingBufferManager getInstance(String name){
		if (InstanceHandler.containsKey(name)){
			return InstanceHandler.get(name);
		} else {
			return null;
		}
	}	

	public RingBufferManager(String name) {
		InstanceHandler.put(name, this);
		updateFilter();
	}
	
	public void emptyBuffer(){
		for(int i=0;i<count;i++)
			ring[i].emptyBuffer();
	}


	public void insert(short[] values){		
		for(int i = 0; i < values.length; i++){
			ring[i].insert(values[i]);
		}
	}

	public short[][] getBufferCopy(int triggerPos){
		double preTrigger=adPara.sampleRate*adPara.xScale*5;
		short[][] A2 = new short[count][];
		for(int i = 0; i < count; i++){
			A2[i]=ring[i].getBufferCopy(triggerPos,(int)preTrigger);
		}		

		return A2;
	}
	
	public int calcFullForPos(){
		return ring[0].calcFullForPos();

	}	

}
