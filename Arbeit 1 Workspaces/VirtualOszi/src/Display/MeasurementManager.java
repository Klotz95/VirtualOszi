package Display;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Communication.AD_Parameter;
import Settings.MeasType;
import Settings.Settings;
import Settings.WaveForms;

/**
 * Class managing the measurement unit of the virtual oscilloscope.  
 * @author Lars Töttel 
 */
public class MeasurementManager {
	private List<AbstractMeasurement> currentMeasurements = new ArrayList<>();
	private List<AbstractMeasurement> currentCursors = new ArrayList<>();
	private List<AbstractMeasurement> cursorPositions = new ArrayList<>();

	private boolean showCursorArea = false;
	private boolean showMeasureArea = false;	

	OsciDisplay osciDisplay = OsciDisplay.getInstance();	 
	private static int pixHeader=23; 	
	private static int pixLeft=17;
	private static int widthGraph = 522;
	private static int heightGraph = 368;

	public static List<Integer> rEdges1 = new ArrayList<>();
	public static List<Integer> fEdges1 = new ArrayList<>();
	public static List<Integer> rEdges2 = new ArrayList<>();
	public static List<Integer> fEdges2 = new ArrayList<>();
	public static List<Integer> rEdges3 = new ArrayList<>();
	public static List<Integer> fEdges3 = new ArrayList<>();
	public static List<Integer> rEdges4 = new ArrayList<>();
	public static List<Integer> fEdges4 = new ArrayList<>();
	public static short[] thresUP = {0, 0, 0, 0}, thresMI = {0, 0, 0, 0}, thresLO = {0, 0, 0, 0};
	public static boolean[] edgesCalculated = {false, false, false, false};

	private static final class InstanceHolder {
		static final MeasurementManager INSTANCE = new MeasurementManager();
	}

