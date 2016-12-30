package Controls;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import Settings.Settings;

import java.awt.Color;

public class IncrementButton extends AbstractOsziComponent {

    private int last=0;
    private long sum=0;
    boolean toggle=false;

    static final long serialVersionUID = 1L;

    public enum FunctionType {
        PUSH, NOPUSH, PUSH_1
    }

    FunctionType function;

    public IncrementButton(Settings id) {
        this(id,FunctionType.PUSH);
    }

    public IncrementButton(Settings id, double lowlim, double highlim) {
        this(id,FunctionType.PUSH);
    }

    public IncrementButton(Settings id, final FunctionType function) {
        super(id);
        this.function=function;
        setFocusable(true);

        addKeyListener(new KeyListener(){
        	@Override
        	public void keyTyped(KeyEvent arg0) {}
            
        	@Override
        	public void keyReleased(KeyEvent arg0) {}

        	@Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_UP){
                    step(1);        	
                }
                else if(e.getKeyCode() == KeyEvent.VK_DOWN){
                    step(-1);
                }
            }        	
        });


        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                if(notches!=0){
                    last=notches;
                }
                else{
                    notches=-last;
                }
                step(notches);
            }


        });        

        if(function==FunctionType.PUSH){
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseEntered(MouseEvent e){
                	requestFocus();
                }
                
                @Override
                public void mouseClicked(MouseEvent e){
                    toggle=!toggle;
                    repaint();
                    getParent().repaint();
                    startSound();
                }                
            });
        }//Pressed

        if(function==FunctionType.PUSH_1){
            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseEntered(MouseEvent e){
                	requestFocus();
                }
            	
            	public void mousePressed(MouseEvent e) {
                    repaint();
                    getParent().repaint();
                    startSound();
                    if(function==FunctionType.PUSH_1){//gegändert zu ClickEvent
                        _listener.handleClickEvent(new MyGuiEvent(getThis(),toggle));
                    } else{
                        _listener.handleClickEvent(new MyGuiEvent(getThis()));
                    }
                }
            });
        }
    }
    
    private void step(int notches){
        sum+=notches;
        sum=sum%CalcSteps();
        _listener.handleTickEvent(new MyGuiEvent(getThis(),notches,toggle));
        repaint();
        getParent().repaint();
        startSound();        	
    }    
    
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int centerX=getWidth()/2;
        int centerY=getHeight()/2;
        int w=getWidth();
        int h=getHeight();

        int rand2;
        if(toggle){
            rand2=4;
            g.setColor(Color.DARK_GRAY);
            g.fillOval(0, 0, w, h);
        }
        else rand2=0;
        int rand=rand2+10;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(rand2/2, rand2/2, w-rand2, h-rand2);


        for(int i=0;i<CalcLines();i++){
            double angle=(double)i/CalcLines()*2*Math.PI+CalcAngle();

            double y1=(w-rand2)/2f*Math.sin(angle)+centerY;
            double x1=(w-rand2)/2f*Math.cos(angle)+centerX;
            double y2=(w-rand)/2f*Math.sin(angle)+centerY;
            double x2=(w-rand)/2f*Math.cos(angle)+centerX;
            g.setColor(Color.BLACK);
            g.drawLine((int)(x2+0.5), (int)(y2+0.5), (int)(x1+0.5), (int)(y1+0.5));
        }

        g.setColor(Color.WHITE);
        g.fillOval(rand/2, rand/2, w-rand, h-rand);

        g.setColor(Color.LIGHT_GRAY);
        double angle=CalcAngle();
        double y=(w-rand-10)/2f*Math.sin(angle)+centerY;
        double x=(w-rand-10)/2f*Math.cos(angle)+centerX;
        g.fillOval((int)(x-5+0.5), (int)(y-5+0.5),10,10);
    }

    private int CalcLines(){
        return (int)(getWidth()/8f+0.5);
    }

    private double CalcAngle(){
        return (sum/(float)CalcSteps())*2*Math.PI;
    }

    private int CalcSteps(){
        return 6*CalcLines();
    }

    @Override
    public String toString() {
        return "Value: ";
	}



}
   