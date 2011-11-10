package tw.com.citi.catalog.web.pages;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import tw.com.citi.catalog.dao.IAppDao;
import tw.com.citi.catalog.dao.IAppFileDao;
import tw.com.citi.catalog.dao.IAppPathDao;
import tw.com.citi.catalog.dao.IFileMoveDetailDao;
import tw.com.citi.catalog.dao.IRegisterHistoryDao;
import tw.com.citi.catalog.dao.IScrDao;
import tw.com.citi.catalog.dao.IScrFileDao;
import tw.com.citi.catalog.dto.ScrFileDto;
import tw.com.citi.catalog.model.App;
import tw.com.citi.catalog.model.AppFile;
import tw.com.citi.catalog.model.AppPath.PathType;
import tw.com.citi.catalog.model.FileType;
import tw.com.citi.catalog.model.ProcessResult;
import tw.com.citi.catalog.model.RegisterHistory;
import tw.com.citi.catalog.model.Scr;
import tw.com.citi.catalog.model.ScrFile;
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

    @SpringBean(name = "scrFileDao")
    private IScrFileDao scrFileDao;

    @SpringBean(name = "registerHistoryDao")
    private IRegisterHistoryDao registerHistoryDao;

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
        List<Scr> scrList = scrDao.findClosedScr();
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
        String prodBackupPath = (String) appPaths.get(PathType.PROD_BACKUP);
        List<String> prodSourcePath = (List<String>) appPaths.get(PathType.PROD_SOURCE);
        List<String> prodExecutionPath = (List<String>) appPaths.get(PathType.PROD_EXECUTION);
        // 將 BACKUP 目錄下的檔案複製到 PROD 目錄下
        // 列出目前 BACKUP 目錄下的檔案
        List<FileObject> backupSourceFiles = new ArrayList<FileObject>();
        List<FileObject> backupExecutionFiles = new ArrayList<FileObject>();
        try {
            if (SmbFileUtil.exist(prodBackupPath + "source\\", null)) {
                backupSourceFiles.addAll(SmbFileUtil.getFiles(prodBackupPath + "source\\"));
            }
            if (SmbFileUtil.exist(prodBackupPath + "execution\\", null)) {
                backupExecutionFiles.addAll(SmbFileUtil.getFiles(prodBackupPath + "execution\\"));
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
            F.updateEndTime(fLogId, new Date());
            throw new RuntimeException("List backup files error. " + e.getMessage(), e);
        }
        // 查出最後一次註冊的檔案
        String sqlCode = "select * from JC_REGISTER_HISTORY where jc_scr_id= :scrId and register_count="
                + scr.getRegisterCount();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("scrId", sScrId);
        List<RegisterHistory> registerHistoryList = registerHistoryDao.query(sqlCode, params);
        List<String> renamedFiles = new ArrayList<String>();
        String tempId = String.valueOf(System.currentTimeMillis());
        for (RegisterHistory rh : registerHistoryList) {
            ScrFile sf = scrFileDao.findById(rh.getJcScrFileId());
            String fileName = sf.getFileName();
            String filePath = sf.getFilePath();
            if (rh.getRegisterAction() == 0 || rh.getRegisterAction() == 1) {
                // 0:register action 為 new，代表 backup 中沒有該檔案，roll back 前，應把此檔刪除
                // or 更名
                // 1:register action 為 update，代表 backup 中有該檔案，roll back 前，應把此檔更名
                if (FileType.SOURCE == sf.getFileType()) {
                    // rename source file
                    try {
                        for (String path : prodSourcePath) {
                            if (SmbFileUtil.exist(path + filePath, fileName)) {
                                SmbFileUtil.renameTo(path + filePath, fileName, path + filePath, fileName + "_"
                                        + tempId);
                                renamedFiles.add(fileName);
                            }
                        }
                    } catch (FileSystemException e) {
                        e.printStackTrace();
                        // rename 失敗，代表該檔案無法 rename，還原已經 rename 成功的檔案
                        for (String renamedFile : renamedFiles) {
                            try {
                                SmbFileUtil.renameTo(filePath, renamedFile + "_" + tempId, filePath, renamedFile);
                            } catch (FileSystemException e1) {
                                e1.printStackTrace();
                                // 還原 renamed file 失敗
                                throw new RuntimeException(
                                        "Rollback renamed file error. There should be files named with _" + tempId
                                                + ", please rename back manually.", e1);
                            }
                        }
                        throw new RuntimeException("Rename file error. " + fileName, e);
                    }
                } else {
                    // rename execution file
                    try {
                        for (String path : prodExecutionPath) {
                            if (SmbFileUtil.exist(path + filePath, fileName)) {
                                SmbFileUtil.renameTo(path + filePath, fileName, path + filePath, fileName + "_"
                                        + tempId);
                                renamedFiles.add(fileName);
                            }
                        }
                    } catch (FileSystemException e) {
                        e.printStackTrace();
                        // rename 失敗，代表該檔案無法 rename，還原已經 rename 成功的檔案
                        for (String renamedFile : renamedFiles) {
                            try {
                                SmbFileUtil.renameTo(filePath, renamedFile + "_" + tempId, filePath, renamedFile);
                            } catch (FileSystemException e1) {
                                e1.printStackTrace();
                                // 還原 renamed file 失敗
                                throw new RuntimeException(
                                        "Rollback renamed file error. There should be files named with _" + tempId
                                                + ", please rename back manually.", e1);
                            }
                        }
                        throw new RuntimeException("Rename file error. " + fileName, e);
                    }
                }
            } else if (rh.getRegisterAction() == 2) {
                // register action 為 delete，不用處理，roll back 後，檔案自然復原
            }
        }

        // roll back 現有的 BACKUP 的檔案到 QA/PROD source/execution
        params.put("JC_FUNCTION_LOG_ID", fLogId);
        try {
            for (RegisterHistory rh : registerHistoryList) {
                ScrFile sf = scrFileDao.findById(rh.getJcScrFileId());
                String fileName = sf.getFileName();
                String filePath = sf.getFilePath();
                AppFile appFile = appFileDao.findByUK(scr.getJcAppId(), filePath, fileName);
                params.put("JC_APP_FILE_ID", appFile.getId());
                if (FileType.SOURCE == sf.getFileType()) {
                    params.put("PATH_TYPE", PathType.PROD_SOURCE.ordinal());
                    for (String path : prodSourcePath) {
                        params.put("TARGET_PATH", path + filePath);
                        if (SmbFileUtil.exist(prodBackupPath + "source\\" + filePath, fileName)) {
                            SmbFileUtil.copyFile(prodBackupPath + "source\\"+ filePath, path + filePath,
                                    new String[] { fileName });
                        }
                        params.put("PROCESS_RESULT", ProcessResult.SUCCESS.ordinal());
                        fileMoveDetailDao.create(params);
                    }
                } else {
                    params.put("PATH_TYPE", PathType.PROD_EXECUTION.ordinal());
                    for (String path : prodExecutionPath) {
                        params.put("TARGET_PATH", path + filePath);
                        if (SmbFileUtil.exist(prodBackupPath + "execution\\"+filePath, fileName)) {
                            SmbFileUtil.copyFile(prodBackupPath + "execution\\"+filePath, path + filePath,
                                    new String[] { fileName });
                        }
                        params.put("PROCESS_RESULT", ProcessResult.SUCCESS.ordinal());
                        fileMoveDetailDao.create(params);
                    }
                }
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
            params.put("PROCESS_RESULT", ProcessResult.FAILURE.ordinal());
            fileMoveDetailDao.create(params);
            F.updateEndTime(fLogId, new Date());
            throw new RuntimeException("Backup current PROD folder error. " + e.getMessage(), e);
        }
        F.updateEndTime(fLogId, new Date());
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("functionLogId", fLogId);
        return gson.toJson(results);
    }

    private String getFiles(Map<String, String> dataMap) {
        String sId = (String) dataMap.get("id");
        Scr scr = scrDao.findById(Long.parseLong(sId));
        List<ScrFileDto> files = new ArrayList<ScrFileDto>();
        String sqlCode = "select * from JC_REGISTER_HISTORY where jc_scr_id= :scrId and register_count="
                + scr.getRegisterCount();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("scrId", sId);
        List<RegisterHistory> registerHistoryList = registerHistoryDao.query(sqlCode, params);
        for (RegisterHistory rh : registerHistoryList) {
            ScrFile sf = scrFileDao.findById(rh.getJcScrFileId());
            ScrFileDto sfd = new ScrFileDto();
            sfd.setFileName(sf.getFileName());
            sfd.setFilePath(sf.getFilePath());
            sfd.setFileType(sf.getFileType().ordinal());
            files.add(sfd);
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("files", files);
        return gson.toJson(data);
    }

}