	public static MeasurementManager getInstance () {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * Creates new instance of MeasurementManager. Initializes all lists.
	 */
	private MeasurementManager() { 
		//clear all lists at startup
		currentMeasurements.clear();
		currentCursors.clear();
		cursorPositions.clear();	
		rEdges1.clear();
		fEdges1.clear();	
		rEdges2.clear();
		fEdges2.clear();	
		rEdges3.clear();
		fEdges3.clear();	
		rEdges4.clear();
		fEdges4.clear();
		//add the standard cursor calculations to the cursor list in the right area of the display
		currentCursors.add(new DeltaX());
		currentCursors.add(new DeltaXInverted());
		currentCursors.add(new DeltaY(Settings.CURSOR_CHANNEL.getInt()));
		//add every cursor to the list which is displayed in the bottom right, if cursor menu is chosen
		cursorPositions.add(new CursorX(1));
		cursorPositions.add(new CursorX(2));
		cursorPositions.add(new CursorY(Settings.CURSOR_CHANNEL.getInt(), 1));
		cursorPositions.add(new CursorY(Settings.CURSOR_CHANNEL.getInt(), 2));
	}

	/**
	 * Calculates the number of rising and falling edges and their positions. Thereby also
	 * calculates the values of the upper, middle and lower thresholds. This method is called if measurements
	 * request these values. It is excluded from the measurement itself because this data is requested
	 * by multiple measurements.
	 * @param channelNo	Number of the channel the values are calculated for.
	 * @param displayBuffer	Array containing all currently shown values of the chosen channel in the display.
	 * @return Map containing two lists, one for rising edges and one for falling edges.
	 */	
	public static Map<String,List<Integer>> calculateEdgesAndThresholds(int channelNo, short[] displayBuffer) {
		Map<String,List<Integer>> map = new HashMap<String, List<Integer>>();

		//only calculate if the values have not been calculated yet in this cycle
		if (edgesCalculated[channelNo-1] == false) {		
			//create new lists first
			List<Integer> rEdges = new ArrayList<>(), fEdges = new ArrayList<>(), zero = new ArrayList<Integer>();

			short max = displayBuffer[0];
			short min = displayBuffer[0];

			//find minimum and maximum values in order to calculate threshold values afterwards
			for (int i=1; i<displayBuffer.length-1; i++) {
				if (displayBuffer[i] > max)
					max = displayBuffer[i];
				if (displayBuffer[i] < min)
					min = displayBuffer[i];
			}

			//calculate lower, middle, upper threshold values
			//necessary to find "real" rising and falling edges
			thresUP[channelNo-1] = (short) (max - ((Math.abs(max) + Math.abs(min)) * (1.0 - (Settings.MEAS_THRRESHOLD_UPPER_PERCENT_1.getDouble() / 100.0))));
			thresMI[channelNo-1] = (short) ((Math.abs(max) - Math.abs(min)) * (Settings.MEAS_THRRESHOLD_MIDLE_PERCENT_1.getDouble() / 100.0));
			thresLO[channelNo-1] = (short) (min + ((Math.abs(max) + Math.abs(min)) * (Settings.MEAS_THRRESHOLD_LOWER_PERCENT_1.getDouble() / 100.0)));

			//find all zero points in buffer, beginning at index 1 to be able to find out if rising/falling edge
			for (int i=2; i<displayBuffer.length-1; i++) {
				//check if the middle threshold has been crossed
				if 	(displayBuffer[i-1] >= thresMI[channelNo-1] && displayBuffer[i] < thresMI[channelNo-1]
						|| displayBuffer[i-1] < thresMI[channelNo-1] && displayBuffer[i] >= thresMI[channelNo-1]) {
					//choose the position where the value is smaller
					if (Math.abs(displayBuffer[i-1]) < Math.abs(displayBuffer[i]))
						zero.add(i-1);
					else
						zero.add(i);

				}
			}

			//analyze all zero points: check if rising/falling edge,
			//check if it crossed lower threshold before and upper threshold after
			for (int i=0; i<zero.size(); i++) {		
				if (displayBuffer[zero.get(i)-1] < thresMI[channelNo-1] && displayBuffer[zero.get(i)+1] >= thresMI[channelNo-1]) {
					//rising edge at first zero point, go backwards to 10% threshold value
					for (int j=zero.get(i)-1; j>0; j--) {
						//all values up to the lower threshold value have to be smaller, 
						//else it is not a rising edge
						if (!(displayBuffer[j] <= displayBuffer[j+1])) 
							break;
						if (displayBuffer[j]<=thresLO[channelNo-1]) {
							//signal crosses lower threshold value before edge
							//check if it also crosses upper threshold value
							//go forwards to upper threshold value
							for (int k=zero.get(i)+1; k<520; k++) {
								//all values up to the lower threshold value have to be higher, 
								//else it is not a rising edge
								if (!(displayBuffer[k] >= displayBuffer[k-1])) 
									break;
								if (displayBuffer[k]>=thresUP[channelNo-1]) {
									rEdges.add(zero.get(i));
									break;
								}
							}
							break;
						}
					}				
				}
				else if (displayBuffer[zero.get(i)-1] >= thresMI[channelNo-1] && displayBuffer[zero.get(i)+1] < thresMI[channelNo-1]) {
					//falling edge at first zero point, go backwards to upper threshold value
					for (int j=zero.get(i)-1; j>0; j--) {
						//all values up to the upper threshold value have to be higher, 
						//else it is not a falling edge
						if (!(displayBuffer[j] >= displayBuffer[j+1])) 
							break;
						if (displayBuffer[j]>=thresUP[channelNo-1]) {
							//signal crosses upper threshold value before edge
							//check if it also crosses lower threshold value
							//go forwards to lower threshold value
							for (int k=zero.get(i)+1; k<520; k++) {
								//all values up to the lower threshold value have to be smaller, 
								//else it is not a falling edge
								if (!(displayBuffer[k] <= displayBuffer[k-1])) 
									break;
								if (displayBuffer[k]<=thresLO[channelNo-1]) {
									fEdges.add(zero.get(i));
									break;
								}
							}
							break;
						}
					}
				}
			}
			edgesCalculated[channelNo-1] = true;
			switch (channelNo) {
			case 1:
				rEdges1 = rEdges;
				fEdges1 = fEdges;
				break;
			case 2:
				rEdges2 = rEdges;
				fEdges2 = fEdges;
				break;
			case 3:
				rEdges3 = rEdges;
				fEdges3 = fEdges;
				break;
			case 4:
				rEdges4 = rEdges;
				fEdges4 = fEdges;
				break;	
			}
			map.put("risingEdges",rEdges);
			map.put("fallingEdges",fEdges);
		}
		else {
			switch (channelNo) {
			case 1:
				map.put("risingEdges",rEdges1);
				map.put("fallingEdges",fEdges1);	
				break;
			case 2:
				map.put("risingEdges",rEdges2);
				map.put("fallingEdges",fEdges2);	
				break;
			case 3:
				map.put("risingEdges",rEdges3);
				map.put("fallingEdges",fEdges3);	
				break;
			case 4:
				map.put("risingEdges",rEdges4);
				map.put("fallingEdges",fEdges4);	
				break;	
			}		
		}
		return map;
	}

	/**
	 * Converts the digital value given by parameter value to an analog voltage value. Uses the channel number 
	 * to choose the right offset and scale factor (V/Div) for the calculation.
	 * @param value	Digital value to be converted. Range is signed short: -32768 to 32767.
	 * @param channelNo	Number of the channel the calculation is done for.
	 * @return	Analog voltage value of the measured digital value.
	 */
	public static double toVoltage(int value, int channelNo) { 
		double scale = 0, offset = 0;
		switch (channelNo) {
		case 1:
			scale = Settings.VC_SCALE_KNOB_ONE.getDouble();
			offset = Settings.VC_OFFSET_ONE.getDouble();
			break;
		case 2:
			scale = Settings.VC_SCALE_KNOB_TWO.getDouble();
			offset = Settings.VC_OFFSET_TWO.getDouble();
			break;
		case 3:
			scale = Settings.VC_SCALE_KNOB_THREE.getDouble();
			offset = Settings.VC_OFFSET_THREE.getDouble();
			break;
		case 4:
			scale = Settings.VC_SCALE_KNOB_FOUR.getDouble();
			offset = Settings.VC_OFFSET_FOUR.getDouble();
			break;
		default:
			break;
		}

		return (value / 32767.0 / 0.25 * scale - offset); 
	}

	/**
	 * Converts the digital value given by parameter value to an analog time value. 
	 * @param value	Digital value to be converted. Range is position in display: 0 to 522.
	 * @return	Analog time value of the measured digital value.
	 */
	public static double toTime(int value) {
		double scale = Settings.HORIZONTAL_SCALE_KNOB.getDouble();
		return (value / 52.0 * scale);
	}

	/**
	 * Converts the digital value given by parameter value to a corresponding value the vertical cursor can be placed at. 
	 * @param value	Digital value to be converted. Range is signed short: -32768 to 32767.
	 * @return	Corresponding value the vertical cursor can be placed at.
	 */
	public static float toVerticalCursorPos(int value) { return (-1f * value / 32767f * heightGraph / 2f); }

	/**
	 * Converts the digital value given by parameter value to a corresponding value the horizontal cursor can be placed at. 
	 * @param value	Digital value to be converted. Range is position in display: 0 to 522.
	 * @return	Corresponding value the horizontal cursor can be placed at.
	 */
	public static float toHorizontalCursorPos(int value) { return (value - 261); }

	/**
	 * Adds the chosen measurement defined by parameter type to the list currentMeasurements. Maximum of measurements 
	 * is 4, by adding a new one the oldest at index 0 is removed.
	 * @param type	String defining which measurement is to be added.
	 * @param channelNo	The number of the channel (1-4, Math, Reference) the measurement is processed for.
	 */
	public void addMeasurement(MeasType type, int channelNo, int channelNo2) {
		AbstractMeasurement meas = null;
		switch (type) {
		case SNAPSHOT_ALL:
			break;
		case PEEK_PEEK:
			meas = new PeekPeek(channelNo);
			break;
		case MAXIMUM:
			meas = new Maximum(channelNo);
			break;
		case MINIMUM:
			meas = new Minimum(channelNo);
			break;
		case AMPLITUDE:
			meas = new Amplitude(channelNo);
			break;
		case OBEN:
			meas = new Oben(channelNo);
			break;
		case BASIS:
			meas = new Basis(channelNo);
			break;
		case OVERSHOOT:
			meas = new Overshoot(channelNo);
			break;
		case PRESHOOT:
			meas = new Preshoot(channelNo);
			break;
		case AVERAGE_CYCLE:
			meas = new AverageCycle(channelNo);
			break;
		case AVERAGE_SCREEN:
			meas = new AverageScreen(channelNo);
			break;
		case DC_RMS_CYCLE:
			meas = new DCRMSCycle(channelNo);
			break;
		case DC_RMS_SCREEN:
			meas = new DCRMSScreen(channelNo);
			break;
		case AC_RMS_CYCLE:
			meas = new ACRMSCycle(channelNo);
			break;
		case AC_RMS_SCREEN:
			meas = new ACRMSScreen(channelNo);
			break;
		case RATIO_CYCLE:
			meas = new RatioCycle(channelNo,channelNo2);
			break;
		case RATIO_SCREEN:
			meas = new RatioScreen(channelNo,channelNo2);
			break;
		case PERIOD:
			meas = new Period(channelNo);
			break;
		case FREQUENCY:
			meas = new Frequency(channelNo);
			break;
		case COUNTER:
			meas = new Counter(channelNo);
			break;
		case WIDTH_PLUS:
			meas = new WidthPlus(channelNo);
			break;
		case WIDTH_MINUS:
			meas = new WidthMinus(channelNo);
			break;
		case BURST_WIDTH:
			meas = new BurstWidth(channelNo);
			break;
		case DUTY_CYCLE:
			meas = new DutyCycle(channelNo);
			break;
		case RISE_TIME:
			meas = new RiseTime(channelNo);
			break;
		case FALL_TIME:
			meas = new FallTime(channelNo);
			break;
		case DELAY:
			meas = new Delay(channelNo,channelNo2);
			break;
		case PHASE:
			meas = new Phase(channelNo,channelNo2);
			break;
		case X_AT_MIN_Y:
			meas = new XatMinY(channelNo);
			break;
		case X_AT_MAX_Y:
			meas = new XatMaxY(channelNo);
			break;
		case POSITIVE_PULSE_COUNT:
			meas = new PositivePulseCount(channelNo);
			break;
		case NEGATIVE_PULSE_COUNT:
			meas = new NegativePulseCount(channelNo);
			break;
		case RISING_EDGE_COUNT:
			meas = new RisingEdgeCount(channelNo);
			break;
		case FALLING_EDGE_COUNT:
			meas = new FallingEdgeCount(channelNo);
			break;
		case AREA_CYCLE:
			meas = new AreaCycle(channelNo);
			break;
		case AREA_SCREEN:
			meas = new AreaScreen(channelNo);
			break;
		default:
			break;
		}

		//if measurement is already in the list (for the same channel), remove it before adding it to the top of the list
		if (currentMeasurements.contains(meas))
			currentMeasurements.remove(meas);

		//if list is full (4 entries), remove the oldest measurement (at index 0) in the list
		else if (currentMeasurements.size() >= 4) 
			currentMeasurements.remove(0);

		//add measurement
		if (meas != null)
			currentMeasurements.add(meas);

		//update button labels with current measurements
		updateDeleteMeasurementButtonLabels();
	}

	/**
	 * Removes the measurement at the specified index from the list currentMeasurements.
	 * @param index	Index of the measurement to be deleted. If the index is out of bounds, nothing is removed.
	 */
	public void removeMeasurement(int index) {
		switch (index) {
		case 0:
			if (currentMeasurements.size() > 0)
				currentMeasurements.remove(0);
			break;
		case 1:
			if (currentMeasurements.size() > 1)
				currentMeasurements.remove(1);
			break;
		case 2:
			if (currentMeasurements.size() > 2)
				currentMeasurements.remove(2);
			break;
		case 3:
			if (currentMeasurements.size() > 3)
				currentMeasurements.remove(3);
			break;
		default:
			break;        		
		}
		updateDeleteMeasurementButtonLabels();
	}

	/**
	 * Updates the labels of the buttons by which the current measurements can be deleted.
	 * If no measurement is placed at the respective position, label is "Mess. löschen " + position number.
	 * Else the label also includes the name of the respective measurement.
	 */
	private void updateDeleteMeasurementButtonLabels() {
		Settings[] array = {Settings.MEAS_DELL_ONE, Settings.MEAS_DELL_TWO, Settings.MEAS_DELL_THREE, Settings.MEAS_DELL_FOUR };
		for (int i=0; i<currentMeasurements.size(); i++)    
			array[i].setHeaderText("Mess. löschen " + String.valueOf(i+1) + "\n" + currentMeasurements.get(i).getName() + "(" + currentMeasurements.get(i).getChannelNo() + ")");
		for (int i=currentMeasurements.size(); i<4; i++)
			array[i].setHeaderText("Mess. löschen " + String.valueOf(i+1));
	}

	/**
	 * Draws the area on the right of the display showing the list of currently processed measurements.
	 * @param g2	Graphics object the area is drawn on.
	 */
	private void drawMeasureArea(Graphics2D g2) {
		//draw header of area
		g2.setColor(Color.GRAY);
		g2.fillRect(543, 223, 127, 16);
		g2.setColor(Color.WHITE);
		g2.drawString("Messung", 580, 235);

		g2.setColor(Color.WHITE);
		for (int i = 0; i<currentMeasurements.size(); i++) {
			g2.drawString(currentMeasurements.get(i).getNameAndChannel(), 550, 253 + i*37);
			g2.drawString(currentMeasurements.get(i).getValueInScienceNotation("###.####"), 662 - g2.getFontMetrics().stringWidth(currentMeasurements.get(i).getValueInScienceNotation()), 270 + i*37);						
		}

		//draw separator lines
		for (int i=0; i<3; i++) {
			g2.setColor(Color.GRAY);
			g2.drawLine(550, 275 + i*37, 662, 275 + i*37);		
		}
	}

	/**
	 * Draws the area on the right of the display showing the list of currently processed operations on the manual cursors in the display.
	 * Also draws the area on the bottom right showing the currently values of each of the four cursors in the display.
	 * @param g2	Graphics object the area is drawn on. 
	 */
	private void drawCursorArea(Graphics2D g2) {		
		//draw header of area
		g2.setColor(Color.GRAY);
		g2.fillRect(543, 223, 127, 16);
		g2.setColor(Color.WHITE);
		g2.drawString("Cursor", 580, 235);

		//draw separator lines
		for (int i=0; i<3; i++) {
			g2.setColor(Color.GRAY);
			g2.drawLine(550, 275 + i*37, 662, 275 + i*37);		
		}	

		//add items to the area on the right, saved in List "cursorList"
		g2.setColor(Color.WHITE);
		for (int i = 0; i<currentCursors.size(); i++) {
			if (i!=2)
				g2.drawString(currentCursors.get(i).name + ":", 550, 253 + i*37);
			else
				g2.drawString(currentCursors.get(i).getNameAndChannel(), 550, 253 + i*37);
			g2.drawString(currentCursors.get(i).getValueInScienceNotation("###.####"), 662 - g2.getFontMetrics().stringWidth(currentCursors.get(i).getValueInScienceNotation()), 270 + i*37);						
		}		

		//draw area at the bottom right next to the display menu buttons showing values of each cursor
		g2.setColor(Color.GRAY);
		g2.drawRoundRect(450, 420, 220, 35, 20, 20);
		g2.drawLine(575, 420, 575, 455);

		String captions[][] = {{"X1","X2"},{"Y1","Y2"}};
		for (int i=0;i<2;i++) {
			if (Settings.CURSOR_TYPE.get().contains(captions[0][i]))
				g2.setColor(Color.ORANGE);
			else
				g2.setColor(Color.WHITE);				
			g2.drawString(captions[0][i] + ":", 455, 433 + i*17);
			g2.drawString(cursorPositions.get(0+i).getValueInScienceNotation(), 573 - g2.getFontMetrics().stringWidth(cursorPositions.get(0+i).getValueInScienceNotation()), 433 + i*17);
			if (Settings.CURSOR_TYPE.get().contains(captions[1][i]))
				g2.setColor(Color.ORANGE);
			else
				g2.setColor(Color.WHITE);				
			g2.drawString(captions[1][i] + ":", 579, 433 + i*17);
			g2.drawString(cursorPositions.get(2+i).getValueInScienceNotation(), 662 - g2.getFontMetrics().stringWidth(cursorPositions.get(2+i).getValueInScienceNotation()), 433 + i*17);
		}
	}

	/**
	 * Toggles which area (cursor menu or measurement menu) is to be shown on the right of the display. If the chosen area is already displayed, 
	 * the area is hidden. 
	 * @param area	String defining which area is to be shown. Only "cursor" or "measurement" are accepted.
	 */
	public void setShownArea(String area) {
		if (area == "cursor") {
			showCursorArea = true;			
			showMeasureArea = false;
		}
		if (area == "measurement") {
			showMeasureArea = true;
			showCursorArea = false;
		}			
	}

	/**
	 * Returns a String defining which area (measurement or cursor) is shown on the right of the display. If none of these areas is 
	 * currently displayed, an empty String is returned.
	 * @return	String of the area which is currently shown on the right of the display.
	 */
	public String getShownArea() {
		if (showMeasureArea)
			return "measure";
		else if (showCursorArea)
			return "cursor";
		else 
			return "";
	}

	/**
	 * Draws the automatically placed cursors for the most recent measurement in the list (at the highest index of currentMeasurements).
	 * Dynamically displays 0 up to 4 cursors.
	 * @param g2	Graphics object the area is drawn on.
	 */
	private void drawCursorsForMeasurement(Graphics2D g2) {
		int index = currentMeasurements.size() - 1;
		if (index >= 0) {
			if (currentMeasurements.get(index).usedCursors[0] == true)
				drawVerticalCursor(g2, currentMeasurements.get(index).cursorPos[0], 0);
			if (currentMeasurements.get(index).usedCursors[1] == true)
				drawVerticalCursor(g2, currentMeasurements.get(index).cursorPos[1], 1);
			if (currentMeasurements.get(index).usedCursors[2] == true)
				drawHorizontalCursor(g2, currentMeasurements.get(index).cursorPos[2], 0);			
			if (currentMeasurements.get(index).usedCursors[3] == true)
				drawHorizontalCursor(g2, currentMeasurements.get(index).cursorPos[3], 1);	
		}
	}

	/**
	 * Draws a vertical cursor at the position given by parameter y. Cursor is either long or short dashed, defined by style parameter.
	 * @param g2	Graphics object the area is drawn on.
	 * @param y		The y coordinate the cursor is drawn at.
	 * @param style	0: long dashed, 1: short dashed line.
	 */
	private void drawVerticalCursor(Graphics2D g2, float y, int style) {
		if (style == 0)
			drawLongDashedLine(g2, pixLeft, pixHeader + heightGraph/2 + y, pixLeft + widthGraph, pixHeader + heightGraph/2 + y); 
		else
			drawShortDashedLine(g2, pixLeft, pixHeader + heightGraph/2 + y, pixLeft + widthGraph, pixHeader + heightGraph/2 + y);	       
	}

	/**
	 * Draws a horizontal cursor at the position given by parameter x. Cursor is either long or short dashed, defined by style parameter.
	 * @param g2	Graphics object the area is drawn on.
	 * @param x		The x coordinate the cursor is drawn at.
	 * @param style	0: long dashed, 1: short dashed line.
	 */
	private void drawHorizontalCursor(Graphics2D g2, float x, int style) {
		if (style == 0)
			drawLongDashedLine(g2, pixLeft + widthGraph/2 + x, pixHeader, pixLeft + widthGraph/2 + x, pixHeader + heightGraph); 	      
		else
			drawShortDashedLine(g2, pixLeft + widthGraph/2 + x, pixHeader, pixLeft + widthGraph/2 + x, pixHeader + heightGraph); 	            
	}

	/**
	 * Draws a long dashed line in the display used to display a cursor.
	 * @param g2	Graphics object the area is drawn on.
	 * @param x1	the first point's x coordinate.
	 * @param y1	the first point's y coordinate.
	 * @param x2	the second point's x coordinate.
	 * @param y2	the second point's y coordinate.
	 */
	private void drawLongDashedLine(Graphics2D g2, float x1, float y1, float x2, float y2) {
		g2.setColor(Color.orange);
		//Long Dashes for Cursor X1 and Y1  
		Stroke alt = g2.getStroke();
		g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {8f,2.5f}, 0));
		g2.drawLine((int)(x1+.5),(int)(y1+.5),(int)(x2+.5),(int)(y2+.5));
		g2.setStroke(alt);
	}

	/**
	 * Draws a shortly dashed line in the display used to display a cursor.
	 * @param g2	Graphics object the area is drawn on.
	 * @param x1	the first point's x coordinate.
	 * @param y1	the first point's y coordinate.
	 * @param x2	the second point's x coordinate.
	 * @param y2	the second point's y coordinate.
	 */
	private void drawShortDashedLine(Graphics2D g2, float x1, float y1, float x2, float y2) {
		g2.setColor(Color.orange);
		//Short Dashes for Cursor X2 and Y2  
		Stroke alt = g2.getStroke();
		g2.setStroke (new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {5f,1.5f}, 0));
		g2.drawLine((int)(x1+.5),(int)(y1+.5),(int)(x2+.5),(int)(y2+.5));
		g2.setStroke(alt);
	}

	/**
	 * Paints the chosen options in the screen. The members showMeasureArea and showCursorArea define which option is chosen. 
	 * The painting includes the area on the right as well as the cursors. 
	 * Also updates the values of the measurements and the cursors before drawing them. 
	 * @param g2	Graphics object the area is drawn on.
	 */
	public void paint(Graphics2D g2) {  
		for (int i=0; i<4; i++)
			edgesCalculated[i] = false;

		if(showMeasureArea) {			
			//show measure area and the chosen measurements including automatically placed cursors			
			//process all currently shown measurements in the list
			for (AbstractMeasurement measure: currentMeasurements) 
				measure.process();

			//draw area on the right
			drawMeasureArea(g2);	

			//draw cursor lines for the most recent measurement (at highest index in currenMeasurements list)
			drawCursorsForMeasurement(g2);
		}
		else if (showCursorArea) {				
			//show cursor areas on the right and the bottom right and the four cursors in the display			
			//process all currently shown measurements in the list
			for (AbstractMeasurement measure: currentCursors) 
				measure.process();
			//update the area showing the cursor positions
			for (AbstractMeasurement measure: cursorPositions) 
				measure.process();	        

			//draw area on the right
			drawCursorArea(g2);

			//draw all cursors (X1,X2,Y1,Y2)
			drawHorizontalCursor(g2, cursorPositions.get(0).cursorPos[2], 0); 	        
			drawHorizontalCursor(g2, cursorPositions.get(1).cursorPos[2], 1); 	        
			drawVerticalCursor(g2, cursorPositions.get(2).cursorPos[0], 0); 	        
			drawVerticalCursor(g2, cursorPositions.get(3).cursorPos[0], 1); 	           
		}
	}
}



