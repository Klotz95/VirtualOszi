package Settings;
import Controls.MyGuiEvent;
import Controls.MyGuiEventInterface;
import DataRing.RingBufferManager;
import Display.OsciDisplay;
import Display.OsciDisplayMenu;
import Display.MeasurementManager;
import Trigger.TriggerConditionManager;

public class GuiListenerSingleton implements MyGuiEventInterface {
	private OsciDisplayMenu osciMenu = OsciDisplayMenu.getInstance();
	private OsciDisplay osciDisplay = OsciDisplay.getInstance();

	private static final class InstanceHolder {
		static final GuiListenerSingleton INSTANCE = new GuiListenerSingleton();
	}

	private GuiListenerSingleton () {
	}

	public static GuiListenerSingleton getInstance () {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Changes the value of a given setting option to the given value.
	 * @param setting The Setting to be changed.
	 * @param value The Value to whom the setting will be changed.
	 */
	public void handleMenuButtonSetEntryMarkedEvent(Settings setting, String value) {
		System.err.println("Set Entry Marked Event: " + setting + " " + value);
		switch(setting){
		case TRIG_TYPE: 
			TriggerConditionManager.getInstance().setTriggerCondition(value);
			// System.err.println("Set Triggertyp: "+value);
			break;
			/* Für weitere Abfragen hier weitere cases einfügen*/
		case DISPLAY_MENU:
			switch(value){
			case "Variable Nachleuchtdauer":
				osciDisplay.startPersistenceTimer();
				break;
			default:
				osciDisplay.stopPersistenceTimer();				
				break;
			}
			break;
		case CHANNEL:
			TriggerConditionManager.getInstance().setChannel(value);
			break;
		case HORIZ_NORMAL_TIME_REF:
			restartAquisition();
			break;			
		case ACQUIRE_MOD:
			osciDisplay.recalcViewBuffer();
			break;
		case COUPLING_CHANNEL_1:
		case COUPLING_CHANNEL_2:
		case COUPLING_CHANNEL_3:
		case COUPLING_CHANNEL_4:
		case COUPLING:		
			resetRings();
			break;
		case A_HELP_LANGUAGE:
			break;
		default: break;
		} // switch(s)

	} // handleMenuButtonSetEntryMarkedEvent()


	public void handleTickButtonEvent(Settings buttonID,int sign) {
		System.err.println("TickButton "+ buttonID);

		int itmp;
		switch(buttonID) {
		case DISPLAY_INTESITY:
			itmp=buttonID.getInt()+sign;
			if(itmp<0) itmp=0;
			else if(itmp>100) itmp=100;
			buttonID.set(itmp);
			break;
		case DISPLAY_VAR_LIGHT_TIME:
			itmp=buttonID.getInt()+sign;
			if(itmp<0) itmp=0;
			else if(itmp>60) itmp=60;
			buttonID.set(itmp);
			break;
		case ACQUIRE_MIDLE:
			itmp=(31-Integer.numberOfLeadingZeros(buttonID.getInt())+sign);
			if(itmp<1) itmp=1;
			else if(itmp>16) itmp=16;
			buttonID.set(2<<(itmp-1));
			break;

		default:
			buttonID.set(buttonID.getInt()+sign);
			break;
		}
	}



	public void handleClickButtonEvent(Settings buttonID) {
		System.err.println("ClickButton "+ buttonID);

		switch(buttonID) {
		case ADD_MEASSURE:
			int channelNo1 = Settings.CHANNEL_MEAS.getInt(), channelNo2 = Settings.MEAS_CHANNEL_TWO.getInt();
			switch (Settings.MEAS_TYPE.get()) {
			case "Snapshot All":
				break;
			case "Spitze-Spitze":
				MeasurementManager.getInstance().addMeasurement(MeasType.PEEK_PEEK,channelNo1,channelNo2);
				break;
			case "Maximum":
				MeasurementManager.getInstance().addMeasurement(MeasType.MAXIMUM,channelNo1,channelNo2);
				break;
			case "Minimum":
				MeasurementManager.getInstance().addMeasurement(MeasType.MINIMUM,channelNo1,channelNo2);
				break;
			case "Amplitude":
				MeasurementManager.getInstance().addMeasurement(MeasType.AMPLITUDE,channelNo1,channelNo2);
				break;
			case "Oben":
				MeasurementManager.getInstance().addMeasurement(MeasType.OBEN,channelNo1,channelNo2);
				break;
			case "Basis":
				MeasurementManager.getInstance().addMeasurement(MeasType.BASIS,channelNo1,channelNo2);
				break;
			case "Überschwingen":
				MeasurementManager.getInstance().addMeasurement(MeasType.OVERSHOOT,channelNo1,channelNo2);
				break;
			case "Vorschwingen":
				MeasurementManager.getInstance().addMeasurement(MeasType.PRESHOOT,channelNo1,channelNo2);
				break;
			case "Mittelwert - N Zyklen":
				MeasurementManager.getInstance().addMeasurement(MeasType.AVERAGE_CYCLE,channelNo1,channelNo2);
				break;
			case "Mittelwert - Vollbild":
				MeasurementManager.getInstance().addMeasurement(MeasType.AVERAGE_SCREEN,channelNo1,channelNo2);
				break;
			case "DC-RMS - N Zyklen":
				MeasurementManager.getInstance().addMeasurement(MeasType.DC_RMS_CYCLE,channelNo1,channelNo2);
				break;
			case "DC-RMS - Vollbild":
				MeasurementManager.getInstance().addMeasurement(MeasType.DC_RMS_SCREEN,channelNo1,channelNo2);
				break;
			case "AC-RMS - N Zyklen":
				MeasurementManager.getInstance().addMeasurement(MeasType.AC_RMS_CYCLE,channelNo1,channelNo2);
				break;
			case "AC-RMS - Vollbild":
				MeasurementManager.getInstance().addMeasurement(MeasType.AC_RMS_SCREEN,channelNo1,channelNo2);
				break;
			case "Verh. - N Zyklen":
				MeasurementManager.getInstance().addMeasurement(MeasType.RATIO_CYCLE,channelNo1,channelNo2);
				break;
			case "Verh. - Vollbild":
				MeasurementManager.getInstance().addMeasurement(MeasType.RATIO_SCREEN,channelNo1,channelNo2);
				break;
			case "Periode":
				MeasurementManager.getInstance().addMeasurement(MeasType.PERIOD,channelNo1,channelNo2);
				break;
			case "Frequenz":
				MeasurementManager.getInstance().addMeasurement(MeasType.FREQUENCY,channelNo1,channelNo2);
				break;
			case "Zähler":
				MeasurementManager.getInstance().addMeasurement(MeasType.COUNTER,channelNo1,channelNo2);
				break;
			case "+Breite":
				MeasurementManager.getInstance().addMeasurement(MeasType.WIDTH_PLUS,channelNo1,channelNo2);
				break;
			case "-Breite":
				MeasurementManager.getInstance().addMeasurement(MeasType.WIDTH_MINUS,channelNo1,channelNo2);
				break;
			case "Burst-Breite":
				MeasurementManager.getInstance().addMeasurement(MeasType.BURST_WIDTH,channelNo1,channelNo2);
				break;
			case "Arbeitszyklus":
				MeasurementManager.getInstance().addMeasurement(MeasType.DUTY_CYCLE,channelNo1,channelNo2);
				break;
			case "Anstiegszeit":
				MeasurementManager.getInstance().addMeasurement(MeasType.RISE_TIME,channelNo1,channelNo2);
				break;
			case "Abstiegszeit":
				MeasurementManager.getInstance().addMeasurement(MeasType.FALL_TIME,channelNo1,channelNo2);
				break;
			case "Verzögerung":
				MeasurementManager.getInstance().addMeasurement(MeasType.DELAY,channelNo1,channelNo2);
				break;
			case "Phase":
				MeasurementManager.getInstance().addMeasurement(MeasType.PHASE,channelNo1,channelNo2);
				break;
			case "X bei Min. Y":
				MeasurementManager.getInstance().addMeasurement(MeasType.X_AT_MIN_Y,channelNo1,channelNo2);
				break;
			case "X bei Max. Y":
				MeasurementManager.getInstance().addMeasurement(MeasType.X_AT_MAX_Y,channelNo1,channelNo2);
				break;
			case "Anz. positive Impulse":
				MeasurementManager.getInstance().addMeasurement(MeasType.POSITIVE_PULSE_COUNT,channelNo1,channelNo2);
				break;
			case "Anz. negative Impulse":
				MeasurementManager.getInstance().addMeasurement(MeasType.NEGATIVE_PULSE_COUNT,channelNo1,channelNo2);
				break;
			case "Anz. steigende Flanken":
				MeasurementManager.getInstance().addMeasurement(MeasType.RISING_EDGE_COUNT,channelNo1,channelNo2);
				break;
			case "Anz. fallende Flanken":
				MeasurementManager.getInstance().addMeasurement(MeasType.FALLING_EDGE_COUNT,channelNo1,channelNo2);
				break;
			case "Bereich - N Zyklen":
				MeasurementManager.getInstance().addMeasurement(MeasType.AREA_CYCLE,channelNo1,channelNo2);
				break;
			case "Bereich - Vollbild":
				MeasurementManager.getInstance().addMeasurement(MeasType.AREA_SCREEN,channelNo1,channelNo2);
				break;
			default:
				break;
			}
			break;

		default:
			break;
		case MEAS_DELL_ONE:
			MeasurementManager.getInstance().removeMeasurement(0);
			break;
		case MEAS_DELL_TWO:
			MeasurementManager.getInstance().removeMeasurement(1);
			break;
		case MEAS_DELL_THREE:
			MeasurementManager.getInstance().removeMeasurement(2);
			break;
		case MEAS_DELL_FOUR:
			MeasurementManager.getInstance().removeMeasurement(3);
			break;
		case MEAS_DELL_ALL:
			for (int i=0; i<4; i++)
				MeasurementManager.getInstance().removeMeasurement(0);
			break;
		case DISPLAY_DEL_PER:
			osciDisplay.delPersistence();
			break;
		case A_DISPLAY_SET:
			osciDisplay.addPersistence();
			break;
		case DISPLAY_DEL_DISPLAY:
			osciDisplay.delDisplay();
			break;
		case A_SAVERECALL_SET:
			OsciSettingsSingelton.getInstance().saveProp();
			break;
		case A_SAVERECALL_RECALL:
			OsciSettingsSingelton.getInstance().loadProp();
			break;
		}
	}


	public void handleToggleButtonEvent(Settings buttonID) {
		System.err.println("Toggle Button " + buttonID);
		switch(buttonID){
		case BW_LIMIT_1:
		case BW_LIMIT_2:
		case BW_LIMIT_3:
		case HF_CANCELLING:
			resetRings();
			break;
		default:
			break;
		}

	}

	public void handleTickButtonEvent(Settings buttonID) {
		System.err.println("Tick Button " + buttonID);
	}


	public void handleClickEvent(MyGuiEvent e){

		System.err.println(" Click "+e.id.name());
		switch(e.id){

		case BACK:
			osciMenu.clickBackButton();
			break;
		case FORWARD:
			break;
		case BACKWARD:
			break;
		case LUPE:
			break;
		case STOP:
			break;
		case INTENSITY:
			break;
		case GRAY1:
			osciMenu.setMarked(0);
			break;
		case GRAY2:
			osciMenu.setMarked(1);
			break;
		case GRAY3:
			osciMenu.setMarked(2);
			break;
		case GRAY4:
			osciMenu.setMarked(3);
			break;
		case GRAY5:
			osciMenu.setMarked(4);
			break;
		case GRAY6:
			osciMenu.setMarked(5);
			break;
		case TRIGGER_LEVEL:
			String channelStr = Settings.CHANNEL.get();
			switch(channelStr){
			case "1": 
				Settings.TRIGGER_LEVEL_ONE.set(0);
				break;
			case "2": 
				Settings.TRIGGER_LEVEL_TWO.set(0);
				break;
			case "3": 
				Settings.TRIGGER_LEVEL_THREE.set(0); 
				break;
			case "4": 
				Settings.TRIGGER_LEVEL_FOUR.set(0); 
				break;
			}
			resetTrigger();
			break;
		case VC_SCALE_KNOB_ONE:
			break;
		case VC_SCALE_KNOB_TWO:
			break;
		case VC_SCALE_KNOB_THREE:
			break;
		case VC_SCALE_KNOB_FOUR:
			break;
		case VC_OFFSET_ONE:
			e.id.set(0);
			osciDisplay.updateOffset(1);
			restartAquisition();
			break;
		case VC_OFFSET_TWO:
			e.id.set(0);
			osciDisplay.updateOffset(2);
			restartAquisition();
			break;
		case VC_OFFSET_THREE:
			e.id.set(0);
			osciDisplay.updateOffset(3);
			restartAquisition();
			break;
		case VC_OFFSET_FOUR:
			e.id.set(0);
			osciDisplay.updateOffset(4);
			restartAquisition();
			break;
		case MULTIPLEXED_SCALE_1:
			break;
		case MULTIPLEXED_SCALE_2:
			break;
		case COURSOR:
			break;
		case HORIZONTAL_POSITION_KNOB:
			Settings.HORIZONTAL_POSITION_KNOB.set(0);
			restartAquisition();
			break;
		case HORIZONTAL_SCALE_KNOB:
			break;
		case RUNSTOP:
			break;
		case SINGLE:
			break;
		case DEFAULTSETUP:
			break;
		case AUTOSCALE:
			break;
		case HORIZ:
			osciMenu.setNewButtonList(Settings.A_HORIZ);
			break;
		case SEARCH:
			break;
		case NAVIGATION:
			osciMenu.setNewButtonList(Settings.A_NAVIGATE);
			break;
		case TRIGGER:
			osciMenu.setNewButtonList(Settings.A_TRIGGER);
			break;
		case FORCETRIGGER:
			TriggerConditionManager.getInstance().forceTrigger();
			break;
		case MODECOUPLLING:
			osciMenu.setNewButtonList(Settings.A_MODE_COUPLING);
			break;
		case UTILLITY:
			break;
		case WAVEGEN:
			break;
		case QUICKACTION:
			break;
		case ANALYZE:
			osciMenu.setNewButtonList(Settings.A_ANALYSE);
			break;
		case CURSORS:
			osciMenu.setNewButtonList(Settings.A_CURSORS_MEUNU);
			MeasurementManager.getInstance().setShownArea("cursor");
			break;
		case MEAS:
			osciMenu.setNewButtonList(Settings.A_MEAS_MENU);
			MeasurementManager.getInstance().setShownArea("measurement");
			break;
		case ACQUIRE:
			osciMenu.setNewButtonList(Settings.A_ACQUIRE_MENU);
			break;
		case SAVERECALL:
			osciMenu.setNewButtonList(Settings.A_SAVERECALL);
			break;
		case DISPLAY:
			osciMenu.setNewButtonList(Settings.A_DISPLAY_MENU);
			break;
		case PRINT:
			break;
		case SERIAL:
			break;
		case DIGITAL:
			break;
		case _MATH:
			osciMenu.setNewButtonList(Settings.A_MATH_MENU);
			break;
		case REF:
			osciMenu.setNewButtonList(Settings.A_REF_MENU);
			break;
		case ENTRY:
			osciMenu.tickerClick();
			break;
		case HELP:
			osciMenu.setNewButtonList(Settings.A_HELP);		
			break;
		default:
			break;
		}
	}

	public void handleTickEvent(MyGuiEvent e){
		double dtmp;
		switch(e.id){
		case TRIGGER_LEVEL:
			String channelStr = Settings.CHANNEL.get();
			switch(channelStr){
			case "1": 
				/* NAch Überprüfung am Oszilloskop gibt es keinen Toggle-Event für die triggerschwelle
				 * Dieses event wird auch in der Simulation nicht angezeigt.
				 * */
				double act_trigger_level = Double.parseDouble(String.valueOf(Settings.TRIGGER_LEVEL_ONE.get()));
				if(e.toggle){
					act_trigger_level += 10*e.tick * (0.0625 * Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_ONE.get()))/5.0);
				}else{
					act_trigger_level += e.tick * (0.0625 * Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_ONE.get()))/5.0);
				}
				if (act_trigger_level > 6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_ONE.get())) - Settings.VC_OFFSET_ONE.getDouble()){
					act_trigger_level = (int) (6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_ONE.get())) - Settings.VC_OFFSET_ONE.getDouble());
				}
				else if (act_trigger_level < -6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_ONE.get())) - Settings.VC_OFFSET_ONE.getDouble()){
					act_trigger_level = (int) (-6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_ONE.get())) - Settings.VC_OFFSET_ONE.getDouble());
				}
				Settings.TRIGGER_LEVEL_ONE.set(Double.toString(act_trigger_level));
				break;
			case "2": 
				act_trigger_level = Double.parseDouble(String.valueOf(Settings.TRIGGER_LEVEL_TWO.get()));
				if(e.toggle){
					act_trigger_level += 10*e.tick * (0.0625 * Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_TWO.get()))/5.0);
				}else{
					act_trigger_level += e.tick * (0.0625 * Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_TWO.get()))/5.0);
				}
				if (act_trigger_level > 6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_TWO.get())) - Settings.VC_OFFSET_TWO.getDouble()){
					act_trigger_level = (int) (6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_TWO.get())) - Settings.VC_OFFSET_TWO.getDouble());
				}
				else if (act_trigger_level < -6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_TWO.get())) - Settings.VC_OFFSET_TWO.getDouble()){
					act_trigger_level = (int) (-6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_TWO.get())) - Settings.VC_OFFSET_TWO.getDouble());
				}
				Settings.TRIGGER_LEVEL_TWO.set(Double.toString(act_trigger_level));
				break;
			case "3": 
				act_trigger_level = Double.parseDouble(String.valueOf(Settings.TRIGGER_LEVEL_THREE.get()));
				if(e.toggle){
					act_trigger_level += 10*e.tick * (0.0625 * Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_THREE.get()))/5.0);
				}else{
					act_trigger_level += e.tick * (0.0625 * Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_THREE.get()))/5.0);
				}
				if (act_trigger_level > 6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_THREE.get())) - Settings.VC_OFFSET_THREE.getDouble()){
					act_trigger_level = (int) (6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_THREE.get())) - Settings.VC_OFFSET_THREE.getDouble());
				}
				else if (act_trigger_level < -6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_THREE.get())) - Settings.VC_OFFSET_THREE.getDouble()){
					act_trigger_level = (int) (-6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_THREE.get())) - Settings.VC_OFFSET_THREE.getDouble());
				}
				Settings.TRIGGER_LEVEL_THREE.set(Double.toString(act_trigger_level));
				break;
			case "4": 
				act_trigger_level = Double.parseDouble(String.valueOf(Settings.TRIGGER_LEVEL_FOUR.get()));
				if(e.toggle){
					act_trigger_level += 10*e.tick * (0.0625 * Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_FOUR.get()))/5.0);
				}else{
					act_trigger_level += e.tick * (0.0625 * Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_FOUR.get()))/5.0);
				}
				if (act_trigger_level > 6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_FOUR.get())) - Settings.VC_OFFSET_FOUR.getDouble()){
					act_trigger_level = (int) (6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_FOUR.get())) - Settings.VC_OFFSET_FOUR.getDouble());
				}
				else if (act_trigger_level < -6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_FOUR.get())) - Settings.VC_OFFSET_FOUR.getDouble()){
					act_trigger_level = (int) (-6* Double.parseDouble(String.valueOf(Settings.VC_SCALE_KNOB_FOUR.get())) - Settings.VC_OFFSET_FOUR.getDouble());
				}
				break;
			}
			resetTrigger();
			break;
		case VC_SCALE_KNOB_ONE:
		case VC_SCALE_KNOB_TWO:
		case VC_SCALE_KNOB_THREE:
		case VC_SCALE_KNOB_FOUR:
			e.id.set(Controls.increment_function.fkt_incrementButton_1(e.id.getDouble(),e.toggle,1/Math.pow(10,3),5,e.tick));
			restartAquisition();
			break;
		case VC_OFFSET_ONE:// GEHT NICHT
			double abhaenigkeit_1 = Settings.VC_SCALE_KNOB_ONE.getDouble();
			e.id.set(Controls.increment_function.fkt_incrementButton_3( e.id.getDouble(), e.toggle, e.tick,abhaenigkeit_1));
			osciDisplay.updateOffset(1);
			restartAquisition();
			break;
		case VC_OFFSET_TWO:
			double abhaenigkeit_2 = Settings.VC_SCALE_KNOB_TWO.getDouble();
			e.id.set(Controls.increment_function.fkt_incrementButton_3( e.id.getDouble(), e.toggle, e.tick,abhaenigkeit_2));
			osciDisplay.updateOffset(2);
			restartAquisition();
			break;
		case VC_OFFSET_THREE:
			double abhaenigkeit_3 = Settings.VC_SCALE_KNOB_THREE.getDouble();
			e.id.set(Controls.increment_function.fkt_incrementButton_3( e.id.getDouble(), e.toggle, e.tick,abhaenigkeit_3));
			osciDisplay.updateOffset(3);
			restartAquisition();
			break;
		case VC_OFFSET_FOUR:
			double abhaenigkeit_4 = Settings.VC_SCALE_KNOB_FOUR.getDouble();
			e.id.set(Controls.increment_function.fkt_incrementButton_3( e.id.getDouble(), e.toggle, e.tick,abhaenigkeit_4));
			osciDisplay.updateOffset(4);
			restartAquisition();
			break;
		case MULTIPLEXED_SCALE_1:
			break;
		case MULTIPLEXED_SCALE_2:
			break;
		case COURSOR:
			if (MeasurementManager.getInstance().getShownArea()!="cursor") break;
			double act_cursor,act_cursor2,temp,temp2;
			switch (String.valueOf(Settings.CURSOR_TYPE.get())) {
			case "X1":
				act_cursor = Settings.CURSORX1.getDouble();
				//Check if left or right border has been reached after incrementing
				temp = Controls.increment_function.fkt_incrementButton_3(act_cursor, e.toggle, e.tick);
				if (temp > -262.0 && temp < 262.0)	act_cursor = temp;
				Settings.CURSORX1.set(act_cursor);
				break;
			case "X2":
				act_cursor = Settings.CURSORX2.getDouble();
				//Check if left or right border has been reached after incrementing
				temp = Controls.increment_function.fkt_incrementButton_3(act_cursor, e.toggle, e.tick);
				if (temp > -262.0 && temp < 262.0)	act_cursor = temp;
				Settings.CURSORX2.set(act_cursor);
				break;
			case "X1-X2-verknüpft":
				act_cursor = Settings.CURSORX1.getDouble();
				act_cursor2 = Settings.CURSORX2.getDouble();
				//Check if left or right border has been reached after incrementing
				temp = Controls.increment_function.fkt_incrementButton_3(act_cursor, e.toggle, e.tick);
				temp2 = Controls.increment_function.fkt_incrementButton_3(act_cursor2, e.toggle, e.tick);
				if (temp > -262.0 && temp < 262.0 && temp2 > -262.0 && temp2 < 262.0) {
					act_cursor = temp;
					act_cursor2 = temp2;
				}
				Settings.CURSORX1.set(act_cursor);
				Settings.CURSORX2.set(act_cursor2);
				break;
			case "Y1":
				act_cursor = Settings.CURSORY1.getDouble();
				//Check if top or bottom border has been reached after incrementing
				temp = Controls.increment_function.fkt_incrementButton_3(act_cursor, e.toggle, e.tick);
				if (temp > -184.1 && temp < 184.1)	act_cursor = temp;
				Settings.CURSORY1.set(act_cursor);
				break;
			case "Y2":
				act_cursor = Settings.CURSORY2.getDouble();
				//Check if top or bottom border has been reached after incrementing
				temp = Controls.increment_function.fkt_incrementButton_3(act_cursor, e.toggle, e.tick);
				if (temp > -184.1 && temp < 184.1)	act_cursor = temp;
				Settings.CURSORY2.set(act_cursor);
				break;
			case "Y1-Y2-verknüpft":
				act_cursor = Settings.CURSORY1.getDouble();
				act_cursor2 = Settings.CURSORY2.getDouble();
				//Check if left or right border has been reached after incrementing
				temp = Controls.increment_function.fkt_incrementButton_3(act_cursor, e.toggle, e.tick);
				temp2 = Controls.increment_function.fkt_incrementButton_3(act_cursor2, e.toggle, e.tick);
				if (temp > -184.1 && temp < 184.1 && temp2 > -184.1 && temp2 < 184.1) {
					act_cursor = temp;
					act_cursor2 = temp2;
				}
				Settings.CURSORY1.set(act_cursor);
				Settings.CURSORY2.set(act_cursor2);
				break;
			default:
				break;
			}	
			break;
		case HORIZONTAL_POSITION_KNOB:
			dtmp=e.id.getDouble()+e.tick*Settings.HORIZONTAL_SCALE_KNOB.getDouble()/50.0;
			if((-dtmp)>(10*Settings.HORIZONTAL_SCALE_KNOB.getDouble())) dtmp=-Settings.HORIZONTAL_SCALE_KNOB.getDouble()*10;
			e.id.set(dtmp);
			//e.id.set(Controls.increment_function.fkt_incrementButton_2(e.id.getInt(), e.toggle, e.tick));
			restartAquisition();
			break;
		case HORIZONTAL_SCALE_KNOB:
			e.id.set(Controls.increment_function.fkt_incrementButton_1(e.id.getDouble(), e.toggle,2/Math.pow(10,9),50,e.tick));			
			restartAquisition();
			break;
		case ENTRY:
			osciMenu.ticker(e.tick);
			break;
		default:
			break;
		}
		System.err.println(e.id.get()+" Tick "+e.id.name()+e.tick+e.toggle);
	}

	public void handleToggleEvent(MyGuiEvent e) {
		switch(e.id){
		case VC_SCALE_KNOB_ONE:
			boolean vc_scale_knob_one_fine;
			vc_scale_knob_one_fine = Boolean.valueOf(String.valueOf(Settings.VC_SCALE_KNOB_ONE_FINE.get()));
			if (vc_scale_knob_one_fine == true) {
				vc_scale_knob_one_fine = false;
			} else {
				vc_scale_knob_one_fine= true;
			}
			Settings.VC_SCALE_KNOB_ONE_FINE.set(Boolean.toString(vc_scale_knob_one_fine));
			break;
		case SINGLE:
			Settings.RUNSTOP.set(true);				
			if(e.id.getBool()==true){
				Settings.MODE_COUPLING.set("Normal");
			}
			break;
		case ONE:
			osciMenu.setNewButtonList(Settings.A_CHANNEL_MENU_1);
			osciDisplay.setChannels(0, e.id.getBool());
			break;
		case TWO:
			osciMenu.setNewButtonList(Settings.A_CHANNEL_MENU_2);
			osciDisplay.setChannels(1, e.id.getBool());
			break;
		case THREE:
			osciMenu.setNewButtonList(Settings.A_CHANNEL_MENU_3);
			osciDisplay.setChannels(2, e.id.getBool());
			break;
		case FOUR:
			osciMenu.setNewButtonList(Settings.A_CHANNEL_MENU_4);
			osciDisplay.setChannels(3, e.id.getBool());
			break;
		default:
			break;
		}
		System.err.println(e.id.get()+" Toggle "+e.id.name()+e.toggle);

	}

	protected void restartAquisition(){
		TriggerConditionManager.getInstance().restartAquisition();
		osciDisplay.recalcViewBuffer();
	}

	protected void resetTrigger(){
		osciDisplay.updateTriggerLine();
		TriggerConditionManager.getInstance().resetTrigger();
	}
	
	protected void resetRings(){
			TriggerConditionManager.getInstance().restartAquisition();
			RingBufferManager.getInstance("SampledRing").updateFilter();
			TriggerConditionManager.getInstance().updateFilter();
			restartAquisition();			
	}

}
