package Trigger;

import DataRing.RingBufferManager;
import DataRing.RingHighPass;
import DataRing.RingLowPass;
import DataRing.RingNarrowPass;
import Communication.AD_Parameter;
import DataRing.RingBase;
import Display.OsciDisplay;
import Settings.Settings;

/**
 */
public class TriggerConditionManager {

	public enum TriggerState {
		PRETRIGGER, POSTTRIGGER, NOTRIGGER
	}

	private TriggerState state=TriggerState.PRETRIGGER;

	private final int ringBufferSize=1000*1000;
	private RingBase triggerRingBuffer;
	private RingBufferManager ringBuffer = RingBufferManager.getInstance("SampledRing");

	private boolean autoTriggered=false;

	int trigChannel = -1;	

	/** trigger object holds object of current trigger condition */
	private AbstractTriggerCondition trigger = null;

	/** Is <i>true</i> when the trigger condition detects a trigger condition, otherwise <i>false</i>. */

	private long startTimeMilli = -1;
	protected long count_since_last_trigger;
	protected long count;

	private AD_Parameter adPara;	

	private static final class InstanceHolder {
		static final TriggerConditionManager INSTANCE = new TriggerConditionManager();
	}	

	/**
	 * Returns the only instance of the class TriggerConditionManager
	 */
	public static TriggerConditionManager getInstance () {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Private constructor
	 */
	private TriggerConditionManager(){		
		/**
		 * Bei erzeugen eines Objekts des TriggerconditionManagers muss die letzte Bedingung
		 * aus den Settings abgefragt werden
		 */
		setTriggerCondition(Settings.TRIG_TYPE.get());
		setChannel(Settings.CHANNEL.get());
		updateFilter();
		startTimeMilli=System.currentTimeMillis();
	}

	public boolean isNormalTriggered(){
		return !autoTriggered;
	}

	public void setParameter(AD_Parameter adPara){
		this.adPara=adPara;
	}

	public void updateFilter(){

		switch(Settings.COUPLING.get()){
		case "DC": 
			if (Settings.HF_CANCELLING.getBool()){				
				// HF-Unterdrückung durch TP-Filter mit Grenzfrequenz 50 kHz
				triggerRingBuffer = new RingLowPass(ringBufferSize,50000,AD_Parameter.get_samplerateOszi());
			}
			else {
				// no filter
				triggerRingBuffer = new RingBase(ringBufferSize);
			}
			break;
		case "AC": 
			if (Settings.HF_CANCELLING.getBool()){
				// HF-Unterdrückung durch TP-Filter mit Grenzfrequenz 50 kHz
				triggerRingBuffer = new RingNarrowPass(ringBufferSize,10,50000,AD_Parameter.get_samplerateOszi());
			}
			else {
				// HP-Filter mit Grenzfrequenz 10Hz
				triggerRingBuffer = new RingHighPass(ringBufferSize,10,AD_Parameter.get_samplerateOszi());
			}
			break;
		case "NF-Unterdrückung": 
			// HP-Filter mit 3-dB-Punkt bei 50kHz in Serie
			triggerRingBuffer = new RingHighPass(ringBufferSize,50000,AD_Parameter.get_samplerateOszi());
			break;
		case "TV": 
			// not implemented
			if (Settings.HF_CANCELLING.getBool()){				
				// HF-Unterdrückung durch TP-Filter mit Grenzfrequenz 50 kHz
				triggerRingBuffer = new RingLowPass(ringBufferSize,50000,AD_Parameter.get_samplerateOszi());
			}
			else {
				// no filter
				triggerRingBuffer = new RingBase(ringBufferSize);
			}
			break;
		}
		setTriggerCondition(Settings.TRIG_TYPE.get());
	}

	public void setTriggerCondition(String type){
		switch(type){
		case "Flanke":
			trigger = new TriggerConditionFlanke(triggerRingBuffer);
			break;
		case "Flanke dann Flanke":
			trigger = new TriggerConditionFlankeDannFlanke(triggerRingBuffer);
			break;
		case "Pulsbreite":
			trigger = new TriggerConditionPulsbreite(triggerRingBuffer);
			break;
		case "Bitmuster":
			trigger = new TriggerConditionBitmuster(triggerRingBuffer);
			break;
		case "ODER":
			trigger = new TriggerConditionODER(triggerRingBuffer);
			break;
		case "Anstiegs-/Abfallzeit":
			trigger = new TriggerConditionAnstiegsAbfallzeit(triggerRingBuffer);
			break;
		case "Nte Flanke Burst":
			trigger = new TriggerConditionNteFlankenBurst(triggerRingBuffer);
			break;
		case "Niedriger Impuls":
			trigger = new TriggerConditionNiedrigerImpuls(triggerRingBuffer);
			break;
		case "Setup und Halten":
			trigger = new TriggerConditionSetupUndHalten(triggerRingBuffer);
			break;
		case "Video":
			trigger = new TriggerConditionVideo(triggerRingBuffer);
			break;
		case "USB":
			trigger = new TriggerConditionUSB(triggerRingBuffer);
			break;
		}
		System.err.println("Set trigger condition: "+type);
	}

	public void setChannel(String channelStr){
		try{
			trigChannel=Integer.parseInt(channelStr);
		}
		catch(NumberFormatException e){
			trigChannel=-1;
		}
		startTimeMilli=System.currentTimeMillis();
	}


	public void checkAutoTrigger(){	
		if((System.currentTimeMillis()-startTimeMilli)>2000){
			count_since_last_trigger=0;
			count=0;
			startTimeMilli=System.currentTimeMillis();
			autoTriggered=true;					
			setDisplayBuffer(ringBuffer.calcFullForPos());
		}
	}

	public void insert(short[] sample){
		int trigCh=trigChannel;//copy because trigChannel can changed from other thread 
		if(trigCh==-1) {
			if (Settings.MODE_COUPLING.get().equals("Auto")){
				checkAutoTrigger();
			}
			return;
		}
		/*-------------------- Triggerkopplung --------------------*/
		// Fälle für pos=0 beachten! y[-1]=y[999999]?
		synchronized (this) {
			triggerRingBuffer.insert(sample[trigCh-1]);
			tryTrigger();
		}
	}

	public void resetTrigger(){
		synchronized (this) {
			count_since_last_trigger=0;
			state=TriggerState.NOTRIGGER;			
		}
	}

	public void restartAquisition(){
		synchronized (this) {
			state=TriggerState.PRETRIGGER;
			triggerRingBuffer.emptyBuffer();
			ringBuffer.emptyBuffer();
			count_since_last_trigger=0;
			count=0;
		}
	}

	/**
	 * Try current trigger condition at current position
	 * @author Sebastian Wetzlaugk
	 * <br />Last Edited: 18.03.2016
	 */
	public void tryTrigger(){

		switch(state){
		case PRETRIGGER:
			count_since_last_trigger++;
			//for xpos=0 5 divs preTrigger
			double xdiv=adPara.sampleRate*adPara.xScale;
			double preTrigger=-adPara.sampleRate*adPara.xPos+5*xdiv;
			if(preTrigger>(10*xdiv)) preTrigger=10*xdiv;

			if(count<preTrigger) count++;
			else state=TriggerState.NOTRIGGER;
		case NOTRIGGER:
			// Holdoff:
			//count_since_last_trigger > 2, to prevent jitter based on double triggers
			if(!Settings.RUNSTOP.getBool()||Settings.SINGLE.getBool()) {
				count_since_last_trigger++;
				double time_since_last_trigger=count_since_last_trigger/adPara.sampleRate;
				if ((time_since_last_trigger > Settings.HOLDOFF_COUPLNG.getDouble())&&(count_since_last_trigger>2)) {
					// Wenn trigger auf Trigger-Bedingung zeigt
					if (trigger != null){
						if (trigger.tryTriggerCondition(triggerRingBuffer.getCurPos(),adPara)){
							// Trigger detected
							state=TriggerState.POSTTRIGGER;						
							count=0;
							count_since_last_trigger=0;
							autoTriggered=false;
							if(Settings.SINGLE.getBool()==true) Settings.SINGLE.set(false);
						} // if
					} // if
				} // if
				if (Settings.MODE_COUPLING.get().equals("Auto")){
					checkAutoTrigger();					
				}
			}
			break;
		case POSTTRIGGER:			
			count_since_last_trigger++;

			double postTrigger=adPara.sampleRate*adPara.xPos+adPara.sampleRate*adPara.xScale*5;
			if(count<postTrigger) count++;
			else  {
				state=TriggerState.NOTRIGGER;
				setDisplayBuffer((int)(trigger.getTriggerPos()+(adPara.xPos*adPara.sampleRate)));
			}
			break;
		default:
			break;

		}
	} // tryTrigger()

	/**
	 * Set Display buffer
	 * @author Sebastian Wetzlaugk
	 * <br />Last Edited: 15.03.2016
	 */
	private void setDisplayBuffer(int triggerpos){
		// Zu Testzwecken kann das Triggersignal nach den Filtern ausgegeben werden
		//display.updateData(triggerRingBuffer.getBuffer().clone());
		OsciDisplay.getInstance().updateData(ringBuffer.getBufferCopy(triggerpos),ringBuffer.getParameter());
	} 

	/**
	 * 
	 * @author Sebastian Wetzlaugk
	 * <br />Last Edited: 18.03.2016
	 */
	public void forceTrigger(){
		state=TriggerState.POSTTRIGGER;						
		count_since_last_trigger=0;
		count=0;
		autoTriggered=true;
	} // forceTrigger()

} // TriggerConditionManager


/**
 * Provides an abstract class defining all necessary attributes and methods for trigger conditions.<br /><br />
 * Each trigger condition must contain a method called <b>tryTriggerCondition()</b> which will validate the trigger condition.
 * 
 * @author Sebastian Wetzlaugk
 * <br />Last Edited: 18.03.2016
 *
 */
abstract class AbstractTriggerCondition {

