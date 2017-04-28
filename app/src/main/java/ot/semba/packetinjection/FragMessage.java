package ot.semba.packetinjection;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;

public class FragMessage extends Fragment {
	private PacketInjectionActivity mTheActivity;
	
	// Interface page accueil
	private EditText valueToSend;
	private TraceManagement traces;
	private Button sendBtn;
    private ListeningThread listeningThread;
    private Button clearBtn;
    
    private int base=16;
    private int packet=1;
    

	public FragMessage() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View vue = inflater.inflate(R.layout.messages, container, false);
		
        
        traces = (TraceManagement)vue.findViewById(R.id.traceDisplay);
        sendBtn = (Button)vue.findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		send();
        	}
    	});
        valueToSend = (EditText)vue.findViewById(R.id.valueToSend);
        
        
        
        clearBtn = (Button)vue.findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		traces.clean();
        	}
    	});
		

        
		traces.setBase(10 );  
		traces.setPacket(1); 
		updateBaseAndPacket(10 ,FragMessage.this.packet);
        updateBaseAndPacket(FragMessage.this.base,1);
        

       
		return vue;
	}


	
	
	
	
    private void updateBaseAndPacket(int newBase, int newPacket){
    	base=newBase;
    	packet=newPacket;
    }
    
       
    /** @result Number of the reserved port or -1 
     */
    public void startFromCustom(String adr){
    	int i=adr.indexOf(":");
		int j=adr.indexOf(":",i+1);

    	if (i>0 && j<0) {
			// No any local port
    		String ip=adr.substring(0,i);
    		String port=adr.substring(i+1);
    		start(ip,port,mTheActivity.isTCPBtnChecked());

    	} else if (i>0 && j>0) {
			// Local port described
			String localPort=adr.substring(0,i);
			String ip=adr.substring(i+1,j);
			String port=adr.substring(j+1);
			start(ip,port, localPort, mTheActivity.isTCPBtnChecked());
		}
	}
	
    
    /** @result Number of the reserved port or -1 
     */
	private void start(String IP, String port, boolean isTCP){
		start(IP, port, "-1", isTCP);
	}

    private void start(String IP, String port, String localPort, boolean isTCP){
    	if(listeningThread!=null) {
    		disconnect();
    	}

		if(localPort==null || localPort.equals("")) localPort="-1";
		int locp = Integer.parseInt(localPort);
		if (locp<1024) {
			localPort="-1";
		}

    	if(isTCP) listeningThread = new TCPThread(traces, 100, IP, Integer.parseInt(port), Integer.parseInt(localPort), this, mTheActivity);
    	else listeningThread = new UDPThread(traces, 100, IP, Integer.parseInt(port), Integer.parseInt(localPort), this, mTheActivity);
    }
    
    synchronized public void callBackForConnect(boolean isConnected, int port) {
    	if (listeningThread!=null && listeningThread.isConnected()) {
    		if(listeningThread instanceof TCPThread) {
    			Toast.makeText(mTheActivity, "TCP connected.", Toast.LENGTH_LONG).show();
    		} else {
    			Toast.makeText(mTheActivity, "UDP socket initialized.", Toast.LENGTH_LONG).show();
    		}
    		mTheActivity.isConnected=true;
    		mTheActivity.publishConnectionStatus(port);
    		//return port;

    	} else {
    		Toast.makeText(mTheActivity, "Connection failed.", Toast.LENGTH_LONG).show();    	
    		mTheActivity.isConnected=false;
    		mTheActivity.publishConnectionStatus(-1);
    		//return -1;
    	}
    }

    
    protected void setAsDisconnected(){
    	mTheActivity.setConnectBtnText("Connect");
    	mTheActivity.isConnected=false;
    }
    
    public void disconnect(){
    	if(listeningThread!=null) listeningThread.closeConnection();
    	listeningThread=null;
    	setAsDisconnected();
    }
    
    public void send(){
    	try{
            //CharSequence b = (CharSequence)(baseSpinner.getAdapter().getItem(baseSpinner.getFirstVisiblePosition()));
            
    		listeningThread.send(messageToBytes());
    	} catch (NullPointerException e){
    		Toast.makeText(mTheActivity, "Not connected.", Toast.LENGTH_LONG).show();    	
    	} catch (Exception e){
    		Toast.makeText(mTheActivity, "Send failed.", Toast.LENGTH_LONG).show();    	
    	}
    }

    
    
    private byte[] messageToBytes(){
    	String rawVal = valueToSend.getText().toString();
    	List<Byte> intermediate = new LinkedList<Byte>();

    	// Step 1 : String to int List
		if(base==16) {
			int pp = packet;
			if (packet==5) {pp=3;}
			
	    	String[] values = rawVal.split(" ");
	    	for(String val:values) {
	    		// Insertion des z���ro muets de t���te pour l'alignement sur la taille des packets
	    		int i=0;
				while(((val.length()+1)/2+i)%pp!=0){ // Test value : ffff0aa 
					intermediate.add((byte)0);
					i++;
				}
				
	    		
				//Ajout de chaque couple de caract���re comme un byte
	    		String s = "";
	    		i=0;
	    		int parite=0;
	    		// Cas o��� le nombre de caract���res est impaire : le premier est un digit ��� part enti���re
	    		if(val.length()%2!=0){
					intermediate.add(Byte.decode("0x"+val.charAt(i)));
					i++;
					parite=1;
	    		}
	    		for( ; i<val.length() ; i++){
					s = s+val.charAt(i);
					if(i%2!=parite){
						int tmp = Integer.decode("0x"+s);
						intermediate.add((byte)tmp);
						s="";
					}
				}
	    	}
	    	Log.i("messageToBytes()", "Sending... : "+intermediate);

		} else if(base==10) {
			// Si base 10 : Conversion en int, puis d���coupage en bytes suivant la taille de packet
	    	String[] values = rawVal.split(" ");
	    	int i=0;
	    	while(i<values.length) {
	    		String val = values[i];

	    		int v=Integer.parseInt(val);
	    		byte b1=(byte)(v & 0xFF);
	    		v=v/256;
	    		byte b2=(byte)(v & 0xFF);
	    		v=v/256;
	    		byte b3=(byte)(v & 0xFF);
	    		v=v/256;
	    		byte b4=(byte)(v & 0xFF);

	    		
	    		switch(packet){
	    		case 4: intermediate.add(b4);
	    		case 3: intermediate.add(b3);
	    		case 2: intermediate.add(b2);
	    		case 1: intermediate.add(b1);
	    			break;
	    		case 5: intermediate.add(b1);
	    				i++;
	    				if(i>=values.length) {
	    					b1=0;
	    					b2=0;
	    				} else {
	    					v=Integer.parseInt(values[i]);
	    					b1=(byte)(v & 0xFF);
	    					v=v/256;
	    					b2=(byte)(v & 0xFF);
	    				}
	    				intermediate.add(b2);
	    				intermediate.add(b1);
	    		}

	    		i++;
	    	}
		} else if(base==256) {
			char[] vals = rawVal.toCharArray();
			for(int i=0 ; i<vals.length ; i++){
				intermediate.add((byte)vals[i]);
			}
		} else if(base==257) {
			char[] vals = rawVal.toCharArray();
			for(int i=0 ; i<vals.length ; i++){
				intermediate.add((byte)vals[i]);
			}
			intermediate.add((byte)'\r');
			intermediate.add((byte)'\n');
		}
		
		// Export of the result
		byte[] res = new byte[intermediate.size()];
		for(int i=0 ; i<res.length ; i++){
			res[i] = intermediate.get(i);
		}
		return res;
    }    	     
        
	
	
    
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	// Display settings methods
	
    
	public void displaySettings() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(mTheActivity);

    	alert.setTitle("Display settings");
    	alert.setMessage("Choose display preferences.");

    	// Set an EditText view to get user input 
    	View theView = mTheActivity.getLayoutInflater().inflate(R.layout.display_settings, null);
    	alert.setView(theView);
    	final Spinner theBase = (Spinner)theView.findViewById(R.id.displayBase);
    	final Spinner theGrouper = (Spinner)theView.findViewById(R.id.displayGroupBy);
    	
    	
    	
        {
	        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mTheActivity, R.array.lesBases, android.R.layout.simple_spinner_item);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        theBase.setAdapter(adapter);

	        adapter = ArrayAdapter.createFromResource(mTheActivity, R.array.groupBy, android.R.layout.simple_spinner_item);
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        theGrouper.setAdapter(adapter);
        }

        switch(base){
        case 10 : theBase.setSelection(0); break;
        case 16 : theBase.setSelection(1); break;
        case 256: theBase.setSelection(2); break;
        case 257: theBase.setSelection(3); break;
        default : theBase.setSelection(0);
        }

        switch(packet){
        case 1  : theGrouper.setSelection(0); break;
        case 2  : theGrouper.setSelection(1); break;
        case 3  : theGrouper.setSelection(2); break;
        case 4  : theGrouper.setSelection(3); break;
        case 5  : theGrouper.setSelection(4); break;
        default : theGrouper.setSelection(0);
        }
        
        

        
    	alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			// if yes
    			
            	String valBase=theBase.getSelectedItem().toString();
            	if(valBase.equals("10"            )) {traces.setBase(10 );  updateBaseAndPacket(10 ,FragMessage.this.packet);}
                if(valBase.equals("16"            )) {traces.setBase(16 );  updateBaseAndPacket(16 ,FragMessage.this.packet);}
            	if(valBase.equals("ASCII"         )) {traces.setBase(256);  updateBaseAndPacket(256,FragMessage.this.packet);}
            	if(valBase.equals("ASCII+\\r\\n"  )) {traces.setBase(257);  updateBaseAndPacket(257,FragMessage.this.packet);}
    			
            	String valGroup=theGrouper.getSelectedItem().toString();
            	if(valGroup.equals("8 bits"        )) {traces.setPacket(1); updateBaseAndPacket(FragMessage.this.base,1);}
            	if(valGroup.equals("16 bits"       )) {traces.setPacket(2); updateBaseAndPacket(FragMessage.this.base,2);}
            	if(valGroup.equals("24 bits"       )) {traces.setPacket(3); updateBaseAndPacket(FragMessage.this.base,3);}
            	if(valGroup.equals("32 bits"       )) {traces.setPacket(4); updateBaseAndPacket(FragMessage.this.base,4);}
            	if(valGroup.equals("8+16 bits"     )) {traces.setPacket(5); updateBaseAndPacket(FragMessage.this.base,5);}
    		}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			// Nothing to do.
    		}
    	});

    	alert.show();
	}
	
	
	
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	//=========================================================
	// Fragment callback methods
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mTheActivity = (PacketInjectionActivity) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mTheActivity = null;
	}

	
	
	
	
}
