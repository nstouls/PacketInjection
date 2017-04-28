package ot.semba.packetinjection;

import java.io.Serializable;

public class ConnexionID implements Serializable {
	private static final long serialVersionUID = -331016205544424420L;
	
	public String name;
    public String ip;
    public int port;
    public int localPort;
    public protocol proto;
    static public enum protocol {TCP, UDP};
    


    public ConnexionID(String n, String i, int p, int localPort, protocol proto){
        name=n;
        ip=i;
        port=p;
        this.proto=proto;
        this.localPort=localPort;
    }


    public ConnexionID(String n, String i, int p, int localPort, boolean isTcp){
        this(n,i,p,localPort,(isTcp)?protocol.TCP:protocol.UDP);
    }


    public ConnexionID(String n, String i, int p, protocol proto){
        this(n,i,p,-1,proto);
    }


    public ConnexionID(String n, String i, int p, boolean isTcp){
        this(n,i,p,(isTcp)?protocol.TCP:protocol.UDP);
    }



    @Override
    public boolean equals(Object o) {
        if(o==null) return false;
    	if(!(o instanceof ConnexionID)) return false;
    	ConnexionID c = (ConnexionID)o;
    	return (c.ip.equals(ip) && c.port==port && c.proto==proto && c.localPort==localPort) || (name.equals(c.name));
    }
    
    @Override
    public String toString() {
        String lp = "";
        if(localPort>=1024) {
            lp=localPort+":";
        }

    	if(proto==protocol.UDP)
            return name + " ("+lp+ip+":"+port+"-UDP)";
    	else 
            return name + " ("+lp+ip+":"+port+"-TCP)";
    }

    
}
