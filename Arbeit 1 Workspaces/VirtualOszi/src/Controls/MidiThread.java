package Controls;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

class MidiThread  {
	
	class MidiThreadRun extends Thread {	  
		MidiChannel chan;
		int note;
	    
	    public MidiThreadRun(MidiChannel chan,int  note) {
	    	super("MidiThread");
		    this.chan = chan;
		    this.note = note;
		}
  
	    public void run() {
				chan.noteOn( note, 200 );	
	    }
	}
	
	
	private static final class InstanceHolder {
		  static final MidiThread INSTANCE = new MidiThread();
	  }

	  public static MidiThread getInstance () {
	    return InstanceHolder.INSTANCE;
	  }

	
	private int  note;
	Synthesizer synth;
	MidiChannel chan;
	MidiThreadRun run;
  
    public MidiThread() {
    	this(9,77);
    }
    
    public MidiThread(int channel,int  note) {
	    this.note = note;
		try {
			synth = MidiSystem.getSynthesizer();
	    	synth.open();
	    	chan = synth.getChannels()[channel];
		} catch (MidiUnavailableException e) {
		}
		run = new MidiThreadRun(chan, note);
	}
    
    public void start(){
    	if(run.getState()==Thread.State.TERMINATED){
    		run = new MidiThreadRun(chan, note);
    		run.start();
    	} else {
        	if(run.getState()==Thread.State.NEW)
        		run.start();
    	} 
    }
}	
