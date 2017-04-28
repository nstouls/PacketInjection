package ot.semba.packetinjection;

public class MessageToDisplay {
	public byte[] data;
	public String message;
	
	public MessageToDisplay(String message, byte[] data, int size){
		this.message=message;
		
		if(data!=null) {
			this.data=new byte[size];
			for(int i=0 ; i<size ; i++) {
				this.data[i]=data[i];
			}
		} else {
			this.data=null;
		}
	}
	
	public MessageToDisplay(byte[] data, int size){
		this(null, data, size);
	}
	
	public MessageToDisplay(String message){
		this(message, null, 0);
	}
}
