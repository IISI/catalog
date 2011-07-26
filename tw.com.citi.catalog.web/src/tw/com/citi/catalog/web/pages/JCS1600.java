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
import tw.com.citi.catalog.web.model.Scr.Status;
import tw.com.citi.catalog.web.model.ScrFile;
import tw.com.citi.catalog.web.model.ScrFile.FileStatus;
import tw.com.citi.catalog.web.util.SmbFileUtil;

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
        String sScrId = (String) dataMap.get("scrId");
        String files = (String) dataMap.get("files");
        List<Map<String, String>> fileList = gson.fromJson(files, new TypeToken<List<Map<String, String>>>() {
        }.getType());
        // move file
        Long scrId = Long.parseLong(sScrId);
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
            throw new RuntimeException("Backup current PROD folder error.", e);
        }
        // 複製 QA source/execution 到 PROD
        for (Map<String, String> file : fileList) {
            String filePath = file.get("filePath");
            String fileName = file.get("fileName");
            String fileType = file.get("fileType");
            String fileStatus = file.get("fileStatus");
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
                if ("DELETE".equalsIgnoreCase(fileStatus)) {
                    deleteFromProd(filePath, fileName, prodPaths);
                } else {
                    copyToProd(qaPath, filePath, fileName, prodPaths);
                }
                // TODO PVCS Checkin 處理
            } catch (FileSystemException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        scrDao.updateStatus(scr.getId(), Status.MOVE_TO_PROD);
        return null;
    }

    private void copyToProd(String qaPath, String filePath, String fileName, List<String> prodPaths)
            throws FileSystemException {
        for (String prodPath : prodPaths) {
            SmbFileUtil.copyFile(qaPath + filePath, prodPath + filePath, new String[] { fileName });
        }
    }

    private void deleteFromProd(String filePath, String fileName, List<String> prodPaths) throws FileSystemException {
        for (String prodPath : prodPaths) {
            SmbFileUtil.deleteFile(prodPath + filePath, fileName);
        }
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
        // check 檔案是否真的存在 qaSourcePath
        for (ScrFile file : files) {
            try {
                if (file.getDeleted()) {
                    file.setFileStatus(FileStatus.DELETE);
                } else {
                    if (SmbFileUtil.exist(qaSourcePath + file.getFilePath(), file.getFileName())) {
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