	protected boolean triggerDetected;
	protected int triggerPos;
	protected RingBase triggerRingBuffer;

	public boolean isTriggerDetected(){
		return triggerDetected;
	}

	public AbstractTriggerCondition(RingBase triggerRingB){
		triggerRingBuffer=triggerRingB;
		Init();
	}

	protected void Init(){
		triggerDetected=false;
		triggerPos=0;		
	}

	public int getTriggerPos(){
		return triggerPos;
	}

	public abstract boolean tryTriggerCondition(int pos,AD_Parameter adPara);

}

/**
 * Implementation not yet finished.
 * @author Sebastian Wetzlaugk
 * <br />Last Edited: 07.03.2016
 *
 */
class TriggerConditionFlanke extends AbstractTriggerCondition {

	enum ThresoldTyp {
		OVER, UNDER, BETWEEN
	}

	private boolean lastFlankHigh = false;
	Settings[] settings={Settings.TRIGGER_LEVEL_ONE,Settings.TRIGGER_LEVEL_TWO,Settings.TRIGGER_LEVEL_THREE,Settings.TRIGGER_LEVEL_FOUR};

	ThresoldTyp last1;
	ThresoldTyp last2;

	int lastPosUnder;
	int lastPosOver;

	public TriggerConditionFlanke(RingBase triggerRingB){
		super(triggerRingB);
		Init();
	}

