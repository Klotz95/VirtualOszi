package Communication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import Settings.Settings;

public class TCPParameterClient implements Runnable{

	boolean running = true;
	DataOutputStream outToServer;
	BufferedReader inFromServer;

	private static final class InstanceHolder {
		static final TCPParameterClient INSTANCE = new TCPParameterClient();
	}

	private TCPParameterClient () {
		super();
	}

	public static TCPParameterClient getInstance () {
		return InstanceHolder.INSTANCE;
	}

	public void terminate() {
		try {
			running = false;
			if(outToServer!=null) outToServer.close();
			if(outToServer!=null) inFromServer.close();			  
		} catch (IOException e) {
			e.printStackTrace();
		}	    }	  

	@Override
	public void run() {
		while(running) {
			try {
				Socket clientSocket = new Socket();
				clientSocket.connect(new InetSocketAddress("localhost", 16790), 1000000000);
				outToServer = new DataOutputStream(clientSocket.getOutputStream());
				inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				
				
				//Abfragen der aktuellen Wertes
				/*
				 * for Schleife -> enum
				 * { get : Parameter; readline
				 * alle abfragen und auf Settings/ Interface übertragen
				 */	
				outToServer.writeBytes("get:"+Settings.A_HELP_LANGUAGE+"\n");
				String line = inFromServer.readLine();
				Settings.A_HELP_LANGUAGE.set(line);

				String change = line;
				boolean toogle=false;
				String koppl="DC";
				
				while(running) {
					if(change.equals("Deutsch")) change="English";
					else change="Deutsch";
					Thread.sleep(4000);
					
					if(koppl.equals("DC")) koppl="AC";
					else koppl="DC";
					
					//senden der neuen Einstellung
					outToServer.writeBytes("set:"+Settings.A_HELP_LANGUAGE+":"+change+"\n");
					System.err.println(inFromServer.readLine());
					
					outToServer.writeBytes("set:"+Settings.RUNSTOP+":"+Boolean.toString(toogle)+"\n");
					toogle=!toogle;
					System.err.println(inFromServer.readLine());
					
					outToServer.writeBytes("set:"+Settings.COUPLING+":"+koppl+"\n");
					System.err.println(inFromServer.readLine());
					
					/*
					 * Eingangskanal für Änderung der Simulationseinstellungen
					 */
					
					
				}
				clientSocket.close();			
			} catch (IOException | InterruptedException e) {
				System.err.println("No parameter server found");
				//e.printStackTrace();
			}
		}
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