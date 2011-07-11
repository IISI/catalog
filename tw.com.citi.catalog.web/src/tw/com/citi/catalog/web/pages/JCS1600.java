package tw.com.citi.catalog.web.pages;

import java.util.ArrayList;
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
import tw.com.citi.catalog.web.dao.IScrDao;
import tw.com.citi.catalog.web.dao.IScrFileDao;
import tw.com.citi.catalog.web.model.App;
import tw.com.citi.catalog.web.model.AppPath.PathType;
import tw.com.citi.catalog.web.model.BuildUnit;
import tw.com.citi.catalog.web.model.Scr;
import tw.com.citi.catalog.web.model.ScrFile;
import tw.com.citi.catalog.web.model.ScrFile.FileStatus;
import tw.com.citi.catalog.web.utils.FileUtil;

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
        List<Scr> scrList = scrDao
                .find(new HashMap<String, String>(), new String[] {}, "", "SCR_NO", 0, Long.MAX_VALUE);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("scrList", scrList);
        return gson.toJson(data);
    }

    private String query(Map dataMap) {
        String sId = (String) dataMap.get("id");
        // TODO check
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
        String sScrId = (String) dataMap.get("scrId");
        String sBuildUnitId = (String) dataMap.get("buildUnitId");
        String files = (String) dataMap.get("files");
        List<Map<String, String>> fileList = gson.fromJson(files, new TypeToken<List<Map<String, String>>>() {
        }.getType());
        // TODO move file
        Long scrId = Long.parseLong(sScrId);
        Scr scr = scrDao.findById(scrId);
        Long buildUnitId;
        String unitId = "";
        Map<PathType, Object> appPaths = appPathDao.getAppPathsByAppId(scr.getJcAppId());
        if ("all".equalsIgnoreCase(sBuildUnitId)) {
            // 全選
            buildUnitId = null;
        } else {
            // 指定
            buildUnitId = Long.parseLong(sBuildUnitId);
            BuildUnit unit = buildUnitDao.findById(buildUnitId);
            unitId = "\\" + unit.getUnitId();
        }
        // TODO get source path
        String rdPath = appPaths.get(PathType.APP_BASE) + "\\RD";
        String qaSourcePath = (String) appPaths.get(PathType.QA_SOURCE);
        // TODO get target path
        List<String> sourceFileNames = new ArrayList<String>();
        for (Map<String, String> file : fileList) {
            String filePath = file.get("filePath");
            String fileName = file.get("fileName");
            sourceFileNames.add(rdPath + "\\" + filePath + "\\" + fileName);
            try {
                FileUtil.copyFile(rdPath + "\\" + filePath, qaSourcePath + "\\" + filePath, new String[] { fileName });
            } catch (FileSystemException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    private String getFiles(Map dataMap) {
        String sScrId = (String) dataMap.get("scrId");
        String sBuildUnitId = (String) dataMap.get("buildUnitId");
        // TODO check
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
            buildUnit = "\\" + unit.getUnitId();
        }
        if (appPaths.containsKey(PathType.APP_BASE) && appPaths.containsKey(PathType.QA_SOURCE)) {
            String rdPath = appPaths.get(PathType.APP_BASE) + "\\RD";
            String qaSourcePath = (String) appPaths.get(PathType.QA_SOURCE);
            // TODO check 檔案是否真的存在 rdPath
            for (ScrFile file : files) {
                try {
                    if (FileUtil.exist(rdPath + "\\" + file.getFilePath(), file.getFileName())) {
                        file.setFileStatus(FileStatus.EXIST);
                    } else {
                        file.setFileStatus(FileStatus.NOT_FOUND);
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
            data.put("unitId", buildUnit);
            data.put("qaSourcePath", appPaths.get(PathType.QA_SOURCE));
            data.put("qaExecutionPath", appPaths.get(PathType.QA_EXECUTION));
            data.put("prodSourcePaths", appPaths.get(PathType.PROD_SOURCE));
            data.put("prodExecutionPaths", appPaths.get(PathType.PROD_EXECUTION));
            data.put("files", files);
            return gson.toJson(data);
        } else {
            // TODO
            throw new RuntimeException("");
        }
    }

}