	protected void Init(){
		super.Init();
		last1=ThresoldTyp.OVER;
		last2=ThresoldTyp.UNDER;	
	}

	protected void setMeanPos(boolean rising){
		if((lastPosOver<lastPosUnder)^rising)
			triggerPos=(lastPosOver+lastPosUnder)/2;
		else
			triggerPos=(lastPosOver+lastPosUnder+triggerRingBuffer.getLength()-triggerRingBuffer.delayInTicks)/2;			

	}

	public boolean tryTriggerCondition(int pos,AD_Parameter adPara){

		short act_trigger_level = 0;
		String channelStr = Settings.CHANNEL.get();
		switch(channelStr){
		case "1": 
		case "2": 
		case "3": 
		case "4": 
			int channel = Integer.parseInt(channelStr);
			act_trigger_level = adPara.volt2ticks(settings[channel-1].getDouble(),channel);
			break;
		default: 
			return false;
		}

		int hysterese;
		if(Settings.NOISE_CANCELLING.getBool()){
			hysterese = 800;
		}
		else {
			hysterese = 0;
		}

		triggerDetected = false;

		ThresoldTyp current;	
		if(triggerRingBuffer.get(pos)  > (act_trigger_level + hysterese)){
			current=ThresoldTyp.OVER;
			lastPosOver= pos;
		} else if(triggerRingBuffer.get(pos)  <= (act_trigger_level - hysterese)){
			lastPosUnder= pos;
			current=ThresoldTyp.UNDER;
		} else {
			current=ThresoldTyp.BETWEEN;
		}

		if((current!=last1)&(last1==last2)){
			String flank = Settings.FLANK.get();
			switch(flank){
			case "\u2191" + " (Ansteigend)": // WaveForms.UpArrow.getSymbol()
				if (current==ThresoldTyp.OVER){
					triggerDetected = true;
					setMeanPos(true);
				}
			break;
			case "\u2193" + " (Abfallend)":	// WaveForms.DownArrow.getSymbol()
				if (current==ThresoldTyp.UNDER){
					triggerDetected = true;
					setMeanPos(false);
				}
			break;
			case "\u2195" + " (Wechselnd)": // WaveForms.UpDownArrowDouble.getSymbol()
				if (lastFlankHigh){
					if (current==ThresoldTyp.OVER){
						triggerDetected = true;
						setMeanPos(true);
						lastFlankHigh = false;
					}
				} else if (current==ThresoldTyp.UNDER){
					triggerDetected = true;
					setMeanPos(false);
					lastFlankHigh = true;
				}
			break;	
			case "\u21C5" + " (Entweder)":// WaveForms.UpDownArrow.getSymbol()
				if (current!=ThresoldTyp.BETWEEN){
					triggerDetected = true;
					setMeanPos(current==ThresoldTyp.OVER);
				}
			break;	
			} // switch(flank)		
		}

		last2=last1;
		last1=current;
		return triggerDetected;
	}

} // TriggerConditionFlanke


class TriggerConditionFlankeDannFlanke extends AbstractTriggerCondition {

