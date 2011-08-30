package tw.com.citi.catalog.web.pages;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jcifs.smb.SmbAuthException;

import org.apache.commons.vfs.FileSystemException;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import tw.com.citi.catalog.web.dao.IAppDao;
import tw.com.citi.catalog.web.dao.IAppFileDao;
import tw.com.citi.catalog.web.dao.IAppPathDao;
import tw.com.citi.catalog.web.dao.IFileMoveDetailDao;
import tw.com.citi.catalog.web.dao.IScrDao;
import tw.com.citi.catalog.web.model.App;
import tw.com.citi.catalog.web.model.AppFile;
import tw.com.citi.catalog.web.model.AppPath.PathType;
import tw.com.citi.catalog.web.model.FileType;
import tw.com.citi.catalog.web.model.ProcessResult;
import tw.com.citi.catalog.web.model.Scr;
import tw.com.citi.catalog.web.util.AccessControlUtil;
import tw.com.citi.catalog.web.util.F;
import tw.com.citi.catalog.web.util.F.Func;
import tw.com.citi.catalog.web.util.SmbFileUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JCS1700 extends AbstractBasePage {

    @SpringBean(name = "appDao")
    private IAppDao appDao;

    @SpringBean(name = "appPathDao")
    private IAppPathDao appPathDao;

    @SpringBean(name = "scrDao")
    private IScrDao scrDao;

    @SpringBean(name = "appFileDao")
    private IAppFileDao appFileDao;

    @SpringBean(name = "fileMoveDetailDao")
    private IFileMoveDetailDao fileMoveDetailDao;

    private transient Gson gson = new Gson();

    @Override
    public String handleRequest(PageParameters params) {
        String result;
        String actionName = params.getString("actionName");
        String actionParams = params.getString("actionParams");
        Map<String, String> dataMap = gson.fromJson(actionParams, new TypeToken<Map<String, String>>() {
        }.getType());
        if ("Init".equals(actionName)) {
            result = init(dataMap);
        } else if ("Rollback".equals(actionName)) {
            boolean authenticated = AccessControlUtil.authenticateCBCUser(dataMap);
            if (!authenticated) {
                throw new RuntimeException("ID/Password is invalid.");
            }
            result = rollback(dataMap);
        } else if ("Query".equals(actionName)) {
            result = query(dataMap);
        } else if ("GetFiles".equals(actionName)) {
            result = getFiles(dataMap);
        } else {
            throw new IllegalArgumentException("Cannot find actionName: " + actionName);
        }
        return result;
    }

    private String init(Map<String, String> dataMap) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("deleted", "0");
        List<Scr> scrList = scrDao.find(params, new String[] { "equal" }, "", "SCR_NO", 0, Long.MAX_VALUE);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("scrList", scrList);
        return gson.toJson(data);
    }

    private String query(Map<String, String> dataMap) {
        String sId = (String) dataMap.get("id");
        Scr scr = scrDao.findById(Long.parseLong(sId));
        App app = appDao.findById(scr.getJcAppId());
        Map<PathType, Object> appPaths = appPathDao.getAppPathsByAppId(scr.getJcAppId());
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("appId", app.getAppId());
        data.put("qaSourcePath", appPaths.get(PathType.QA_SOURCE));
        data.put("qaExecutionPath", appPaths.get(PathType.QA_EXECUTION));
        data.put("prodBackupPath", appPaths.get(PathType.PROD_BACKUP));
        data.put("prodSourcePath", appPaths.get(PathType.PROD_SOURCE));
        data.put("prodExecutionPath", appPaths.get(PathType.PROD_EXECUTION));
        data.put("status", scr.getStatus());
        return gson.toJson(data);
    }

    private String rollback(Map<String, String> dataMap) {
        Date start = new Date();
        String sScrId = (String) dataMap.get("scrId");
        // move file
        Long scrId = Long.parseLong(sScrId);
        Long fLogId = F.log(scrId, Func.JCS1700, "", start, null);
        Scr scr = scrDao.findById(scrId);
        Map<PathType, Object> appPaths = appPathDao.getAppPathsByAppId(scr.getJcAppId());
        // get path
        String qaSourcePath = (String) appPaths.get(PathType.QA_SOURCE);
        String qaExecutionPath = (String) appPaths.get(PathType.QA_EXECUTION);
        String prodBackupPath = (String) appPaths.get(PathType.PROD_BACKUP);
        List<String> prodSourcePath = (List<String>) appPaths.get(PathType.PROD_SOURCE);
        List<String> prodExecutionPath = (List<String>) appPaths.get(PathType.PROD_EXECUTION);
        // rename current QA/PROD folder
        try {
            List<String> paths = new ArrayList<String>();
            paths.add(qaSourcePath);
            paths.add(qaExecutionPath);
            paths.addAll(prodSourcePath);
            paths.addAll(prodExecutionPath);
            long current = System.currentTimeMillis();
            for (String path : paths) {
                if (SmbFileUtil.exist(path, null)) {
                    SmbFileUtil.renameTo(path, null,
                            path.trim().substring(0, path.trim().length() - 1) + "_" + String.valueOf(current), null);
                }
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
            F.updateEndTime(fLogId, new Date());
            throw new RuntimeException("Rename backup folder error. " + e.getMessage(), e);
        }
        // rollback 現有的 BACKUP 到 QA/PROD source/execution
        try {
            List<String> sourcePaths = new ArrayList<String>();
            sourcePaths.add(qaSourcePath);
            sourcePaths.addAll(prodSourcePath);
            for (String path : sourcePaths) {
                if (SmbFileUtil.exist(prodBackupPath + "source\\", null)) {
                    SmbFileUtil.copyFolder(prodBackupPath + "source\\", path);
                }
            }
            List<String> executionPaths = new ArrayList<String>();
            executionPaths.add(qaExecutionPath);
            executionPaths.addAll(prodExecutionPath);
            for (String path : executionPaths) {
                if (SmbFileUtil.exist(prodBackupPath + "execution\\", null)) {
                    SmbFileUtil.copyFolder(prodBackupPath + "execution\\", path);
                }
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
            F.updateEndTime(fLogId, new Date());
            throw new RuntimeException("Backup current PROD folder error. " + e.getMessage(), e);
        }
        // 紀錄 fileMoveDetail，因為前面都是整個目錄搬移，若失敗就無法紀錄檔案明細
        App app = appDao.findById(scr.getJcAppId());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("JC_FUNCTION_LOG_ID", fLogId);
        params.put("TARGET_PATH", qaSourcePath);
        List<AppFile> appFiles = appFileDao.findByAppId(app.getId());
        for (AppFile appFile : appFiles) {
            params.put("JC_APP_FILE_ID", appFile.getId());
            params.put("PROCESS_RESULT", ProcessResult.SUCCESS.ordinal());
            fileMoveDetailDao.create(params);
        }
        F.updateEndTime(fLogId, new Date());
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("functionLogId", fLogId);
        return gson.toJson(results);
    }

    private String getFiles(Map<String, String> dataMap) {
        String sId = (String) dataMap.get("id");
        Scr scr = scrDao.findById(Long.parseLong(sId));
        App app = appDao.findById(scr.getJcAppId());
        Map<PathType, Object> appPaths = appPathDao.getAppPathsByAppId(scr.getJcAppId());
        List<AppFile> files = new ArrayList<AppFile>();
        String prodSourceBackupPath = (String) appPaths.get(PathType.PROD_BACKUP) + "source\\";
        String prodExecutionBackupPath = (String) appPaths.get(PathType.PROD_BACKUP) + "execution\\";
        List<AppFile> appFiles = appFileDao.findByAppId(app.getId());
        for (AppFile appFile : appFiles) {
            String path = "";
            if (FileType.SOURCE == appFile.getFileType()) {
                path = prodSourceBackupPath + appFile.getFilePath();
            } else {
                path = prodExecutionBackupPath + appFile.getFilePath();
            }
            try {
                if (SmbFileUtil.exist(path, appFile.getFileName())) {
                    files.add(appFile);
                }
            } catch (FileSystemException e) {
                e.printStackTrace();
                if (e.getCause() instanceof SmbAuthException) {
                    throw new RuntimeException("Access PROD Backup Path error.", e);
                }
            }
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("files", files);
        return gson.toJson(data);
    }

}
