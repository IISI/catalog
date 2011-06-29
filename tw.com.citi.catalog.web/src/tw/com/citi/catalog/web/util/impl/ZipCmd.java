package tw.com.citi.catalog.web.util.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tw.com.citi.catalog.web.util.IZipCmd;

public class ZipCmd implements IZipCmd{

	private static Logger logger = LoggerFactory.getLogger(ZipCmd.class);
	
	/**
     * 解壓縮檔案至指定的位置。
     * 
     * pkzipc -extract=all -overwirte=all -times=all -directories -silent -passphrase=${password} ${sourceFile} ${targetPath}
     * 
     * @param sourceFile
     * @param targetPath
     * @param password
     * @return
     */
	@Override
	public int unzip(String sourceFile, String targetPath, String password) {
		// TODO Auto-generated method stub
		int rc=0;
		String result="";
		Process process=null;
		try {
			String command="pkzipc -extract=all -overwrite=all -times=all -directories -silent -passphrase="+password+" "+sourceFile+" "+targetPath;
			logger.debug("command:"+command);
			process = Runtime.getRuntime().exec(command);
			BufferedReader bf  = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String r="";
			while((r=bf.readLine()) !=  null){
				result+=r+"\n";
			}
			bf.close();
			logger.debug(result);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			rc=process.exitValue();
		}
		
		return rc;
	}

	/**
     * 把 source files 壓縮成 zip file。
     * 
     * pkzipc -add -passphrase=${password} -cryptalgorithm=aes,256 -cd=encrypt ${zipFile} ${sourceFiles}
     * 
     * @param zipFile
     * @param sourceFile
     * @param password
     * @return
     */
	@Override
	public int zip(String zipFile, String[] sourceFile, String password) {
		// TODO Auto-generated method stub
		int rc=0;
		Process process=null;
		
		String fileStr="";
		for(String file : sourceFile){
			fileStr+=file+" ";
		}
		
		String result="";
		try {
			String command="pkzipc -add -passphrase="+password+" -cryptalgorithm=aes,256 -cd=encrypt "+zipFile+" "+fileStr;
			logger.debug("command:"+command);
			process = Runtime.getRuntime().exec(command);
			
			BufferedReader bf  = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String r="";
			while((r=bf.readLine())   !=   null){
				result+=r+"\n";
			}
			bf.close();
			logger.debug(result);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			rc = process.exitValue();
		}
		
		return rc;
	}

}
