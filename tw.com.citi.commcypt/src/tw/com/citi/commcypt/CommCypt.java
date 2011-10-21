package tw.com.citi.commcypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import tw.com.citi.commcypt.util.BridgeEncode;
import tw.com.citi.commcypt.util.JarUtil;


public class CommCypt {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {
			CommCypt cypt=new CommCypt();
			System.out.println(cypt.run(BridgeEncode.encode("Newuser1")));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String run(String strSrc){
		
		String strDes="";
		String jarFileSrc="c:\\JCS\\plugins\\tw.com.citi.commcypt_1.0.0.jar";
		String exeResJarPath="tw/com/citi/commcypt/resource/commutil";
		String exePreName="commutil";
		String runtimePath="C:\\JCS\\configuration\\";
		
		JarUtil.xfJarWithNoPath(jarFileSrc, exeResJarPath, runtimePath );

		File tmpFile=new File(runtimePath+exePreName);
		if(tmpFile.exists()){
			tmpFile.renameTo(new File(runtimePath + exePreName + ".exe"));
		}
		
		//run cypt
		
		try {
			Process p;
			String command = "C:\\Catalog\\configuration\\commutil.exe" +" "+ strSrc;
			String[] cmd = new String[] { "cmd", "/C", command };
			p=Runtime.getRuntime().exec(cmd);
			BufferedReader bf  = new BufferedReader(new   InputStreamReader(p.getInputStream()));
			String cgsEncodeResult="";
			String s="";
			while((s=bf.readLine())   !=   null){
				cgsEncodeResult+=s;
			}
			bf.close();
			strDes=cgsEncodeResult.replaceAll(" ", "");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//remove runtime cypt file
		File cyptFile=new File(runtimePath + exePreName +".exe");
		if(cyptFile.exists()){
			cyptFile.setWritable(true);
			cyptFile.delete();
		}

		
		return strDes;
	}
	
	public static void xfJar(String jarPath,String res,String destDir){
		File jarFile=new File(jarPath);
		java.util.jar.JarFile jar;
		try {
			jar = new java.util.jar.JarFile(jarFile);
			Enumeration<JarEntry> enu = jar.entries();
			while (enu.hasMoreElements()) {
			    java.util.jar.JarEntry file = (java.util.jar.JarEntry) enu.nextElement();
			    
			    java.io.File f = new java.io.File(destDir + java.io.File.separator + file.getName());
			    //System.out.println(res+"-"+file.getName()+"-");
			    if (file.isDirectory() && res.indexOf(file.getName())!=-1 ) { // if its a directory, create it  //&& jarPath.indexOf(file.get)
			    	//System.out.println(file.getName());
			    	f.mkdir();
			        continue;
			    }
			    
			    if (file.getName().equalsIgnoreCase(res)){
			    	java.io.InputStream is = jar.getInputStream(file); // get the input stream
				    java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
				    while (is.available() > 0) {  // write contents of 'is' to 'fos'
				        fos.write(is.read());
				    }
				    fos.close();
				    is.close();
			    }
			    
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
