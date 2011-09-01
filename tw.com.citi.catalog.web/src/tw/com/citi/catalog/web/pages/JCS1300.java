package tw.com.citi.catalog.web.pages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
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
import tw.com.citi.catalog.dao.IBuildUnitDao;
import tw.com.citi.catalog.dao.ICompileDetailDao;
import tw.com.citi.catalog.dao.IScrDao;
import tw.com.citi.catalog.dao.IScrFileDao;
import tw.com.citi.catalog.model.App;
import tw.com.citi.catalog.model.AppFile;
import tw.com.citi.catalog.model.BuildUnit;
import tw.com.citi.catalog.model.FileType;
import tw.com.citi.catalog.model.ProcessResult;
import tw.com.citi.catalog.model.Scr;
import tw.com.citi.catalog.model.ScrFile;
import tw.com.citi.catalog.model.AppPath.PathType;
import tw.com.citi.catalog.model.Scr.Status;
import tw.com.citi.catalog.web.util.AccessControlUtil;
import tw.com.citi.catalog.web.util.F;
import tw.com.citi.catalog.web.util.F.Func;
import tw.com.citi.catalog.web.util.NetUseUtil;
import tw.com.citi.catalog.web.util.SmbFileUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JCS1300 extends AbstractBasePage {

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

    @SpringBean(name = "compileDetailDao")
    private ICompileDetailDao compileDetailDao;

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
        } else if ("Compile".equals(actionName)) {
            boolean authenticated = AccessControlUtil.authenticateCBCUser(dataMap);
            if (!authenticated) {
                throw new RuntimeException("ID/Password is invalid.");
            }
            result = compile(dataMap);
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
        String qaSourcePath = (String) appPaths.get(PathType.QA_SOURCE);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("appId", app.getAppId());
        if (appPaths.containsKey(PathType.QA_SOURCE)) {
            data.put("qaSourcePath", qaSourcePath);
        }
        // mapping local path
        // 若 mapping 不到，或是沒有權限寫入，則中斷所有網路磁碟機，重新用 functionId 連網路磁碟機。
        String mappingLocalPath = NetUseUtil.mappingLocalPath(qaSourcePath);
        int rc = 0;
        if (!localPathWritable(mappingLocalPath)) {
            if ((rc = NetUseUtil.disconnectAllNetworkPath()) == 0) {
                rc += NetUseUtil.connectNetworkDrive(qaSourcePath);
            }
            if (rc == 0) {
                mappingLocalPath = NetUseUtil.mappingLocalPath(qaSourcePath);
            } else {
                throw new RuntimeException("Cannot map qaSourcePath to local driver.");
            }
        }
        data.put("status", scr.getStatus());
        data.put("buildUnits", units);
        data.put("mappingLocalPath", mappingLocalPath);
        return gson.toJson(data);
    }

    private boolean localPathWritable(String path) {
        boolean tf = false;
        if (!"".equals(path)) {
            String tmpFileName = String.valueOf(System.currentTimeMillis()) + ".tmp";
            File f = new File(path + tmpFileName);
            try {
                tf = f.createNewFile() && f.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tf;
    }

    private String compile(Map<String, String> dataMap) {
        Map<String, Object> retVal = new HashMap<String, Object>();
        Date start = new Date();
        String sScrId = (String) dataMap.get("scrId");
        Long scrId = Long.parseLong(sScrId);
        Long fLogId = F.log(scrId, Func.JCS1300, "", start, null);
        retVal.put("functionLogId", fLogId);
        String localPath = (String) dataMap.get("localPath");
        String files = (String) dataMap.get("files");
        List<Map<String, String>> fileList = gson.fromJson(files, new TypeToken<List<Map<String, String>>>() {
        }.getType());
        Scr scr = scrDao.findById(scrId);
        Map<PathType, Object> appPaths = appPathDao.getAppPathsByAppId(scr.getJcAppId());
        String qaSourcePath = (String) appPaths.get(PathType.QA_SOURCE);
        String qaExecutionPath = (String) appPaths.get(PathType.QA_EXECUTION);
        List<String[]> consoleLogs = new ArrayList<String[]>();
        List<String> errorMessages = new ArrayList<String>();
        retVal.put("errorMessages", errorMessages);
        for (Map<String, String> file : fileList) {
            String batchFileName = file.get("batchFileName");
            String unitId = batchFileName.substring(6, batchFileName.lastIndexOf("."));
            File batchFile = new File(localPath + batchFileName);
            if (batchFile.exists()) {
                Map<String, Object> results = compile(batchFile);
                int rc = (Integer) results.get("rc");
                consoleLogs.add(new String[] { (String) results.get("result") });
                retVal.put("consoleLogs", gson.toJson(consoleLogs));
                if (rc != 0) {
                    F.updateEndTime(fLogId, new Date());
                    errorMessages.add("Execute " + batchFileName + " fail with return code " + rc);
                    retVal.put("errorMessages", errorMessages);
                    return gson.toJson(retVal);
                } else {
                    // 把 batch 所在目錄下 target 目錄下的所有檔案搬到 qa execution
                    try {
                        SmbFileUtil.renameTo(qaSourcePath + "target\\", null, qaExecutionPath + unitId + "\\", null);
                    } catch (FileSystemException e) {
                        F.updateEndTime(fLogId, new Date());
                        errorMessages.add("Move files from " + qaSourcePath + "target\\ to " + qaExecutionPath + unitId + "\\ fail.");
                        retVal.put("errorMessages", errorMessages);
                        return gson.toJson(retVal);
                    }
                }
            } else {
                F.updateEndTime(fLogId, new Date());
                errorMessages.add("Can't find " + batchFileName + "in Mapping Local Path.");
                retVal.put("errorMessages", errorMessages);
                return gson.toJson(retVal);
            }
        }
        // 更新 Scr 的 status
        scrDao.updateStatus(scr.getId(), Status.COMPILE);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        // 更新 AppFile 的 lastCompileTime
        appFileDao.updateLastCompileTimeByJcAppId(now, scr.getJcAppId());
        // 更新 AppFile 中 EXECTION 的 fileDateTime
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("JC_FUNCTION_LOG_ID", fLogId);
        List<AppFile> appFiles = appFileDao.findByAppId(scr.getJcAppId());
        for (AppFile appFile : appFiles) {
            try {
                if (FileType.EXECUTION == appFile.getFileType()) {
                    FileObject file = SmbFileUtil.getFile(qaExecutionPath + appFile.getFilePath(),
                            appFile.getFileName());
                    appFileDao.updateFileInfoById(new Timestamp(file.getContent().getLastModifiedTime()), file
                            .getContent().getSize(), appFile.getId());
                    params.put("JC_APP_FILE_ID", appFile.getId());
                    params.put("PROCESS_RESULT", ProcessResult.SUCCESS.ordinal());
                    compileDetailDao.create(params);
                }
            } catch (FileSystemException e) {
                e.printStackTrace();
                F.updateEndTime(fLogId, new Date());
                throw new RuntimeException("Update File DateTime for AppFile " + appFile.getFileName() + " fail.", e);
            }
        }
        // 更新 ScrFile 的 lastCompileTime
        scrFileDao.updateLastCompileTimeByJcScrId(now, scrId);
        // 更新 ScrFile 中 EXECTION 的 fileDateTime
        List<ScrFile> scrFiles = scrFileDao.findByScrId(scrId);
        for (ScrFile scrFile : scrFiles) {
            try {
                if (FileType.EXECUTION == scrFile.getFileType()) {
                    FileObject file = SmbFileUtil.getFile(qaExecutionPath + scrFile.getFilePath(),
                            scrFile.getFileName());
                    scrFileDao.updateFileInfoById(new Timestamp(file.getContent().getLastModifiedTime()), file
                            .getContent().getSize(), scrFile.getId());
                }
            } catch (FileSystemException e) {
                e.printStackTrace();
                F.updateEndTime(fLogId, new Date());
                throw new RuntimeException("Update File DateTime for ScrFile " + scrFile.getFileName() + " fail.", e);
            }
        }
        F.updateEndTime(fLogId, new Date());
        return gson.toJson(retVal);
    }

    private Map<String, Object> compile(File batchFile) {
        String result = "";
        Process process = null;
        BufferedReader bf = null;
        try {
            String fullPath = batchFile.getAbsolutePath();
            String path = batchFile.getParent();
            String command = "\"" + fullPath + "\"";
            String[] cmd = new String[] { "cmd", "/C", command, path };
            process = Runtime.getRuntime().exec(cmd);
            bf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = bf.readLine()) != null) {
                result += line + "\n";
                System.out.println(line + "\n");
            }
            bf.close();
            logger.debug(result);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage() + "\n" + result, e);
        }
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("rc", process == null ? -1 : process.exitValue());
        results.put("result", result);
        return results;
    }

    private String getFiles(Map<String, String> dataMap) {
        String mappingLocalPath = (String) dataMap.get("mappingLocalPath");
        String sBuildUnitId = (String) dataMap.get("buildUnitId");
        List<String> files = new ArrayList<String>();
        String buildUnit = "";
        if (!"all".equalsIgnoreCase(sBuildUnitId)) {
            BuildUnit unit = buildUnitDao.findById(Long.parseLong(sBuildUnitId));
            buildUnit = unit.getUnitId();
        }
        // 從 mappingLocalPath 取出 batch file list
        File f = new File(mappingLocalPath);
        File[] batches = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String extension = name.substring(name.lastIndexOf(".") + 1);
                if ("bat".equalsIgnoreCase(extension)) {
                    return true;
                }
                return false;
            }
        });
        for (File file : batches) {
            String fileName = file.getName();
            if ("".equals(buildUnit)) {
                files.add(fileName);
            } else {
                if (fileName.toLowerCase().indexOf(buildUnit.toLowerCase()) > 0) {
                    files.add(fileName);
                }
            }
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("files", files);
        return gson.toJson(data);
    }

}
