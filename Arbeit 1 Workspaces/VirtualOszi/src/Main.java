import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.ToolTipManager;

import java.awt.Color;

import Controls.IncrementButton;
import Controls.OsciButton;
import Controls.IncrementButton.FunctionType;
import Display.OsciDisplay;
import Display.OsciDisplayMenu;
import Settings.OsciSettingsSingelton;
import Settings.Settings;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import Communication.*;

public class Main {


	private JFrame frame;
	private OsciDisplay panel;
	private OsciDisplayMenu osciDisplayMenu;
	
	// Resourcebundle languagefile
	ResourceBundle languageresource = ResourceBundle.getBundle("resource.language", Locale.forLanguageTag("en"));
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		//load all settings at start
		OsciSettingsSingelton.getInstance().init();
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
// TCP /  IP 		
		final Thread c=(new Thread(TCPParameterServer.getInstance(frame), "ParameterServer"));
		c.start();
		
		final Thread a=(new Thread(TCP2SampledRing.getInstance(), "ThreadTCPDataRead"));
		a.start();
		
		
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				TCP2SampledRing.getInstance().terminate();
				TCPParameterServer.getInstance(frame).terminate();
				try {
					//panel.test.cancel();
					a.join();
					c.join();
					System.exit(0);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
	}
// Komponenten Initialisierung
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setAlwaysOnTop(true);
		frame.setResizable(false);
		frame.setBounds(0, 0, 1377, 690);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ToolTipManager.sharedInstance().setInitialDelay(2000);


		/*
		 * Oszibutton
		 */
		//RunStop
		OsciButton RunStop = new OsciButton(Settings.RUNSTOP,OsciButton.DrawType.RADIUSED,Color.RED,Color.GREEN);				
		RunStop.setBounds(1115, 40, 98, 50); // IN GuiLS unter ENUM vermerken// unter initialisierung (Mainklasse) eintragen // Unter GuiLS in switch case eintragen
		//Single
		OsciButton Single = new OsciButton(Settings.SINGLE,OsciButton.DrawType.RADIUSED,Color.YELLOW);
		Single.setBounds(1245, 40, 98, 50);

		//DefaultSetup
		OsciButton DefaultSetup = new OsciButton(Settings.DEFAULTSETUP,OsciButton.DrawType.RADIUSED);
		DefaultSetup.setBounds(1125, 105, 70, 35);
		//AutoScale
		OsciButton AutoScale = new OsciButton(Settings.AUTOSCALE,OsciButton.DrawType.RADIUSED,Color.GREEN);
		AutoScale.setBounds(1265, 105, 70, 35);

		//Horiz
		OsciButton Horiz = new OsciButton(Settings.HORIZ,OsciButton.DrawType.RADIUSED);
		Horiz.setBounds(775, 29, 70, 35);
		//Search
		OsciButton Search = new OsciButton(Settings.SEARCH,OsciButton.DrawType.RADIUSED);
		Search.setBounds(775, 70, 70, 35);
		//Navigation
		OsciButton Navigation = new OsciButton(Settings.NAVIGATION,OsciButton.DrawType.RADIUSED);
		Navigation.setBounds(775, 121, 70, 35);

		//Trigger
		OsciButton Trigger = new OsciButton(Settings.TRIGGER,OsciButton.DrawType.RADIUSED);
		Trigger.setBounds(696, 196, 70, 35);
		//ForceTrigger
		OsciButton ForceTrigger = new OsciButton(Settings.FORCETRIGGER,OsciButton.DrawType.RADIUSED);
		ForceTrigger.setBounds(872, 193, 70, 35);
		//ModeCouplling
		OsciButton ModeCouplling = new OsciButton(Settings.MODECOUPLLING,OsciButton.DrawType.RADIUSED);
		ModeCouplling.setBounds(872, 247, 70, 35);

		//Utillity
		OsciButton Utillity = new OsciButton(Settings.UTILLITY,OsciButton.DrawType.RADIUSED);
		Utillity.setBounds(775, 304, 70, 35);
		//WaveGen
		OsciButton WaveGen = new OsciButton(Settings.WAVEGEN,OsciButton.DrawType.RADIUSED,Color.BLUE);
		WaveGen.setBounds(775, 359, 70, 35);
		//QuickAction
		OsciButton QuickAction = new OsciButton(Settings.QUICKACTION,OsciButton.DrawType.RADIUSED);
		QuickAction.setBounds(872, 304, 70, 35);
		//Analyze
		OsciButton Analyze = new OsciButton(Settings.ANALYZE,OsciButton.DrawType.RADIUSED);
		Analyze.setBounds(872, 359, 70, 35);

