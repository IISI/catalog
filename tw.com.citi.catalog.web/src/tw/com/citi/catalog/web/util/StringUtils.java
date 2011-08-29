package tw.com.citi.catalog.web.util;

import java.io.File;
import java.util.PropertyResourceBundle;

public class StringUtils {
	
	public static String trimToEmpty(String str) {
		if (str != null)
			return str.trim();
		else
			return "";
	}

	public static void makeDir(PropertyResourceBundle log4j) {
		String dir = log4j.getString("logdir");
		if(dir != null){
			try{
				File f = new File(dir);
				if(!f.exists()){
					f.mkdirs();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static String paddingZero(int size, String val) {
		if	(val == null)
			val = "0";
		else
			val = val.trim();
		
		if (size <= val.length())
			return val;
		
		for (int i = val.length(); i < size; i++) {
			val = "0" + val;
		}
		return val;
	}
	
	public static String paddingZeroRight(int size, String val) {
		if	(val == null)
			val = "0";
		else
			val = val.trim();
		
		if (size <= val.length())
			return val;
		
		for (int i = val.length(); i < size; i++) {
			val = val + "0";
		}
		return val;
	}
	
	public static boolean isDigit(String value) {
		if	(value == null)
			return false;
		else
			value = value.trim();
		
		for (int i = 0; i < value.length(); i++) {
			if (!Character.isDigit(value.charAt(i))) {
				return false;
			} 
		}
		return true;
	}
	
	//二字串靠左右對齊
	public static String fix2StringPading(String srcStr, int totLength){
		String destStr=srcStr;
		
		String[] srcStrArr=srcStr.split(" ");
		if(srcStrArr.length==2){
			destStr=srcStrArr[0];
			for(int i=0;i<totLength-srcStrArr[0].length()-srcStrArr[1].length();i++){
				destStr+=" ";
			}
			destStr+=srcStrArr[1];
		}
		
		return destStr;
	}
	
}
