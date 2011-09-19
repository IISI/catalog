package tw.com.citi.catalog.web.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jcifs.smb.SmbAuthException;

import org.apache.commons.vfs.FileSystemException;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import tw.com.citi.catalog.dao.IAppDao;
import tw.com.citi.catalog.dao.IAppFileDao;
import tw.com.citi.catalog.dao.IAppPathDao;
import tw.com.citi.catalog.dao.IBuildUnitDao;
import tw.com.citi.catalog.dao.IFileMoveDetailDao;
import tw.com.citi.catalog.dao.IRegisterHistoryDao;
import tw.com.citi.catalog.dao.IScrDao;
import tw.com.citi.catalog.dao.IScrFileDao;
import tw.com.citi.catalog.model.App;
import tw.com.citi.catalog.model.AppFile;
import tw.com.citi.catalog.model.AppPath;
import tw.com.citi.catalog.model.AppPath.PathType;
import tw.com.citi.catalog.model.BuildUnit;
import tw.com.citi.catalog.model.FileStatus;
import tw.com.citi.catalog.model.FileType;
import tw.com.citi.catalog.model.ProcessResult;
import tw.com.citi.catalog.model.RegisterHistory;
import tw.com.citi.catalog.model.Scr;
import tw.com.citi.catalog.model.Scr.Status;
import tw.com.citi.catalog.model.ScrFile;
import tw.com.citi.catalog.web.util.F;
import tw.com.citi.catalog.web.util.F.Func;
import tw.com.citi.catalog.web.util.IPvcsCmd;
import tw.com.citi.catalog.web.util.SmbFileUtil;
import tw.com.citi.catalog.web.util.impl.PvcsCmd;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JCS1600 extends AbstractBasePage {

    @SpringBean(name = "appDao")
    private IAppDao appDao;

    @SpringBean(name = "buildUnitDao")
    private IBuildUnitDao buildUnitDao;

    @SpringBean(name = "appPathDao")
    private IAppPathDao appPathDao;

    @SpringBean(name = "scrDao")
    private IScrDao scrDao;

    @SpringBean(name = "scrFileDao")
    private IScrFileDao scrFileDao;

    @SpringBean(name = "appFileDao")
    private IAppFileDao appFileDao;

    @SpringBean(name = "fileMoveDetailDao")
    private IFileMoveDetailDao fileMoveDetailDao;
    
    @SpringBean(name = "registerHistoryDao")
    private IRegisterHistoryDao registerHistoryDao;

    private transient Gson gson = new Gson();

    @Override
    public String handleRequest(PageParameters params) {
        String result;
        String actionName = params.getString("actionName");
        String actionParams = params.getString("actionParams");
        Map dataMap = gson.fromJson(actionParams, new TypeToken<Map<String, String>>() {
        }.getType());
        if ("Init".equals(actionName)) {
            result = init(dataMap);
        } else if ("Move".equals(actionName)) {
            result = move(dataMap);
        } else if ("Query".equals(actionName)) {
            result = query(dataMap);
        } else if ("GetFiles".equals(actionName)) {
            result = getFiles(dataMap);
        } else {
            throw new IllegalArgumentException("Cannot find actionName: " + actionName);
        }
        return result;
    }

    private String init(Map dataMap) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("deleted", "0");
        List<Scr> scrList = scrDao.find(params, new String[] { "equal" }, "", "SCR_NO", 0, Long.MAX_VALUE);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("scrList", scrList);
        return gson.toJson(data);
    }

    private String query(Map dataMap) {
        String sId = (String) dataMap.get("id");
        Scr scr = scrDao.findById(Long.parseLong(sId));
        App app = appDao.findById(scr.getJcAppId());
        Map<PathType, Object> appPaths = appPathDao.getAppPathsByAppId(scr.getJcAppId());
        List<BuildUnit> units = buildUnitDao.findByAppId(scr.getJcAppId());
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("appId", app.getAppId());
        data.put("qaSourcePath", appPaths.get(PathType.QA_SOURCE));
        data.put("qaExecutionPath", appPaths.get(PathType.QA_EXECUTION));
        data.put("prodSourcePaths", appPaths.get(PathType.PROD_SOURCE));
        data.put("prodExecutionPaths", appPaths.get(PathType.PROD_EXECUTION));
        data.put("status", scr.getStatus());
        data.put("buildUnits", units);
        return gson.toJson(data);
    }

    private String move(Map dataMap) {
        Date start = new Date();
        String sScrId = (String) dataMap.get("scrId");
        String files = (String) dataMap.get("files");
        List<Map<String, String>> fileList = gson.fromJson(files, new TypeToken<List<Map<String, String>>>() {
        }.getType());
        
        //-----------------------------------calculate pvcs action /add/put/del
        boolean isAddFiles=false;
    	boolean isPutFiles=false;
    	boolean isDelFiles=false;
    	List<String> delFileNameList=new ArrayList<String>();
        for (Map<String, String> file : fileList) {
        	String scrFileId=file.get("id");
        	String sqlCode="select * from JC_REGISTER_HISTORY where jc_scr_id= :scrId and jc_scr_file_id= :scrFileId and file_type=0";
        	Map<String,String> params=new HashMap<String,String>();
        	params.put("scrId", sScrId);
        	params.put("scrFileId", scrFileId);
        	//String filePath=file.get("filePath");
        	//String fileName=file.get("fileName");
        	//String fileType=file.get("fileType");
        	///String fileStatus=file.get("fileStatus");
        	//logger.debug("id="+fileId+", path="+filePath+", name="+fileName+", fileType="+fileType+",fileStatus="+fileStatus);
        	List<RegisterHistory> registerHistoryList=registerHistoryDao.query(sqlCode, RegisterHistory.class, params);
        	
        	 
        	
        	if(registerHistoryList.size()>0){
        		RegisterHistory registerHistory=registerHistoryList.get(registerHistoryList.size()-1);
        		int registerAction=registerHistory.getRegisterAction();
        		if(registerAction==0){
        			//新增
        			isAddFiles=true;
        		}else if(registerAction==1){
        			//修改
        			isPutFiles=true;
        		}else if(registerAction==2){
        			//刪除
        			isDelFiles=true;
        			String delFilePath=file.get("filePath");
        			String delFileName=file.get("fileName");
        			logger.debug("DEL: "+delFilePath+delFileName);
        			delFileNameList.add(delFilePath+delFileName);
        		}
        		logger.debug("id="+scrFileId+", action="+registerAction);
        	}
        	
        }
        
        logger.debug("isAdd="+isAddFiles);
        logger.debug("isPutFiles="+isPutFiles);
        logger.debug("isDelFiles="+isDelFiles+", size="+delFileNameList.size());
        //---------------------------------------------------------------------------------------------------
        //AppPath appPath = appPathDao.findByScrId(Long.parseLong(sScrId), PathType.APP_BASE).get(0);
        //String rdPath = appPath.getPath().endsWith("\\") || appPath.getPath().endsWith("/") ? appPath.getPath().concat("RD\\") : appPath.getPath().concat("\\RD\\");
        //logger.debug("rdPath="+rdPath);
        
        
        
        
        
        // move file
        Long scrId = Long.parseLong(sScrId);
        Long fLogId = F.log(scrId, Func.JCS1600, "", start, null);
        Scr scr = scrDao.findById(scrId);
        Map<PathType, Object> appPaths = appPathDao.getAppPathsByAppId(scr.getJcAppId());
        // get path
        String qaSourcePath = (String) appPaths.get(PathType.QA_SOURCE);
        String qaExecutionPath = (String) appPaths.get(PathType.QA_EXECUTION);
        String prodBackupPath = (String) appPaths.get(PathType.PROD_BACKUP);
        List<String> prodSourcePath = (List<String>) appPaths.get(PathType.PROD_SOURCE);
        List<String> prodExecutionPath = (List<String>) appPaths.get(PathType.PROD_EXECUTION);
        // rename current BACKUP folder
        try {
            if (SmbFileUtil.exist(prodBackupPath, null)) {
                SmbFileUtil.renameTo(
                        prodBackupPath,
                        null,
                        prodBackupPath.trim().substring(0, prodBackupPath.trim().length() - 1) + "_"
                                + String.valueOf(System.currentTimeMillis()), null);
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
            F.updateEndTime(fLogId, new Date());
            throw new RuntimeException("Rename backup folder error.", e);
        }
        // 備份現有的 PROD source/execution 到 BACKUP
        try {
            if (SmbFileUtil.exist(prodSourcePath.get(0), null)) {
                SmbFileUtil.copyFolder(prodSourcePath.get(0), prodBackupPath + "source\\");
            }
            if (SmbFileUtil.exist(prodExecutionPath.get(0), null)) {
                SmbFileUtil.copyFolder(prodExecutionPath.get(0), prodBackupPath + "execution\\");
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
            F.updateEndTime(fLogId, new Date());
            throw new RuntimeException("Backup current PROD folder error.", e);
        }
        // 複製 QA source/execution 到 PROD
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("JC_FUNCTION_LOG_ID", fLogId);
        for (Map<String, String> file : fileList) {
            String filePath = file.get("filePath");
            String fileName = file.get("fileName");
            logger.debug("" + filePath + "-" + fileName);
            String fileType = file.get("fileType");
            String fileStatus = file.get("fileStatus");
            AppFile appFile = appFileDao.findByUK(scr.getJcAppId(), filePath, fileName);
            params.put("JC_APP_FILE_ID", appFile.getId());
            try {
                String qaPath = "";
                List<String> prodPaths = new ArrayList<String>();
                if ("SOURCE".equalsIgnoreCase(fileType)) {
                    qaPath = qaSourcePath;
                    prodPaths = prodSourcePath;
                } else {
                    qaPath = qaExecutionPath;
                    prodPaths = prodExecutionPath;
                }
                for (String prodPath : prodPaths) {
                    params.put("TARGET_PATH", prodPath);
                    if ("DELETE".equalsIgnoreCase(fileStatus)) {
                        SmbFileUtil.deleteFile(prodPath + filePath, fileName);
                    } else {
                        SmbFileUtil.copyFile(qaPath + filePath, prodPath + filePath, new String[] { fileName });

                        // for check in pvcs
                        // copy to c:\temp\Javacatalog\source
                        //SmbFileUtil.copySmbToLocal(qaPath + filePath, "c:\\temp\\Javacatalog\\source" + filePath, new String[] { fileName });

                    }
                    params.put("PROCESS_RESULT", ProcessResult.SUCCESS.ordinal());
                    fileMoveDetailDao.create(params);
                }

            } catch (FileSystemException e) {
                e.printStackTrace();
                params.put("PROCESS_RESULT", ProcessResult.FAILURE.ordinal());
                fileMoveDetailDao.create(params);
                F.updateEndTime(fLogId, new Date());
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        // PVCS Checkin 處理
        // checkin
        IPvcsCmd pvcsCmd = new PvcsCmd();
        String sId = (String) dataMap.get("scrId");
        Scr scrTmp = scrDao.findById(Long.parseLong(sId));
        App appTmp = appDao.findById(scr.getJcAppId());
        String prjDb = appTmp.getPvcsProjDb();
        String prjPath = appTmp.getPvcsProjPath();
        AppPath appPath = appPathDao.findByScrId(Long.parseLong(sId), PathType.APP_BASE).get(0);
        String rdPath = appPath.getPath().endsWith("\\") || appPath.getPath().endsWith("/") ? appPath.getPath().concat("RD\\") : appPath.getPath().concat("\\RD\\");
        if(isAddFiles){
        	//新增
            int[] rc = pvcsCmd.addFiles(prjDb, prjPath, "thomaschan", "1234", "Check In", "Check In", rdPath);
            logger.debug("pvcs check in done!");
        }
        
        if(isPutFiles){
        	//重新checkin
        	SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        	Date date = new Date();
        	String strDate = sdFormat.format(date);
        	int[] rc = pvcsCmd.putFiles(prjDb, prjPath, "thomaschan", "1234", "Check In at "+strDate, "Check In at "+strDate, "RD/*");
            logger.debug("pvcs recheck in done!");
        }
        
        if(isDelFiles){
        	//有檔案要刪除
        	logger.debug("pvcs del start!");
        	String[] delFileArr=new String[delFileNameList.size()];
        	delFileArr=delFileNameList.toArray(delFileArr);
        	int[] rc = pvcsCmd.deleteFiles(prjDb, prjPath, "thomaschan", "1234", delFileArr);
            logger.debug("pvcs del done!");
            
            //update scr status
        }

        scrDao.updateStatus(scr.getId(), Status.MOVE_TO_PROD);
        F.updateEndTime(fLogId, new Date());
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("functionLogId", fLogId);
        return gson.toJson(results);
        
    }

    private String getFiles(Map dataMap) {
        String sScrId = (String) dataMap.get("scrId");
        String sBuildUnitId = (String) dataMap.get("buildUnitId");
        Scr scr = scrDao.findById(Long.parseLong(sScrId));
        List<ScrFile> files = new ArrayList<ScrFile>();
        Map<PathType, Object> appPaths = appPathDao.getAppPathsByAppId(scr.getJcAppId());
        Long buildUnitId;
        if ("all".equalsIgnoreCase(sBuildUnitId)) {
            // 全選
            buildUnitId = null;
            files = scrFileDao.findByScrId(scr.getId());
        } else {
            buildUnitId = Long.parseLong(sBuildUnitId);
            BuildUnit unit = buildUnitDao.findById(buildUnitId);
            List<Long> ids = new ArrayList<Long>();
            ids.add(unit.getId());
            files = scrFileDao.findByBuildUnitIds(ids);
        }
        String qaSourcePath = (String) appPaths.get(PathType.QA_SOURCE);
        String qaExecutionPath = (String) appPaths.get(PathType.QA_EXECUTION);
        // check 檔案是否真的存在 qaSourcePath
        String path;
        for (ScrFile file : files) {
            try {
                if (file.getDeleted()) {
                    file.setFileStatus(FileStatus.DELETE);
                } else {
                    logger.debug(qaSourcePath + " : " + file.getFilePath() + " : " + file.getFileName());
                    if (FileType.SOURCE == file.getFileType()) {
                        path = qaSourcePath;
                    } else {
                        path = qaExecutionPath;
                    }
                    if (SmbFileUtil.exist(path + file.getFilePath(), file.getFileName())) {
                        file.setFileStatus(FileStatus.EXIST);
                    } else {
                        file.setFileStatus(FileStatus.NOT_FOUND);
                    }
                }
            } catch (FileSystemException e) {
                e.printStackTrace();
                if (e.getCause() instanceof SmbAuthException) {
                    file.setFileStatus(FileStatus.ACCESS_DENIED);
                    break;
                }
            }
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("files", files);
        return gson.toJson(data);
    }

}