/* #####################################  Base class for all measurements  ##################################### */

/**
 * Abstract class defining all fields and methods a measurement class has to use.
 * @author Lars Töttel
 */
abstract class AbstractMeasurement {	
	int heightGraph = 368;
	int widthGraph = 522;

	protected String name;
	protected int channelNo;
	protected double value;
	protected String unit;
	protected boolean[] usedCursors = new boolean[4];
	protected float[] cursorPos = new float[4];

	/**
	 * Creates a new measurement and initializes all fields.
	 * @param name	String defining the name of the measurement.
	 * @param channelNo	The number of the channel (1-4, Math, Reference) the measurement is processed for.
	 */
	public AbstractMeasurement(String name, int channelNo) {
		this.name = name;
		this.channelNo = channelNo;	
		this.value = 0;	
		this.unit = "V";
		for (int i=0; i<4; i++) {
			this.cursorPos[i] = 0;
			this.usedCursors[i] = false;
		}
	}	

	/**
	 * Calculates the result of the measurement. Has to be called each time the display is updated.
	 */
	public abstract void process();

	@Override
	public boolean equals(Object object) {
		if ((object != null && object instanceof AbstractMeasurement)
				&& 	(this.name == ((AbstractMeasurement) object).name)
				&& 	(this.channelNo == ((AbstractMeasurement) object).channelNo))
			return true;
		else
			return false;
	}

