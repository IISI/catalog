package tw.com.citi.commcypt.util;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class BridgeEncode {

	public static String encode(String srcStr) throws UnsupportedEncodingException{
		return HexUtil.bytesToHex(Base64.encodeBase64(srcStr.getBytes("utf-8")));
	}
	
}
