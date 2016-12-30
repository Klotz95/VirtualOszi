package Controls;
import Settings.Settings;

public class MyGuiEvent extends java.util.EventObject {
	static final long serialVersionUID = 1L;	
	public boolean toggle;
	public int tick;
	public Settings id;
    //here's the constructor
    public MyGuiEvent(AbstractOsziComponent source,boolean toggle) {
        super(source);
    	this.toggle=toggle;
    	this.id=source.id;
    }

    public MyGuiEvent(AbstractOsziComponent source,int tick,boolean toggle) {
        super(source);
    	this.tick=tick;
    	this.toggle=toggle;
    	this.id=source.id;
    }    
    
    public MyGuiEvent(AbstractOsziComponent source) {
        super(source);
    	this.id=source.id;
    }    
}