package tw.com.citi.catalog.web.pages;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileSystemException;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import tw.com.citi.catalog.util.DateUtil;
import tw.com.citi.catalog.util.HashUtil;
import tw.com.citi.catalog.web.dao.IAppDao;
import tw.com.citi.catalog.web.dao.IAppPathDao;
import tw.com.citi.catalog.web.dao.IScrDao;
import tw.com.citi.catalog.web.grid.IGridHandler;
import tw.com.citi.catalog.web.model.App;
import tw.com.citi.catalog.web.model.AppPath;
import tw.com.citi.catalog.web.model.CheckoutFile;
import tw.com.citi.catalog.web.model.Scr;
import tw.com.citi.catalog.web.model.AppPath.PathType;
import tw.com.citi.catalog.web.model.Scr.Status;
import tw.com.citi.catalog.web.util.AccessControlUtil;
import tw.com.citi.catalog.web.util.IPvcsCmd;
import tw.com.citi.catalog.web.util.IZipCmd;
import tw.com.citi.catalog.web.util.SmbFileUtil;
import tw.com.citi.catalog.web.util.impl.PvcsCmd;
import tw.com.citi.catalog.web.util.impl.ZipCmd;

public class JCS1800 extends AbstractBasePage {


    @SpringBean(name = "scrDao")
    private IScrDao scrDao;
    
    @SpringBean(name = "appDao")
    private IAppDao appDao;
    
    @SpringBean(name = "appPathDao")
    private IAppPathDao appPathDao;
    
    @SpringBean(name = "checkoutGrid")
    private IGridHandler checkoutGrid;
    
    private transient Gson gson = new Gson();

    private static Gson testGson = new Gson();
    
    @Override
    public IGridHandler getGridHandler(String handlerName) {
        if ("checkoutGrid".equals(handlerName)) {
            return checkoutGrid;
        }
        return super.getGridHandler(handlerName);
    }
    
