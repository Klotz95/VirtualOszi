import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import Communication.Data2TCPSingelton;
import Communication.TCPParameterClient;

public class Main
{
    public static void main(String[] args)
    {
    	
    	
    	//Interface für Benutzer
    	/*
    	 * jeweils eine Buttongroup pro Parameter
    	 * mit einem RadioButton für jede Einstellung
    	 * Verbindung zwischen Radiobutton und jeweiligem Setting ?Eventlistener?
    	 * bei Auswahl eines Buttons => "set:Parameter:Einstellung" 
    	 */
        JFrame frame = new JFrame("Interface-Einstellungen");
        frame.setSize(900,500);        
        frame.setVisible(true);
         
        JRadioButton radio1_1 = new JRadioButton ("Param1-Einst1", true);        
        JRadioButton radio1_2 = new JRadioButton ("Param1-Einst2", true);        
        JRadioButton radio1_3 = new JRadioButton ("Param1-Einst3", true);
        JRadioButton radio2_1 = new JRadioButton ("Param2-Einst1", true);        
        JRadioButton radio2_2 = new JRadioButton ("Param2-Einst2", true);
        
        ButtonGroup gruppe1 = new ButtonGroup();
        ButtonGroup gruppe2 = new ButtonGroup();
        gruppe1.add(radio1_1);
        gruppe1.add(radio1_2);
        gruppe1.add(radio1_3);
        gruppe2.add(radio2_1);
        gruppe2.add(radio2_2);
        /*
        frame.add(radio1_1);
        frame.add(radio1_2);
        frame.add(radio1_3);
        frame.add(radio2_1);
        frame.add(radio2_2);
        */
        JPanel panel = new JPanel();
        panel.add(radio1_1);
        panel.add(radio1_2);
        panel.add(radio1_3);
        panel.add(radio2_1);
        panel.add(radio2_2);
        frame.add(panel);
        
        frame.setVisible(true);	//Ende Interface
        
        
		final Thread d=(new Thread(TCPParameterClient.getInstance(), "ParameterClient"));
		d.start();
		
		final Thread b=(new Thread(Data2TCPSingelton.getInstance(), "ThreadTCPDataSend"));
		b.start();

		
        
		frame.addWindowListener(new WindowAdapter()
		{
			
			
			@Override
			public void windowClosing(WindowEvent e)
			{
				TCPParameterClient.getInstance().terminate();
				Data2TCPSingelton.getInstance().terminate();
				try {
					//panel.test.cancel();
					b.join();
					d.join();
					System.exit(0);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
    }
}