	/**
	 * Returns the name of the measurement.
	 * @return	String representation of the name of the measurement.
	 */
	public String getName() { return this.name; }

	/**
	 * Returns the value of the measurement.
	 * @return	Value of the measurement.
	 */
	public double getValue() { return this.value; }

	/**
	 * Returns the channel number the measurement is processed for.
	 * @return	The number of the channel (1-4, Math, Reference) the measurement is processed for.
	 */
	public int getChannelNo() { return this.channelNo; }

	/**
	 * Returns the name and the measured channel in a formatted string.
	 * @return	String showing the name with the measured channel in parentheses.
	 */
	public String getNameAndChannel() { return this.name + "(" + this.channelNo + "):"; }

	/**
	 * Formats the measured value into the appropriate scientific unit.
	 * @return	String showing the measured value with the right unit in the format "###.##########".
	 */
	public String getValueInScienceNotation() {

		return getValueInScienceNotation("###.##########");
	}   

	public String getValueInScienceNotation(String Format) {

		return AD_Parameter.getValueInScienceNotation(Format,this.value)+this.unit;
	}   




}


/* #############################  Second base class for all 2 channel measurements  ############################# */

abstract class Abstract2ChannelMeasurement extends AbstractMeasurement {
	int channelNo2;

	public Abstract2ChannelMeasurement(String name, int channelNo, int channelNo2) {
		super(name,channelNo);
		this.channelNo2 = channelNo2;
	}

	@Override
	public String getNameAndChannel() { return this.name + "(" + this.channelNo + "->" + this.channelNo2 +  "):"; }

	@Override
	public boolean equals(Object object) {
		if ((object != null && object instanceof Abstract2ChannelMeasurement)
				&& 	(this.name == ((Abstract2ChannelMeasurement) object).name)
				&& 	(this.channelNo == ((Abstract2ChannelMeasurement) object).channelNo)
				&&  (this.channelNo2 == ((Abstract2ChannelMeasurement) object).channelNo2))
			return true;
		else
			return false;
	}
}



/* ##################################### Child classes for each measurement ##################################### */
/* #####################################      Implemented measurements      ##################################### */

class PeekPeek extends AbstractMeasurement {
	public PeekPeek (int channelNo) {
		super("Sp-Sp",channelNo);
		this.usedCursors[0] = true;
		this.usedCursors[1] = true;
	}	

	public void process() {			
		short max = OsciDisplay.getInstance().drawbuffer[channelNo-1][0];
		short min = OsciDisplay.getInstance().drawbuffer[channelNo-1][0];

		for (int i=1; i<OsciDisplay.getInstance().drawbuffer[channelNo-1].length; i++) {
			if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i] > max)
				max = OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
			if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i] < min)
				min = OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
		}

		this.value = MeasurementManager.toVoltage(max-min, channelNo);
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(max);
		this.cursorPos[1] = MeasurementManager.toVerticalCursorPos(min);
	}
}



class Maximum extends AbstractMeasurement {

	public Maximum (int channelNo) {
		super("Max.",channelNo);	
		this.usedCursors[0] = true;
	}

	public void process() {
		short max = OsciDisplay.getInstance().drawbuffer[channelNo-1][0];
		for (int i=1; i<OsciDisplay.getInstance().drawbuffer[channelNo-1].length; i++)
			if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i] > max)
				max = OsciDisplay.getInstance().drawbuffer[channelNo-1][i];

		this.value = MeasurementManager.toVoltage(max, channelNo);
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(max);	
	}
}



class Minimum extends AbstractMeasurement {
	public Minimum (int channelNo) {
		super("Min.",channelNo);
		this.usedCursors[0] = true;
	}

	public void process() {
		short min = OsciDisplay.getInstance().drawbuffer[channelNo-1][0];
		for (int i=1; i<OsciDisplay.getInstance().drawbuffer[channelNo-1].length; i++)
			if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i] < min)
				min = OsciDisplay.getInstance().drawbuffer[channelNo-1][i];

		this.value = MeasurementManager.toVoltage(min, channelNo);
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(min);
	}
}



class AverageCycle extends AbstractMeasurement {
	public AverageCycle (int channelNo) {
		super("Mw. - Zyk.",channelNo);
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
		this.usedCursors[3] = true;
	}

	public void process() {		
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		List<Integer> fEdges = map.get("fallingEdges");

		if ((rEdges.size() + fEdges.size()) < 3) {
			//less than 3 edges, cannot calculate average without a cycle
			this.value = 0;
			this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresMI[channelNo-1]);
			this.cursorPos[2] = 0;
			this.cursorPos[3] = 0;
		}
		else {
			int periodBegin, periodEnd;
			if (rEdges.get(0) < fEdges.get(0) && rEdges.size() > 1) {
				periodBegin = rEdges.get(0);
				periodEnd = rEdges.get(1);				
			}
			else {
				periodBegin = fEdges.get(0);
				periodEnd = fEdges.get(1);						
			}

			int n = periodEnd - periodBegin;
			double sum = 0;		
			//loop through all values in cycle in the display buffer and calculate sum
			for (int i=periodBegin; i<periodEnd;i++) 
				sum += OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
			//divide by n and calculate the square root
			int result = (short)(sum/n);

			this.value = MeasurementManager.toVoltage(result, channelNo);
			this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(result);
			this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(periodBegin);
			this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(periodEnd);
		}
	}
}



class AverageScreen extends AbstractMeasurement {
	public AverageScreen (int channelNo) {
		super("Mw. - VB",channelNo);
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
		this.usedCursors[3] = true;
	}

	public void process() {		
		int n = widthGraph;
		double sum = 0;		
		//loop through all values in display buffer and calculate sum
		for (int i=0; i<n;i++) 
			sum += OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
		//divide by n and calculate the square root
		int result = (short)(sum/n);

		this.value = MeasurementManager.toVoltage(result, channelNo);
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(result);
		this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(0);
		this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(widthGraph);
	}
}



class DCRMSCycle extends AbstractMeasurement {
	public DCRMSCycle (int channelNo) {
		super("DC RMS - Zyk.",channelNo);
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
		this.usedCursors[3] = true;
	}

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		List<Integer> fEdges = map.get("fallingEdges");

		if ((rEdges.size() + fEdges.size()) < 3) {
			//less than 3 edges, cannot calculate ACRMS without a cycle
			this.value = 0;
			this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresMI[channelNo-1]);
			this.cursorPos[2] = 0;
			this.cursorPos[3] = 0;
		}
		else {
			int periodBegin, periodEnd;
			if (rEdges.get(0) < fEdges.get(0) && rEdges.size() > 1) {
				periodBegin = rEdges.get(0);
				periodEnd = rEdges.get(1);				
			}
			else {
				periodBegin = fEdges.get(0);
				periodEnd = fEdges.get(1);					
			}					

			int n = periodEnd - periodBegin;
			double sum = 0;		
			//loop through all values in cycle in the display buffer and calculate sum
			for (int i=periodBegin; i<periodEnd;i++) 
				sum += OsciDisplay.getInstance().drawbuffer[channelNo-1][i] * OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
			//divide by n and calculate the square root
			int result = (short)(Math.sqrt((double)sum/n));

			this.value = MeasurementManager.toVoltage(result, channelNo);
			this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(result);
			this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(periodBegin);
			this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(periodEnd);
		}
	}
}



class DCRMSScreen extends AbstractMeasurement {
	public DCRMSScreen (int channelNo) {
		super("DC RMS - VB",channelNo);
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
		this.usedCursors[3] = true;
	}

	public void process() {

		int n = widthGraph;
		double sum = 0;		
		//loop through all values in display buffer and calculate square sum
		for (int i=0; i<n;i++) 
			sum += OsciDisplay.getInstance().drawbuffer[channelNo-1][i]*OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
		//divide by n and calculate the square root
		int result = (short)(Math.sqrt((double)sum/n));

		this.value = MeasurementManager.toVoltage(result, channelNo);		
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(result);
		this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(0);
		this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(widthGraph);
	}
}