		//Cursors
		OsciButton Cursors = new OsciButton(Settings.CURSORS,OsciButton.DrawType.RADIUSED);
		Cursors.setBounds(973, 196, 70, 35);
		//Meas
		OsciButton Meas = new OsciButton(Settings.MEAS,"Meas",OsciButton.DrawType.RADIUSED);
		Meas.setBounds(973, 247, 70, 35);

		//Acquire
		OsciButton Acquire = new OsciButton(Settings.ACQUIRE,OsciButton.DrawType.RADIUSED);
		Acquire.setBounds(973, 304, 70, 35);
		//Display
		OsciButton Display = new OsciButton(Settings.DISPLAY,OsciButton.DrawType.RADIUSED);
		Display.setBounds(1078, 304, 70, 35);

		//SaveRecall
		OsciButton SaveRecall = new OsciButton(Settings.SAVERECALL,OsciButton.DrawType.RADIUSED);
		SaveRecall.setBounds(978, 359, 70, 35);
		//Print
		OsciButton Print = new OsciButton(Settings.PRINT,OsciButton.DrawType.RADIUSED);
		Print.setBounds(1078, 362, 70, 35);

		//Serial
		OsciButton Serial = new OsciButton(Settings.SERIAL,OsciButton.DrawType.RADIUSED);
		Serial.setBounds(1186, 215, 70, 35);
		//Digital
		OsciButton Digital = new OsciButton(Settings.DIGITAL,OsciButton.DrawType.RADIUSED);
		Digital.setBounds(1186, 264, 70, 35);
		//Math
		OsciButton _Math = new OsciButton(Settings._MATH,OsciButton.DrawType.RADIUSED);
		_Math.setBounds(1186, 320, 70, 35);
		//Ref
		OsciButton Ref = new OsciButton(Settings.REF,OsciButton.DrawType.RADIUSED);
		Ref.setBounds(1186, 362, 70, 35);

		//One
		OsciButton One = new OsciButton(Settings.ONE,"1",OsciButton.DrawType.RADIUSED,Color.ORANGE);
		One.setBounds(762, 509, 34, 50);
		//Tow
		OsciButton Two = new OsciButton(Settings.TWO,"2",OsciButton.DrawType.RADIUSED,Color.GREEN);
		Two.setBounds(960, 509, 34, 50);
		//Three
		OsciButton Three = new OsciButton(Settings.THREE,"3",OsciButton.DrawType.RADIUSED,Color.CYAN);
		Three.setBounds(1145, 509, 34, 50);
		//Four
		OsciButton Four = new OsciButton(Settings.FOUR,"4",OsciButton.DrawType.RADIUSED,Color.MAGENTA);
		Four.setBounds(1290, 509, 34, 50);
		// Vor
		OsciButton Forward = new OsciButton(Settings.FORWARD,">",OsciButton.DrawType.ROUND);
		Forward.setBounds(1062, 136, 30, 30);
		// ZurÃ¼ck
		OsciButton Backward = new OsciButton(Settings.BACKWARD,"<",OsciButton.DrawType.ROUND);
		Backward.setBounds(899, 136, 30, 30);
		// Stop
		OsciButton Stop = new OsciButton(Settings.STOP,"<>",OsciButton.DrawType.ROUND);
		Stop.setBounds(988, 136, 30, 30);
		//Label
		OsciButton Label = new OsciButton(Settings.LABEL,OsciButton.DrawType.RADIUSED);
		Label.setBounds(872, 509, 70, 35);
		//Help
		OsciButton Help = new OsciButton(Settings.HELP,OsciButton.DrawType.RADIUSED);
		Help.setBounds(1045, 509, 70, 35);

