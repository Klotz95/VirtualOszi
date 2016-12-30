package Display;
import Settings.Settings;
import Trigger.TriggerConditionManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import Communication.AD_Parameter;
import Communication.ChannelInfo;

import java.util.Timer;
import java.util.TimerTask;

public class OsciDisplay extends JPanel {

	private static int widthAll;
	private static int heightAll;
	private static int pixHeader; //space to top
	private static int pixTrailer; //space to bottom //.08	
	private static int pixLeft; //space left
	private static int pixRight; //space right //.2
	private static int widthGraph;
	private static int heightGraph;

	static final long serialVersionUID = 1L;
	public static int chanelNumber =1;

	private short[][] inbuffer = null;

	short[][] drawbuffer = null;
	BufferedImage persistencebitmap=null;
	BufferedImage drawBitmap=null;

	private byte[] persistencetime = null;
	Timer persistencetimer;

	AD_Parameter adPara = null;

	int channelSpaceX = 80;
	int channelPosY = 18;
	int channelPosX[] = new int[4];
	int getChannelPosXTmp = 18;
	int valueSpaceX = 20;

	double display_sample_rate;

	private static Timer offsetInvisibleTimer;
	private static boolean  offsetVisible=false;

	private static Timer triggerLineInvisibleTimer;
	private static boolean  triggerLineVisible=false;

	static String channelColorStr[]={"Orange","Green","Cyan","Magenta"};
	static Settings[] settingsTrigLevel={
			Settings.TRIGGER_LEVEL_ONE,
			Settings.TRIGGER_LEVEL_TWO,
			Settings.TRIGGER_LEVEL_THREE,
			Settings.TRIGGER_LEVEL_FOUR};

	
	static Settings[] settingsCoupligChannel={
			Settings.COUPLING_CHANNEL_1,
			Settings.COUPLING_CHANNEL_2,
			Settings.COUPLING_CHANNEL_3,
			Settings.COUPLING_CHANNEL_4};	
	


	private boolean[] channelActive = new boolean[4];

	Font defaultFont = new Font("default", Font.PLAIN, 11);
	Font bigFont = new Font("default", Font.BOLD, 15);

	//channel colors
	Color channelColor[] = { Color.YELLOW, Color.GREEN, Color.CYAN, Color.MAGENTA };


	private static final class InstanceHolder {
		static final OsciDisplay INSTANCE = new OsciDisplay();
	}

	public static OsciDisplay getInstance () {
		return InstanceHolder.INSTANCE;
	}

	private OsciDisplay() {
		super();

		offsetInvisibleTimer = new Timer("InvisibleTimer");
		triggerLineInvisibleTimer = new Timer("TriggerLineInvisibleTimer");

		initChannelPaint();

		channelActive[0] = Settings.ONE.getBool();
		channelActive[1] = Settings.TWO.getBool();
		channelActive[2] = Settings.THREE.getBool();
		channelActive[3] = Settings.FOUR.getBool();

		if(Settings.DISPLAY_MENU.get().equals("Variable Nachleuchtdauer")) startPersistenceTimer();
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		widthAll = getWidth();
		heightAll = getHeight();
		pixHeader=(int)(.05f*heightAll); //space to top
		pixTrailer=(int)(.16f*heightAll); //space to bottom //.08
		pixLeft=(int)(.025f*widthAll); //space left
		pixRight=(int)(.2f*widthAll); //space right //.2
		widthGraph = widthAll-pixLeft-pixRight;
		heightGraph = heightAll-pixTrailer-pixHeader;
		delPersistence();
	};



