package ot.semba.packetinjection;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ScrollView;
import android.widget.TextView;

public class TraceManagement extends ScrollView {

	private final static int maxLines = 100;
	private TextView textview;
	volatile private List<String> formatedLines;
	volatile private List<MessageToDisplay> rawLines;

	private int base=10;
	private int packet=2; // nb octets / packet affich� 
	
	
	
	public TraceManagement(Context context) {
		super(context);
		init();
	}

	public TraceManagement(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TraceManagement(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		int c = Color.argb(255,216,255,253);
		setBackgroundColor(c);
		textview = new TextView(getContext());
		textview.setTypeface(Typeface.MONOSPACE);
		textview.setTextSize(11);
		textview.setTextColor(Color.BLACK);
		addView(textview);
		clean();
	}



	
	
	synchronized public void clean() {
		formatedLines = new LinkedList<String>();
		rawLines = new LinkedList<MessageToDisplay>();
		textview.setText("");
	}

	
	private String dataToAscii(byte[] data, String msg){
		String res = msg;
		for (int i = 0; i < data.length; i++) {
			res  = res + ((char)data[i]);
		}
		return res;
	}
	
	private String dataToHexa(byte[] data, String msg){
		String recuHex = msg;
		final String sep = " 0x";
		final String opSep = "/";
		int packetSize=packet;
		boolean withOpSep = false;
		if(packet==5) {
			packetSize=3;
			withOpSep=true;
		}
		
		for (int i = 0; i < data.length; i++) {
			if(i%packetSize==0) recuHex  = recuHex  + sep;
			if(withOpSep && i%packetSize==1) recuHex  = recuHex  + opSep;
			recuHex  = recuHex  + Convert.byte2Hexa(data[i]);
		}
		return recuHex;
	}
	

	private String dataToDeci(byte[] data, String msg){
		String recuDeci = msg;
		String sep = " ";
		long val=0;
		
		if(packet==5) {
			
			final int milieu8 = 128;
			final int milieu16 = (int)Math.pow(2, 15);
			int i=0;
			String rep = "";
			while (i <data.length) {
				val=data[i];
				if(val>=milieu8) val=val-milieu8-milieu8;
				rep = String.format("%5d", val);

				
				i++; // Hypoth�se forte : Le tableau est bien form�.
				val=0;
				if(i<data.length) {
					val=data[i];
					if(val<0) val=val+256;

					i++; // Hypoth�se forte : Le tableau est bien form�.
					if(i<data.length) {
						val=256*val+data[i];
						if(data[i]<0) val=val+256;


						if(val>=milieu16) val=val-milieu16-milieu16;
					}
				}
				rep = rep + "/" + val;
				val=0;
				i++;
				
				while(rep.length()<12){
					rep=rep+" ";
				}
				recuDeci= recuDeci + sep + rep;
			}

		} else {
		
			int milieu = (int)Math.pow(2, (8*packet)-1);
			for (int i = 0; i <data.length; i++) {
				val=256*val+data[i];
				if(data[i]<0) val=val+256;


				if((i+1)%packet==0) {
					if(val>=milieu) val=val-milieu-milieu;
					recuDeci = recuDeci + sep + String.format("%"+(2*packet+3)+"d", val);
					val=0;
				}
			}
			if((data.length)%packet!=0) {
				recuDeci = recuDeci + sep + String.format("%"+(2*packet+3)+"d", val);
			}
		}
		return recuDeci;
	}
	

	
	private void updateFormat(){
		synchronized(formatedLines){
			formatedLines.clear();
			for(MessageToDisplay msg:rawLines) {
				if(msg.data != null) formatedLines.add(formatData(msg.data,(msg.message==null)?"":msg.message));
				else if(msg.message!=null) formatedLines.add(msg.message);
			}
		}
		flush();
	}

	synchronized public void setFormat(int base, int packet){
		this.base=base;
		this.packet=packet;
		updateFormat();
	}

	synchronized public void setBase(int base){
		this.base=base;
		updateFormat();
	}

	synchronized public void setPacket(int packet){
		this.packet=packet;
		updateFormat();
	}
	
	
	
	private String formatData(byte[] data, String msg){
		String res="";
		switch(base){
		case 10:return dataToDeci(data, msg);
		case 16:return dataToHexa(data, msg);
		case 256:return dataToAscii(data, msg);
		case 257:return dataToAscii(data, msg);
		}
		return res;
	}
	
	
	synchronized public void publishMessage(MessageToDisplay msg){
		synchronized(formatedLines){
			if(msg.data != null) formatedLines.add(formatData(msg.data,(msg.message==null)?"":msg.message));
			else if(msg.message!=null) formatedLines.add(msg.message);
			rawLines.add(msg);
			flush();
		}
	}

	
	synchronized public void publishMessage(String msg){
		MessageToDisplay m=new MessageToDisplay(msg);
		publishMessage(m);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// Primitives pour la mise � jour de l'affichage.
	final Handler mHandler = new Handler();
	final Runnable runnableFlush = new Runnable() {
		public void run() {
			privateFlush();
		}
	};

	private Boolean alreadyWaiting = false;
	synchronized public void flush() {
		synchronized (alreadyWaiting) {
			if (!alreadyWaiting) {
				mHandler.post(runnableFlush);
				alreadyWaiting = true;
			}
		}
	}

	private void privateFlush() {
		synchronized (alreadyWaiting) {
			while(formatedLines.size()>maxLines) formatedLines.remove(0);
			while(rawLines.size()>maxLines) rawLines.remove(0);
			
			String s="";
			for (int i = 0; i < formatedLines.size(); i++) {
				s = formatedLines.get(i) + "\n"+s;
			}

			textview.setText(s);
			alreadyWaiting = false;
		}
	}
}