	public TriggerConditionFlankeDannFlanke(RingBase triggerRingB){
		super(triggerRingB);
	}

	public boolean tryTriggerCondition(int pos,AD_Parameter adPara){
		return false;
		//triggerDetected = true;
		//triggerPos = pos;
	} // tryTriggerCondition
} // TriggerConditionFlankeDannFlanke

class TriggerConditionPulsbreite extends AbstractTriggerCondition {
	public TriggerConditionPulsbreite(RingBase triggerRingB){
		super(triggerRingB);
	}	

	public boolean tryTriggerCondition(int pos,AD_Parameter adPara){
		return false;

	} // tryTriggerCondition
} // TriggerConditionPulsbreite

class TriggerConditionBitmuster extends AbstractTriggerCondition {
	public TriggerConditionBitmuster(RingBase triggerRingB){
		super(triggerRingB);
	}

	public boolean tryTriggerCondition(int pos,AD_Parameter adPara){
		return false;

	} // tryTriggerCondition
} // TriggerConditionBitmuster

class TriggerConditionODER extends AbstractTriggerCondition {
	public TriggerConditionODER(RingBase triggerRingB){
		super(triggerRingB);
	}

	public boolean tryTriggerCondition(int pos,AD_Parameter adPara){
		return false;

	} // tryTriggerCondition
} // TriggerConditionODER

class TriggerConditionAnstiegsAbfallzeit extends AbstractTriggerCondition {
	public TriggerConditionAnstiegsAbfallzeit(RingBase triggerRingB){
		super(triggerRingB);
	}

	public boolean tryTriggerCondition(int pos,AD_Parameter adPara){
		return false;

	} // tryTriggerCondition
} // TriggerConditionAnstiegsAbfallzeit

class TriggerConditionNteFlankenBurst extends AbstractTriggerCondition {
	public TriggerConditionNteFlankenBurst(RingBase triggerRingB){
		super(triggerRingB);
	}

	public boolean tryTriggerCondition(int pos,AD_Parameter adPara){
		return false;

	} // tryTriggerCondition
} // TriggerConditionNteFlankenBurst

class TriggerConditionNiedrigerImpuls extends AbstractTriggerCondition {
	public TriggerConditionNiedrigerImpuls(RingBase triggerRingB){
		super(triggerRingB);
	}

	public boolean tryTriggerCondition(int pos,AD_Parameter adPara){
		return false;

	} // tryTriggerCondition
} // TriggerConditionNiedrigerImpuls

class TriggerConditionSetupUndHalten extends AbstractTriggerCondition {
	public TriggerConditionSetupUndHalten(RingBase triggerRingB){
		super(triggerRingB);
	}

	public boolean tryTriggerCondition(int pos,AD_Parameter adPara){
		return false;

	} // tryTriggerCondition
} // TriggerConditionSetupUndHalten

class TriggerConditionVideo extends AbstractTriggerCondition {
	public TriggerConditionVideo(RingBase triggerRingB){
		super(triggerRingB);
	}
	public boolean tryTriggerCondition(int pos,AD_Parameter adPara){
		return false;

	} // tryTriggerCondition
} // TriggerConditionVideo

class TriggerConditionUSB extends AbstractTriggerCondition {
	public TriggerConditionUSB(RingBase triggerRingB){
		super(triggerRingB);
	}
	public boolean tryTriggerCondition(int pos,AD_Parameter adPara){
		return false;

	} // tryTriggerCondition
} // TriggerConditionUSB