	@Override
	protected void finalize(){
		try {
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void initChannelPaint() {
		for(int i = 0; i < 4; i++) {
			channelPosX[i] = getChannelPosXTmp;
			getChannelPosXTmp += channelSpaceX;
		}
	}


	private void drawInfoHeader(Graphics2D g2, String text, int y) {
		g2.setColor(Color.GRAY);
		g2.fillRect(543, y, 127, 16);
		g2.setColor(Color.WHITE);
		g2.drawString(text, 580, y+12);
	}

	private static void drawRect(Graphics g,float x1,float y1,float x2,float y2) {
		g.drawRect((int)(x1+.5),(int)(y1+.5),(int)(x2+.5),(int)(y2+.5));
	}

	private static void drawLine(Graphics g,float x1,float y1,float x2,float y2) {
		g.drawLine((int)(x1+.5),(int)(y1+.5),(int)(x2+.5),(int)(y2+.5));
	}

	/**
	 * Zeichnen der Signale
	 */
	private void drawSignalsandMeasure(Graphics2D g2) {

		synchronized(this){
			g2.drawImage(drawBitmap, pixLeft, pixHeader, pixLeft+widthGraph,  pixHeader+heightGraph, 0, 0, widthGraph, heightGraph, null);
			g2.drawImage(persistencebitmap, pixLeft, pixHeader, pixLeft+widthGraph,  pixHeader+heightGraph, 0, 0, widthGraph, heightGraph, null);
			//paint measurement/cursor area and cursors
			MeasurementManager.getInstance().paint(g2);
		}
	}

	public static void setTransparency(BufferedImage img, byte degree) {
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				img.setRGB(x, y, (img.getRGB(x, y) & 0x00FFFFFF) | degree << 24); // wenn ich mich nicht verz�hlt hab
			}
		}
	}		


