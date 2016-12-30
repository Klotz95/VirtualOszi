package Communication;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;

import Settings.Settings;

public class TCPParameterServer implements Runnable{

	boolean running =true;
	ServerSocket welcomeSocketA;
	JFrame frameI;


	private static final class InstanceHolder {
		static final TCPParameterServer INSTANCE = new TCPParameterServer();
	}

	private TCPParameterServer() {
	}

	public static TCPParameterServer getInstance (JFrame frame) {
		TCPParameterServer a=InstanceHolder.INSTANCE;
		a.frameI=frame;
		return a;
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
		try {
			while(running) {
				welcomeSocketA = new ServerSocket(16790);
				Socket connectionSocket = welcomeSocketA.accept();
				DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream()); 
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
				BufferedReader in = new BufferedReader(new InputStreamReader(inFromClient));


				while(running&&!welcomeSocketA.isClosed())
				{
					String line=in.readLine();
					
					if(line==null){
						welcomeSocketA.close();
						break;
					}

					String [] ele=line.split(":");

					if(ele.length>2){
						Settings setele=Settings.getEnumByString(ele[1]);
						if(setele==null){
							outToClient.writeBytes("Error: Settings unknown\n");
						} else {
							if(ele[0].equals("set")){
								setele.set(ele[2]);
								frameI.repaint();
								outToClient.writeBytes(setele.name()+" set with "+ele[2]+"\n");
							}	
						}
					}
					else if(ele.length>1){
						if(ele[0].equals("get")){
							Settings setele = Settings.getEnumByString(ele[1]);
							if(setele==null){
								outToClient.writeBytes("Error: Settings unknown\n");
							} else {
								 outToClient.writeBytes(setele.get()+"\n");
							}
						}

					} else {
						outToClient.writeBytes("Error: Not enough parameter\n");
					}

				}
			}
		} catch (IOException e) {
			System.err.println("Socket Closed");
			//e.printStackTrace();
		}		
	}	

}
