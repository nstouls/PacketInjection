package p2i6.packetinjection;

public class ThreadParameter {
	TraceManagement messagesTrace;
	int delay;
	String ip;
	int port;
	FragMessage callBackForConnect;
	int localPort;

	public ThreadParameter(TraceManagement mt, int d, String i, int port, int localPort,FragMessage callBackForConnect) {
		messagesTrace=mt;
		delay=d;
		ip=i;
		this.port=port;
		this.callBackForConnect=callBackForConnect;
		this.localPort=localPort;
	}

}
