package tw.com.citi.commcypt.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;

public class JarUtil {

	//extract jar file ,with specific path
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
	
	//extract jar file
	public static void xfJarWithNoPath(String jarPath,String res,String destDir){
		File jarFile=new File(jarPath);
		java.util.jar.JarFile jar;
		try {
			jar = new java.util.jar.JarFile(jarFile);
			Enumeration<JarEntry> enu = jar.entries();
			while (enu.hasMoreElements()) {
			    java.util.jar.JarEntry jarEntry = (java.util.jar.JarEntry) enu.nextElement();
			    
			    File f = new File(jarEntry.getName());
			    File savedFile=new File(destDir+f.getName());
			    
			    if (jarEntry.getName().equalsIgnoreCase(res)){
			    	java.io.InputStream is = jar.getInputStream(jarEntry); // get the input stream
				    java.io.FileOutputStream fos = new java.io.FileOutputStream(savedFile);
				    while (is.available() > 0) {  // write contents of 'is' to 'fos'
				        fos.write(is.read());
				    }
				    fos.close();
				    is.close();
			    }
			    savedFile=null;
			    
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
