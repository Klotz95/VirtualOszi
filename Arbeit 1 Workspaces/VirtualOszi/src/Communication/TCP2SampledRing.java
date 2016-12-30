package Communication;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import DataRing.RingBufferManager;
import Trigger.TriggerConditionManager;

public class TCP2SampledRing implements Runnable{

	boolean running =true;
	ServerSocket welcomeSocketA;
	public RingBufferManager rbuffer;


	private static final class InstanceHolder {
		static final TCP2SampledRing INSTANCE = new TCP2SampledRing();
	}

	private TCP2SampledRing () {
		rbuffer = new RingBufferManager("SampledRing"); //change this settings to set a different sized ring buffer
	}

	public static TCP2SampledRing getInstance () {
		return InstanceHolder.INSTANCE;
	}



	public void terminate() {
		try {
			running = false;
			if(!welcomeSocketA.isClosed()) welcomeSocketA.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	  

	@Override
	public void run() {
		while(running) {
			try {
				welcomeSocketA = new ServerSocket(6789);
				Socket connectionSocket = welcomeSocketA.accept();
				DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream()); 
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

				int runs=0;
				int pos=0;
				double step=1;

				while(running&&!welcomeSocketA.isClosed())
				{
					//Informationen aus Header auslesen
					int max_value_Input=inFromClient.readInt();
					int max_value_Input_Volt=inFromClient.readInt(); //max value of input in microvolt
					int samplerate_Input=inFromClient.readInt();//in Samples/s
					int count_Input=inFromClient.readInt();//Count of received 16Bit datapoints
					byte channelsBits=(byte) (inFromClient.readByte() & 15);
					int[] channel=AD_Parameter.getChannels(channelsBits);
					int[] outputImpedanceMilliOhm=new int[channel.length];
					for(int i=0;i<channel.length;i++){
						outputImpedanceMilliOhm[i]=inFromClient.readInt();
					}

					//buffer byte array muss doppelte menge da short = 2*byte
					byte [] buffer = new byte[2*count_Input];

					int count=0;
					while(running&(count<2*count_Input)){
						count+=inFromClient.read(buffer,count,2*count_Input-count);
					}
					outToClient.writeBytes("OK:"+AD_Parameter.get_samplerateOszi()+"\n");

					//short array anlegen zum speichern der gesendeten Daten
					short [] sendValues = new short[count_Input];

					//empfangene daten aus buffer in sendValues ablegen
					for(int i=0;i<count_Input;i++){
						sendValues[i]= (short)(((int)(buffer[2*i]& 0xFF))+((int)(buffer[2*i+1]& 0xFF)<<8));
					}

					short channels[][] = new short[count_Input/channel.length][4];
					int counter = 0;
					AD_Parameter adPara=new AD_Parameter();
					adPara.freeze_current_settings();
					int []inputImpedanceMilli=AD_Parameter.getInputRessistorInMilliOhm();
					for (int i = 0; i < count_Input/channel.length; i++) {
						for (int j = 0; j < channel.length; j++) {
							double scale=inputImpedanceMilli[j]/((double)outputImpedanceMilliOhm[j]+inputImpedanceMilli[j])*max_value_Input_Volt/(double)max_value_Input;
							double eingang_anal=sendValues[counter]*scale; //-1 bsi

							channels[i][channel[j]-1]=adPara.volt2ticks(eingang_anal,channel[j]);
							counter++;
						}
					} 

					int x_samples=(int)(count_Input/channel.length);

					double samplerate_oszi= AD_Parameter.get_samplerateOszi();

					if(step!=samplerate_Input/samplerate_oszi){
						step=samplerate_Input/samplerate_oszi;
						runs=0;
						pos=0;
					}

					rbuffer.setParameter(adPara);
					TriggerConditionManager.getInstance().setParameter(adPara);

					int xin_min=x_samples*runs;

					if(x_samples/step>1){
						for(int i=0;i<x_samples/step;i++){
							short[] nearestneighbour=channels[(int)(i*step)%x_samples];
							rbuffer.insert(nearestneighbour);
							TriggerConditionManager.getInstance().insert(nearestneighbour);
						}
					}
					else{
						for(int i=0;i<x_samples;i++){
							int posin=(i+xin_min);
							if((pos/step)<posin){
								short[] nearestneighbour=channels[i];
								pos++;
								rbuffer.insert(nearestneighbour);
								TriggerConditionManager.getInstance().insert(nearestneighbour);

							}
						}
					}
					runs++;					
				}
			}
			catch (IOException e) {
				System.err.println(e.getMessage());
				try {
					welcomeSocketA.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//e.printStackTrace();
			}		
		}
	}

}
