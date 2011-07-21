package tw.com.citi.catalog.web.pages;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import tw.com.citi.catalog.web.dao.IAppDao;
import tw.com.citi.catalog.web.dao.IAppPathDao;
import tw.com.citi.catalog.web.dao.IBuildUnitDao;
import tw.com.citi.catalog.web.dao.IScrDao;
import tw.com.citi.catalog.web.model.App;
import tw.com.citi.catalog.web.model.AppPath.PathType;
import tw.com.citi.catalog.web.model.BuildUnit;
import tw.com.citi.catalog.web.model.Scr;
import tw.com.citi.catalog.web.model.Scr.Status;
import tw.com.citi.catalog.web.util.NetUseUtil;

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
        } else if ("Compile".equals(actionName)) {
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

    private String compile(Map dataMap) {
        // TODO
        String sScrId = (String) dataMap.get("scrId");
        Long scrId = Long.parseLong(sScrId);
        Scr scr = scrDao.findById(scrId);
        scrDao.updateStatus(scr.getId(), Status.COMPILE);
        return null;
    }

    private String getFiles(Map dataMap) {
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
