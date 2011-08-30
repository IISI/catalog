package tw.com.citi.commcypt.util;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

public class BridgeEncode {

	public static String encode(String srcStr) throws UnsupportedEncodingException{
		System.out.println("encode Str=" + srcStr);
		if(srcStr!=null){
			return HexUtil.bytesToHex(Base64.encodeBase64(srcStr.getBytes("utf-8")));
		}else{
			return "";
		}
		
	}
	
}
