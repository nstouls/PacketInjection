package ot.semba.packetinjection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.util.Log;

public class UDPThread extends ListeningThread {

	private TraceManagement messagesTrace = null;
	private int delay = 0;
	private boolean stopProcess = true;

	// UDP variables
	private DatagramSocket UDPSocket = null;
	private DatagramPacket UDPPacket;
	private int UDPPort;
	private InetAddress UDPAddress;
	private boolean isUDPConnected = false;
	private String ip;
	private int port;
	private int localPort;
	
	public UDPThread(TraceManagement messagesTrace, int delay, String ip, int port, int localPort, FragMessage callBackForConnect, PacketInjectionActivity activity) {
		super(activity);
		ThreadParameter tp = new ThreadParameter(messagesTrace, delay, ip, port, localPort, callBackForConnect);
		this.execute(tp);
		try {
			Thread.sleep(delay);
		} catch (Exception ex) {
		}
	}

	synchronized public boolean isConnected(){
		// Default case : UDP
		return true && isUDPConnected; // UDPSocket.isConnected();
	}

	
	@Override
	protected Void doInBackground(ThreadParameter... params) {
		byte[] data = new byte[2048]; // Using a too large constant in order to
										// never trunck the incomming stream.
		int size = 0;

		
		this.messagesTrace = params[0].messagesTrace;
		this.delay = params[0].delay;
		this.ip = params[0].ip;
		this.port = params[0].port;
		this.localPort = params[0].localPort;
		this.callBackForConnect=params[0].callBackForConnect;
		
		UDPConnect();

		// UDP connection
		while (!stopProcess && isUDPConnected) {
			try {
				// input frames treatment
				UDPPacket = new DatagramPacket(data, data.length);
				UDPSocket.receive(UDPPacket);
				size = UDPPacket.getLength();
				
				publishProgress(new MessageToDisplay("[In  " + size + " bytes] ", data, size));

			} catch (NullPointerException e) {
//				messagesTrace.publishMessage(new MessageToDisplay(
				publishProgress(new MessageToDisplay(
						"Network error ("+e.getMessage()+")."));
			} catch (IOException e) {
//				messagesTrace.publishMessage(new MessageToDisplay(
				publishProgress(new MessageToDisplay(
						"Input error ("+e.getMessage()+")."));
			} catch (Exception e) {
//				messagesTrace.publishMessage(new MessageToDisplay(
				publishProgress(new MessageToDisplay(
						"Unexpected error ("+e.getMessage()+")."));
			}
		}
		Log.d("MOI","Out of the Thread");
//		messagesTrace.flush();

		isUDPConnected = false;
		int oldDelay = delay;
		delay = 1000;
		try {
			Thread.sleep(oldDelay + delay);
		} catch (Exception ex) {
		}

		UDPSocket = null;
		UDPPacket = null;
//		messagesTrace.publishMessage(new MessageToDisplay("[Socket closed]"));
//		messagesTrace.flush();
		publishProgress(new MessageToDisplay("[Socket closed]"));
		return null;
	}


	@Override
	protected void onProgressUpdate(MessageToDisplay... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
		for(MessageToDisplay m : values) {
			messagesTrace.publishMessage(m);
		}
		messagesTrace.flush();
	}

	
	
	
	synchronized public void closeConnection() {
		isUDPConnected=false;
		stopProcess = true;
		UDPSocket.close();
		Log.d("MOI","Stop process setted");
		publishConnectionStatus(false, -1, callBackForConnect);
	}

	private boolean UDPConnect() {
		try {
			if(localPort>0) {
				UDPSocket = new DatagramSocket(localPort);
			} else {
				UDPSocket = new DatagramSocket();
			}
			UDPPort = port;
			UDPAddress = InetAddress.getByName(ip);

			messagesTrace.publishMessage(new MessageToDisplay(
					"[Socket open - Starting Listening thread]"));
			send(new byte[0]);
			isUDPConnected = true;
			stopProcess=false;
			
			publishConnectionStatus(true, getLocalPort(), callBackForConnect);
			return true;
		} catch (IOException f) {
			isUDPConnected = false;
			messagesTrace.publishMessage(new MessageToDisplay(
					"[Client failed to start]"));

			publishConnectionStatus(false, -1, callBackForConnect);
			return false;
		}
	}

	public void send(byte[] data) throws IOException {
		messagesTrace.publishMessage(new MessageToDisplay("[Out " + data.length
				+ " bytes] ", data, data.length));
		final DatagramPacket packet = new DatagramPacket(data, data.length,
				UDPAddress, UDPPort);
		(new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					UDPThread.this.UDPSocket.send(packet);
				} catch (IOException e) {
					// TODO : Add something to said that an error occured
					Log.e("PacketInjection", "Sending error ");
				}
			}
		}).start();
	}

	public int getLocalPort() {
		if (UDPSocket != null)
			return UDPSocket.getLocalPort();
		return -1;
	}

}

