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
import tw.com.citi.catalog.web.dao.IAppPathDao;
import tw.com.citi.catalog.web.dao.IBuildUnitDao;
import tw.com.citi.catalog.web.dao.IFileMoveDetailDao;
import tw.com.citi.catalog.web.dao.IScrDao;
import tw.com.citi.catalog.web.dao.IScrFileDao;
import tw.com.citi.catalog.web.model.App;
import tw.com.citi.catalog.web.model.AppPath.PathType;
import tw.com.citi.catalog.web.model.BuildUnit;
import tw.com.citi.catalog.web.model.FileStatus;
import tw.com.citi.catalog.web.model.ProcessResult;
import tw.com.citi.catalog.web.model.Scr;
import tw.com.citi.catalog.web.model.Scr.Status;
import tw.com.citi.catalog.web.model.ScrFile;
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

    @SpringBean(name = "fileMoveDetailDao")
    private IFileMoveDetailDao fileMoveDetailDao;

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

    private String move(Map dataMap) {
        Date start = new Date();
        String sScrId = (String) dataMap.get("scrId");
        String files = (String) dataMap.get("files");
        List<Map<String, String>> fileList = gson.fromJson(files, new TypeToken<List<Map<String, String>>>() {
        }.getType());
        // move file
        Long scrId = Long.parseLong(sScrId);
        Scr scr = scrDao.findById(scrId);
        Long fLogId = F.log(scr.getId(), Func.JCS1200, "", "", start, null);
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
            ScrFile scrFile = scrFileDao.findByUK(scrId, filePath, fileName);
            params.put("JC_SCR_FILE_ID", scrFile.getId());
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
        F.updateEndTime(fLogId, new Date());
        return null;
    }

    private String getFiles(Map dataMap) {
        String sScrId = (String) dataMap.get("scrId");
        String sBuildUnitId = (String) dataMap.get("buildUnitId");
        Scr scr = scrDao.findById(Long.parseLong(sScrId));
        List<ScrFile> files = new ArrayList<ScrFile>();
        Map<PathType, Object> appPaths = appPathDao.getAppPathsByAppId(scr.getJcAppId());
        Long buildUnitId;
        String buildUnit = "";
        if ("all".equalsIgnoreCase(sBuildUnitId)) {
            // 全選
            buildUnitId = null;
            // get Files in RD_PATH
            files = scrFileDao.findSourceFilesByScrId(Long.parseLong(sScrId));
        } else {
            buildUnitId = Long.parseLong(sBuildUnitId);
            BuildUnit unit = buildUnitDao.findById(buildUnitId);
            // get Files in RD_PATH
            files = scrFileDao.findSourceFilesByBuildUnitId(buildUnitId);
            buildUnit = unit.getUnitId() + "\\";
        }
        String rdPath = appPaths.get(PathType.APP_BASE) + "RD\\";
        String qaSourcePath = (String) appPaths.get(PathType.QA_SOURCE);
        // check 檔案是否真的存在 rdPath
        for (ScrFile file : files) {
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
