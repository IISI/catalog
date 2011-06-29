package tw.com.citi.catalog.web.pages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import tw.com.citi.catalog.web.dao.IAppDao;
import tw.com.citi.catalog.web.dao.IBuildUnitDao;
import tw.com.citi.catalog.web.dao.IBuildUnitPathDao;
import tw.com.citi.catalog.web.grid.IGridHandler;
import tw.com.citi.catalog.web.model.BuildUnit;
import tw.com.citi.catalog.web.model.BuildUnitPath.PathType;
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

    @SpringBean(name = "buildUnitDao")
    private IBuildUnitDao buildUnitDao;

    @SpringBean(name = "buildUnitPathDao")
    private IBuildUnitPathDao buildUnitPathDao;

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
        String unitId = dataMap.get("unitId");
        String basePath = dataMap.get("basePath");
        if (unitId == null || "".equals(unitId.trim())) {
            throw new IllegalArgumentException("unitId");
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
        String pvcsProjDb = dataMap.get("pvcsProjDb");
        String pvcsProjPath = dataMap.get("pvcsProjPath");
        String units = dataMap.get("units");
        List<Map<String, String>> unitList = gson.fromJson(units, new TypeToken<List<Map<String, String>>>() {
        }.getType());
        // insert app
        Map<String, Object> app = new HashMap<String, Object>();
        app.put("app_id", appId);
        app.put("description", description);
        app.put("app_base_path", appBasePath);
        app.put("pvcs_proj_db", pvcsProjDb);
        app.put("pvcs_proj_path", pvcsProjPath);
        Long jcAppId = appDao.create(app);
        // insert buildunit & buildunitpath
        for (Map<String, String> unitData : unitList) {
            String unitId = unitData.get("unitId");
            String qaSourcePath = unitData.get("qaSourcePath");
            String qaExecutionPath = unitData.get("qaExecutionPath");
            String prodBackupPath = unitData.get("prodBackupPath");
            String[] prodSourcePaths = unitData.get("prodSourcePath").split(",");
            String[] prodExecutionPaths = unitData.get("prodExecutionPath").split(",");
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("JC_APP_ID", jcAppId);
            m.put("UNIT_ID", unitId);
            Long buildUnitId = buildUnitDao.create(m);
            insertUnitPaths(buildUnitId, qaSourcePath, qaExecutionPath, prodBackupPath, prodSourcePaths,
                    prodExecutionPaths);
        }
        return "";
    }

    private String modify(Map<String, String> dataMap) {
        Long jcAppId = Long.parseLong(dataMap.get("jcAppId"));
        String appId = dataMap.get("appId");
        String description = dataMap.get("description");
        String appBasePath = dataMap.get("appBasePath");
        String pvcsProjDb = dataMap.get("pvcsProjDb");
        String pvcsProjPath = dataMap.get("pvcsProjPath");
        String units = dataMap.get("units");
        List<Map<String, String>> unitList = gson.fromJson(units, new TypeToken<List<Map<String, String>>>() {
        }.getType());
        // insert/update unit/unitpath
        for (Map<String, String> unitData : unitList) {
            String unitId = unitData.get("unitId");
            String qaSourcePath = unitData.get("qaSourcePath");
            String qaExecutionPath = unitData.get("qaExecutionPath");
            String prodBackupPath = unitData.get("prodBackupPath");
            String[] prodSourcePaths = unitData.get("prodSourcePath").split(",");
            String[] prodExecutionPaths = unitData.get("prodExecutionPath").split(",");
            BuildUnit buildUnit = buildUnitDao.findUnique(appId, unitId);
            if (buildUnit != null) {
                // delete all unitPath related to unitId
                buildUnitPathDao.deleteAllPathByUnitId(buildUnit.getId());
                // insert unitPath
                insertUnitPaths(buildUnit.getId(), qaSourcePath, qaExecutionPath, prodBackupPath, prodSourcePaths,
                        prodExecutionPaths);
            } else {
                // insert unit
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("JC_APP_ID", jcAppId);
                m.put("UNIT_ID", unitId);
                Long buildUnitId = buildUnitDao.create(m);
                // insert unitPath
                insertUnitPaths(buildUnitId, qaSourcePath, qaExecutionPath, prodBackupPath, prodSourcePaths,
                        prodExecutionPaths);
            }
        }
        // update app
        Map<String, Object> app = new HashMap<String, Object>();
        app.put("id", jcAppId);
        app.put("appId", appId);
        app.put("description", description);
        app.put("appBasePath", appBasePath);
        app.put("pvcsProjDb", pvcsProjDb);
        app.put("pvcsProjPath", pvcsProjPath);
        appDao.update(app);
        return "";
    }

    private String delete(Map dataMap) {
        appDao.delete(dataMap);
        return "";
    }

    private Map<String, Object> createBuildUnitPathMap(Long buildUnitId, PathType pathType, String path) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("JC_BUILD_UNIT_ID", buildUnitId);
        m.put("PATH_TYPE", pathType.ordinal());
        m.put("PATH", path);
        return m;
    }

    private void insertUnitPaths(Long buildUnitId, String qaSourcePath, String qaExecutionPath, String prodBackupPath,
            String[] prodSourcePaths, String[] prodExecutionPaths) {
        buildUnitPathDao.create(createBuildUnitPathMap(buildUnitId, PathType.QA_SOURCE, qaSourcePath));
        buildUnitPathDao.create(createBuildUnitPathMap(buildUnitId, PathType.QA_EXECUTION, qaExecutionPath));
        buildUnitPathDao.create(createBuildUnitPathMap(buildUnitId, PathType.PROD_BACKUP, prodBackupPath));
        for (String prodSourcePath : prodSourcePaths) {
            buildUnitPathDao.create(createBuildUnitPathMap(buildUnitId, PathType.PROD_SOURCE, prodSourcePath));
        }
        for (String prodExecutionPath : prodExecutionPaths) {
            buildUnitPathDao.create(createBuildUnitPathMap(buildUnitId, PathType.PROD_EXECUTION, prodExecutionPath));
        }
    }
}