class ACRMSCycle extends AbstractMeasurement {
	public ACRMSCycle (int channelNo) {
		super("AC RMS - Zyk.",channelNo);
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
		this.usedCursors[3] = true;
	}

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		List<Integer> fEdges = map.get("fallingEdges");

		if ((rEdges.size() + fEdges.size()) < 3) {
			//less than 3 edges, cannot calculate ACRMS without a cycle
			this.value = 0;
			this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresMI[channelNo-1]);
			this.cursorPos[2] = 0;
			this.cursorPos[3] = 0;
		}
		else {
			int periodBegin, periodEnd;
			if (rEdges.get(0) < fEdges.get(0) && rEdges.size() > 1) {
				periodBegin = rEdges.get(0);
				periodEnd = rEdges.get(1);				
			}
			else {
				periodBegin = fEdges.get(0);
				periodEnd = fEdges.get(1);						
			}

			int n = periodEnd - periodBegin;
			double rms = 0, average = 0;	
			//loop through all values in cycle in the display buffer and calculate sum
			for (int i=periodBegin; i<periodEnd;i++) {
				average += OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
				rms += OsciDisplay.getInstance().drawbuffer[channelNo-1][i] * OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
			}
			//divide by n (average), divide by n and calculate the square root (rms)
			average = (double)average/n;
			rms = Math.sqrt((double)rms/n);			
			int result = (short)(rms - average);

			this.value = MeasurementManager.toVoltage(result, channelNo);
			this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(result);
			this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(periodBegin);
			this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(periodEnd);
		}
	}
}



class ACRMSScreen extends AbstractMeasurement {
	public ACRMSScreen (int channelNo) {
		super("AC RMS - VB",channelNo);
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
		this.usedCursors[3] = true;
	}

	public void process() {
		//AC RMS is DC RMS without DC component (= without average)
		//calculate average first, subtract it from the DC RMS

		int n = widthGraph;
		double rms = 0, average = 0;		
		//loop through all values in display buffer and calculate square sum
		for (int i=0; i<n;i++) {
			average += OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
			rms += OsciDisplay.getInstance().drawbuffer[channelNo-1][i]*OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
		}
		//divide by n (average), divide by n and calculate the square root (rms)
		average = (double)average/n;
		rms = Math.sqrt((double)rms/n);
		short result = (short)(rms - average);

		this.value = MeasurementManager.toVoltage(result, channelNo);		
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(result);
		this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(0);
		this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(widthGraph);
	}
}



class Period extends AbstractMeasurement {
	public Period (int channelNo) {
		super("Periode",channelNo);
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
		this.usedCursors[3] = true;
		this.unit = "s";
	}	

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		List<Integer> fEdges = map.get("fallingEdges");

		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresMI[channelNo-1]);
		if ((rEdges.size() + fEdges.size()) < 3) {
			//less than 3 edges, cannot calculate period
			this.value = 0;
			this.cursorPos[2] = 0;
			this.cursorPos[3] = 0;
		}
		else {
			if (rEdges.get(0) < fEdges.get(0) && rEdges.size() > 1) {
				this.value = MeasurementManager.toTime(rEdges.get(1) - rEdges.get(0));
				this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(rEdges.get(0));
				this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(rEdges.get(1));				
			}
			else {
				this.value = MeasurementManager.toTime(fEdges.get(1) - fEdges.get(0));
				this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(fEdges.get(0));
				this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(fEdges.get(1));				
			}
		}
	}
}



class Frequency extends AbstractMeasurement {
	public Frequency (int channelNo) {
		super("Freq.",channelNo);
		this.unit = "Hz";
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
		this.usedCursors[3] = true;
	}	

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		List<Integer> fEdges = map.get("fallingEdges");

		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresMI[channelNo-1]);
		if ((rEdges.size() + fEdges.size()) < 3) {
			//less than 3 edges, cannot calculate period
			this.value = 0;
			this.cursorPos[2] = 0;
			this.cursorPos[3] = 0;
		}
		else {
			if((fEdges.size() > 0)&&(rEdges.size() > 0)){
				if (rEdges.get(0) < fEdges.get(0) && rEdges.size() > 1) {
					this.value = 1 / MeasurementManager.toTime(rEdges.get(1) - rEdges.get(0));
					this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(rEdges.get(0));
					this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(rEdges.get(1));				
				}
				else {
					if(fEdges.size()>=2){
						double time=MeasurementManager.toTime(fEdges.get(1) - fEdges.get(0));
						if(time>0.0000000000001){
							this.value = 1 / MeasurementManager.toTime(fEdges.get(1) - fEdges.get(0));
							this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(fEdges.get(0));
							this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(fEdges.get(1));
						}
					}
				}
			}			
		}
	}
}


class WidthPlus extends AbstractMeasurement {
	public WidthPlus (int channelNo) {
		super("+Breite",channelNo);
		this.unit ="s";
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
		this.usedCursors[3] = true;
	}	

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		List<Integer> fEdges = map.get("fallingEdges");

		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresMI[channelNo-1]);

		if ( 	((rEdges.size() + fEdges.size()) < 2)
				||	(rEdges.size() < 2 && rEdges.get(0) > fEdges.get(fEdges.size()-1))) {
			//less than 2 edges or the only rising edge is not followed by a falling edge, 
			//cannot calculate width of a positive pulse
			this.value = 0;
			this.cursorPos[2] = 0;
			this.cursorPos[3] = 0;
		}
		else {
			//start at the first rising edge and find the following falling edge
			int begin = rEdges.get(0), end = rEdges.get(0);			
			for (int i=0; i<fEdges.size(); i++) 
				if (fEdges.get(i) > begin) {
					end = fEdges.get(i);
					break;
				}

			this.value = MeasurementManager.toTime(end - begin);
			this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(begin);	
			this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(end);	
		}
	}
}



class WidthMinus extends AbstractMeasurement {
	public WidthMinus (int channelNo) {
		super("- Breite",channelNo);
		this.unit = "s";
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
		this.usedCursors[3] = true;
	}	

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		List<Integer> fEdges = map.get("fallingEdges");

		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresMI[channelNo-1]);

		if ( 	((rEdges.size() + fEdges.size()) < 2)
				||	(fEdges.size() < 2 && fEdges.get(0) > rEdges.get(rEdges.size()-1))) {
			//less than 2 edges or the only falling edge is not followed by a rising edge, 
			//cannot calculate width of a negative pulse
			this.value = 0;
			this.cursorPos[2] = 0;
			this.cursorPos[3] = 0;
		}
		else {
			//start at the first falling edge and find the following rising edge
			int begin = fEdges.get(0), end = fEdges.get(0);			
			for (int i=0; i<rEdges.size(); i++) 
				if (rEdges.get(i) > begin) {
					end = rEdges.get(i);
					break;
				}

			//this.value = (end - begin) / 52.0 * this.settings.getDouble();
			this.value = MeasurementManager.toTime(end - begin);
			this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(begin);	
			this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(end);	
		}
	}
}



class BurstWidth extends AbstractMeasurement {
	public BurstWidth (int channelNo) {
		super("Burst-Breite",channelNo);
		this.unit = "s";
		this.usedCursors[2] = true;
		this.usedCursors[3] = true;
	}	

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		List<Integer> fEdges = map.get("fallingEdges");

		if ((rEdges.size() + fEdges.size()) < 2) {
			//less than 2 edges, cannot calculate burst width
			this.value = 0;
			this.cursorPos[2] = 0;
			this.cursorPos[3] = 0;
		}
		else {
			if (rEdges.get(0) < fEdges.get(0)) {
				this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(rEdges.get(0));
				if (rEdges.get(rEdges.size()-1) > fEdges.get(fEdges.size()-1)) {
					this.value = MeasurementManager.toTime(rEdges.get(rEdges.size()-1) - rEdges.get(0));
					this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(rEdges.get(rEdges.size()-1));	
				}
				else {
					this.value = MeasurementManager.toTime(fEdges.get(fEdges.size()-1) - rEdges.get(0));
					this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(fEdges.get(fEdges.size()-1));	
				}			
			}
			else {
				this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(fEdges.get(0));	
				if (rEdges.get(rEdges.size()-1) > fEdges.get(fEdges.size()-1)) {
					this.value = MeasurementManager.toTime(rEdges.get(rEdges.size()-1) - fEdges.get(0));
					this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(rEdges.get(rEdges.size()-1));	
				}
				else {
					this.value = MeasurementManager.toTime(fEdges.get(fEdges.size()-1) - fEdges.get(0));
					this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(fEdges.get(fEdges.size()-1));	
				}
			}
		}			
	}
}



class DutyCycle extends AbstractMeasurement {
	public DutyCycle (int channelNo) {
		super("Arbeit",channelNo);
		this.unit = "%";
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
		this.usedCursors[3] = true;
	}	

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		List<Integer> fEdges = map.get("fallingEdges");

