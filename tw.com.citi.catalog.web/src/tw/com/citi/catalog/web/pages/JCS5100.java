package tw.com.citi.catalog.web.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileSystemException;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tw.com.citi.catalog.web.dao.IAppDao;
import tw.com.citi.catalog.web.dao.IAppFileDao;
import tw.com.citi.catalog.web.dao.IAppPathDao;
import tw.com.citi.catalog.web.dao.IBuildUnitDao;
import tw.com.citi.catalog.web.dao.IScrFileDao;
import tw.com.citi.catalog.web.grid.IGridHandler;
import tw.com.citi.catalog.web.model.AppFile;
import tw.com.citi.catalog.web.model.AppPath;
import tw.com.citi.catalog.web.model.AppPath.PathType;
import tw.com.citi.catalog.web.model.BuildUnit;
import tw.com.citi.catalog.web.model.ScrFile;
import tw.com.citi.catalog.web.utils.AccessControlUtil;
import tw.com.citi.catalog.web.utils.FileUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JCS5100 extends AbstractBasePage {

    @SpringBean(name = "appGrid")
    private IGridHandler appGrid;

    @SpringBean(name = "unitGrid")
    private IGridHandler unitGrid;

    @SpringBean(name = "appDao")
    private IAppDao appDao;

    @SpringBean(name = "appPathDao")
    private IAppPathDao appPathDao;

    @SpringBean(name = "appFileDao")
    private IAppFileDao appFileDao;

    @SpringBean(name = "scrFileDao")
    private IScrFileDao scrFileDao;

    @SpringBean(name = "buildUnitDao")
    private IBuildUnitDao buildUnitDao;

    private transient Gson gson = new Gson();

    @Override
    public IGridHandler getGridHandler(String handlerName) {
        if ("appGrid".equals(handlerName)) {
            return appGrid;
        } else if ("unitGrid".equals(handlerName)) {
            return unitGrid;
        }
        return null;
    }

    @Override
    public String handleRequest(PageParameters params) {
        String results;
        String actionName = params.getString("actionName");
        String actionParams = params.getString("actionParams");
        if (actionName == null || "".equals(actionName.trim())) {
            throw new IllegalArgumentException("Cannot find parameter: actionName");
        }
        Map<String, String> dataMap = gson.fromJson(actionParams, new TypeToken<Map<String, String>>() {
        }.getType());
        if ("addPathCheck".equalsIgnoreCase(actionName)) {
            results = addPathCheck(dataMap);
        } else if ("Add".equalsIgnoreCase(actionName) && AccessControlUtil.authenticateCBCUser(dataMap)) {
            try {
                results = add(dataMap);
            } catch (FileSystemException e) {
                e.printStackTrace();
                throw new SecurityException("Add Application fail.", e);
            }
        } else if ("Modify".equalsIgnoreCase(actionName) && AccessControlUtil.authenticateCBCUser(dataMap)) {
            try {
                results = modify(dataMap);
            } catch (FileSystemException e) {
                e.printStackTrace();
                throw new SecurityException("Modify Application fail.", e);
            }
        } else if ("Delete".equalsIgnoreCase(actionName) && AccessControlUtil.authenticateCBCUser(dataMap)) {
            results = delete(dataMap);
        } else {
            throw new IllegalArgumentException("Cannot find actionName: " + actionName);
        }
        return results;
    }

    private String addPathCheck(Map<String, String> dataMap) {
        String appId = dataMap.get("appId");
        String basePath = dataMap.get("basePath");
        if (appId == null || "".equals(appId.trim())) {
            throw new IllegalArgumentException("Please input application id.");
        }
        if (basePath == null || "".equals(basePath.trim())) {
            throw new IllegalArgumentException("Please input base path.");
        }
        if (!FileUtil.writable(basePath)) {
            throw new IllegalArgumentException("Cannot access base path.");
        }
        return "";
    }

    private String add(Map<String, String> dataMap) throws FileSystemException {
        String appId = dataMap.get("appId");
        String description = dataMap.get("description");
        String appBasePath = dataMap.get("appBasePath");
        String qaSourcePath = dataMap.get("qaSourcePath");
        String qaExecutionPath = dataMap.get("qaExecutionPath");
        String prodBackupPath = dataMap.get("prodBackupPath");
        String prodPaths = dataMap.get("prodPaths");
        String pvcsProjDb = dataMap.get("pvcsProjDb");
        String pvcsProjPath = dataMap.get("pvcsProjPath");
        String units = dataMap.get("units");
        List<Map<String, String>> prodPathList = gson.fromJson(prodPaths, new TypeToken<List<Map<String, String>>>() {
        }.getType());
        List<Map<String, String>> unitList = gson.fromJson(units, new TypeToken<List<Map<String, String>>>() {
        }.getType());
        // insert app
        Map<String, Object> app = new HashMap<String, Object>();
        app.put("app_id", appId);
        app.put("description", description);
        // app.put("app_base_path", appBasePath);
        app.put("pvcs_proj_db", pvcsProjDb);
        app.put("pvcs_proj_path", pvcsProjPath);
        Long jcAppId = appDao.create(app);
        // insert appPath
        List<String> prodSourcePaths = new ArrayList<String>();
        List<String> prodExecutionPaths = new ArrayList<String>();
        for (Map<String, String> prodPathData : prodPathList) {
            String sourcePath = prodPathData.get("sourcePath");
            prodSourcePaths.add(sourcePath);
            String executionPath = prodPathData.get("executionPath");
            prodExecutionPaths.add(executionPath);
        }
        insertAppPaths(jcAppId, appBasePath, qaSourcePath, qaExecutionPath, prodBackupPath, prodSourcePaths,
                prodExecutionPaths);
        // insert buildunit
        for (Map<String, String> unitData : unitList) {
            String unitId = unitData.get("unitId");
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("JC_APP_ID", jcAppId);
            m.put("UNIT_ID", unitId);
            buildUnitDao.create(m);
        }
        return "";
    }

    private String modify(Map<String, String> dataMap) throws FileSystemException {
        Long jcAppId = Long.parseLong(dataMap.get("jcAppId"));
        String appId = dataMap.get("appId");
        String description = dataMap.get("description");
        String appBasePath = dataMap.get("appBasePath");
        String qaSourcePath = dataMap.get("qaSourcePath");
        String qaExecutionPath = dataMap.get("qaExecutionPath");
        String prodBackupPath = dataMap.get("prodBackupPath");
        String prodPaths = dataMap.get("prodPaths");
        String pvcsProjDb = dataMap.get("pvcsProjDb");
        String pvcsProjPath = dataMap.get("pvcsProjPath");
        String units = dataMap.get("units");
        List<Map<String, String>> prodPathList = gson.fromJson(prodPaths, new TypeToken<List<Map<String, String>>>() {
        }.getType());
        List<Map<String, String>> unitList = gson.fromJson(units, new TypeToken<List<Map<String, String>>>() {
        }.getType());
        // get all appPaths to determine which appPaths should be deleted
        List<AppPath> appPathToBeDeleted = appPathDao.findByAppId(jcAppId);
        AppPath tmp = new AppPath();
        tmp.setPath(appBasePath);
        appPathToBeDeleted.remove(tmp);
        tmp.setPath(qaSourcePath);
        appPathToBeDeleted.remove(tmp);
        tmp.setPath(qaExecutionPath);
        appPathToBeDeleted.remove(tmp);
        tmp.setPath(prodBackupPath);
        appPathToBeDeleted.remove(tmp);
        // insert appPaths that are not exist
        List<String> prodSourcePaths = new ArrayList<String>();
        List<String> prodExecutionPaths = new ArrayList<String>();
        for (Map<String, String> prodPathData : prodPathList) {
            String sourcePath = prodPathData.get("sourcePath");
            prodSourcePaths.add(sourcePath);
            tmp.setPath(sourcePath);
            appPathToBeDeleted.remove(tmp);
            String executionPath = prodPathData.get("executionPath");
            prodExecutionPaths.add(executionPath);
            tmp.setPath(executionPath);
            appPathToBeDeleted.remove(tmp);
        }
        // check relation before update db
        if (appPathToBeDeleted.size() > 0) {
            for (AppPath appPath : appPathToBeDeleted) {
                String path = appPath.getPath();
                if (FileUtil.exist(path, null)) {
                    if (FileUtil.listFiles(path) != null && FileUtil.listFiles(path).size() > 0) {
                        throw new IllegalStateException("Cannot remove prod path, because there are files in it.");
                    }
                }
            }
        }
        appPathDao.delete(appPathToBeDeleted);
        insertAppPaths(jcAppId, appBasePath, qaSourcePath, qaExecutionPath, prodBackupPath, prodSourcePaths,
                prodExecutionPaths);
        // get all units to determine which units should be deleted
        List<BuildUnit> unitsToBeDeleted = buildUnitDao.findByAppId(jcAppId);
        // insert units that are not exist
        for (Map<String, String> unitData : unitList) {
            String unitId = unitData.get("unitId");
            BuildUnit buildUnit = buildUnitDao.findUnique(appId, unitId);
            if (buildUnit == null) {
                // insert unit
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("JC_APP_ID", jcAppId);
                m.put("UNIT_ID", unitId);
                buildUnitDao.create(m);
            } else {
                unitsToBeDeleted.remove(buildUnit);
            }
        }
        // check relation before update db
        if (unitsToBeDeleted.size() > 0) {
            List<Long> buildUnitIds = new ArrayList<Long>();
            for (BuildUnit unit : unitsToBeDeleted) {
                buildUnitIds.add(unit.getId());
            }
            List<ScrFile> scrFiles = scrFileDao.findByBuildUnitIds(buildUnitIds);
            List<AppFile> appFiles = appFileDao.findByBuildUnitIds(buildUnitIds);
            if ((scrFiles != null && scrFiles.size() > 0) || (appFiles != null && appFiles.size() > 0)) {
                throw new IllegalStateException("Cannot remove build unit that already be associated with files.");
            }
        }
        buildUnitDao.delete(unitsToBeDeleted);
        // update app
        Map<String, Object> app = new HashMap<String, Object>();
        app.put("id", jcAppId);
        app.put("appId", appId);
        app.put("description", description);
        app.put("pvcsProjDb", pvcsProjDb);
        app.put("pvcsProjPath", pvcsProjPath);
        appDao.update(app);
        return "";
    }

    private String delete(Map dataMap) {
        String sId = (String) dataMap.get("id");
        if (!StringUtils.hasText(sId)) {
            throw new IllegalArgumentException("Cannot find parameter: appId");
        }
        if (CollectionUtils.isEmpty(appFileDao.findByAppId(Long.parseLong(sId)))) {
            appDao.delete(dataMap);
        } else {
            throw new IllegalStateException("Cannot delete application that already be associated with files.");
        }
        return "";
    }

    private Map<String, Object> createAppPathMap(Long appId, PathType pathType, String path) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("JC_APP_ID", appId);
        m.put("PATH_TYPE", pathType.ordinal());
        m.put("PATH", path);
        return m;
    }

    private void insertAppPaths(Long appId, String appBasePath, String qaSourcePath, String qaExecutionPath,
            String prodBackupPath, List<String> prodSourcePaths, List<String> prodExecutionPaths)
            throws FileSystemException {
        if (appPathDao.findUnique(appBasePath) == null) {
            appPathDao.create(createAppPathMap(appId, PathType.APP_BASE, appBasePath));
            FileUtil.createFolder(appBasePath);
        }
        if (appPathDao.findUnique(qaSourcePath) == null) {
            appPathDao.create(createAppPathMap(appId, PathType.QA_SOURCE, qaSourcePath));
            FileUtil.createFolder(qaSourcePath);
        }
        if (appPathDao.findUnique(qaExecutionPath) == null) {
            appPathDao.create(createAppPathMap(appId, PathType.QA_EXECUTION, qaExecutionPath));
            FileUtil.createFolder(qaExecutionPath);
        }
        if (appPathDao.findUnique(prodBackupPath) == null) {
            appPathDao.create(createAppPathMap(appId, PathType.PROD_BACKUP, prodBackupPath));
            FileUtil.createFolder(prodBackupPath);
        }
        for (String prodSourcePath : prodSourcePaths) {
            if (appPathDao.findUnique(prodSourcePath) == null) {
                appPathDao.create(createAppPathMap(appId, PathType.PROD_SOURCE, prodSourcePath));
                FileUtil.createFolder(prodSourcePath);
            }
        }
        for (String prodExecutionPath : prodExecutionPaths) {
            if (appPathDao.findUnique(prodExecutionPath) == null) {
                appPathDao.create(createAppPathMap(appId, PathType.PROD_EXECUTION, prodExecutionPath));
                FileUtil.createFolder(prodExecutionPath);
            }
        }
    }
}
