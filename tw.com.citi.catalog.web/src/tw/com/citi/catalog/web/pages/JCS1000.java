package tw.com.citi.catalog.web.pages;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import tw.com.citi.catalog.web.dao.IAppDao;
import tw.com.citi.catalog.web.dao.ICoordinatorDao;
import tw.com.citi.catalog.web.dao.IProgrammerDao;
import tw.com.citi.catalog.web.dao.IScrDao;
import tw.com.citi.catalog.web.grid.IGridHandler;
import tw.com.citi.catalog.web.model.App;
import tw.com.citi.catalog.web.model.Coordinator;
import tw.com.citi.catalog.web.model.Programmer;
import tw.com.citi.catalog.web.model.Scr;
import tw.com.citi.catalog.web.model.Scr.Status;
import tw.com.citi.catalog.web.utils.AccessControlUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JCS1000 extends AbstractBasePage {

    @SpringBean(name = "scrFileGrid")
    private IGridHandler scrFileGrid;

    @SpringBean(name = "appDao")
    private IAppDao appDao;

    @SpringBean(name = "programmerDao")
    private IProgrammerDao programmerDao;

    @SpringBean(name = "coordinatorDao")
    private ICoordinatorDao coordinatorDao;

    @SpringBean(name = "scrDao")
    private IScrDao scrDao;

    private transient Gson gson = new Gson();

    @Override
    public IGridHandler getGridHandler(String handlerName) {
        if ("scrFileGrid".equals(handlerName)) {
            return scrFileGrid;
        }
        return null;
    }

    @Override
    public String handleRequest(PageParameters params) {
        String result;
        String actionName = params.getString("actionName");
        String actionParams = params.getString("actionParams");
        Map dataMap = gson.fromJson(actionParams, new TypeToken<Map<String, String>>() {
        }.getType());
        if ("Create".equals(actionName)) {
            result = create(dataMap);
        } else if ("Modify".equals(actionName)) {
            result = update(dataMap);
        } else if ("Delete".equals(actionName)) {
            result = delete(dataMap);
        } else if ("Init".equals(actionName)) {
            result = init(dataMap);
        } else if ("Query".equals(actionName)) {
            result = query(dataMap);
        } else {
            throw new IllegalArgumentException("Cannot find actionName: " + actionName);
        }
        return result;
    }

    private String create(Map dataMap) {
        if (!AccessControlUtil.authenticateCBCUser(dataMap)) {
            throw new IllegalArgumentException("Authentication fail");
        }
        String jcAppId = (String) dataMap.get("jc_app_id");
        String scrNo = (String) dataMap.get("scr_no");
        String librarian = (String) dataMap.get("librarian");
        String programmer = (String) dataMap.get("jc_programmer_id");
        String coordinator = (String) dataMap.get("jc_coordinator_id");
        String description = (String) dataMap.get("description");
        // TODO check
        // TODO put created value
        Date now = new Date();
        dataMap.put("create_time", now);
        dataMap.put("status", Status.CREATE.ordinal());
        dataMap.put("process_time", now);
        dataMap.put("register_count", 0);
        Long id = scrDao.create(dataMap);
        Scr scr = scrDao.findById(id);
        return gson.toJson(scr);
    }

    private String update(Map dataMap) {
        if (!AccessControlUtil.authenticateCBCUser(dataMap)) {
            throw new IllegalArgumentException("Authentication fail");
        }
        String id = (String) dataMap.get("id");
        String jcAppId = (String) dataMap.get("jc_app_id");
        String scrNo = (String) dataMap.get("scr_no");
        String librarian = (String) dataMap.get("librarian");
        String programmer = (String) dataMap.get("jc_programmer_id");
        String coordinator = (String) dataMap.get("jc_coordinator_id");
        String description = (String) dataMap.get("description");
        // TODO check
        scrDao.updateBasicInfo(dataMap);
        return "";
    }

    private String delete(Map dataMap) {
        if (!AccessControlUtil.authenticateCBCUser(dataMap)) {
            throw new IllegalArgumentException("Authentication fail");
        }
        scrDao.delete(dataMap);
        return "";
    }

    private String init(Map dataMap) {
        List<Scr> scrList = scrDao
                .find(new HashMap<String, String>(), new String[] {}, "", "SCR_NO", 0, Long.MAX_VALUE);
        List<App> appList = appDao
                .find(new HashMap<String, String>(), new String[] {}, "", "APP_ID", 0, Long.MAX_VALUE);
        List<Coordinator> coordinatorList = coordinatorDao.find(new HashMap<String, String>(), new String[] {}, "",
                "NAME", 0, Long.MAX_VALUE);
        List<Programmer> programmerList = programmerDao.find(new HashMap<String, String>(), new String[] {}, "",
                "NAME", 0, Long.MAX_VALUE);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("scrList", scrList);
        data.put("appList", appList);
        data.put("coordinatorList", coordinatorList);
        data.put("programmerList", programmerList);
        return gson.toJson(data);
    }

    private String query(Map dataMap) {
        String sId = (String) dataMap.get("id");
        if(sId == null || "".equals(sId.trim())) {
            throw new IllegalArgumentException("Please select a SCR No.");
        }
        Scr scr = scrDao.findById(Long.parseLong((String) dataMap.get("id")));
        return gson.toJson(scr);
    }

}
