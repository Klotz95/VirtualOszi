package Communication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;

public class Data2TCPSingelton implements Runnable{

	boolean running = true;
	DataOutputStream outToServer;
	BufferedReader inFromServer;

	private static final class InstanceHolder {
		static final Data2TCPSingelton INSTANCE = new Data2TCPSingelton();
	}

	private Data2TCPSingelton () {
		super();
	}

	public static Data2TCPSingelton getInstance () {
		return InstanceHolder.INSTANCE;
	}

	public void terminate() {
		try {
			running = false;
			if(outToServer!=null) outToServer.close();
			if(inFromServer!=null) inFromServer.close();			  
		} catch (IOException e) {
			e.printStackTrace();
		}	    }	  

	@Override
	public void run() {
		while(running) {
		try {
			Socket clientSocket = new Socket();
			clientSocket.connect(new InetSocketAddress("localhost", 6789), 1000000000);
			outToServer = new DataOutputStream(clientSocket.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


			//Informationen in Header
			int maxValue=(int) (10000);
			int maxValueVolt=10;
			int outputImpedanceMilliOhm=50000;
			byte channelNumbers= 15; //binar coded
			//			byte channelQuantity=(byte)((channelNumbers&1)+((channelNumbers&2)>>1)+((channelNumbers&4)>>2)+((channelNumbers&8)>>3));
			int sampleRate= 4*1000*1000*1000;
			int dataSet= 10*1024;
			//Ende-Informationen in Header

			int []x_offet = new int[4];
			x_offet[0]=0;
			x_offet[1]=0;
			x_offet[2]=0;
			x_offet[3]=0;

			boolean ti=false;
			int pos2=0;

			Random rand=new Random();
			
			while(running) {
				
				outToServer.writeInt(maxValue);
				outToServer.writeInt(maxValueVolt);
				outToServer.writeInt(sampleRate);
				outToServer.writeInt(dataSet);
				
				short []offset = new short[4];
				offset[0]=0;
				offset[1]=1000;
				offset[2]=0;
				offset[3]=-20000;

				int [] channel = getChannels(channelNumbers);
				outToServer.writeByte(channelNumbers);
				
				for(int i=0;i<channel.length;i++){
					outToServer.writeInt(outputImpedanceMilliOhm);
					
				}				

				short channels[][] = new short [4][dataSet/channel.length];

				short input[]= new short[dataSet];
				int i;
				for(i=0; i<(dataSet/channel.length); i++)
					channels[0][i]=(short)(10000*Math.sin((x_offet[0]+i)/(200.0)*2*Math.PI)+offset[0]);//1MS/s/500=> 2kHz => 50µs
				x_offet[0]=(x_offet[0]+i)%200;

				for(i=0; i<(dataSet/channel.length); i++)
					channels[1][i]=(short)(3383.0*Math.sin((x_offet[1]+i)/(50.0)*2*Math.PI)+offset[1]-2000+rand.nextInt(1000));
				//channels[1][0]=30000;
				//channels[1][1000]=-30000;
				x_offet[1]=(x_offet[1]+i)%50;

				for(i=0; i<(dataSet/channel.length); i++)
				{
					channels[2][i]=(short) (pos2*100);
					if(ti==true){
						if(pos2==250){
							ti=false;
						}
						pos2++;
					}
					else{
						if(pos2==0){
							ti=true;
						}
						pos2--;
					}
					channels[2][i]+=0;
				}

				for(i=0; i<(dataSet/channel.length); i++)
					channels[3][i]=offset[3];

				int counter = 0;				
				for(i=0;i<dataSet/channel.length;i++) {	
					for(int j = 0; j<channel.length; j++){
						input[counter] =channels[channel[j]-1][i];
						counter++;
					}
				}
				//				Thread.sleep(10);
				outToServer.write(toByteArray(input));
				String[] ServerResponce=inFromServer.readLine().split(":");
				if((ServerResponce.length>1)&(ServerResponce[0].equals("OK"))){
					sampleRate=(int)Double.parseDouble(ServerResponce[1]);
				} else
				{
					System.err.println("Server responce not OK: "+ ServerResponce);
					running=false;
				}
			}
			clientSocket.close();			
		} catch (IOException e) {
			System.err.println("No data server found");
			//e.printStackTrace();
		}
		}
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

	static public byte[] toByteArray(short input[]){

		byte [] buffer = new byte[2*input.length];

		int byte_index = 0;
		for(int short_index = 0; short_index != input.length; short_index++,byte_index += 2 )
		{
			buffer[byte_index]     = (byte) (input[short_index] & 0x00FF); 
			buffer[byte_index + 1] = (byte) ((input[short_index] & 0xFF00) >> 8);
		}
		return buffer;
	}
}