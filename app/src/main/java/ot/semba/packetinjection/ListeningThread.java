package ot.semba.packetinjection;

import java.io.IOException;
import android.os.AsyncTask;

public abstract class ListeningThread extends AsyncTask<ThreadParameter, MessageToDisplay, Void> {
	protected FragMessage callBackForConnect;
	protected PacketInjectionActivity mTheActivity;
	
	public ListeningThread(PacketInjectionActivity activity) {
		mTheActivity = activity;
	}

	abstract public boolean isConnected();
	abstract public void closeConnection() ;
	abstract public void send(byte[] data) throws IOException;
	abstract public int getLocalPort();
	
	protected void publishConnectionStatus(final boolean isConnected, final int port, final FragMessage callBackForConnect) {
		mTheActivity.runOnUiThread( 
			(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					callBackForConnect.callBackForConnect(isConnected, port);
				}
			})
		);
	}
}