		//place cursor at 50% threshold
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresMI[channelNo-1]);

		//calculate period and width of a positive pulse		
		if ( 	((rEdges.size() + fEdges.size()) < 2)
				||	(rEdges.size() < 2 && rEdges.get(0) > fEdges.get(fEdges.size()-1))) {
			//less than 2 edges or the only rising edge is not followed by a falling edge, 
			//cannot calculate width of a positive pulse and period
			this.value = 0;
			this.cursorPos[2] = 0;
			this.cursorPos[3] = 0;
		}
		else {
			//calculate width of a positive pulse first
			//start at the first rising edge and find the following falling edge
			int begin = rEdges.get(0), end = rEdges.get(0);			
			for (int i=0; i<fEdges.size(); i++) 
				if (fEdges.get(i) > begin) {
					end = fEdges.get(i);
					break;
				}

			//calculate period
			if (rEdges.get(0) < fEdges.get(0) && rEdges.size() > 1) {
				this.value = ((double)(end - begin) / (double)(rEdges.get(1) - rEdges.get(0))) * 100.0;
				this.cursorPos[2] = rEdges.get(0) - 262;
				this.cursorPos[3] = rEdges.get(1) - 262;				
			}
			else {
				this.value = ((double)(end - begin) / (double)(fEdges.get(1) - fEdges.get(0))) * 100.0;
				this.cursorPos[2] = fEdges.get(0) - 262;
				this.cursorPos[3] = fEdges.get(1) - 262;				
			}
		}
	}
}



class RiseTime extends AbstractMeasurement {
	public RiseTime (int channelNo) {
		super("Ansteigen",channelNo);
		this.unit = "s";
		for (int i=0;i<4;i++)
			this.usedCursors[i] = true;
	}	

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		//List<Integer> fEdges = map.get("fallingEdges");

		//place y-cursors at upper and lower thresholds
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresUP[channelNo-1]);
		this.cursorPos[1] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresLO[channelNo-1]);

		//if no rising edge is found, rise time cannot be calculated
		if (rEdges.size() < 1) {
			this.value = 0;
			this.cursorPos[2] = 0;
			this.cursorPos[3] = 0;
		}
		else {
			int begin = rEdges.get(0), end = rEdges.get(0);
			//start at the crossing of the first rising edge with the middle threshold
			//first go back to crossing with the lower threshold
			for (int i=rEdges.get(0)-1; i>0; i--) {
				if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i]<=MeasurementManager.thresLO[channelNo-1]) {
					//crossing with lower threshold found
					begin = i;
					break;
				}
			}	
			//now go forward to crossing with the upper threshold
			for (int i=rEdges.get(0)+1; i<520; i++) {
				if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i]>=MeasurementManager.thresUP[channelNo-1]) {
					//crossing with upper threshold found
					end = i;
					break;
				}
			}				

			this.value = MeasurementManager.toTime(end - begin);
			this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(begin);	
			this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(end);	
		}
	}
}



class FallTime extends AbstractMeasurement {
	public FallTime (int channelNo) {
		super("Abfall",channelNo);
		this.unit = "s";
		for (int i=0;i<4;i++)
			this.usedCursors[i] = true;
	}	

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> fEdges = map.get("fallingEdges");

		//place y-cursors at upper and lower thresholds
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresUP[channelNo-1]);
		this.cursorPos[1] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresLO[channelNo-1]);

		//if no rising edge is found, rise time cannot be calculated
		if (fEdges.size() < 1) {
			this.value = 0;
			this.cursorPos[2] = 0;
			this.cursorPos[3] = 0;
		}
		else {
			int begin = fEdges.get(0), end = fEdges.get(0);
			//start at the crossing of the first rising edge with the 50% threshold
			//first go back to crossing with the upper threshold
			for (int i=fEdges.get(0)-1; i>0; i--) {
				if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i]>=MeasurementManager.thresUP[channelNo-1]) {
					//crossing with upper threshold found
					begin = i;
					break;
				}
			}	
			//now go forward to crossing with the lower threshold
			for (int i=fEdges.get(0)+1; i<520; i++) {
				if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i]<=MeasurementManager.thresLO[channelNo-1]) {
					//crossing with lower threshold found
					end = i;
					break;
				}
			}				

			this.value = MeasurementManager.toTime(end - begin);
			this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(begin);	
			this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(end);	
		}
	}
}



class Delay extends Abstract2ChannelMeasurement {	
	public Delay (int channelNo, int channelNo2) {
		super("Verz.",channelNo,channelNo2);
		this.unit = "s";
		for (int i=0;i<4;i++)
			this.usedCursors[i] = true;
	}	

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		List<Integer> fEdges = map.get("fallingEdges");
		map.clear();
		map = MeasurementManager.calculateEdgesAndThresholds(channelNo2, OsciDisplay.getInstance().drawbuffer[channelNo2-1]);
		List<Integer> rEdges2 = map.get("risingEdges");
		List<Integer> fEdges2 = map.get("fallingEdges");

		//initialize new lists and find out which edges are investigated 
		//(defined by buttons MEAS_EDGE_ONE and MEAS_EDGE_TWO)
		List<Integer> edges1, edges2;		
		if (Settings.MEAS_EDGE_ONE.get() == WaveForms.UpArrow.getSymbol())
			edges1 = rEdges;
		else
			edges1 = fEdges;
		if (Settings.MEAS_EDGE_TWO.get() == WaveForms.UpArrow.getSymbol())
			edges2 = rEdges2;
		else
			edges2 = fEdges2;

		//place vertical cursors at both middle threshold values
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresMI[channelNo-1]);
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresMI[channelNo2-1]);

		//if no rising edges are found in both signals, delay cannot be calculated
		if (edges1.size() == 0 || edges2.size() == 0) {
			this.value = 0;
			this.cursorPos[2] = 0;
			this.cursorPos[3] = 0;			
		}
		else {
			//compare the first edges of the chosen direction that are found in the signals
			int pos1 = edges1.get(0), pos2 = edges2.get(0);	
			this.value = MeasurementManager.toTime(pos2 - pos1);
			this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(pos1);	
			this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(pos2);	
		}		
	}
}



class XatMinY extends AbstractMeasurement {
	public XatMinY (int channelNo) {
		super("X@Min.",channelNo);
		this.unit = "s";
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
	}	

	public void process() {
		//find minimum first with index
		short min = OsciDisplay.getInstance().drawbuffer[channelNo-1][0];
		int position = 0;
		for (int i=1; i<OsciDisplay.getInstance().drawbuffer[channelNo-1].length; i++)
			if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i] < min) {
				min = OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
				position = i;
			}

		//place vertical cursor at minimum, horizontal cursor at its time value
		this.value = MeasurementManager.toTime(position-261) - Settings.HORIZONTAL_POSITION_KNOB.getDouble(); 
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(min);
		this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(position);
	}
}



class XatMaxY extends AbstractMeasurement {
	public XatMaxY (int channelNo) {
		super("X@Max.",channelNo);
		this.unit = "s";
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
	}	

	public void process() {
		//find maximum first with index
		short max = OsciDisplay.getInstance().drawbuffer[channelNo-1][0];
		int position = 0;
		for (int i=1; i<OsciDisplay.getInstance().drawbuffer[channelNo-1].length; i++)
			if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i] > max) {
				max = OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
				position = i;
			}

		//place vertical cursor at maximum, horizontal cursor at its x value
		this.value = MeasurementManager.toTime(position-261) - Settings.HORIZONTAL_POSITION_KNOB.getDouble(); 
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(max);
		this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(position);
	}
}



class PositivePulseCount extends AbstractMeasurement {
	public PositivePulseCount (int channelNo) {
		super("+ Impulse Anz.",channelNo);
		this.unit = "";
	}	

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		List<Integer> fEdges = map.get("fallingEdges");

		int count = 0;
		//needs at least one rising edge that is followed by a falling edge
		if (rEdges.size() + fEdges.size() < 2) {
			this.value = 0;                     
		}
		else {
			int offset = 0;
			if (rEdges.get(0) < fEdges.get(0)) {
				//first rising edge is followed by falling edge, first pulse is found
				count++;
				offset++;
			}
			//search for following positive pulses
			for (int i=offset; i<rEdges.size(); i++) {
				if (fEdges.size() > i+(1-offset)) 	
					count++;
			}
			this.value = count;
		}
	}
}



class NegativePulseCount extends AbstractMeasurement {
	public NegativePulseCount (int channelNo) {
		super("- Impulse Anz.",channelNo);
		this.unit = "";
	}	

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		List<Integer> fEdges = map.get("fallingEdges");

		int count = 0;
		//needs at least one rising edge that is followed by a falling edge
		if (rEdges.size() + fEdges.size() < 2) {
			this.value = 0;                     
		}
		else {
			int begin = 0;
			if (fEdges.get(0) < rEdges.get(0)) {
				//first falling edge is followed by rising edge, first pulse is found
				count++;
				begin++;
			}
			//search for following negative pulses
			for (int i=begin; i<fEdges.size(); i++) {
				if (rEdges.size() > i+(1-begin)) 	
					count++;
			}
			this.value = count;
		}
	}
}



class RisingEdgeCount extends AbstractMeasurement {
	public RisingEdgeCount (int channelNo) {
		super("Steigende Flan...",channelNo);
		this.unit = "";
	}	

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");

