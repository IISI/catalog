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
import tw.com.citi.catalog.dto.ScrFileDto;
import tw.com.citi.catalog.model.App;
import tw.com.citi.catalog.model.AppFile;
import tw.com.citi.catalog.model.AppPath.PathType;
import tw.com.citi.catalog.model.BuildUnit;
import tw.com.citi.catalog.model.FileStatus;
import tw.com.citi.catalog.model.FileType;
import tw.com.citi.catalog.model.ProcessResult;
import tw.com.citi.catalog.model.RegisterHistory;
import tw.com.citi.catalog.model.Scr;
import tw.com.citi.catalog.model.Scr.Status;
import tw.com.citi.catalog.model.ScrFile;
import tw.com.citi.catalog.web.util.AccessControlUtil;
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
            boolean authenticated = AccessControlUtil.authenticateCBCUser(dataMap);
            if (!authenticated) {
                throw new RuntimeException("ID/Password is invalid.");
            }
            result = move(dataMap);
        } else if ("Query".equals(actionName)) {
            result = query(dataMap);
        } else if ("GetFiles".equals(actionName)) {
            result = getFiles(dataMap);
        } else if ("CheckIn".equals(actionName)) {
            result = pvcsCheckIn(dataMap);
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

    private String pvcsCheckIn(Map dataMap) {
        String sScrId = (String) dataMap.get("scrId");
        String files = (String) dataMap.get("files");
        String fLogId = (String) dataMap.get("functionLogId");
        List<Map<String, String>> fileList = gson.fromJson(files, new TypeToken<List<Map<String, String>>>() {
        }.getType());
        Long scrId = Long.parseLong(sScrId);
        Scr scr = scrDao.findById(scrId);
        Map<String, Object> moveDetail = new HashMap<String, Object>();
        moveDetail.put("JC_FUNCTION_LOG_ID", fLogId);
        // -----------------------------------calculate pvcs action /add/put/del
        boolean isAddFiles = false;
        boolean isPutFiles = false;
        boolean isDelFiles = false;
        List<String[]> addFileNameList = new ArrayList<String[]>();
        List<String[]> putFileNameList = new ArrayList<String[]>();
        List<String[]> delFileNameList = new ArrayList<String[]>();
        for (Map<String, String> file : fileList) {
            String scrFileId = file.get("id");
            String sqlCode = "select * from JC_REGISTER_HISTORY where jc_scr_id= :scrId and jc_scr_file_id= :scrFileId and file_type=0 and register_count="
                    + scr.getRegisterCount();
            Map<String, String> params = new HashMap<String, String>();
            params.put("scrId", sScrId);
            params.put("scrFileId", scrFileId);
            // String filePath=file.get("filePath");
            // String fileName=file.get("fileName");
            // String fileType=file.get("fileType");
            // /String fileStatus=file.get("fileStatus");
            // logger.debug("id="+fileId+", path="+filePath+", name="+fileName+", fileType="+fileType+",fileStatus="+fileStatus);
            List<RegisterHistory> registerHistoryList = registerHistoryDao.query(sqlCode, params);

            if (registerHistoryList.size() > 0) {
                RegisterHistory registerHistory = registerHistoryList.get(registerHistoryList.size() - 1);
                int registerAction = registerHistory.getRegisterAction();
                String filePath = file.get("filePath");
                String fileName = file.get("fileName");
                if (registerAction == 0) {
                    // 新增
                    isAddFiles = true;
                    addFileNameList.add(new String[] { filePath, fileName });
                } else if (registerAction == 1) {
                    // 修改
                    isPutFiles = true;
                    putFileNameList.add(new String[] { filePath, fileName });
                } else if (registerAction == 2) {
                    // 刪除
                    isDelFiles = true;
                    logger.debug("DEL: " + filePath + fileName);
                    delFileNameList.add(new String[] { filePath, fileName });
                }
                logger.debug("id=" + scrFileId + ", action=" + registerAction);
            }

        }

        logger.debug("isAdd=" + isAddFiles);
        logger.debug("isPutFiles=" + isPutFiles);
        logger.debug("isDelFiles=" + isDelFiles + ", size=" + delFileNameList.size());
        // ---------------------------------------------------------------------------------------------------
        // PVCS Checkin 處理
        // checkin
        IPvcsCmd pvcsCmd = new PvcsCmd();

        String pvcsId = (String) dataMap.get("pvcsId");
        String pvcsPwd = (String) dataMap.get("pvcsPwd");
        System.out.println("pvcs login:" + pvcsId + " ," + pvcsPwd);

        String sId = (String) dataMap.get("scrId");
        Scr scrTmp = scrDao.findById(Long.parseLong(sId));
        App appTmp = appDao.findById(scr.getJcAppId());
        String prjDb = appTmp.getPvcsProjDb();
        String prjPath = appTmp.getPvcsProjPath();
        String label = scrTmp.getScrNo() + scrTmp.getRegisterCount();
        boolean error = false;
        moveDetail.put("PATH_TYPE", PathType.PVCS.ordinal());
        if (isAddFiles) {
            // 新增
            for (String[] fileInfo : addFileNameList) {
                AppFile appFile = appFileDao.findByUK(scr.getJcAppId(), fileInfo[0], fileInfo[1]);
                moveDetail.put("TARGET_PATH", fileInfo[0]);
                moveDetail.put("JC_APP_FILE_ID", appFile.getId());
                int rc = pvcsCmd.addFile(prjDb, prjPath + fileInfo[0], pvcsId, pvcsPwd, label, "Check In", fileInfo[1]);
                if (rc == 0) {
                    moveDetail.put("PROCESS_RESULT", ProcessResult.SUCCESS.ordinal());
                } else {
                    moveDetail.put("PROCESS_RESULT", ProcessResult.FAILURE.ordinal());
                    error = true;
                }
                fileMoveDetailDao.create(moveDetail);
            }
            logger.debug("pvcs add done!");
        }

        if (isPutFiles) {
            // 重新checkin
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
            Date date = new Date();
            String strDate = sdFormat.format(date);
            for (String[] fileInfo : putFileNameList) {
                AppFile appFile = appFileDao.findByUK(scr.getJcAppId(), fileInfo[0], fileInfo[1]);
                moveDetail.put("TARGET_PATH", fileInfo[0]);
                moveDetail.put("JC_APP_FILE_ID", appFile.getId());
                int rc = pvcsCmd.putFile(prjDb, prjPath + fileInfo[0], pvcsId, pvcsPwd, label,
                        "Check In at " + strDate, fileInfo[1]);
                if (rc == 0) {
                    moveDetail.put("PROCESS_RESULT", ProcessResult.SUCCESS.ordinal());
                } else {
                    moveDetail.put("PROCESS_RESULT", ProcessResult.FAILURE.ordinal());
                    error = true;
                }
                fileMoveDetailDao.create(moveDetail);
            }
            logger.debug("pvcs put done!");
        }

        if (isDelFiles) {
            // 有檔案要刪除
            logger.debug("pvcs del start!");
            for (String[] fileInfo : delFileNameList) {
                AppFile appFile = appFileDao.findByUK(scr.getJcAppId(), fileInfo[0], fileInfo[1]);
                moveDetail.put("TARGET_PATH", fileInfo[0]);
                moveDetail.put("JC_APP_FILE_ID", appFile.getId());
                int rc = pvcsCmd.deleteFile(prjDb, prjPath + fileInfo[0], pvcsId, pvcsPwd, fileInfo[1]);
                if (rc == 0) {
                    moveDetail.put("PROCESS_RESULT", ProcessResult.SUCCESS.ordinal());
                } else {
                    moveDetail.put("PROCESS_RESULT", ProcessResult.FAILURE.ordinal());
                    error = true;
                }
                fileMoveDetailDao.create(moveDetail);
            }
            logger.debug("pvcs del done!");
        }
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("PVCS", error ? " Fail." : "OK.");
        results.put("functionLogId", fLogId);
        return gson.toJson(results);
    }

    private String move(Map dataMap) {
        Date start = new Date();
        String sScrId = (String) dataMap.get("scrId");
        String files = (String) dataMap.get("files");
        List<Map<String, String>> fileList = gson.fromJson(files, new TypeToken<List<Map<String, String>>>() {
        }.getType());
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
        // 備份失敗時將 rename 後的 BACKUP folder 還原
        String tempId = String.valueOf(System.currentTimeMillis());
        try {
            if (SmbFileUtil.exist(prodBackupPath, null)) {
                SmbFileUtil.renameTo(prodBackupPath, null,
                        prodBackupPath.trim().substring(0, prodBackupPath.trim().length() - 1) + "_" + tempId, null);
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
            F.updateEndTime(fLogId, new Date());
            throw new RuntimeException("Rename backup folder error.", e);
        }
        // 備份現有的 PROD source/execution 到 BACKUP
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("JC_FUNCTION_LOG_ID", fLogId);
        String sourcePath;
        String targetPath;
        for (Map<String, String> file : fileList) {
            String filePath = file.get("filePath");
            String fileName = file.get("fileName");
            String fileType = file.get("fileType");
            AppFile appFile = appFileDao.findByUK(scr.getJcAppId(), filePath, fileName);
            params.put("JC_APP_FILE_ID", appFile.getId());
            if ("SOURCE".equalsIgnoreCase(fileType)) {
                sourcePath = prodSourcePath.get(0) + filePath;
                targetPath = prodBackupPath + "source\\" + filePath;
            } else {
                sourcePath = prodExecutionPath.get(0) + filePath;
                targetPath = prodBackupPath + "execution\\" + filePath;
            }
            try {
                params.put("TARGET_PATH", targetPath);
                params.put("PATH_TYPE", PathType.PROD_BACKUP.ordinal());
                if (SmbFileUtil.exist(sourcePath, fileName)) {
                    SmbFileUtil.copyFile(sourcePath, targetPath, new String[] { fileName });
                    params.put("PROCESS_RESULT", ProcessResult.SUCCESS.ordinal());
                    fileMoveDetailDao.create(params);
                }
            } catch (FileSystemException e) {
                e.printStackTrace();
                // 複製失敗時，刪除 BACKUP folder，將原本 rename 的 backup folder 復原
                try {
                    SmbFileUtil.deleteFile(prodBackupPath, null);
                    SmbFileUtil.renameTo(prodBackupPath.trim().substring(0, prodBackupPath.trim().length() - 1) + "_"
                            + tempId, null, prodBackupPath, null);
                } catch (FileSystemException e1) {
                    // 還原
                    e1.printStackTrace();
                    throw new RuntimeException("Rollback backup folder error. There should be a folder named with _"
                            + tempId + ", please rename back manually.", e1);
                }
                params.put("PROCESS_RESULT", ProcessResult.FAILURE.ordinal());
                fileMoveDetailDao.create(params);
                throw new RuntimeException("Backup file error. " + fileName, e);
            }
        }
        // 避免檔案被佔用時，複製檔案失敗
        // rename PROD folder 下，所有會被更新的檔案，
        // 如果 rename 失敗，就代表有檔案被佔用，
        List<String> renamedFiles = new ArrayList<String>();
        for (Map<String, String> file : fileList) {
            String filePath = file.get("filePath");
            String fileName = file.get("fileName");
            logger.debug("" + filePath + "-" + fileName);
            String fileType = file.get("fileType");
            List<String> prodPaths = new ArrayList<String>();
            if ("SOURCE".equalsIgnoreCase(fileType)) {
                prodPaths = prodSourcePath;
            } else {
                prodPaths = prodExecutionPath;
            }
            for (String prodPath : prodPaths) {
                try {
                    if (SmbFileUtil.exist(prodPath + filePath, fileName)) {
                        SmbFileUtil.renameTo(prodPath + filePath, fileName, prodPath + filePath, fileName + "_"
                                + tempId);
                        renamedFiles.add(fileName);
                    }
                } catch (FileSystemException e) {
                    e.printStackTrace();
                    // rename 失敗，代表該檔案無法 rename，還原已經 rename 成功的檔案
                    for (String renamedFile : renamedFiles) {
                        try {
                            SmbFileUtil.renameTo(prodPath + filePath, renamedFile + "_" + tempId, prodPath + filePath,
                                    renamedFile);
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
        }

        // 複製 QA source/execution 到 PROD
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
                    params.put("PATH_TYPE", PathType.PROD_SOURCE.ordinal());
                } else {
                    qaPath = qaExecutionPath;
                    prodPaths = prodExecutionPath;
                    params.put("PATH_TYPE", PathType.PROD_EXECUTION.ordinal());
                }
                for (String prodPath : prodPaths) {
                    params.put("TARGET_PATH", prodPath);
                    if ("DELETE".equalsIgnoreCase(fileStatus)) {
                        SmbFileUtil.deleteFile(prodPath + filePath, fileName);
                    } else {
                        SmbFileUtil.copyFile(qaPath + filePath, prodPath + filePath, new String[] { fileName });
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
        // 將 AppFile 及 ScrFile 的 CheckOut flag 設為 false
        for (Map<String, String> file : fileList) {
            String filePath = file.get("filePath");
            String fileName = file.get("fileName");
            logger.debug("" + filePath + "-" + fileName);
            AppFile appFile = appFileDao.findByUK(scr.getJcAppId(), filePath, fileName);
            ScrFile scrFile = scrFileDao.findByUK(scr.getId(), filePath, fileName);
            appFileDao.updateCheckOutFlag(appFile.getId());
            scrFileDao.updateCheckOutFlag(scrFile.getId());
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
        List<ScrFileDto> files = new ArrayList<ScrFileDto>();
        Map<PathType, Object> appPaths = appPathDao.getAppPathsByAppId(scr.getJcAppId());
        Long buildUnitId;
        if ("all".equalsIgnoreCase(sBuildUnitId)) {
            // 全選
            buildUnitId = null;
        } else {
            buildUnitId = Long.parseLong(sBuildUnitId);
            BuildUnit unit = buildUnitDao.findById(buildUnitId);
            List<Long> ids = new ArrayList<Long>();
            ids.add(unit.getId());
        }
        files.addAll(scrFileDao.findBy(Long.parseLong(sScrId), buildUnitId, FileType.SOURCE));
        files.addAll(scrFileDao.findBy(Long.parseLong(sScrId), buildUnitId, FileType.EXECUTION));
        String qaSourcePath = (String) appPaths.get(PathType.QA_SOURCE);
        String qaExecutionPath = (String) appPaths.get(PathType.QA_EXECUTION);
        // check 檔案是否真的存在 qaSourcePath
        String path;
        for (ScrFileDto file : files) {
            try {
                if (file.getDeleted()) {
                    file.setFileStatus(FileStatus.DELETE);
                } else {
                    logger.debug(qaSourcePath + " : " + file.getFilePath() + " : " + file.getFileName());
                    if (FileType.SOURCE.ordinal() == file.getFileType()) {
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