		//Gray1
		OsciButton Gray1 = new OsciButton(Settings.GRAY1);
		Gray1.setBounds(55, 580, 58, 42);
		//Gray2
		OsciButton Gray2 = new OsciButton(Settings.GRAY2);
		Gray2.setBounds(157, 580, 58, 42);
		//Gray3
		OsciButton Gray3 = new OsciButton(Settings.GRAY3);
		Gray3.setBounds(252, 580, 58, 42);
		//Gray4
		OsciButton Gray4 = new OsciButton(Settings.GRAY4);
		Gray4.setBounds(358, 580, 58, 42);
		//Gray5
		OsciButton Gray5 = new OsciButton(Settings.GRAY5);
		Gray5.setBounds(476, 580, 58, 42);
		//Gray6
		OsciButton Gray6 = new OsciButton(Settings.GRAY6);
		Gray6.setBounds(596, 580, 58, 42);
		//Black
		OsciButton Back = new OsciButton(Settings.BACK,OsciButton.DrawType.ROUND);
		Back.setBounds(0, 582, 40, 40);
		// Lupe
		OsciButton Lupe = new OsciButton(Settings.LUPE,"O",OsciButton.DrawType.ROUND);
		Lupe.setBounds(899, 34, 30, 30);
		//Intensity
		OsciButton Intensity = new OsciButton(Settings.INTENSITY,".",OsciButton.DrawType.ROUND,Color.GREEN);
		Intensity.setBounds(700, 375, 10, 10);
		/*
		 * Inkrimentbutton
		 */
		//horizontal_position_knob
		IncrementButton Horizontal_Position_Knob = new IncrementButton(Settings.HORIZONTAL_POSITION_KNOB,FunctionType.PUSH_1);
		Horizontal_Position_Knob.setBounds(978, 60, 40, 40);

		//horizontal_scale_knob
		IncrementButton Horizontal_Scale_Knob = new IncrementButton(Settings.HORIZONTAL_SCALE_KNOB);
		Horizontal_Scale_Knob.setBounds(700, 40, 60, 60);

		//Entry
		IncrementButton Entry = new IncrementButton(Settings.ENTRY, FunctionType.PUSH_1);
		Entry.setBounds(720, 321, 40, 40);

		//Trigger_Level
		IncrementButton Trigger_Level = new IncrementButton(Settings.TRIGGER_LEVEL,FunctionType.PUSH_1);
		Trigger_Level.setBounds(794, 215, 40, 40);

		//vc_scale_knob_one
		IncrementButton vc_scale_knob_one = new IncrementButton(Settings.VC_SCALE_KNOB_ONE);
		vc_scale_knob_one.setBounds(752, 435, 60, 60);
		//Vc_Skalenknopf_TWO
		IncrementButton vc_scale_knob_two = new IncrementButton(Settings.VC_SCALE_KNOB_TWO);
		vc_scale_knob_two.setBounds(950, 435, 60, 60);
		//Vc_Skalenknopf_THREE
		IncrementButton vc_scale_knob_three = new IncrementButton(Settings.VC_SCALE_KNOB_THREE);
		vc_scale_knob_three.setBounds(1135, 435, 60, 60);
		//Vc_Skalenknopf_FOUR
		IncrementButton vc_scale_knob_four = new IncrementButton(Settings.VC_SCALE_KNOB_FOUR);
		vc_scale_knob_four.setBounds(1284, 435, 60, 60);

		//VC_Offset_ONE
		IncrementButton Vc_Offset_one = new IncrementButton(Settings.VC_OFFSET_ONE,FunctionType.PUSH_1);
		Vc_Offset_one.setBounds(762, 594, 40, 40);
		//Vc_Ytwo
		IncrementButton Vc_Offset_two = new IncrementButton(Settings.VC_OFFSET_TWO,FunctionType.PUSH_1);
		Vc_Offset_two.setBounds(970, 594, 40, 40);
		//Vc_xthree
		IncrementButton Vc_Offset_three = new IncrementButton(Settings.VC_OFFSET_THREE,FunctionType.PUSH_1);
		Vc_Offset_three.setBounds(1139, 594, 40, 40);
		//Vc_xfour
		IncrementButton Vc_Offset_four = new IncrementButton(Settings.VC_OFFSET_FOUR,FunctionType.PUSH_1);
		Vc_Offset_four.setBounds(1304, 594, 40, 40);


		//Multiplexed_Scale_1
		IncrementButton Multiplexed_Scale_1 = new IncrementButton(Settings.MULTIPLEXED_SCALE_1);
		Multiplexed_Scale_1.setBounds(1284, 210, 40, 40);
		//Multiplexed_Scale_1
		IncrementButton Multiplexed_Scale_2 = new IncrementButton(Settings.MULTIPLEXED_SCALE_2);
		Multiplexed_Scale_2.setBounds(1284, 321, 40, 40);
		//Cursor
		IncrementButton Coursor = new IncrementButton(Settings.COURSOR,FunctionType.PUSH_1);
		Coursor.setBounds(1097, 215, 40, 40);

