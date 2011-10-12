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

import tw.com.citi.catalog.dao.IAppDao;
import tw.com.citi.catalog.dao.IAppFileDao;
import tw.com.citi.catalog.dao.IAppPathDao;
import tw.com.citi.catalog.dao.IBuildUnitDao;
import tw.com.citi.catalog.dao.IFileMoveDetailDao;
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
import tw.com.citi.catalog.model.Scr;
import tw.com.citi.catalog.model.Scr.Status;
import tw.com.citi.catalog.web.util.AccessControlUtil;
import tw.com.citi.catalog.web.util.F;
import tw.com.citi.catalog.web.util.F.Func;
import tw.com.citi.catalog.web.util.SmbFileUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JCS1200 extends AbstractBasePage {

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
        List<BuildUnit> units = buildUnitDao.findByAppId(scr.getJcAppId());
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("appId", app.getAppId());
        if (appPaths.containsKey(PathType.APP_BASE)) {
            data.put("rdPath", appPaths.get(PathType.APP_BASE) + "RD\\");
        }
        if (appPaths.containsKey(PathType.QA_SOURCE)) {
            data.put("qaSourcePath", appPaths.get(PathType.QA_SOURCE));
        }
        data.put("status", scr.getStatus());
        data.put("buildUnits", units);
        return gson.toJson(data);
    }

    private String move(Map<String, String> dataMap) {
        Date start = new Date();
        String sScrId = (String) dataMap.get("scrId");
        String files = (String) dataMap.get("files");
        List<Map<String, String>> fileList = gson.fromJson(files, new TypeToken<List<Map<String, String>>>() {
        }.getType());
        // move file
        Long scrId = Long.parseLong(sScrId);
        Scr scr = scrDao.findById(scrId);
        Long fLogId = F.log(scr.getId(), Func.JCS1200, "", start, null);
        Map<PathType, Object> appPaths = appPathDao.getAppPathsByAppId(scr.getJcAppId());
        // get source path
        String rdPath = appPaths.get(PathType.APP_BASE) + "RD\\";
        String qaSourcePath = (String) appPaths.get(PathType.QA_SOURCE);
        // get target path
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("JC_FUNCTION_LOG_ID", fLogId);
        params.put("TARGET_PATH", qaSourcePath);
        for (Map<String, String> file : fileList) {
            String filePath = file.get("filePath");
            String fileName = file.get("fileName");
            String status = file.get("fileStatus");
            AppFile appFile = appFileDao.findByUK(scr.getJcAppId(), filePath, fileName);
            params.put("JC_APP_FILE_ID", appFile.getId());
            try {
                if ("DELETE".equalsIgnoreCase(status)) {
                    SmbFileUtil.deleteFile(qaSourcePath + filePath, fileName);
                } else {
                    SmbFileUtil.copyFile(rdPath + filePath, qaSourcePath + filePath, new String[] { fileName });
                }
                params.put("PROCESS_RESULT", ProcessResult.SUCCESS.ordinal());
                fileMoveDetailDao.create(params);
            } catch (FileSystemException e) {
                params.put("PROCESS_RESULT", ProcessResult.FAILURE.ordinal());
                fileMoveDetailDao.create(params);
                e.printStackTrace();
                F.updateEndTime(fLogId, new Date());
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        scrDao.updateStatus(scr.getId(), Status.MOVE_TO_QA);
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("functionLogId", fLogId);
        F.updateEndTime(fLogId, new Date());
        return gson.toJson(results);
    }

    private String getFiles(Map<String, String> dataMap) {
        String sScrId = (String) dataMap.get("scrId");
        String sBuildUnitId = (String) dataMap.get("buildUnitId");
        Scr scr = scrDao.findById(Long.parseLong(sScrId));
        List<ScrFileDto> files = new ArrayList<ScrFileDto>();
        Map<PathType, Object> appPaths = appPathDao.getAppPathsByAppId(scr.getJcAppId());
        Long buildUnitId;
        String buildUnit = "";
        if ("all".equalsIgnoreCase(sBuildUnitId)) {
            // 全選
            buildUnitId = null;
        } else {
            buildUnitId = Long.parseLong(sBuildUnitId);
            BuildUnit unit = buildUnitDao.findById(buildUnitId);
            buildUnit = unit.getUnitId() + "\\";
        }
        files = scrFileDao.findBy(Long.parseLong(sScrId), buildUnitId, FileType.SOURCE);
        String rdPath = appPaths.get(PathType.APP_BASE) + "RD\\";
        String qaSourcePath = (String) appPaths.get(PathType.QA_SOURCE);
        // check 檔案是否真的存在 rdPath
        for (ScrFileDto file : files) {
            try {
                if (file.getDeleted()) {
                    file.setFileStatus(FileStatus.DELETE);
                } else {
                    if (SmbFileUtil.exist(rdPath + file.getFilePath(), file.getFileName())) {
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
        // get QA SOURCE PATH
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("rdPath", rdPath + buildUnit);
        data.put("qaSourcePath", qaSourcePath + buildUnit);
        data.put("files", files);
        return gson.toJson(data);
    }

}