	/**
	 * Zeichnen der Triggrschwelle
	 */
	private void drawTriggerLevel(Graphics2D g2){

		int channel;
		try{
			channel=Integer.parseInt(Settings.CHANNEL.get())-1;	
		}catch(NumberFormatException e){
			return;
		}

		/**
		 * TODO_001: Wenn die obere oder untere Grenze erreicht wird, zeigt das reale Oszilloskop ein anderes Bild an.
		 */
		BufferedImage img;
		try {
			img = ImageIO.read(new File(getClass().getResource("/images/Triggerlevel_"+channelColorStr[channel]+".png").toURI()));
		} catch (IOException | URISyntaxException e) {
			System.err.println("Image triggerlevel"+e);
			return;
		}

		ChannelInfo cInfo=AD_Parameter.get_min_max_off(channel);
		int trigger_level_pos;

		if(Settings.HORIZ_TIME_MOD.get().equals("XY")&&(channel==0)){
			double act_trigger_level = - AD_Parameter.volt2plusmius1(settingsTrigLevel[channel].getDouble(), cInfo.scale, cInfo.offset,5);
			trigger_level_pos = (int)(pixLeft+widthGraph/2f+act_trigger_level*widthGraph/2f+.5);
			g2.setColor(channelColor[channel]);
			if(triggerLineVisible){
				g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {8f,2.5f}, 0));
				drawLine(g2, trigger_level_pos, pixHeader, trigger_level_pos, (int) (pixHeader + heightGraph));
			}
			g2.drawImage(img,  trigger_level_pos-7, 4,  trigger_level_pos+5, 16, 0, 0, 80, 80, null);
		} else {
			double act_trigger_level = - AD_Parameter.volt2plusmius1(settingsTrigLevel[channel].getDouble(), cInfo.scale, cInfo.offset,4);
			trigger_level_pos = (int)(pixHeader+heightGraph/2f+act_trigger_level*heightGraph/2f+.5);
			g2.setColor(channelColor[channel]);
			if(triggerLineVisible){
				g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {8f,2.5f}, 0));
				drawLine(g2, pixLeft, trigger_level_pos, (int) (pixLeft + widthGraph), trigger_level_pos);
			}
			g2.drawImage(img, 4, trigger_level_pos-7, 16, trigger_level_pos+5,  0, 0, 80, 80, null);
		}

		double posx=0;
		switch(Settings.HORIZ_NORMAL_TIME_REF.get()){
		case "Mitte":
			posx+=widthGraph/2f;
			break;
		case "Rechts":
			posx+=widthGraph;
			break;
		};		try {
			BufferedImage img2 = ImageIO.read(new File(getClass().getResource("/images/timeReferenceIndicator.png").toURI()));
			g2.drawImage(img2, (int)(posx+pixLeft-4), (int)(pixHeader), (int)(posx+pixLeft+4), (int)(pixHeader+8),  0, 0, 80, 80, null);
		} catch (IOException | URISyntaxException e) {
			System.err.println("Image timeReferenceIndicator"+e);
		}

		double posinPercent = -Settings.HORIZONTAL_POSITION_KNOB.getDouble()/(Settings.HORIZONTAL_SCALE_KNOB.getDouble()*10);
		double scrennpos=posx+posinPercent*widthGraph;
		if(scrennpos>widthGraph) scrennpos=widthGraph;
		else if(scrennpos<0) scrennpos=0;
		try {
			BufferedImage img2 = ImageIO.read(new File(getClass().getResource("/images/triggerIndicator.png").toURI()));
			g2.drawImage(img2, (int)(pixLeft+scrennpos-4), (int)(pixHeader), (int)(pixLeft+scrennpos+4), (int)(pixHeader+8),  0, 0, 80, 80, null);
		} catch (IOException | URISyntaxException e) {
			System.err.println("Image triggerindicator"+e);
		}		
	}

	private void drawLevelIndicator(Graphics2D g2){
		BufferedImage image = null;

		ChannelInfo[] cInfo=AD_Parameter.get_min_max_off();

		for(int i = 0; i < 4 ; i++){

			//Nullliniensymbol zeichnen
			if(channelActive[i]){
				try{
					image = ImageIO.read(new File(getClass().getResource("/images/Level_Indicator_"+channelColorStr[i]+".png").toURI()));
				} catch(IOException | URISyntaxException e) {
					System.err.println("Image triggerlevel"+e);
				}
				if((i==0)&(Settings.HORIZ_TIME_MOD.get().equals("XY"))){
					double offset_level = -AD_Parameter.volt2plusmius1(0, cInfo[i],5);
					int offset_level_pos = (int)(pixHeader+widthGraph/2f+offset_level*widthGraph/2f+.5);
					g2.drawImage(image, offset_level_pos-11,20,offset_level_pos+4,38,  0, 0, 39, 33, null);
				}
				else{
					double offset_level = -AD_Parameter.volt2plusmius1(0, cInfo[i],4);
					int offset_level_pos = (int)(pixHeader+heightGraph/2f+offset_level*heightGraph/2f+.5);
					g2.drawImage(image, 1,offset_level_pos-4,19,offset_level_pos+11,  0, 0, 39, 33, null);					
				}
			}
		}
	}

	private void drawOffsetInfo(Graphics2D g2, int channelNumber){
		String val=AD_Parameter.settingsOff[channelNumber-1].getScienceNotation();
		String s = "Kan("+ channelNumber + ") = "+ val +"V";
		g2.setColor(Color.black);
		g2.fillRect(360, 45, 155, 18);
		g2.setColor(Color.WHITE);
		g2.setStroke (new BasicStroke((float) 0.5));
		g2.drawRoundRect(360, 45, 155, 18, 6, 6);
		g2.setColor(channelColor[channelNumber-1]);
		g2.drawString(s, 364, 58);
	}


	/**
	 * Anzeigen in der Kopfzeile
	 */
	private void drawTriggerInformation(Graphics2D g2){

		try{
			int ch=Integer.parseInt(Settings.CHANNEL.get());	
			g2.setColor(channelColor[ch-1]);
			g2.drawString(Settings.CHANNEL.get(), channelPosX[3] + 320, channelPosY);
			g2.drawString(settingsTrigLevel[ch-1].getScienceNotation() +settingsTrigLevel[ch-1].getUnit(), channelPosX[3] + 360, channelPosY);
		}catch(NumberFormatException e){
			g2.setColor(Color.WHITE);
			g2.drawString("W", channelPosX[3] + 320, channelPosY);
		}

		g2.setColor(Color.WHITE);

		// Triggerlevel


		//Trigger Text
		if(Settings.MODE_COUPLING.get().equals("Auto")){
			if(TriggerConditionManager.getInstance().isNormalTriggered()){
				g2.drawString("Auto", channelPosX[3] + 240, channelPosY);
			}
			else {
				g2.drawString("Auto?", channelPosX[3] + 240, channelPosY);
			}
		}
		else{
			if(TriggerConditionManager.getInstance().isNormalTriggered()){
				g2.drawString("Getrigg.", channelPosX[3] + 240, channelPosY);
			}
			else {
				g2.drawString("Getr.?", channelPosX[3] + 240, channelPosY);
			}
		}
		// Triggerbedingung


		// TODO_003: Das reale Oszilloskop blendet statt der verwendeten Texte zumeist Symbole ein
		g2.setColor(Color.WHITE);
		switch(Settings.TRIG_TYPE.get()){
		case "Flanke":
			// Anzeige abhängig von gewählter Flanke
			g2.drawString("Fl", channelPosX[3] + 280, channelPosY);
			break;
		case "Flanke dann Flanke":
			g2.drawString("FlFl", channelPosX[3] + 280, channelPosY);
			break;
		case "Pulsbreite":
			g2.drawString("Pu", channelPosX[3] + 280, channelPosY);
			break;
		case "Bitmuster":
			g2.drawString("Pat", channelPosX[3] + 280, channelPosY);
			break;
		case "ODER":
			g2.drawString("Or", channelPosX[3] + 280, channelPosY);
			break;
		case "Anstiegs-/Abfallzeit":
			g2.drawString("Ri", channelPosX[3] + 280, channelPosY);
			break;
		case "Nte Flanke Burst":
			g2.drawString("NthE", channelPosX[3] + 280, channelPosY);
			break;
		case "Niedriger Impuls":
			g2.drawString("Imp", channelPosX[3] + 280, channelPosY);
			break;
		case "Setup und Halten":
			g2.drawString("S&H", channelPosX[3] + 280, channelPosY);
			break;
		case "Video":
			g2.drawString("Vid", channelPosX[3] + 280, channelPosY);
			break;
		case "USB":
			g2.drawString("USB", channelPosX[3] + 280, channelPosY);
			break;	
		}
	}

	public void updateData(short[][] buffer,AD_Parameter adPara){
		this.adPara =adPara;

		switch(Settings.ACQUIRE_MOD.get()){
		case "Spitze Erkennen":
		case "Hohe Auflösung":
		case "Normal":
			inbuffer = buffer;
			break;
		case "Mittelwert":
			if((inbuffer!=null)&&(inbuffer.length==buffer.length)&&(inbuffer[0].length==buffer[0].length)){
				int factor=Settings.ACQUIRE_MIDLE.getInt()-1;
				for (int i = 0; i < buffer.length; i++) {
					for (int j = 0; j < buffer[0].length; j++) {
						inbuffer[i][j]=(short)((((buffer[i][j]<<10)+(inbuffer[i][j]<<10)*factor)/(factor+1))>>10);			
					}
				}				
			} else {
				inbuffer = buffer;
			}
			break;
		};

		recalcViewBuffer();
	}

	public void setChannels(int channelNo, boolean channelValue) {
		channelActive[channelNo] = channelValue;
	}

	public void updateOffset(int Number){
		chanelNumber = Number;
		setOffsetInvisibleTimer();
	}

	public void updateTriggerLine(){
		setTriggerLineInvisibleTimer();
	}


	private void setOffsetInvisibleTimer(){
		offsetVisible=true;
		offsetInvisibleTimer.cancel();
		offsetInvisibleTimer = new Timer("OffsetInvisibleTimer");
		offsetInvisibleTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				offsetInvisibleTimer.cancel();
				offsetVisible=false;
				repaint();
			}
		}, 3000);
	}

	private void setTriggerLineInvisibleTimer(){
		triggerLineVisible=true;
		triggerLineInvisibleTimer.cancel();
		triggerLineInvisibleTimer = new Timer("TriggerLineInvisibleTimer");
		triggerLineInvisibleTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				triggerLineInvisibleTimer.cancel();
				triggerLineVisible=false;
				repaint();
			}
		}, 3000);
	}

	public void paintComponent(Graphics g) {

		Graphics2D g2=(Graphics2D)g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		//Background
		g.setColor(Color.black);
		g.fillRect(0, 0, widthAll, heightAll);

		//Borders
		g.setColor(Color.white);
		//        drawLine(g, 0, pixHeader, pixLeft,pixHeader);
		//        drawLine(g, 0, heightAll-pixTrailer, pixLeft, heightAll-pixTrailer); // ?
		drawRect(g, pixLeft, pixHeader,widthGraph, heightGraph);

		g.setColor(new Color(1f,1f,1f,Settings.DISPLAY_INTESITY.getInt()/100.0f));

		//Rasterlines X
		g2.setStroke (new BasicStroke(heightGraph, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {1f,(widthGraph)/10f-1.0f}, 0));
		drawLine(g2, pixLeft, pixHeader+heightGraph/2, widthGraph+pixLeft,pixHeader+heightGraph/2);

		//Rasterlines Y
		g2.setStroke (new BasicStroke(widthGraph, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {1f,(heightGraph)/8f-1.0f}, 0));
		drawLine(g2, pixLeft+widthGraph/2, pixHeader, pixLeft+widthGraph/2,heightGraph+pixHeader);

		//X-Achses Dashes  
		g2.setStroke (new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {1f,(widthGraph)/40f-1}, 0));
		drawLine(g2, pixLeft, pixHeader+heightGraph/2+0.5f, widthGraph+pixLeft,pixHeader+heightGraph/2+0.5f);

		//Y-Achses Dashes
		g2.setStroke (new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f, new float[] {1f,heightGraph/40f-1}, 0));
		drawLine(g2, pixLeft+widthGraph/2+0.5f, pixHeader, pixLeft+widthGraph/2+0.5f,heightGraph+pixHeader);

		g2.setStroke(new BasicStroke(1));
		//set default font
		g2.setFont(defaultFont);
		g2.setColor(Color.WHITE);


		//draw channel information
		for(int i = 0; i < 4; i++) {
			g2.setColor(channelColor[i]);
			g2.drawString(String.valueOf(i + 1), channelPosX[i], channelPosY);
			if(channelActive[i]){
				g2.setColor(channelColor[i]);
				g2.drawString(AD_Parameter.settingsScale[i].getScienceNotation() + AD_Parameter.settingsScale[i].getUnit(), channelPosX[i] + valueSpaceX, channelPosY);
			}
		}

		drawSignalsandMeasure(g2);
		drawTriggerLevel(g2);
		drawTriggerInformation(g2);

		drawLevelIndicator(g2);
		if(offsetVisible) drawOffsetInfo(g2,chanelNumber);

		//draw header info
		g2.setColor(Color.WHITE);		
		g2.drawString(Settings.HORIZONTAL_POSITION_KNOB.getScienceNotation() + Settings.HORIZONTAL_POSITION_KNOB.getUnit(), channelPosX[3] + 120, channelPosY);
		g2.drawString(Settings.HORIZONTAL_SCALE_KNOB.getScienceNotation() + Settings.HORIZONTAL_SCALE_KNOB.getUnit(), channelPosX[3] + 175, channelPosY);


		/*--------------------------------------------------*/
		/* 
		 * Dieser Abschnitt enthält bisher nur Dummy-Text.
		 * Umsetzung auf die tatsächlichen Werte der Kanäle
		 */

		//draw info menu right
		g2.setColor(Color.GRAY);
		g2.setStroke (new BasicStroke((float) 0.5));		
		g2.drawRoundRect(543, 23, 127, 367, 20, 20);

		//info menu header
		g2.setColor(Color.WHITE);
		g2.setFont(bigFont);
		g2.drawString("Agilent", 580, 48);
		g2.setFont(defaultFont);

		//erfassung
		drawInfoHeader(g2, "Erfassung", 60);
		g2.drawString("Mittelwert: 8", 555, 92);

		g2.drawString(AD_Parameter.getValueInScienceNotation("###.##",AD_Parameter.get_samplerateOszi())+" Sa/s", 555, 108);

		//channels
		drawInfoHeader(g2, "Kan�le", 125);

		//info view
		int []posLine=new int[]{155,172,189,206};
		for(int i=0;i<4;i++){
			g2.setColor(channelColor[i]);
			g2.drawString(settingsCoupligChannel[i].get(), 550, posLine[i]);
			g2.drawString(AD_Parameter.settingsScale[i].get()+AD_Parameter.settingsScale[i].getUnit(), 590, posLine[i]);
			g2.drawString("1:1", 630, posLine[i]);		
		}

		//paint buttons and menu
		OsciDisplayMenu.getInstance().paint(g2);
	}

	public void recalcViewBuffer(){
		if(adPara==null) return;
		synchronized(this){
			double data_sample_rate = adPara.sampleRate;
			display_sample_rate = AD_Parameter.get_samplerateOszi();

			double step=data_sample_rate/display_sample_rate;

			int width=(int)(inbuffer[0].length/step);

			double ticksOnDisplay=display_sample_rate*Settings.HORIZONTAL_SCALE_KNOB.getDouble()*10;

			double step2=ticksOnDisplay/widthGraph;

			int pI = (int)((AD_Parameter.get_xpos() - adPara.xPos)*display_sample_rate/step2);

			int start=(int)((ticksOnDisplay-width)/2);

			int min=Math.max(0,(int)((start)/step2-pI+0.5));
			int max=Math.min((int)widthGraph,(int)((width-1+start)/step2-pI+.5));

			if(max-min<0) max=min;//no negativ drawbuffer allowed

			drawbuffer= new short[4][max-min]; 
			drawBitmap= new BufferedImage(widthGraph, heightGraph,BufferedImage.TYPE_INT_ARGB); 

			switch(Settings.HORIZ_TIME_MOD.get()){
			case "Normal":
				switch(Settings.ACQUIRE_MOD.get()){
				case "Normal":
					Normal(min, max, pI, step2, start,step);
					break;
				case "Mittelwert":
					Mittel(min, max, pI, step2, start,step);
					break;
				case "Hohe Auflösung":
					break;
				case "Spitze Erkennen":
					Peak(min, max, pI, step2, start,step);
					break;
				};
				break;
			case "XY":
				XY(min, max, pI, step2, start,step);
				break;
			}

			if(!Settings.DISPLAY_MENU.get().equals("Aus")){
				addPersistence();
			}		}
		repaint();
	}

	public void XY(int min, int max, int pI, double step2, int start,double step){
		Graphics2D g2 = drawBitmap.createGraphics();
		BasicStroke bs = new BasicStroke(1.2f);
		g2.setStroke(bs);

		double scaleX=adPara.val_oszi[0].scale;
		scaleX=scaleX/AD_Parameter.settingsScale[0].getDouble();
		double scaleY=adPara.val_oszi[1].scale;
		scaleY=scaleY/AD_Parameter.settingsScale[1].getDouble();

		double maxV=AD_Parameter.resolution;

		g2.setColor(channelColor[0]);

		int xPosOld=0;
		int yPosOld=0;

		for(int i=min;i<max;i++){
			int posI=(int)(((i+pI+0.5)*step2-start)*step);
			if((posI>=0)&&(posI<inbuffer[0].length)){

				double offsetX=adPara.val_oszi[0].offset;				
				offsetX=-AD_Parameter.volt2ticks(-offsetX/1000000, AD_Parameter.get_min_max_off()[0]);
				double offsetY=adPara.val_oszi[1].offset;				
				offsetY=-AD_Parameter.volt2ticks(-offsetY/1000000, AD_Parameter.get_min_max_off()[1]);
				
				double x=(-inbuffer[0][posI]+offsetX)*scaleX;
				double y=(-inbuffer[1][posI]+offsetY)*scaleY;
				if(x>maxV) x=maxV;
				else if(x<-maxV) x=-maxV;
				if(y>maxV) y=maxV;
				else if(y<-maxV) y=-maxV;

				int xPos=(int)((x/maxV*4.0/5.0)*widthGraph/2f+widthGraph/2f);
				int yPos=(int)((y/maxV)*heightGraph/2f+heightGraph/2f);

				if(i!=min) g2.drawLine(xPosOld,yPosOld,xPos,yPos);	
				xPosOld=xPos;
				yPosOld=yPos;
			}
		}		
	}

	public void Normal(int min, int max, int pI, double step2, int start,double step){
		Graphics2D g2 = drawBitmap.createGraphics();
		BasicStroke bs = new BasicStroke(1.5f);
		g2.setStroke(bs);

		double offy=heightGraph/2f;
		double maxV=AD_Parameter.resolution;

		short[] dbuffer = new short[max-min];

		for (int channelnum = 0; channelnum < 4; channelnum++){
			if (channelActive[channelnum]){
				
				double offset=adPara.val_oszi[channelnum].offset;				
				offset=-AD_Parameter.volt2ticks(-offset/1000000, AD_Parameter.get_min_max_off()[channelnum]);
				double scale=adPara.val_oszi[channelnum].scale;

				scale=scale/AD_Parameter.settingsScale[channelnum].getDouble();

				for(int i=min;i<max;i++){
					int posI=(int)(((i+pI+0.5)*step2-start)*step);
					if((posI>=0)&&(posI<inbuffer[channelnum].length)){

						int value=(int)((inbuffer[channelnum][posI]-offset)*scale);
						if(value>Short.MAX_VALUE) value=Short.MAX_VALUE;
						else if(value<Short.MIN_VALUE) value=Short.MIN_VALUE;

						dbuffer[i-min] = (short)(value);
						drawbuffer[channelnum][i-min]=(short)(value);
					}
				}

				g2.setColor(channelColor[channelnum]);
				//drawRect(g2, offx, (int)(offy-(drawbuffer[channelnum][0])/maxV), 1,1);
				for(int i=1;i<dbuffer.length;i++){
					//drawRect(g2, i+offx, (int)(offy-drawbuffer[channelnum][i]/maxV*heightGraph/2f+.5), 1,1);
					g2.drawLine((int) (i+2+min), (int)(offy-dbuffer[i]/maxV*heightGraph/2f+.5),(int) (i+1+min), (int)(offy-dbuffer[i-1]/maxV*heightGraph/2f+.5));						
				}				
			}
		}		
	}	

	public void Peak(int min, int max, int pI, double step2, int start,double step){

		Graphics2D g2 = drawBitmap.createGraphics();
		BasicStroke bs = new BasicStroke(1);
		g2.setStroke(bs);

		double offy=heightGraph/2f;
		double maxV=AD_Parameter.resolution;

		short[] dbufferMin = new short[max-min];
		short[] dbufferMax = new short[max-min];

		for (int channelnum = 0; channelnum < 4; channelnum++){
			if (channelActive[channelnum]){
				double scale=adPara.val_oszi[channelnum].scale;
				double offset=adPara.val_oszi[channelnum].offset;				
				offset=-AD_Parameter.volt2ticks(-offset/1000000, AD_Parameter.get_min_max_off()[channelnum]);

				scale=scale/AD_Parameter.settingsScale[channelnum].getDouble();

				for(int i=min;i<max-1;i++){
					int Min=Short.MAX_VALUE;
					int Max=Short.MIN_VALUE;
					for(int j=0;j<step2;j++){
						int posI=(int)(((i+pI+0.5)*step2-start+j)*step);
						if((posI>=0)&&(posI<inbuffer[channelnum].length)){
							int value=(int)((inbuffer[channelnum][posI]-offset)*scale);
							if(Min>value) Min=value;
							if(Max<value) Max=value;
						}

					}
					if(Max>Short.MAX_VALUE) Max=Short.MAX_VALUE;
					else if(Min<Short.MIN_VALUE) Min=Short.MIN_VALUE;

					dbufferMax[i-min] = (short)(Max); 
					dbufferMin[i-min] = (short)(Min); 
					drawbuffer[channelnum][i-min]=(short)(Max-Min);
				}

				//drawRect(g2, offx, (int)(offy-(drawbuffer[channelnum][0])/maxV), 1,1);
				for(int i=1;i<dbufferMax.length;i++){
					g2.setColor(channelColor[channelnum]);
					int a=(int)(offy-dbufferMax[i]/maxV*heightGraph/2f+.5);
					int b=(int)(offy-dbufferMin[i]/maxV*heightGraph/2f+.5);
					int c=(int)(offy-dbufferMin[i-1]/maxV*heightGraph/2f+.5);
					int d=(int)(offy-dbufferMax[i-1]/maxV*heightGraph/2f+.5);

					g2.drawPolygon(new int[]{i+2+min,i+2+min,i+1+min,i+1+min}, new int[]{a,b,c,d}, 4);
					g2.fillPolygon(new int[]{i+2+min,i+2+min,i+1+min,i+1+min}, new int[]{a,b,c,d}, 4);
				}
			}
		}
	}	

	public void Mittel(int min, int max, int pI, double step2, int start,double step){

		Graphics2D g2 = drawBitmap.createGraphics();
		BasicStroke bs = new BasicStroke(1.5f);
		g2.setStroke(bs);

		double offy=heightGraph/2f;
		double maxV=AD_Parameter.resolution;

		short[] dbuffer = new short[max-min];

		for (int channelnum = 0; channelnum < 4; channelnum++){
			if (channelActive[channelnum]){
				double scale=adPara.val_oszi[channelnum].scale;
				double offset=adPara.val_oszi[channelnum].offset;				
				offset=-AD_Parameter.volt2ticks(-offset/1000000, AD_Parameter.get_min_max_off()[channelnum]);

				scale=scale/AD_Parameter.settingsScale[channelnum].getDouble();

				for(int i=min;i<max-1;i++){
					double sum=0;
					int count=0;
					for(int j=0;j<step2;j++){
						int posI=(int)(((i+pI+0.5)*step2-start+j)*step);
						if((posI>=0)&&(posI<inbuffer[channelnum].length)){
							sum+=(inbuffer[channelnum][posI]-offset)*scale;
							count++;

						}
					}
					sum=sum/count;
					if(sum>Short.MAX_VALUE) sum=Short.MAX_VALUE;
					else if(sum<Short.MIN_VALUE) sum=Short.MIN_VALUE;

					dbuffer[i-min] = (short)(sum); 
					drawbuffer[channelnum][i-min]=(short)(sum);
				}

				g2.setColor(channelColor[channelnum]);
				//drawRect(g2, offx, (int)(offy-(drawbuffer[channelnum][0])/maxV), 1,1);
				for(int i=1;i<dbuffer.length;i++){
					//drawRect(g2, i+offx, (int)(offy-drawbuffer[channelnum][i]/maxV*heightGraph/2f+.5), 1,1);
					g2.drawLine((int) (i+2+min), (int)(offy-dbuffer[i]/maxV*heightGraph/2f+.5),(int) (i+1+min), (int)(offy-dbuffer[i-1]/maxV*heightGraph/2f+.5));						
				}
			}
		}
	}

	public void delPersistence(){
		persistencebitmap=new BufferedImage(widthGraph, heightGraph,BufferedImage.TYPE_INT_ARGB);
		persistencetime=new byte[heightGraph*widthGraph];
	}

	public void addPersistence(){
		byte time=(byte)Settings.DISPLAY_VAR_LIGHT_TIME.getInt();

		int[] pixel=drawBitmap.getRGB(0, 0, widthGraph, heightGraph, null, 0, widthGraph);
		int[] pixel2=persistencebitmap.getRGB(0, 0, widthGraph, heightGraph, null, 0, widthGraph);

		for(int i=0;i<pixel.length;i++){
			if((pixel[i]&0xFF000000)!=0){
				pixel2[i]=pixel[i]&0x88FFFFFF;				
				persistencetime[i]=time;
			}
		}
		persistencebitmap.setRGB(0, 0, widthGraph, heightGraph, pixel2, 0, widthGraph);
	}

	public void decrementPersistenceTime(){
		int[] pixel2=persistencebitmap.getRGB(0, 0, widthGraph, heightGraph, null, 0, widthGraph);
		int rgbcolor=0x00FFFFFF;	//Transparent
		for (int channelnum = 0; channelnum < 4; channelnum++){
			if (channelActive[channelnum]){
				for(int y=0;y<persistencetime.length;y++){
					if(persistencetime[y]>0){
						persistencetime[y]--;
					}
					else {
						pixel2[y]=rgbcolor;				
						persistencetime[y]=0;
					}

				}
			}
		}
		persistencebitmap.setRGB(0, 0, widthGraph, heightGraph, pixel2, 0, widthGraph);
		repaint();
	}

	public void delDisplay(){
		delPersistence();
		drawBitmap= new BufferedImage(widthGraph, heightGraph,BufferedImage.TYPE_INT_ARGB); 

	}

	public void stopPersistenceTimer(){
		if(persistencetimer!=null)
			persistencetimer.cancel();
	}

	public void startPersistenceTimer(){
		stopPersistenceTimer();
		persistencetimer = new Timer("Persistencetimer");
		persistencetimer.schedule(new TimerTask() {
			@Override
			public void run() 
			{
				decrementPersistenceTime();
			}
		}, 1000,1000);
	}

}