		// Initialisierung 


		panel = OsciDisplay.getInstance();
		panel.setBounds(5, 108, 673, 466);

		osciDisplayMenu = OsciDisplayMenu.getInstance();
		osciDisplayMenu.setPanelHeigth(466);
		osciDisplayMenu.init();

		frame.getContentPane().setLayout(null);
		frame.getContentPane().add(Entry);
		frame.getContentPane().add(Back);
		frame.getContentPane().add(Lupe);
		frame.getContentPane().add(Intensity);
		frame.getContentPane().add(Gray1);
		frame.getContentPane().add(Gray2);
		frame.getContentPane().add(Gray3);
		frame.getContentPane().add(Gray4);
		frame.getContentPane().add(Gray5);
		frame.getContentPane().add(Gray6);
		frame.getContentPane().add(panel);
		frame.getContentPane().add(Horizontal_Scale_Knob);
		frame.getContentPane().add(Trigger_Level);
		frame.getContentPane().add(vc_scale_knob_one);
		frame.getContentPane().add(vc_scale_knob_two);
		frame.getContentPane().add(vc_scale_knob_three);
		frame.getContentPane().add(vc_scale_knob_four);
		frame.getContentPane().add(Vc_Offset_one);
		frame.getContentPane().add(Vc_Offset_two);
		frame.getContentPane().add(Vc_Offset_three);
		frame.getContentPane().add(Vc_Offset_four);
		frame.getContentPane().add(Multiplexed_Scale_1);
		frame.getContentPane().add(Multiplexed_Scale_2);
		frame.getContentPane().add(Coursor);
		frame.getContentPane().add(RunStop);
		frame.getContentPane().add(DefaultSetup);
		frame.getContentPane().add(AutoScale);
		frame.getContentPane().add(Horizontal_Position_Knob);
		frame.getContentPane().add(Single);
		frame.getContentPane().add(Search);
		frame.getContentPane().add(Navigation);
		frame.getContentPane().add(Trigger);
		frame.getContentPane().add(ForceTrigger);
		frame.getContentPane().add(ModeCouplling);
		frame.getContentPane().add(Utillity);
		frame.getContentPane().add(WaveGen);
		frame.getContentPane().add(QuickAction);
		frame.getContentPane().add(Analyze);
		frame.getContentPane().add(Cursors);
		frame.getContentPane().add(Meas);
		frame.getContentPane().add(Acquire);
		frame.getContentPane().add(SaveRecall);
		frame.getContentPane().add(Display);
		frame.getContentPane().add(Print);
		frame.getContentPane().add(Serial);
		frame.getContentPane().add(Digital);
		frame.getContentPane().add(_Math);
		frame.getContentPane().add(Ref);
		frame.getContentPane().add(Horiz);
		frame.getContentPane().add(One);
		frame.getContentPane().add(Two);
		frame.getContentPane().add(Three);
		frame.getContentPane().add(Four);
		frame.getContentPane().add(Label);
		frame.getContentPane().add(Help);
		frame.getContentPane().add(Backward);
		frame.getContentPane().add(Stop);
		frame.getContentPane().add(Forward);
		
		/*
		 * Labels
		 */
		JLabel lblWaveform = new JLabel(languageresource.getString("label_waveform"));
		lblWaveform.setFont(new Font("Sylfaen", Font.PLAIN, 20));
		lblWaveform.setBounds(1017, 284, 98, 20);
		frame.getContentPane().add(lblWaveform);

		JLabel lblRunControl = new JLabel(languageresource.getString("label_runcontrol"));
		lblRunControl.setFont(new Font("Sylfaen", Font.PLAIN, 20));
		lblRunControl.setBounds(1173, 4, 131, 20);
		frame.getContentPane().add(lblRunControl);

		JLabel lblmeasure = new JLabel(languageresource.getString("label_measure"));
		lblmeasure.setFont(new Font("Sylfaen", Font.PLAIN, 20));
		lblmeasure.setBounds(1027, 172, 88, 20);
		frame.getContentPane().add(lblmeasure);

