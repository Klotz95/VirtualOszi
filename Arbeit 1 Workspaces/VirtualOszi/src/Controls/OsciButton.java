package Controls;

import Settings.LangSing;
import Settings.Settings;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Color;
import java.awt.Cursor;

public class OsciButton extends AbstractOsziComponent {

	public enum DrawType {
	    ROUND, RADIUSED, RADIUSED_BOTTOM 
	}
	
	public enum FunctionType {
	    PUSH, TOGGLE 
	}		
	
	private boolean pressed = false;

	static final long serialVersionUID = 1L;	
	
    private final static float roundness =0.2f;
    DrawType draw;
    FunctionType function;
	Color col;
	Color col2;

	public OsciButton(Settings id) {
		this(id,"",DrawType.RADIUSED_BOTTOM,FunctionType.PUSH,null,Color.LIGHT_GRAY);
	}

	public OsciButton(Settings id,DrawType draw) {
		this(id,draw,FunctionType.PUSH,null,Color.LIGHT_GRAY);
	}

	public OsciButton(Settings id,String Text,DrawType draw) {
		this(id,Text,draw,FunctionType.PUSH,null,Color.LIGHT_GRAY);
	}

	public OsciButton(Settings id,DrawType draw,Color col) {
		this(id,draw,FunctionType.TOGGLE,col,Color.LIGHT_GRAY);
	}

	public OsciButton(Settings id,DrawType draw,Color col,Color col2) {
		this(id,draw,FunctionType.TOGGLE,col,col2);
	}

	public OsciButton(Settings id,String Text,DrawType draw,Color col) {
		this(id,Text,draw,FunctionType.TOGGLE,col,Color.LIGHT_GRAY);
	}
	
	public OsciButton(Settings id, DrawType draw, final FunctionType function,Color col, Color col2) {
		this(id,LangSing.getInst().getTranslation(id),draw,function,col,col2);
	}

	public OsciButton(Settings id, String Text,DrawType draw, final FunctionType function,Color col, Color col2) {
		super(id);
				
		id.setConnectedComponent(this);
		id.headerText=Text;
		this.draw=draw;
		this.function=function;
		this.col=col;
		this.col2=col2;

		setCursor(new Cursor(Cursor.HAND_CURSOR));
		setLayout(null);
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				pressed=true;
				repaint();
				getParent().repaint();
				startSound();
				id.set(!id.getBool());
				if(function==FunctionType.TOGGLE){
					_listener.handleToggleEvent(new MyGuiEvent(getThis(),id.getBool()));
				} else{
					_listener.handleClickEvent(new MyGuiEvent(getThis()));
				}
			}

			public void mouseReleased(MouseEvent e) {
				repaint();
				getParent().repaint();
				Timer timer = new Timer("Mouse Released");
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						pressed=false;
						repaint();
						getParent().repaint();
					}
				}, 50);
			}
		});
	}


	@Override
    protected void paintComponent(final Graphics g) {
    	int Shadow=2;
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (pressed){
        	g.setColor(Color.DARK_GRAY);
            if(draw==DrawType.ROUND)
            	g.fillOval(Shadow, Shadow, getWidth()-Shadow, getHeight()-Shadow);        	
            else{
            	g.fillRoundRect(Shadow, Shadow, getWidth()-Shadow, getHeight()-Shadow, (int)(getWidth()*roundness),(int)(getHeight()*roundness));        	
            }                
        }
        
        
        if(function==FunctionType.TOGGLE){
        	if(id.getBool()) g.setColor(col);
        	else g.setColor(col2);
        }
        else g.setColor(Color.LIGHT_GRAY);

        if(draw==DrawType.ROUND)
        	g.fillOval(0, 0, getWidth()-Shadow, getHeight()-Shadow);        	
        else{
        	g.fillRoundRect(0, 0, getWidth()-Shadow, getHeight()-Shadow, (int)(getWidth()*roundness),(int)(getHeight()*roundness));        	
        }
              
        if(draw==DrawType.RADIUSED_BOTTOM){
        	g.fillRect(0, 0, getWidth()-Shadow, (int)(getWidth()*roundness));        	
        }
        
    	g.setColor(Color.black);
    	String Text=LangSing.getInst().getTranslation(id);
    	if(Text!=""){
        	String[] lines = Text.split("\n");
        	for (int i=0;i<lines.length;i++){
            	TextLayout txt = new TextLayout(lines[i], g2.getFont(),  g2.getFontRenderContext());
            	Rectangle2D bounds = txt.getBounds();
            	double lineh=g2.getFont().getSize();
            	
            	int x = (int) ((getWidth() - bounds.getWidth()) / 2.0 );
            	int y = (int) ((getHeight() - lines.length*lineh) / 2.0 + i*lineh);
            	// g2 is the graphics object 
                g2.drawString(lines[i], x, (int)(y+lineh));    		
        	}   		
    	}
    }
}