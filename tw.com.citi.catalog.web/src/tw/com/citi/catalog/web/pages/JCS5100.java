package tw.com.citi.catalog.web.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import tw.com.citi.catalog.web.dao.IAppDao;
import tw.com.citi.catalog.web.dao.IAppPathDao;
import tw.com.citi.catalog.web.dao.IBuildUnitDao;
import tw.com.citi.catalog.web.grid.IGridHandler;
import tw.com.citi.catalog.web.model.AppPath;
import tw.com.citi.catalog.web.model.AppPath.PathType;
import tw.com.citi.catalog.web.model.BuildUnit;
import tw.com.citi.catalog.web.utils.AccessControlUtil;

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
            results = add(dataMap);
        } else if ("Modify".equalsIgnoreCase(actionName) && AccessControlUtil.authenticateCBCUser(dataMap)) {
            results = modify(dataMap);
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
            throw new IllegalArgumentException("appId");
        }
        if (basePath == null || "".equals(basePath.trim())) {
            throw new IllegalArgumentException("Please input base path.");
        }
        // TODO check if path is accessible.
        return "";
    }

    private String add(Map<String, String> dataMap) {
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
            Long buildUnitId = buildUnitDao.create(m);
        }
        return "";
    }

    private String modify(Map<String, String> dataMap) {
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
        appPathToBeDeleted.remove(appBasePath);
        appPathToBeDeleted.remove(qaSourcePath);
        appPathToBeDeleted.remove(qaExecutionPath);
        appPathToBeDeleted.remove(prodBackupPath);
        // insert appPaths that are not exist
        List<String> prodSourcePaths = new ArrayList<String>();
        List<String> prodExecutionPaths = new ArrayList<String>();
        for (Map<String, String> prodPathData : prodPathList) {
            String sourcePath = prodPathData.get("sourcePath");
            prodSourcePaths.add(sourcePath);
            appPathToBeDeleted.remove(sourcePath);
            String executionPath = prodPathData.get("executionPath");
            prodExecutionPaths.add(executionPath);
            appPathToBeDeleted.remove(executionPath);
        }
        insertAppPaths(jcAppId, appBasePath, qaSourcePath, qaExecutionPath, prodBackupPath, prodSourcePaths,
                prodExecutionPaths);
        appPathDao.delete(appPathToBeDeleted);
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
        // TODO check
        appDao.delete(dataMap);
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
            String prodBackupPath, List<String> prodSourcePaths, List<String> prodExecutionPaths) {
        if (appPathDao.findUnique(appBasePath) == null) {
            appPathDao.create(createAppPathMap(appId, PathType.APP_BASE, appBasePath));
        }
        if (appPathDao.findUnique(qaSourcePath) == null) {
            appPathDao.create(createAppPathMap(appId, PathType.QA_SOURCE, qaSourcePath));
        }
        if (appPathDao.findUnique(qaExecutionPath) == null) {
            appPathDao.create(createAppPathMap(appId, PathType.QA_EXECUTION, qaExecutionPath));
        }
        if (appPathDao.findUnique(prodBackupPath) == null) {
            appPathDao.create(createAppPathMap(appId, PathType.PROD_BACKUP, prodBackupPath));
        }
        for (String prodSourcePath : prodSourcePaths) {
            if (appPathDao.findUnique(prodSourcePath) == null) {
                appPathDao.create(createAppPathMap(appId, PathType.PROD_SOURCE, prodSourcePath));
            }
        }
        for (String prodExecutionPath : prodExecutionPaths) {
            if (appPathDao.findUnique(prodExecutionPath) == null) {
                appPathDao.create(createAppPathMap(appId, PathType.PROD_EXECUTION, prodExecutionPath));
            }
        }
    }
}