		JLabel lbltrigger = new JLabel(languageresource.getString("label_trigger"));
		lbltrigger.setFont(new Font("Sylfaen", Font.PLAIN, 20));
		lbltrigger.setBounds(761, 172, 101, 20);
		frame.getContentPane().add(lbltrigger);

		JLabel lblfile = new JLabel(languageresource.getString("label_file"));
		lblfile.setFont(new Font("Sylfaen", Font.PLAIN, 20));
		lblfile.setBounds(1047, 341, 58, 20);
		frame.getContentPane().add(lblfile);

		JLabel lbltools = new JLabel(languageresource.getString("label_tools"));
		lbltools.setFont(new Font("Sylfaen", Font.PLAIN, 20));
		lbltools.setBounds(831, 284, 51, 20);
		frame.getContentPane().add(lbltools);

		JLabel lblvertical = new JLabel(languageresource.getString("label_vertical"));
		lblvertical.setFont(new Font("Sylfaen", Font.PLAIN, 20));
		lblvertical.setBounds(1006, 413, 76, 20);
		frame.getContentPane().add(lblvertical);

		JLabel lblhorizontal = new JLabel(languageresource.getString("label_horizontal"));
		lblhorizontal.setFont(new Font("Sylfaen", Font.PLAIN, 20));
		lblhorizontal.setBounds(685, 4, 111, 20);
		frame.getContentPane().add(lblhorizontal);
		
		/*
		 * SINUS
		 */

