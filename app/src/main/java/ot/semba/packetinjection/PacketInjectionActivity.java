package ot.semba.packetinjection;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


/**
 * @author Androsoft, What Else ?
 *
 * <p><b>New updates in version 2.4 :</b></p>
 * <ul>
 *   <li>Adding an option to precise a local port</li>
 *   <li>Correction of some TCP bugs</li>
 * </ul>
 * <p><b>New updates in version 2.3 :</b></p>
 * <ul>
 *   <li>Correction of some TCP bugs</li>
 *   <li>Correction of disconnect/reconnect bug</li>
 * </ul>
 * <p><b>New updates in version 2.2 :</b></p>
 * <ul>
 *   <li>Adding ASCII+\r\n format</li>
 * </ul>
 * <p><b>New updates in version 2.1 :</b></p>
 * <ul>
 *   <li>TCP bug correction</li>
 *   <li>Cleaning code</li>
 * </ul>
 * <p><b>New updates in version 2.0 :</b></p>
 * <ul>
 *   <li>Moving to Android 3.0 minimum API</li>
 *   <li>Adding sleep management</li>
 *   <li>Display of the local port</li>
 *   <li>Moving settings into menu</li>
 * </ul>
 * <p><b>New updates in version 1.3 :</b></p>
 * <ul>
 *   <li>Default IP address changed</li>
 *   <li>IPV6 display bug</li>
 * </ul>
 * <p><b>New updates in version 1.2.5 :</b></p>
 * <ul>
 *   <li>Default display set to decimal + 8bits.</li>
 *   <li>Default IP address changed</li>
 *   <li>Crashing bug fixed</li>
 *   <li>Freezing bug fixed</li>
 * </ul>
 * <p><b>New updates in version 1.2.4 :</b></p>
 * <ul>
 *   <li>Default display set to hexadecimal + 8bits.</li>
 * </ul>
 * <p><b>New updates in version 1.2.3 :</b></p>
 * <ul>
 *   <li>Correction of Decimal value display.</li>
 *   <li>Correction of message size when sending a message in 8+16 bits mode.</li>
 * </ul>
 * <p><b>New updates in version 1.2.2 :</b></p>
 * <ul>
 *   <li>Correction of Hexa value conversion in some particular cases.</li>
 *   <li>Correction of message size when sending a message in 8+16 bits mode.</li>
 * </ul>
 * <p><b>New updates in version 1.2.1 :</b></p>
 * <ul>
 *   <li>More understandable choice of the display (base and packet)</li>
 *   <li>Adding a 24bit display, enhanced for 8+16 bits (bits code operation and 16bits parameters)</li>
 *   <li>Correction of some synchronization bugs appearing sometimes while stopping a connection.</li>
 * </ul>
 */
/*@TODO
 *  + bug TCP/IP qui crash l'appli. La cr��ation du socket doit ��tre dans un processus.
 *  + Problems to disconnect and re-connect (even in UDP)
 *  + To optimize the send method by maintaining a sending thread in memory (with wiat/notify/synchronized primitives)
 *  + Permettre de param���trer le nombre de lignes max en m���moire
 */


public class PacketInjectionActivity extends FragmentActivity implements ActionBar.TabListener {

	

/**
 * The {@link android.support.v4.view.PagerAdapter} that will provide
 * fragments for each of the sections. We use a
 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
 * will keep every loaded fragment in memory. If this becomes too memory
 * intensive, it may be best to switch to a
 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
 */
SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	private ViewPager mViewPager;
	private TextView localPort;
	private FragMessage messages=null;
	private FragConnexion connections=null;
	boolean isConnected = false;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_pager);


		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Remove icon and application name from the bar in order to reduce the used space.
		// It could be done through an XML way : http://stackoverflow.com/questions/5720715/remove-application-icon-and-title-from-honeycomb-action-bar
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false); 

		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});


		// For each of the sections in the app, add a tab to the action bar.
		actionBar.addTab(actionBar.newTab()
				.setText(mSectionsPagerAdapter.getPageTitle(0))
				.setIcon(getResources().getDrawable(R.drawable.highlevelmanagement))
				.setTabListener(this));

		actionBar.addTab(actionBar.newTab()
				.setText(mSectionsPagerAdapter.getPageTitle(1))
				.setIcon(getResources().getDrawable(R.drawable.rawnetwork))
				.setTabListener(this));

		

		
		
		
		
		
        // Version manager
        String version="  Ver.: ";
		try {
			version+=this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Toast.makeText(this,"Error while accessing Application version number", Toast.LENGTH_SHORT).show();
		}
        ((TextView)findViewById(R.id.version)).setText(version);

        
        ((TextView)findViewById(R.id.localIPs)).setText("Local IP addresses: "+ getLocalIpAddress());
		
        localPort = ((TextView)findViewById(R.id.localPort));
        publishLocalPort("-");
		
        // As long as the window will be visible, the sreen will be ON and Bright
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_new_setting:
                connections.askForPresetName();
                return true;
                
            case R.id.action_display_settings:
                messages.displaySettings();
                return true;
                
            default:
                return super.onOptionsItemSelected(item);
        }
    }    
    
    
	private void publishLocalPort(String p) {
        localPort.setText("Local Port: "+p);
	}




	public String getLocalIpAddress() {
		String res="";
		String sep="{";
		try {
    		Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
    		while(en.hasMoreElements()) {
    			NetworkInterface intf = en.nextElement();
    			Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); 
    			while(enumIpAddr.hasMoreElements()) {
    				InetAddress inetAddress = enumIpAddr.nextElement();
    				if (!inetAddress.isLoopbackAddress()) {
    					String s = inetAddress.getHostAddress().toString();
    					// On n'affiche que les adresses IPV4	
    					if(s.indexOf(":")==-1) {
    						res=res+sep+s;
    						sep=", ";
    					}
    				}
    			}
    		}
    	} catch (SocketException ex) {
    	}
    	return res+"}";
	}



    

	
    
	/*@Override
	protected void onResume() {
		super.onResume();
        ((TextView)findViewById(R.id.localIPs)).setText("Local IP addresses: "+ getLocalIpAddress());
	}*/


	@Override
	protected void onPause() {
		super.onPause();
		//if(traces!=null) traces.clean();
		disconnect();
	}


    
    
    
    
    public void setConnectBtnText(String s){
    	connections.setConnectBtnText(s);
    }
    
    public boolean isTCPBtnChecked () {
    	return mSectionsPagerAdapter.getConnexion().isTCPBtnChecked();
    	//return false; 
    }
    
	public void disconnect() {
		mSectionsPagerAdapter.getMessage().disconnect();
	}
	public void startFromCustom(String adr) {
		mSectionsPagerAdapter.getMessage().startFromCustom(adr);
	}
	
	public void publishConnectionStatus(int p) {
		if (p==-1) {
			publishLocalPort("-");
	    	setConnectBtnText("Connect");
	    	isConnected=false;
		} else {
			publishLocalPort(""+p);
	    	setConnectBtnText("Disconnect");
	    	isConnected=true;
		}
	}

    
    
    
    
    
    
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public FragConnexion getConnexion() {
			if(connections == null) connections=new FragConnexion();
			return connections;
		}

		public FragMessage getMessage() {
			if(messages == null) messages=new FragMessage();
			return messages;
		}
		
		
		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			switch (position) {
			case 0: return getConnexion();
			case 1: return getMessage();
			}

			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0: return "Connection";
			case 1: return "Send & receive";
			}
			return null;
		}
	}


	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}


	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}


	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}



}