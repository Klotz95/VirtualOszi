package Controls; 

import javax.swing.JComponent;

import Settings.Settings;

import Settings.GuiListenerSingleton;

import java.awt.Cursor;
import java.awt.Dimension;

public abstract  class AbstractOsziComponent extends JComponent {
	
	MidiThread sound = MidiThread.getInstance();

	MyGuiEventInterface _listener;
	
	static final long serialVersionUID = 1L;
	
	public final Settings id;

    public AbstractOsziComponent(Settings id) {
    	super();
    	this.id=id;
    	
    	_listener = GuiListenerSingleton.getInstance();
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setLayout(null);
        setSize(getWidth(),getWidth());
        setToolTipText(id.toString());
    }
    
    protected void startSound(){
   		sound.start();
    }
    
    protected AbstractOsziComponent getThis(){
    	return this;
    }
    
    @Override
    public void repaint(){
    	super.repaint();
    }
    
    @Override
    public void setSize(Dimension pos){
    	pos.width=pos.height;
    	super.setSize(pos);
    }
    
    @Override
    public void setSize(int x, int y){
    	x=y;
    	super.setSize(x,y);
    }    
    
    @Override
    public Dimension getPreferredSize(){
        return new Dimension(50, 50);
    }    

    @Override
    public Dimension getMinimumSize(){
        return new Dimension(30, 30);
    }
       
    
    @Override
    public Dimension getMaximumSize(){
        return new Dimension(200,200);
    }
    
    
    @Override
    public String toString() {
        return "Value: ";
    }    
}