		// Sinus gestaucht
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(Main.class.getResource("/images/sinuns.png")));
		lblNewLabel.setBounds(682, 91, 30, 30);
		frame.getContentPane().add(lblNewLabel);

		JLabel label = new JLabel("");
		label.setIcon(new ImageIcon(Main.class.getResource("/images/sinuns.png")));
		label.setBounds(741, 91, 30, 30);
		frame.getContentPane().add(label);

		JLabel label_1 = new JLabel("");
		label_1.setIcon(new ImageIcon(Main.class.getResource("/images/sinuns.png")));
		label_1.setBounds(1265, 239, 30, 30);
		frame.getContentPane().add(label_1);

		JLabel label_2 = new JLabel("");
		label_2.setIcon(new ImageIcon(Main.class.getResource("/images/sinuns.png")));
		label_2.setBounds(730, 475, 30, 30);
		frame.getContentPane().add(label_2);

		JLabel label_3 = new JLabel("");
		label_3.setIcon(new ImageIcon(Main.class.getResource("/images/sinuns.png")));
		label_3.setBounds(926, 475, 30, 30);
		frame.getContentPane().add(label_3);

		JLabel label_4 = new JLabel("");
		label_4.setIcon(new ImageIcon(Main.class.getResource("/images/sinuns.png")));
		label_4.setBounds(1107, 475, 30, 30);
		frame.getContentPane().add(label_4);


		JLabel label_7 = new JLabel("");
		label_7.setIcon(new ImageIcon(Main.class.getResource("/images/sinuns.png")));
		label_7.setBounds(1254, 475, 30, 30);
		frame.getContentPane().add(label_7);

		// Sinus gestreckt
		JLabel label_8 = new JLabel("");
		label_8.setIcon(new ImageIcon(Main.class.getResource("/images/sin_2.png")));
		label_8.setBounds(1326, 239, 30, 34);
		frame.getContentPane().add(label_8);

		JLabel label_9 = new JLabel("");
		label_9.setIcon(new ImageIcon(Main.class.getResource("/images/sin_2.png")));
		label_9.setBounds(815, 471, 30, 34);
		frame.getContentPane().add(label_9);

		JLabel label_10 = new JLabel("");
		label_10.setIcon(new ImageIcon(Main.class.getResource("/images/sin_2.png")));
		label_10.setBounds(1013, 471, 30, 34);
		frame.getContentPane().add(label_10);

		JLabel label_11 = new JLabel("");
		label_11.setIcon(new ImageIcon(Main.class.getResource("/images/sin_2.png")));
		label_11.setBounds(1197, 471, 30, 34);
		frame.getContentPane().add(label_11);

		JLabel label_12 = new JLabel("");
		label_12.setIcon(new ImageIcon(Main.class.getResource("/images/sin_2.png")));
		label_12.setBounds(1341, 471, 30, 34);
		frame.getContentPane().add(label_12);
		/*
		 * <  > Pfeile
		 */
		JLabel label_13 = new JLabel("");
		label_13.setIcon(new ImageIcon(Main.class.getResource("/images/dreieck.png")));
		label_13.setBounds(804, 600, 30, 34);
		frame.getContentPane().add(label_13);

		JLabel label_14 = new JLabel("");
		label_14.setIcon(new ImageIcon(Main.class.getResource("/images/dreieck.png")));
		label_14.setBounds(1006, 600, 30, 34);
		frame.getContentPane().add(label_14);

		JLabel label_15 = new JLabel("");
		label_15.setIcon(new ImageIcon(Main.class.getResource("/images/dreieck.png")));
		label_15.setBounds(1173, 600, 30, 34);
		frame.getContentPane().add(label_15);

		JLabel label_16 = new JLabel("");
		label_16.setIcon(new ImageIcon(Main.class.getResource("/images/dreieck.png")));
		label_16.setBounds(1341, 600, 30, 34);
		frame.getContentPane().add(label_16);

		JLabel label_17 = new JLabel("");
		label_17.setIcon(new ImageIcon(Main.class.getResource("/images/dreieck.png")));
		label_17.setBounds(1326, 327, 30, 34);
		frame.getContentPane().add(label_17);
		/*
		 * Abgrenzungen
		 */
		JLabel lblNewLabel_1 =  new JLabel(""); //$NON-NLS-1$
		lblNewLabel_1.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung.png")));
		lblNewLabel_1.setBounds(1093, 418, 251, 9);
		frame.getContentPane().add(lblNewLabel_1);

		JLabel label_18 = new JLabel("");
		label_18.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung.png")));
		label_18.setBounds(743, 418, 251, 9);
		frame.getContentPane().add(label_18);

		JLabel label_19 = new JLabel("");
		label_19.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung.png")));
		label_19.setBounds(1118, 290, 30, 9);
		frame.getContentPane().add(label_19);

		JLabel label_20 = new JLabel("");
		label_20.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung.png")));
		label_20.setBounds(980, 290, 30, 9);
		frame.getContentPane().add(label_20);

		JLabel label_21 = new JLabel("");
		label_21.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung.png")));
		label_21.setBounds(1088, 346, 60, 9);
		frame.getContentPane().add(label_21);

		JLabel label_22 = new JLabel("");
		label_22.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung.png")));
		label_22.setBounds(983, 346, 60, 9);
		frame.getContentPane().add(label_22);

		JLabel label_23 = new JLabel("");
		label_23.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung.png")));
		label_23.setBounds(1107, 176, 30, 9);
		frame.getContentPane().add(label_23);

		JLabel label_24 = new JLabel("");
		label_24.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung.png")));
		label_24.setBounds(981, 176, 40, 9);
		frame.getContentPane().add(label_24);

		JLabel label_25 = new JLabel("");
		label_25.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung.png")));
		label_25.setBounds(882, 290, 58, 9);
		frame.getContentPane().add(label_25);

		JLabel label_26 = new JLabel("");
		label_26.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung.png")));
		label_26.setBounds(776, 290, 51, 9);
		frame.getContentPane().add(label_26);

		JLabel label_27 = new JLabel("");
		label_27.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung.png")));
		label_27.setBounds(831, 176, 111, 9);
		frame.getContentPane().add(label_27);

		JLabel label_28 = new JLabel("");
		label_28.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung.png")));
		label_28.setBounds(696, 176, 64, 9);
		frame.getContentPane().add(label_28);

		JLabel label_29 = new JLabel("");
		label_29.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung.png")));
		label_29.setBounds(1115, 10, 51, 9);
		frame.getContentPane().add(label_29);

		JLabel label_30 = new JLabel("");
		label_30.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung.png")));
		label_30.setBounds(1284, 10, 51, 9);
		frame.getContentPane().add(label_30);

		JLabel label_31 = new JLabel("");
		label_31.setIcon(new ImageIcon("/images/Abgrenzung.png"));
		label_31.setBounds(950, 193, 4, 90);
		frame.getContentPane().add(label_31);

		JLabel label_32 = new JLabel((String) null);
		label_32.setIcon(new ImageIcon("/images/Abgrenzung.png"));
		label_32.setBounds(950, 304, 4, 90);
		frame.getContentPane().add(label_32);
		
		/*
		 * Math Funktionen Dreiecke
		 */
		JLabel label_33 = new JLabel("");
		label_33.setIcon(new ImageIcon(Main.class.getResource("/images/Dreieck_kl.png")));
		label_33.setBounds(1155, 215, 20, 20);
		frame.getContentPane().add(label_33);

		JLabel label_34 = new JLabel("");
		label_34.setIcon(new ImageIcon(Main.class.getResource("/images/Dreieck_kl.png")));
		label_34.setBounds(1155, 265, 20, 20);
		frame.getContentPane().add(label_34);

		JLabel label_35 = new JLabel("");
		label_35.setIcon(new ImageIcon(Main.class.getResource("/images/Dreieck_kl.png")));
		label_35.setBounds(1155, 315, 20, 20);
		frame.getContentPane().add(label_35);

		JLabel label_36 = new JLabel("");
		label_36.setIcon(new ImageIcon(Main.class.getResource("/images/Dreieck_kl.png")));
		label_36.setBounds(1155, 365, 20, 20);
		frame.getContentPane().add(label_36);
		
		/*
		 * Math Funktionen Abgrenzungen
		 */
		JLabel label_37 = new JLabel("");
		label_37.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung_2.png")));
		label_37.setBounds(1165, 235, 4, 28);
		frame.getContentPane().add(label_37);

		JLabel label_38 = new JLabel("");
		label_38.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung_2.png")));
		label_38.setBounds(1165, 285, 4, 28);
		frame.getContentPane().add(label_38);

		JLabel label_39 = new JLabel("");
		label_39.setIcon(new ImageIcon(Main.class.getResource("/images/Abgrenzung_2.png")));
		label_39.setBounds(1165, 335, 4, 28);
		frame.getContentPane().add(label_39);

		// DHBW Logo
		JLabel lblNewLabel_2 = new JLabel("");
		lblNewLabel_2.setIcon(new ImageIcon(Main.class.getResource("/images/DHBW_Logo.png")));
		lblNewLabel_2.setBounds(2, 2, 210, 111);
		frame.getContentPane().add(lblNewLabel_2);
		/*
		 * Push for Fine & Push to zero & Push for 50%
		 */
		JLabel label_40 = new JLabel(languageresource.getString("label_push_ff"));
		label_40.setBounds(709, 116, 51, 40);
		label_40.setFont(new Font("Sylfaen", Font.PLAIN, 10));
		frame.getContentPane().add(label_40);

		JLabel label_41 = new JLabel(languageresource.getString("label_push_ff"));
		label_41.setFont(new Font("Sylfaen", Font.PLAIN, 10));
		label_41.setBounds(848, 441, 51, 40);
		frame.getContentPane().add(label_41);

		JLabel label_42 = new JLabel(languageresource.getString("label_push_ff"));
		label_42.setFont(new Font("Sylfaen", Font.PLAIN, 10));
		label_42.setBounds(1230, 441, 51, 40);
		frame.getContentPane().add(label_42);

		JLabel label_43 = new JLabel(languageresource.getString("label_push_tz"));
		label_43.setFont(new Font("Sylfaen", Font.PLAIN, 10));
		label_43.setBounds(848, 594, 51, 40);
		frame.getContentPane().add(label_43);

		JLabel label_44 = new JLabel(languageresource.getString("label_push_tz"));
		label_44.setFont(new Font("Sylfaen", Font.PLAIN, 10));
		label_44.setBounds(1207, 594, 51, 40);
		frame.getContentPane().add(label_44);

		JLabel label_45 = new JLabel(languageresource.getString("label_push_tz"));
		label_45.setFont(new Font("Sylfaen", Font.PLAIN, 10));
		label_45.setBounds(1041, 35, 51, 40);
		frame.getContentPane().add(label_45);

		JLabel label_46 = new JLabel(languageresource.getString("label_push_ff_1"));
		label_46.setFont(new Font("Sylfaen", Font.PLAIN, 10));
		label_46.setBounds(1280, 174, 76, 20);
		frame.getContentPane().add(label_46);

		JLabel label_47 = new JLabel(languageresource.getString("label_push_tz_1"));
		label_47.setFont(new Font("Sylfaen", Font.PLAIN, 10));
		label_47.setBounds(1280, 285, 76, 20);
		frame.getContentPane().add(label_47);

		JLabel label_48 = new JLabel(languageresource.getString("label_push_ts"));
		label_48.setFont(new Font("Sylfaen", Font.PLAIN, 10));
		label_48.setBounds(1090, 193, 76, 20);
		frame.getContentPane().add(label_48);

		JLabel label_49 = new JLabel(languageresource.getString("label_push_f50"));
		label_49.setFont(new Font("Sylfaen", Font.PLAIN, 10));
		label_49.setBounds(781, 196, 76, 20);
		frame.getContentPane().add(label_49);

		JLabel label_50 = new JLabel(languageresource.getString("label_push_ts"));
		label_50.setFont(new Font("Sylfaen", Font.PLAIN, 10));
		label_50.setBounds(700, 293, 76, 20);
		frame.getContentPane().add(label_50);

		JLabel label_51 = new JLabel(languageresource.getString("label_intensity"));
		label_51.setFont(new Font("Sylfaen", Font.PLAIN, 10));
		label_51.setBounds(720, 374, 51, 20);
		frame.getContentPane().add(label_51);

		JLabel label_52 = new JLabel(languageresource.getString("label_level"));
		label_52.setFont(new Font("Sylfaen", Font.PLAIN, 15));
		label_52.setBounds(794, 260, 40, 20);
		frame.getContentPane().add(label_52);
		// Beschiftung Ã¼ber Display
		JLabel label_53 = new JLabel(languageresource.getString("label_name"));
		label_53.setFont(new Font("Sylfaen", Font.PLAIN, 35));
		label_53.setBounds(246, 2, 231, 120);
		frame.getContentPane().add(label_53);

		JLabel label_54 = new JLabel(languageresource.getString("label_version"));
		label_54.setFont(new Font("Sylfaen", Font.PLAIN, 35));
		label_54.setBounds(430, 2, 167, 120);
		frame.getContentPane().add(label_54);

		JLabel label_55 = new JLabel(languageresource.getString("label_mhz"));
		label_55.setFont(new Font("Sylfaen", Font.PLAIN, 25));
		label_55.setBounds(526, 2, 98, 120);
		frame.getContentPane().add(label_55);

		// S Felder 1-3
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(204, 204, 204));
		panel_1.setBounds(1152, 174, 204, 236);
		frame.getContentPane().add(panel_1);
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addGap(0, 190, Short.MAX_VALUE)
		);
		gl_panel_1.setVerticalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addGap(0, 165, Short.MAX_VALUE)
		);
		panel_1.setLayout(gl_panel_1);

		JPanel panel_3 = new JPanel();
		panel_3.setBackground(new Color(128, 128, 128));
		panel_3.setBounds(696, 252, 77, 145);
		frame.getContentPane().add(panel_3);

		JLabel lblNewLabel_3 = new JLabel("");
		lblNewLabel_3.setIcon(new ImageIcon(Main.class.getResource("/images/Umdrehung.png")));
		GroupLayout gl_panel_3 = new GroupLayout(panel_3);
		gl_panel_3.setHorizontalGroup(
				gl_panel_3.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_3.createSequentialGroup()
								.addContainerGap()
								.addComponent(lblNewLabel_3, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		gl_panel_3.setVerticalGroup(
				gl_panel_3.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_3.createSequentialGroup()
								.addComponent(lblNewLabel_3, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)
								.addContainerGap(98, Short.MAX_VALUE))
		);
		panel_3.setLayout(gl_panel_3);

		JLabel lblNewLabel_4 = new JLabel(languageresource.getString("Main.lblNewLabel_4.text"));
		frame.getContentPane().add(lblNewLabel_4);

		JPanel panel_2 = new JPanel();
		panel_2.setBackground(new Color(204, 204, 204));
		panel_2.setBounds(682, 4, 418, 166);
		frame.getContentPane().add(panel_2);
		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
				gl_panel_2.createParallelGroup(Alignment.LEADING)
						.addGap(0, 204, Short.MAX_VALUE)
						.addGap(0, 204, Short.MAX_VALUE)
		);
		gl_panel_2.setVerticalGroup(
				gl_panel_2.createParallelGroup(Alignment.LEADING)
						.addGap(0, 236, Short.MAX_VALUE)
						.addGap(0, 236, Short.MAX_VALUE)
		);
		panel_2.setLayout(gl_panel_2);

		JLabel lblCursors = new JLabel(languageresource.getString("Main.lblCursors.text")); //$NON-NLS-1$
		lblCursors.setFont(new Font("Sylfaen", Font.PLAIN, 15));
		lblCursors.setBounds(1097, 262, 51, 20);
		frame.getContentPane().add(lblCursors);


	}
}
