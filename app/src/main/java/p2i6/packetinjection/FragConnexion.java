package p2i6.packetinjection;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.Inflater;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
public class FragConnexion extends Fragment {

	private PacketInjectionActivity mTheActivity;
	
	private ListView listIPs;
	private EditText customAdress;
	private Button connectBtn;	
	private ToggleButton isTCPBtn;	
//	private boolean isConnected=false;
    
//    private Button addPresetBtn;
//    private EditText presetName;
    private List<ConnexionID> Presets;

    final static String PACKET_INJECTION_PRESETS_FILE_NAME="PacketInjection.Presets";

    
    
	public FragConnexion() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View vue = inflater.inflate(R.layout.connexion, container, false);

		
        

        //IHM Construction
        customAdress = (EditText)vue.findViewById(R.id.customAdress);
        connectBtn = (Button)vue.findViewById(R.id.connectcustom);
        connectBtn.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if(mTheActivity.isConnected){
        			mTheActivity.disconnect();
        		}else {
        			mTheActivity.startFromCustom(customAdress.getText().toString());
        		}
     	   	}
    	});
        
        
        isTCPBtn = (ToggleButton)vue.findViewById(R.id.isTCP);


        loadPresets();
//        presetName=(EditText)vue.findViewById(R.id.presetName);
/*        addPresetBtn=(Button)vue.findViewById(R.id.addPreset);
        addPresetBtn.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		//addNewPreset(presetName.getText().toString());
        		askForPresetName();
        	}
    	});
  */
        
        listIPs = (ListView)vue.findViewById(R.id.listIPs);
        listIPs.setTextFilterEnabled(true);
        listIPs.setOnItemClickListener(new OnItemClickListener(){
			@Override
        	public void onItemClick(AdapterView<?> items, View view, int position, long rowId) {
        		ConnexionID ci = (ConnexionID)(items.getItemAtPosition(position));
        		customAdress.setText(ci.ip+":"+ci.port);
      			isTCPBtn.setChecked(ci.proto==ConnexionID.protocol.TCP);
        	}
        });   
        
        listIPs.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> items, View view, int position, long rowId) {
				removeIPFromList(position);
				return true; // true if the callback consumed the long click, false otherwise 
			}
        });   
        updateListItems();
		
		
		return vue;
	}

	
	
	
    public void setConnectBtnText(String s){
    	connectBtn.setText(s);
    }
	
	
    public boolean isTCPBtnChecked () {
    	return isTCPBtn.isChecked();
    }
	
	
	
	
	
	
	
	
	
	//================================================================
	//================================================================
	//================================================================
	//================================================================
	//================================================================
	//================================================================
	// Presets management


    void askForPresetName(){
    	askForPresetName("","", "",false, "");
    }
	
    private void askForPresetName(String iIp, String iPort, String iLocalPort, boolean iIsTCP, String iName){
    	AlertDialog.Builder alert = new AlertDialog.Builder(mTheActivity);

    	alert.setTitle("Adding a preset");
    	alert.setMessage("Choose a name for this new preset");

    	// Set an EditText view to get user input 
    	View theView = mTheActivity.getLayoutInflater().inflate(R.layout.new_preset, null);
    	alert.setView(theView);
    	final EditText name = (EditText)theView.findViewById(R.id.name);
    	final EditText IP = (EditText)theView.findViewById(R.id.IP);
    	final EditText port = (EditText)theView.findViewById(R.id.port);
		final EditText localPort = (EditText)theView.findViewById(R.id.localPort);
    	final ToggleButton isTCP = (ToggleButton)theView.findViewById(R.id.isTCP);


    	name.setText(iName);
    	IP.setText(iIp);
    	port.setText(iPort);
		localPort.setText(iLocalPort);
    	isTCP.setSelected(iIsTCP);
    	
    	
    	// Filter for IP Numbers
    	InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start,
                    int end, Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart) +
                    source.subSequence(start, end) +
                    destTxt.substring(dend);
                    if (!resultingTxt.matches ("^\\d{1,3}(\\." +
                            "(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) { 
                        return "";
                    } else {
                        String[] splits = resultingTxt.split("\\.");
                        for (int i=0; i<splits.length; i++) {
                            if (Integer.valueOf(splits[i]) > 255) {
                                return "";
                            }
                        }
                    }
                }
            return null;
            }
        };
        IP.setFilters(filters);
        
        
    	
    	alert.setPositiveButton("Add preset", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			String lePort = port.getText().toString();
				String leLocalPort = localPort.getText().toString();
    			String leNom = name.getText().toString();
    			String lIp = IP.getText().toString();
    			boolean proto = isTCP.isSelected();

				System.out.println("Le port : "+lePort);
				System.out.println("Le port local : "+leLocalPort);
    			System.out.println("Le nom : "+leNom);
    			System.out.println("Le lIp : "+lIp);
    			System.out.println("Le proto : "+proto);

    			boolean addStatus = addNewPreset(lIp, lePort, leLocalPort, proto, leNom);
    			if(!addStatus) {
    				askForPresetName(lIp, lePort, leLocalPort, proto, leNom);
    			}
    		}
    	});

    	alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int whichButton) {
    			// Nothing to do.
    		}
    	});

    	alert.show();
    }
	
	public void updateListItems() {
        listIPs.setAdapter(new ArrayAdapter<ConnexionID>(getActivity(), R.layout.item_of_ip_list, Presets));
    }
    
    private ConnexionID selectedItem;
    private void removeIPFromList(int num){
    	selectedItem=Presets.get(num);
    	
		// Generation of a Yes/No picker
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        	@Override
        	public void onClick(DialogInterface dialog, int which) {
        		switch (which){
        		case DialogInterface.BUTTON_POSITIVE:
        			Presets.remove(selectedItem);
        			updateListItems();
        			savePresets();
        			Toast.makeText(getActivity(),FragConnexion.this.selectedItem+" removed", Toast.LENGTH_LONG).show();    	
        			break;

        		case DialogInterface.BUTTON_NEGATIVE:
            		Toast.makeText(mTheActivity, "Canceled", Toast.LENGTH_LONG).show();    	
        			break;
        		}
        	}
        };

		AlertDialog.Builder builder = new AlertDialog.Builder(mTheActivity);
		builder.setMessage("Do you want to remove "+selectedItem+" ?").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
    }
    
    
    
    
    
    private boolean addNewPreset(String ip, String portString, String localPortString, boolean isTCP, String name){
    	if(name==null || name.equals("")){
    		Toast.makeText(mTheActivity, "Please provide a name for this preset. Adding canceled", Toast.LENGTH_LONG).show();
    		return false;
    	}
    	
		int port=0;
		try{
			port=Integer.parseInt(portString);
		} catch(NumberFormatException e){
    		Toast.makeText(mTheActivity, "Please provide a valid port number for this preset. Adding canceled", Toast.LENGTH_LONG).show();
    		return false;
		}
		int localPort=0;
		try{
			localPort=Integer.parseInt(localPortString);
		} catch(NumberFormatException e){
			Toast.makeText(mTheActivity, "Please provide a valid port number for this preset. Adding canceled", Toast.LENGTH_LONG).show();
			return false;
		}
    	ConnexionID ci = new ConnexionID(name, ip, port, localPort, isTCP);
    	
    	
    	if (Presets.contains(ci)){
    		Toast.makeText(mTheActivity, "This name or address already in presets. Adding canceled", Toast.LENGTH_LONG).show();
    		return false;
    	}

    	Presets.add(ci);
    	updateListItems();
    	
    	savePresets();
		return true;
    }

    
    
    private void savePresets(){
    	try {
			// ouverture d'un flux de sortie vers le fichier "personne.serial"
			FileOutputStream fos = getActivity().openFileOutput(PACKET_INJECTION_PRESETS_FILE_NAME, Context.MODE_PRIVATE);

			// cr�ation d'un "flux objet" avec le flux fichier
			ObjectOutputStream oos= new ObjectOutputStream(fos);
			try {
				// s�rialisation : �criture de l'objet dans le flux de sortie
				oos.writeObject(Presets); 
				oos.flush();
			} finally {
				//fermeture des flux
				try {
					oos.close();
				} finally {
					fos.close();
				}
			}
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}    	
    }
    
	private void loadPresets(){
    	try {
			// ouverture d'un flux de sortie vers le fichier "personne.serial"
			FileInputStream fis = getActivity().openFileInput(PACKET_INJECTION_PRESETS_FILE_NAME);

			// cr�ation d'un "flux objet" avec le flux fichier
			ObjectInputStream ois= new ObjectInputStream(fis);
			try {
				// s�rialisation : �criture de l'objet dans le flux de sortie
				Presets = (List<ConnexionID>) ois.readObject(); 
			} finally {
				//fermeture des flux
				try {
					ois.close();
				} finally {
					fis.close();
				}
			}
    	} catch(IOException ioe) {
			ioe.printStackTrace();
			
	    	Presets = new ArrayList<ConnexionID>();
	        // Retrieving presets from XML file.
			String[] tab = getResources().getStringArray(R.array.presets);
	        for(int i=0 ; i<tab.length ; i++) {
	        	String[] p = tab[i].split("\\|");
	        	System.out.println ("*"+p[0]+"*"+p[1]+"*"+p[2]+"*"+p[3]+"*");
	        	Presets.add(new ConnexionID(p[0].trim(), p[1].trim(), Integer.parseInt(p[2].trim()), ((p[3].trim().equals("UDP"))?ConnexionID.protocol.UDP:ConnexionID.protocol.TCP) ));
	        }
			
	        savePresets();
			Toast.makeText(getActivity(), "No presets found. New presets generated.", Toast.LENGTH_LONG).show();
		} catch(ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
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
