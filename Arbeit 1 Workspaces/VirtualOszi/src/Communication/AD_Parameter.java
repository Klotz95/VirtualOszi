/*************created by M. Kessel 09.02.2016*****************/



package Communication;

import java.text.DecimalFormat;

import Settings.Settings;

public class AD_Parameter{

	//Attribute
	public  ChannelInfo[] val_oszi= new ChannelInfo[4];		//maximal darzustellender Wert am Oszi
	public double sampleRate;			//in Samples/Second
	public double xPos;
	public double xPosRef;
	public double xScale;
	
	public final static int resolution=(int)(Math.pow(2,12)-1);
	final static double VtomicroV=Math.pow(10,6);
	
	
	public final static Settings[] settingsOff={Settings.VC_OFFSET_ONE,Settings.VC_OFFSET_TWO,Settings.VC_OFFSET_THREE,Settings.VC_OFFSET_FOUR};
	public final static Settings[] settingsScale={Settings.VC_SCALE_KNOB_ONE,Settings.VC_SCALE_KNOB_TWO,Settings.VC_SCALE_KNOB_THREE,Settings.VC_SCALE_KNOB_FOUR};
	public final static Settings[] settingsTrigLevel={Settings.TRIGGER_LEVEL_ONE,Settings.TRIGGER_LEVEL_TWO,Settings.TRIGGER_LEVEL_THREE,Settings.TRIGGER_LEVEL_FOUR};
	public final static Settings[] settingsImpedance={Settings.IMPEDANCE_1,Settings.IMPEDANCE_2,Settings.IMPEDANCE_3,Settings.IMPEDANCE_4};
	
	
	
	public AD_Parameter(){
	}
	
    public static String getValueInScienceNotation(String Format,double value) {
    	String[] sizes = { "p","n","µ","m","", "k", "M", "G", "T", "Peta", "Exa" };   
        int arrayOffset=4;
        double sign=1;
        if (value<0) {
        	sign=-1;
            value=-value;
        }        
        int exponent = (int)Math.log10(value/999);
        int group = exponent / 3;
        if(exponent>0) group++;
        group=Math.min(group,sizes.length-arrayOffset-1);
        group=Math.max(group,-arrayOffset);               
        double divisor = Math.pow(10, group * 3);
        
        return (new DecimalFormat(Format)).format(sign * value / divisor) + " " +sizes[group+arrayOffset];
    } 	
	
	//methoden zum umrechnen
	//Berechung von maximal und minimal darzustellenden Werten am Oszi(in MicroVolt)
	public void freeze_current_settings(){
		sampleRate=AD_Parameter.get_samplerateOszi();
		val_oszi=get_min_max_off();
		xScale=Settings.HORIZONTAL_SCALE_KNOB.getDouble();
		xPos=get_xpos();
	}
	
	public static double get_xpos(){
		double pos=Settings.HORIZONTAL_POSITION_KNOB.getDouble();
		switch(Settings.HORIZ_NORMAL_TIME_REF.get()){
		case "Links":
			pos+=Settings.HORIZONTAL_SCALE_KNOB.getDouble()*5;
			break;
		case "Rechts":
			pos=-Settings.HORIZONTAL_SCALE_KNOB.getDouble()*5;
			break;
		};
		return pos;
	}

	public static ChannelInfo get_min_max_off(int index){
		return get_min_max_off()[index];
	}
	
	public static ChannelInfo[] get_min_max_off(){
		ChannelInfo[] val_o= new ChannelInfo[4];	
		
		for(int i=0;i<4;i++){
			val_o[i]=new ChannelInfo();
			val_o[i].offset = settingsOff[i].getDouble()*VtomicroV;
			val_o[i].scale = settingsScale[i].getDouble();				
		}
		return val_o;
	}

	public static int[] getInputRessistorInMilliOhm(){
		int [] ressistor= new int[4];
		for(int i=0;i<4;i++){
			switch (settingsImpedance[i].get()){
			case "50 Ohm":
				ressistor[i]=50000;
				break;
			default:
				ressistor[i]=1000000000;
				break;
			}
		}
		return ressistor;
	}	
	
	public static int[] getChannels(int bits){
		int count= (bits&1)+((bits&2)>>1)+((bits&4)>>2)+((bits&8)>>3);

		int j=0;
		int[] channelsLUT=new int[count];
		for(int i=0;i<count;i++){
			while((bits&(1<<j))==0) j++;
			channelsLUT[i]=j+1;
			j++;
		}
		return channelsLUT;
	}

	public static short volt2ticks(double val_in_volt, double scale, double offset)
	{
		double value=(val_in_volt+offset/VtomicroV)/scale*resolution/4;
		if(value>Short.MAX_VALUE) return Short.MAX_VALUE;
		else if(value<Short.MIN_VALUE) return Short.MIN_VALUE;
		else return (short)value;
	}	
	
	public static double volt2plusmius1(double val_in_volt, ChannelInfo cInfo,int divs)
	{
		return volt2plusmius1(val_in_volt,cInfo.scale,cInfo.offset,divs);
	}	
	
	public static double volt2plusmius1(double val_in_volt, double scale, double offset,int divs)
	{
		double value=(val_in_volt+offset/VtomicroV)/scale/divs;
		if(value>1) return 1;
		else if(value<-1) return -1;
		else return value;
	}
	
	public short volt2ticks(double val_in_volt, int channel){
		channel=channel-1;
		return volt2ticks(val_in_volt, val_oszi[channel].scale,  val_oszi[channel].offset);
	}

	public static short volt2ticks(double val_in_volt, ChannelInfo cInfo){
		return volt2ticks(val_in_volt, cInfo.scale, cInfo.offset);
	}

	public static double get_samplerateOszi(){
		double rate=5000/Settings.HORIZONTAL_SCALE_KNOB.getDouble();
		if(rate>4000000000d) rate=4000000000d;
		return rate;
	}

}