    @Override
    public String handleRequest(PageParameters params){
        String result = null;
        String actionName = params.getString("actionName");
        
        if ("findScrNo".equals(actionName)) {
            result=  findScrNo();
            
        }else if ("getScrInfo".equals(actionName)) {
            Long scrId = params.getAsLong("actionParams[scrId]");
            result = getScrInfo(scrId);
            //System.out.println(AccessControlUtil.authenticateCBCUser("TESTUSR1", null));
        }else if ("dualControl".equals(actionName)) {
        	 String checkerId = params.getString("actionParams[checkerId]");
             String checkerPwd = params.getString("actionParams[checkerPwd]");
             //logger.debug("checker id = {}, checker password = {}", checkerId, checkerPwd);
             boolean authenticated = AccessControlUtil.authenticateCBCUser(checkerId, checkerPwd);
             System.out.println("authenticated="+authenticated);
             if (!authenticated) {
            	 result="ID/Password is invalid";
             }else{
            	 result="success";
             }
             
        } else if ("allCheckoutList".equals(actionName)) {
        	String checkoutFile= params.getString("actionParams[checkoutFile]");
        	result = parseCheckoutListFile(checkoutFile);
        	
        } else if ("checkout".equals(actionName)) {
        	String scrNo=params.getString("actionParams[scrNo]");
        	String appId=params.getString("actionParams[appId]");
        	String checkoutLabel=params.getString("actionParams[checkoutLabel]");
        	String checkoutPath=params.getString("actionParams[checkoutPath]");
        	String checkoutId=params.getString("actionParams[checkoutId]");
        	String checkoutPass=params.getString("actionParams[checkoutPass]");
        	String zipfilePath=params.getString("actionParams[zipfilePath]");
        	String zipPassword=params.getString("actionParams[zipPassword]");
            String checkoutList = params.getString("actionParams[checkoutList]");
            List<CheckoutFile> CheckoutFileList=gson.fromJson(checkoutList, new TypeToken<List<CheckoutFile>>(){}.getType());
            //System.out.println("scrNo="+scrNo);
            //System.out.println("appId="+appId);
            //System.out.println("checkoutLabel="+checkoutLabel);
            //System.out.println("checkoutPath="+checkoutPath);
            //System.out.println("checkoutId="+checkoutId);
            //System.out.println("checkoutPass="+checkoutPass);
            //System.out.println("zipfilePath="+zipfilePath);
            //System.out.println("CheckoutFileList="+CheckoutFileList.size());
            
            
            AppPath appPath = appPathDao.findByScrId(Long.parseLong(scrNo), PathType.APP_BASE).get(0);
            String rdPath = appPath.getPath().endsWith("\\") || appPath.getPath().endsWith("/") ? appPath.getPath().concat("RD\\") : appPath.getPath().concat("\\RD\\");
            System.out.println("rdPath="+rdPath);
    
            //pvcs checkout
            App app=appDao.findByAppId(appId);
            String prjDb=app.getPvcsProjDb();
            String prjPath=app.getPvcsProjPath();
            //傳送給pcli的filename string[] 
            String[] chkfiles=new String[CheckoutFileList.size()];
            for(int i=0;i<CheckoutFileList.size();i++){
            	CheckoutFile file=CheckoutFileList.get(i);
            	System.out.println("ppppp:"+file.getFilePath());
            	String srcPath=file.getSrcPath().replaceAll("\\\\", "/");
            	System.out.println("aaaa:"+srcPath);
            	file.setSrcPath(srcPath);
            	chkfiles[i]=" RD/"+file.getSrcPath()+file.getSrcFileName();
            }
            IPvcsCmd pvcsCmd=new PvcsCmd();
            System.out.println(prjDb+"-"+prjPath);
            pvcsCmd.getFiles(prjDb, prjPath, checkoutId, checkoutPass, chkfiles);
            
            //zip 
            //把checkoutFileList 搬到  C:\temp\JavaCatalog
            String[] zipSrcArr=new String[CheckoutFileList.size()];
            try {
            	Runtime.getRuntime().exec(new String[] { "cmd", "/C", "rmdir /s /q C:\\temp\\JavaCatalog\\zip\\RD" });
            	Runtime.getRuntime().exec(new String[] { "cmd", "/C", "mkdir C:\\temp\\JavaCatalog\\zip\\RD" });
	            for(int i=0;i<CheckoutFileList.size();i++){
	            	CheckoutFile coFile=CheckoutFileList.get(i);
	            	String coFileRdPath=rdPath + coFile.getSrcPath();
	            	System.out.println(coFileRdPath+"----"+"c:\\temp\\Javacatalog\\zip\\RD\\" + coFile.getSrcPath()+"----"+coFile.getSrcFileName());
	            	SmbFileUtil.copySmbToLocal(coFileRdPath, "c:\\temp\\Javacatalog\\zip\\RD\\" + coFile.getSrcPath(), new String[] { coFile.getSrcFileName() });
	            	zipSrcArr[i]="c:\\temp\\Javacatalog\\zip\\RD\\" + coFile.getFilePath();
	            }
            } catch (FileSystemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
			//filePath=srcFilePath+srcFileName
			for(CheckoutFile coFile : CheckoutFileList){
            	coFile.setFilePath(rdPath+coFile.getFilePath());
            	//System.out.println("zip:"+coFile.getFilePath());
            }

           
           //gen hash
           List<HashFileModel> hashFileList=new ArrayList<HashFileModel>();
           BufferedWriter bufferedWriter = null;
           try { 
	           bufferedWriter = new BufferedWriter(new FileWriter(zipfilePath+"\\hashInfoFile.txt"));
           
            	
	            //String[] hashArr=new String[CheckoutFileList.size()];
	            for(int i=0;i<CheckoutFileList.size();i++){
	            	CheckoutFile chkFile=CheckoutFileList.get(i);
	            	HashFileModel hashFile=new HashFileModel();
	            	File beChkFile=new File(chkFile.getFilePath());
	            	System.out.println(chkFile.getSrcPath());
	            	String md5=HashUtil.getMD5Checksum(beChkFile);
	            	hashFile.setMd5(md5);
	            	//write to file
	            	bufferedWriter.write(chkFile.getBuildUnit()+"|"+chkFile.getSrcPath()+"|"+chkFile.getSrcFileName()+"|"+md5);
	                bufferedWriter.newLine();
	            }
            
            }catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception e) {
				e.printStackTrace();
			} finally {
                //Close the BufferedWriter
                try {
                    if (bufferedWriter != null) {
                        bufferedWriter.flush();
                        bufferedWriter.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            
            //gen zip
			System.out.println("zip password="+zipPassword);
            IZipCmd zipCmd=new ZipCmd();
            zipCmd.zip(zipfilePath+"\\source.zip", zipSrcArr, zipPassword);
            
            
            
            try {
            	Runtime.getRuntime().exec(new String[] { "cmd", "/C", "rmdir /s /q C:\\temp\\JavaCatalog\\zip\\RD" });
            	Runtime.getRuntime().exec(new String[] { "cmd", "/C", "mkdir C:\\temp\\JavaCatalog\\zip\\RD" });
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			//update scrStatus
			// 更新 Scr 的 status
			System.out.println(Long.parseLong(scrNo));
	        scrDao.updateStatus(Long.parseLong(scrNo), Status.CHECKOUT);
            
            
            
        } 
        return result;
    }
    
    private String findScrNo() {
        List<Scr> scrList = scrDao.findAll();
        StringBuilder sb = new StringBuilder();
        for (Scr scr : scrList) {
            sb.append("<option value='").append(scr.getId()).append("'>");
            sb.append(scr.getScrNo());
            sb.append("</option>");
        }
        return sb.toString();
    }
    
    private String getScrInfo(Long scrId) {
        Scr scr = scrDao.findById(scrId);
        App app = appDao.findById(scr.getJcAppId());
        AppPath appPath = appPathDao.findByScrId(scrId, PathType.APP_BASE).get(0);
        String rdPath = appPath.getPath().endsWith("\\") || appPath.getPath().endsWith("/") ? appPath.getPath().concat("RD\\") : appPath.getPath().concat("\\RD\\");
       
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("scrNo", scr.getScrNo());
        results.put("appId", app.getAppId());
        results.put("rdPath", rdPath);
        results.put("label", scr.getScrNo()+scr.getRegisterCount());
       
        return gson.toJson(results);
    }
    
    private String parseCheckoutListFile(String filePath){
    	String result=null;
    	List<Map<String,Object>> resultsList=new ArrayList<Map<String,Object>>();
    	try
        {    
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            if (!in.ready())
                throw new IOException();
            
            String line;
            while ((line = in.readLine()) != null) {
            	
            	String[] lineArr=line.split("\\|");
            	String colBuildUnit=lineArr[0];
            	String colBuildPath=lineArr[1]+lineArr[2];
            	Map<String, Object> results = new HashMap<String, Object>();;
            	results.put("filePath", colBuildPath);
            	results.put("buildUnit", colBuildUnit);
            	results.put("srcPath", lineArr[1]);
            	results.put("srcFileName", lineArr[2]);
            	resultsList.add(results);
            }
                
            in.close();
        }
        catch (IOException e)
        {
            System.out.println(e);
            return null;
        }
    	
    	//System.out.println(testGson.toJson(resultsList));
    	
    	return gson.toJson(resultsList);
    }
    
    private void copyDirectory(File sourceLocation , File targetLocation) throws IOException {
        
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            
            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
            
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);
            
            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }
    
    private class FileModel {

        private String fileName;

        private String sourcePath;

        private String sourceFileName;

        private String buildUnit;

        private long fileSize;

        private Date fileDatetime;

        private String action;

        private boolean inZip;

        private String expectedMd5;

        private String fileMd5;

        private String executionPath;

        private String executionFileName;

        private boolean checkin;

        private boolean error;

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getSourcePath() {
            return sourcePath;
        }

        public void setSourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
        }

        public String getSourceFileName() {
            return sourceFileName;
        }

        public void setSourceFileName(String sourceFileName) {
            this.sourceFileName = sourceFileName;
        }

        public String getBuildUnit() {
            return buildUnit;
        }

        public void setBuildUnit(String buildUnit) {
            this.buildUnit = buildUnit;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        public Date getFileDatetime() {
            return fileDatetime;
        }

        public void setFileDatetime(Date fileDatetime) {
            this.fileDatetime = fileDatetime;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public boolean isInZip() {
            return inZip;
        }

        public void setInZip(boolean inZip) {
            this.inZip = inZip;
        }

        public String getExpectedMd5() {
            return expectedMd5;
        }

        public void setExpectedMd5(String expectedMd5) {
            this.expectedMd5 = expectedMd5;
        }

        public String getFileMd5() {
            return fileMd5;
        }

        public void setFileMd5(String fileMd5) {
            this.fileMd5 = fileMd5;
        }

        public String getExecutionPath() {
            return executionPath;
        }

        public void setExecutionPath(String executionPath) {
            this.executionPath = executionPath;
        }

        public String getExecutionFileName() {
            return executionFileName;
        }

        public void setExecutionFileName(String executionFileName) {
            this.executionFileName = executionFileName;
        }

        public boolean isCheckin() {
            return checkin;
        }

        public void setCheckin(boolean checkin) {
            this.checkin = checkin;
        }

        public boolean isError() {
            return error;
        }

        public void setError(boolean error) {
            this.error = error;
        }

    }
    
    private class HashFileModel {

        private String buildUnit;

        private String sourcePath;

        private String sourceFileName;

        private String md5;

        public String getBuildUnit() {
            return buildUnit;
        }

        public void setBuildUnit(String buildUnit) {
            this.buildUnit = buildUnit;
        }

        public String getSourcePath() {
            return sourcePath;
        }

        public void setSourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
        }

        public String getSourceFileName() {
            return sourceFileName;
        }

        public void setSourceFileName(String sourceFileName) {
            this.sourceFileName = sourceFileName;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

    }


}
