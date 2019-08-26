package p2i6.packetinjection;

import java.util.LinkedList;

public class Convert {

  public static short bytebyte2short(byte b1, byte b2) {
    return (short)(0xFFFF & ((0xFF00 & (b1*256))+(0xFF & b2)));
  }

  public static String byte2Hexa(byte b) {
    String tmp = Integer.toString(b & 0xFF, 16);
    for(int i=tmp.length();i<2;i++){
      tmp="0"+tmp;
    }
//    return "0x" + tmp;
    return tmp;
  }

  public static String short2Hexa(short b) {
    String tmp = Integer.toString(b & 0xFFFF, 16);
    for(int i=tmp.length();i<4;i++){
      tmp="0"+tmp;
    }
    return "0x" + tmp;
  }
  public static short int2param(int b) {
    return (short)(b & 0xFFFF);
  }

  public static byte int2codeOp(int b) {
    return (byte)(b & 0xFF);
  }


  public static int int2degrees(int v){
    return short2degrees((short)(v-128));
  }

  public static int short2degrees(short v){
    /*0->0 
     * -128 -> -90
     * 127 -> 90
     */
    if(v<0){
      return (v*90)/128;
    } else {
      return (v*90)/127;
    }
  }

  public static int short2pression(short v){
    /*0->0 
     * -128 -> -90
     * 127 -> 90
     */
    return (int)(0xFFFF & v);
  }

  public static int short2UnsignedInt(int s) {
    return short2UnsignedInt((short)s);
  }
  public static int short2UnsignedInt(short s) {
    if (s>= 0) {
      return (int)s;
    } else {
      return (int)(65536+s);
    }
  }

    static byte short2HighByte(short s) {
      return (byte)(0xFF & ((0xFF00 & s)/256));
    }

    static byte short2LowByte(short s) {
      return (byte)(0xFF & s);
    }

    
    static byte[] hexaToBytes(String s){
      if(s==null || s.length()==0) return new byte[0];
      // Si un préfixe 0x
      if(s.length()>2 && s.substring(0,2).equals("0x")) {
        String[] tmsg = s.split(" "); // Si plusieurs "0x??" séparés par des espaces

        // Stockage dans LL temporaire.
        LinkedList<Byte> llRes = new LinkedList<>();
        for(String m : tmsg) {
          llRes.add((byte)(0xFF & Short.decode(m)));
        }

        // Stockage dans tableau de bytes
        byte[] res = new byte[llRes.size()];
        for(int i = 0 ; i<llRes.size() ; i++) {
          res[i]=llRes.get(i).byteValue();
        }
        return res;
      } else {
      // Si pas de préfixe 0x
        // Ajout d'un 0 initial pour avoir un nombre pair de digit
        if (s.length()%2==1) s="0"+s;

        char[] c=s.toCharArray();
        String m;
        byte[] res=new byte[c.length/2];
        for(int i=0 ; i<c.length/2 ; i++) {
            m="0x"+c[i*2]+c[i*2+1];
            res[i]=(byte)(0xFF & Short.decode(m));
        }
        return res;
      }
    }
    
    
}