		//found values, calculate result
		this.value = rEdges.size();
		if (rEdges.size() > 0) {
			this.usedCursors[2] = true;
			this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(rEdges.get(0));
			if (rEdges.size() > 1) {
				this.usedCursors[3] = true;
				this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(rEdges.get(1));				
			}
			else
				this.usedCursors[3] = false;
		}
		else
			this.usedCursors[2] = false;
	}
}



class FallingEdgeCount extends AbstractMeasurement {
	public FallingEdgeCount (int channelNo) {
		super("Fallende Flanken",channelNo);
		this.unit = "";
	}	

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> fEdges = map.get("fallingEdges");

		//found values, calculate result
		this.value = fEdges.size();
		if (fEdges.size() > 0) {
			this.usedCursors[2] = true;
			this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(fEdges.get(0));
			if (fEdges.size() > 1) {
				this.usedCursors[3] = true;
				this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(fEdges.get(1));				
			}
			else
				this.usedCursors[3] = false;
		}
		else
			this.usedCursors[2] = false;
	}
}



class AreaCycle extends AbstractMeasurement {
	public AreaCycle (int channelNo) {
		super("Bereich - Zyk.",channelNo);
		this.unit = "Vs";
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
		this.usedCursors[3] = true;
	}	

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		List<Integer> fEdges = map.get("fallingEdges");

		if ((rEdges.size() + fEdges.size()) < 3) {
			//less than 3 edges, cannot calculate ACRMS without a cycle
			this.value = 0;
			this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresMI[channelNo-1]);
			this.cursorPos[2] = 0;
			this.cursorPos[3] = 0;
		}
		else {
			int periodBegin, periodEnd;
			if (rEdges.get(0) < fEdges.get(0) && rEdges.size() > 1) {
				periodBegin = rEdges.get(0);
				periodEnd = rEdges.get(1);				
			}
			else {
				periodBegin = fEdges.get(0);
				periodEnd = fEdges.get(1);					
			}					

			double upper = 0, lower = 0;		
			//loop through all values in cycle in the display buffer and calculate sum
			for (int i=periodBegin; i<periodEnd;i++) {
				if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i] < MeasurementManager.thresMI[channelNo-1]) 
					upper += (OsciDisplay.getInstance().drawbuffer[channelNo-1][i] - MeasurementManager.thresMI[channelNo-1]) * Settings.HORIZONTAL_SCALE_KNOB.getDouble() / 52.0;
				else
					lower += (OsciDisplay.getInstance().drawbuffer[channelNo-1][i] - MeasurementManager.thresMI[channelNo-1]) * Settings.HORIZONTAL_SCALE_KNOB.getDouble() / 52.0;
			}
			//subtract lower part from upper part of the signal
			int result = (int)(upper+lower);

			this.value = MeasurementManager.toVoltage(result, channelNo);		
			this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresMI[channelNo-1]);
			this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(periodBegin);
			this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(periodEnd);
		}
	}
}



class AreaScreen extends AbstractMeasurement {
	public AreaScreen (int channelNo) {
		super("Bereich - VB",channelNo);
		this.unit = "Vs";
		this.usedCursors[0] = true;
		this.usedCursors[2] = true;
		this.usedCursors[3] = true;
	}	

	public void process() {
		MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);			

		int n = widthGraph;
		double upper = 0, lower = 0;		
		//loop through all values in display buffer and calculate square sum
		for (int i=0; i<n;i++) {
			if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i] < MeasurementManager.thresMI[channelNo-1]) 
				upper += OsciDisplay.getInstance().drawbuffer[channelNo-1][i] * Settings.HORIZONTAL_SCALE_KNOB.getDouble() / 52.0;
			else
				lower += OsciDisplay.getInstance().drawbuffer[channelNo-1][i] * Settings.HORIZONTAL_SCALE_KNOB.getDouble() / 52.0;
		}
		//subtract lower part from upper part of the signal
		int result = (int)(upper-lower);

		this.value = MeasurementManager.toVoltage(result, channelNo);		
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresMI[channelNo-1]);
		this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(0);
		this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(widthGraph);
	}
}


/* #####################################  Not yet implemented measurements  ##################################### */

class Amplitude extends AbstractMeasurement {
	public Amplitude (int channelNo) {
		super("Ampl",channelNo);
		this.usedCursors[0] = true;
		this.usedCursors[1] = true;
	}

	//has the same implementation as PeekPeek, has to be modified
	public void process() {	

		short max = OsciDisplay.getInstance().drawbuffer[channelNo-1][0];
		short min = OsciDisplay.getInstance().drawbuffer[channelNo-1][0];

		for (int i=1; i<OsciDisplay.getInstance().drawbuffer[channelNo-1].length; i++) {
			if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i] > max)
				max = OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
			if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i] < min)
				min = OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
		}

		this.value = MeasurementManager.toVoltage((short)(max-min), channelNo);
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(max);
		this.cursorPos[1] = MeasurementManager.toVerticalCursorPos(min);
	}
}



class Oben extends AbstractMeasurement {
	public Oben (int channelNo) {
		super("Oben",channelNo);
		this.usedCursors[0] = true;
	}

	//has the same implementation as Maximum, has to be modified
	public void process() {
		short max = OsciDisplay.getInstance().drawbuffer[channelNo-1][0];
		for (int i=1; i<OsciDisplay.getInstance().drawbuffer[channelNo-1].length; i++)
			if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i] > max)
				max = OsciDisplay.getInstance().drawbuffer[channelNo-1][i];

		this.value = MeasurementManager.toVoltage(max, channelNo);
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(max);
	}
}



class Basis extends AbstractMeasurement {
	public Basis (int channelNo) {
		super("Basis",channelNo);
		this.usedCursors[0] = true;
	}

	//has the same implementation as Minimum, has to be modified
	public void process() {
		short min = OsciDisplay.getInstance().drawbuffer[channelNo-1][0];
		for (int i=1; i<OsciDisplay.getInstance().drawbuffer[channelNo-1].length; i++)
			if (OsciDisplay.getInstance().drawbuffer[channelNo-1][i] < min)
				min = OsciDisplay.getInstance().drawbuffer[channelNo-1][i];

		this.value = MeasurementManager.toVoltage(min, channelNo);
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(min);
	}
}



class Overshoot extends AbstractMeasurement {
	public Overshoot (int channelNo) {
		super("Über",channelNo);
		for (int i=0;i<4;i++)
			this.usedCursors[i] = true;
	}

	public void process() {
		this.value = 0;
		this.cursorPos[0] = 0;
		this.cursorPos[1] = 0;
		this.cursorPos[2] = 0;
		this.cursorPos[3] = 0;
	}
}



class Preshoot extends AbstractMeasurement {
	public Preshoot (int channelNo) {
		super("Vorschw.",channelNo);
		for (int i=0;i<4;i++)
			this.usedCursors[i] = true;
	}

	public void process() {
		this.value = 0;
		this.cursorPos[0] = 0;
		this.cursorPos[1] = 0;
		this.cursorPos[2] = 0;
		this.cursorPos[3] = 0;
	}
}



class RatioCycle extends Abstract2ChannelMeasurement {
	int channelNo2;

	public RatioCycle (int channelNo, int channelNo2) {
		super("Verh. - Zyk.",channelNo,channelNo2);
		this.unit = "%";
		for (int i=0;i<4;i++)
			this.usedCursors[i] = true;
	}

	//has to be overriden because no scientific unit is used
	@Override
	public String getValueInScienceNotation() {
		double value = this.value;
		String unit = this.unit;             
		return (new DecimalFormat("###.##########")).format(value) + unit;		
	}

	public void process() {
		Map<String,List<Integer>> map = MeasurementManager.calculateEdgesAndThresholds(channelNo, OsciDisplay.getInstance().drawbuffer[channelNo-1]);		
		List<Integer> rEdges = map.get("risingEdges");
		List<Integer> fEdges = map.get("fallingEdges");

		if ((rEdges.size() + fEdges.size()) < 3) {
			//less than 3 edges, cannot calculate ACRMS without a cycle
			this.value = 0;
			//this.cursorPos[0] = (double)MeasurementManager.thresMI[channelNo] / 32767.0 * heightGraph/2.0;
			this.cursorPos[0] = MeasurementManager.toVerticalCursorPos(MeasurementManager.thresMI[channelNo]);
			this.cursorPos[2] = 0;
			this.cursorPos[3] = 0;
		}
		else {
			int periodBeginCh1, periodEndCh1;
			if (rEdges.get(0) < fEdges.get(0) && rEdges.size() > 1) {
				periodBeginCh1 = rEdges.get(0);
				periodEndCh1 = rEdges.get(1);				
			}
			else {
				periodBeginCh1 = fEdges.get(0);
				periodEndCh1 = fEdges.get(1);						
			}

			int n = periodEndCh1 - periodBeginCh1;
			double rmsCh1 = 0, averageCh1 = 0, rmsCh2 = 0, averageCh2 = 0;	
			//loop through all values in cycle in the display buffer and calculate sum
			for (int i=periodBeginCh1; i<periodEndCh1;i++) {
				averageCh1 += OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
				rmsCh1 += OsciDisplay.getInstance().drawbuffer[channelNo-1][i] * OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
				averageCh2 += OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
				rmsCh2 += OsciDisplay.getInstance().drawbuffer[channelNo-1][i]*OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
			}
			//divide by n (average), divide by n and calculate the square root (rms)
			averageCh1 = (double)averageCh1/n;
			rmsCh1 = Math.sqrt((double)rmsCh1/n);
			averageCh2 = (double)averageCh2/n;
			rmsCh2 = Math.sqrt((double)rmsCh2/n);

			//double value1 = (rmsCh1 - averageCh1) / 32767.0 / 0.25 * this.settings.getDouble();
			double value1 = MeasurementManager.toVoltage((short)(rmsCh1 - averageCh1), channelNo);
			//double value2 = (rmsCh2 - averageCh2) / 32767.0 / 0.25 * this.settings.getDouble();
			double value2 = MeasurementManager.toVoltage((short)(rmsCh2 - averageCh2), channelNo2);
			this.value = value1 / value2 * 100;

			this.cursorPos[0] = MeasurementManager.toVerticalCursorPos((short)(rmsCh1 - averageCh1));
			this.cursorPos[1] = MeasurementManager.toVerticalCursorPos((short)(rmsCh2 - averageCh2));
			this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(periodBeginCh1);
			this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(periodEndCh1);
		}
	}
}



