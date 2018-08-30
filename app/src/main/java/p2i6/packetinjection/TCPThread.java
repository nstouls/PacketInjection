package p2i6.packetinjection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;


public class TCPThread extends ListeningThread {
	private TraceManagement messagesTrace = null;
	private int delay = 0;
	private boolean stopProcess = true;

	private String ip;
	private int port;
	private DataInputStream TCPInStream = null;
	private DataOutputStream TCPOutStream = null;
	private Socket TCPSocket = null;
	final static int TIME_OUT = 1000; // 1 millisecond 
	private List<byte[]> sendingQueue = new LinkedList<byte[]>();
    private boolean isTCPConnected=false;
	private int localPort;


	public TCPThread(TraceManagement messagesTrace, int delay, String ip, int port, int localPort, FragMessage callBackForConnect, PacketInjectionActivity activity) {
		super(activity);
		ThreadParameter tp = new ThreadParameter(messagesTrace, delay, ip, port, localPort, callBackForConnect);
		this.execute(tp);
		try {
			Thread.sleep(delay);
		} catch (Exception ex) {
		}
	}
		
		
 
	synchronized public boolean isConnected(){
		return isTCPConnected &&
			   TCPSocket.isConnected() && 
	           !TCPSocket.isClosed() &&
	           !TCPSocket.isInputShutdown() &&
	           !TCPSocket.isOutputShutdown();
	}

	
	
	
	public void send(byte[] data) throws IOException {
		synchronized (sendingQueue) {
			sendingQueue.add(data);
		}
	}


	public int getLocalPort() {
		if(TCPSocket != null) return TCPSocket.getLocalPort();
		return -1;
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
		
		TCPConnect();
		
		while (!stopProcess && isConnected()) {
			try {
				// input frames treatment
				size = 0;
				if (0 < TCPInStream.available()) {
					size = TCPInStream.read(data);
//					messagesTrace.publishMessage(new MessageToDisplay("[In  "+size+" bytes] ",data, size));
					publishProgress(new MessageToDisplay("[In  "+size+" bytes] ",data, size));
				}

				
				synchronized (sendingQueue) {
					while(sendingQueue.size()>0){
						byte[] sentData = sendingQueue.remove(0);
//						messagesTrace.publishMessage(new MessageToDisplay("[Out "+sentData.length+" bytes] ",sentData, sentData.length));
						publishProgress(new MessageToDisplay("[Out "+sentData.length+" bytes] ",sentData, sentData.length));
						TCPOutStream.write(sentData);
						TCPOutStream.flush();
					}
				}

				
			} catch (NullPointerException e) {
//				messagesTrace.publishMessage(new MessageToDisplay("Network error."));
				publishProgress(new MessageToDisplay("Network error."));
			} catch (IOException e) {
//				messagesTrace.publishMessage(new MessageToDisplay("Input error."));
				publishProgress(new MessageToDisplay("Input error."));
			} catch (Exception e) {
//				messagesTrace.publishMessage(new MessageToDisplay("Unexpected error."));
				publishProgress(new MessageToDisplay("Unexpected error."));
			}

			try {
				Thread.sleep(delay);
			} catch (Exception ex) {
			}
		}
			
//		messagesTrace.flush();
		TCPClose();
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
		stopProcess=true;
		isTCPConnected=false;
	}
	
	

	private void TCPClose() {
		isTCPConnected = false;
		int oldDelay=delay;
		stopProcess = true;
		delay = 1000;
		try { Thread.sleep(oldDelay+delay); } catch (Exception ex) { }

		try {if(TCPOutStream!=null) TCPOutStream.close();} catch (IOException e) {}
		try {if(TCPInStream!=null) TCPInStream.close();} catch (IOException e) {}
		try {if(TCPSocket!=null) TCPSocket.close();} catch (IOException e) {}
		TCPInStream = null;
	    TCPOutStream = null;
	    TCPSocket = null;

		publishConnectionStatus(false, -1, callBackForConnect);
		messagesTrace.publishMessage(new MessageToDisplay("[Socket closed]"));
	}
		  
	
	private boolean TCPConnect() {
		try {
			InetSocketAddress host = new InetSocketAddress(ip, port);
			TCPSocket = new Socket();
			if(localPort>0) {
				TCPSocket.bind(new InetSocketAddress(localPort));
			}
			TCPSocket.connect(host, TIME_OUT);
			
			TCPInStream = new DataInputStream(TCPSocket.getInputStream());
			TCPOutStream = new DataOutputStream(TCPSocket.getOutputStream());

			messagesTrace.publishMessage(new MessageToDisplay("[Socket open - Starting Listening thread]"));

			stopProcess=false;
			isTCPConnected = true;

			publishConnectionStatus(true, getLocalPort(), callBackForConnect);
			return true;
		} catch (IOException f) {
			isTCPConnected = false;
			TCPSocket = null;
			TCPOutStream = null;
			TCPInStream = null;
			messagesTrace.publishMessage(new MessageToDisplay("[Client failed to start]"));

			publishConnectionStatus(false,-1, callBackForConnect);
			return false;
		}
	}	
	
}
