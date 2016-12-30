package DataRing;

public class RingBase {

	protected short [] buffer;

	protected int pos;
	public int delayInTicks;
	

	public int calcFullForPos(){
		return calcFullForPos(pos);
	}

	public int calcFullForPos(int pos){
		return (pos+buffer.length/2)%buffer.length;
	}
	
	public void setSampleRate(double samplerateHz){
	}	

	public RingBase(int length) {
		buffer = new short[length];
		pos = 0;
		delayInTicks=0;
	}
	
	public int getLength(){
		return buffer.length;
	}
	
	public short[] getBufferCopy(int triggerPos,int preTrigger){
	triggerPos=triggerPos+delayInTicks+buffer.length;
	//triggerPos=triggerPos+buffer.length;
		int rounds=triggerPos/buffer.length;
		triggerPos-=(rounds*buffer.length);

		int startPos=(triggerPos+buffer.length-preTrigger)%buffer.length;
		int length=(int)(2*preTrigger);
		short[] A2 = new short[length];
		if(startPos+length>buffer.length){
			int border=buffer.length-startPos;
			System.arraycopy( buffer, startPos, A2, 0,border);
			System.arraycopy( buffer, 0, A2, border,length-border);

		} else{
			System.arraycopy( buffer, startPos, A2, 0 , length );			
		}
		
		return A2;
	}

	/**
	 * Empties the ring buffer by creating a new array of the same size

	 */
	public void emptyBuffer(){
		buffer = new short[buffer.length];
		pos=0;
	}


	/**
	 * Insert an array of short values in ring buffer.
	 */
	public void insert(short value){		
		pos = (pos+1)%buffer.length;
		buffer[pos] = value;
	}

	public short get(){
		return buffer[pos];
	}
	
	public short getOldest(){
		return buffer[(pos+1)%buffer.length];
	}	

	/**
	 * Get short values at given position.
	 */
	public short get(int position){
		return buffer[(position+buffer.length)%buffer.length];
	}


	/**
	 * Get the current Position of the ring buffer
	 */
	public int getCurPos(){
		return pos;
	}

}