class RatioScreen extends Abstract2ChannelMeasurement {

	public RatioScreen (int channelNo, int channelNo2) {
		super("Verh. - VB",channelNo,channelNo2);
		this.unit = "%";
		for (int i=0;i<4;i++)
			this.usedCursors[i] = true;
	}

	//has to be overriden because no scientific unit is used
	@Override
	public String getValueInScienceNotation() {
		double value = this.value;
		String unit = this.unit;
		double sign=1;
		if (value<0) {
			sign=-1;
			value=-value;
		}               

		return (new DecimalFormat("###.##########")).format(sign * value) + unit;		
	}

	public void process() {
		//AC RMS is DC RMS without DC component (= without average)
		//calculate average first, subtract it from the DC RMS

		int n = (int)widthGraph;
		double rmsCh1 = 0, averageCh1 = 0, rmsCh2 = 0, averageCh2 = 0;		
		//loop through all values in display buffer and calculate square sum
		for (int i=0; i<n;i++) {
			averageCh1 += OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
			rmsCh1 += OsciDisplay.getInstance().drawbuffer[channelNo-1][i]*OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
			averageCh2 += OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
			rmsCh2 += OsciDisplay.getInstance().drawbuffer[channelNo-1][i]*OsciDisplay.getInstance().drawbuffer[channelNo-1][i];
		}
		//divide by n (average), divide by n and calculate the square root (rms)
		averageCh1 = (double)averageCh1/n;
		rmsCh1 = Math.sqrt((double)rmsCh1/n);
		averageCh2 = (double)averageCh2/n;
		rmsCh2 = Math.sqrt((double)rmsCh2/n);

		//double value1 = (rmsCh1 - averageCh1) / 32767.0 / 0.25 * this.settings.getDouble();
		double value1 = MeasurementManager.toVoltage((short)(rmsCh1 - averageCh1), channelNo);
		//double value2 = (rmsCh2 - averageCh2) / 32767.0 / 0.25 * this.settings.getDouble();
		double value2 = MeasurementManager.toVoltage((short)(rmsCh2 - averageCh2), channelNo2);
		this.value = value1 / value2 * 100;

		//this.cursorPos[0] = -1 * (rmsCh1 - averageCh1) / 32767.0 * heightGraph/2.0;
		//this.cursorPos[1] = -1 * (rmsCh2 - averageCh2) / 32767.0 * heightGraph/2.0;
		this.cursorPos[0] = MeasurementManager.toVerticalCursorPos((short)(rmsCh1 - averageCh1));
		this.cursorPos[1] = MeasurementManager.toVerticalCursorPos((short)(rmsCh2 - averageCh2));
		this.cursorPos[2] = MeasurementManager.toHorizontalCursorPos(0);
		this.cursorPos[3] = MeasurementManager.toHorizontalCursorPos(widthGraph);
	}
}



class Counter extends AbstractMeasurement {
	public Counter (int channelNo) {
		super("Zähler",channelNo);
		this.unit = "";
		this.usedCursors[0] = true;
	}	

	public void process() {
		this.value = 0;
		this.cursorPos[0] = 0;
	}
}



class Phase extends Abstract2ChannelMeasurement {

	public Phase (int channelNo, int channelNo2) {
		super("Phase",channelNo,channelNo2);
		this.unit = "s";
		for (int i=0;i<4;i++)
			this.usedCursors[i] = true;
	}	

	public void process() {
		this.value = 0;
		this.cursorPos[0] = 0;
		this.cursorPos[1] = 0;
		this.cursorPos[2] = 0;
		this.cursorPos[3] = 0;
	}
}





/* #####################################        Cursor classes        ##################################### */

class CursorX extends AbstractMeasurement {
	public CursorX (int cursorNo) {
		super("X1",1);
		this.unit = "s";
		this.usedCursors[2] = true;

		if (cursorNo == 1) 
			this.name = "X1";
		else if (cursorNo == 2) 
			this.name = "X2";
		else 
			this.name = "";
	}

	public void process() {
		if (this.name == "X1") {
			this.value = MeasurementManager.toTime((int)Settings.CURSORX1.getDouble());
			this.cursorPos[2] = (float)Settings.CURSORX1.getDouble();
		}
		else if (this.name == "X2") {
			this.value = MeasurementManager.toTime((int)Settings.CURSORX2.getDouble());
			this.cursorPos[2] = (float)Settings.CURSORX2.getDouble();
		}
		else {
			this.value = 0;		
			this.cursorPos[2] = 0;
		}
	}
}



class CursorY extends AbstractMeasurement {	
	public CursorY (int channelNo, int cursorNo) {
		super("Y1",channelNo);
		this.usedCursors[0] = true;

		if (cursorNo == 1)
			this.name = "Y1";
		else if (cursorNo == 2) 
			this.name = "Y2";
		else 
			this.name = "";
	}	
	public void process() {
		double scale = 0;
		//check if channel number for the cursor has changed and update it
		switch (Settings.CURSOR_CHANNEL.getInt()) {
		case 1:
			scale = Settings.VC_SCALE_KNOB_ONE.getDouble();
			this.channelNo = 1;
			break;
		case 2:
			scale = Settings.VC_SCALE_KNOB_TWO.getDouble();
			this.channelNo = 2;
			break;
		case 3:
			scale = Settings.VC_SCALE_KNOB_THREE.getDouble();
			this.channelNo = 3;
			break;
		case 4:
			scale = Settings.VC_SCALE_KNOB_FOUR.getDouble();
			this.channelNo = 4;
			break;
		default:
			scale = Settings.VC_SCALE_KNOB_ONE.getDouble();
			this.channelNo = 1;
			break;
		}		
		if (this.name == "Y1") {
			this.value = -1 * Settings.CURSORY1.getDouble() / 46f * scale;
			this.cursorPos[0] = (float)Settings.CURSORY1.getDouble();
		}
		else if (this.name == "Y2") {
			this.value = -1 * Settings.CURSORY2.getDouble() / 46f * scale;
			this.cursorPos[0] = (float)Settings.CURSORY2.getDouble();			
		}
		else {
			this.value = 0;
			this.cursorPos[0] = 0;
		}
	}
}

/* #####################################      Cursor measurements      ##################################### */

class DeltaX extends AbstractMeasurement {	
	public DeltaX () {
		super("ΔX",1);
		this.unit = "s";
	}

	public void process() {
		this.value = MeasurementManager.toTime((int)Math.abs((Settings.CURSORX1.getDouble() - Settings.CURSORX2.getDouble())));	
	}
}



class DeltaXInverted extends AbstractMeasurement {
	public DeltaXInverted () {
		super("1/ΔX",1);
		this.unit = "Hz";
	}

	public void process() {		
		this.value = 1 / MeasurementManager.toTime((int)Math.abs((Settings.CURSORX1.getDouble() - Settings.CURSORX2.getDouble())));
	}
}



class DeltaY extends AbstractMeasurement {
	public DeltaY (int channelNo) {
		super("ΔY",channelNo);
	}

	public void process() {
		double scale = 0;
		switch (Settings.CURSOR_CHANNEL.getInt()) {
		case 1:
			scale = Settings.VC_SCALE_KNOB_ONE.getDouble();
			this.channelNo = 1;
			break;
		case 2:
			scale = Settings.VC_SCALE_KNOB_TWO.getDouble();
			this.channelNo = 2;
			break;
		case 3:
			scale = Settings.VC_SCALE_KNOB_THREE.getDouble();
			this.channelNo = 3;
			break;
		case 4:
			scale = Settings.VC_SCALE_KNOB_FOUR.getDouble();
			this.channelNo = 4;
			break;
		default:
			scale = Settings.VC_SCALE_KNOB_ONE.getDouble();
			this.channelNo = 1;
			break;
		}		
		this.value = Math.abs((Settings.CURSORY1.getDouble() - Settings.CURSORY2.getDouble()) / 46f * scale);
	}
}