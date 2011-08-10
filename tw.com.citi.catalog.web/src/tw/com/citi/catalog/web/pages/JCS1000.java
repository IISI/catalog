package tw.com.citi.catalog.web.pages;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.StringUtils;

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
import tw.com.citi.catalog.web.util.AccessControlUtil;

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
        String programmer = (String) dataMap.get("jc_programmer_id");
        String coordinator = (String) dataMap.get("jc_coordinator_id");
        // check
        if (!StringUtils.hasText(scrNo)) {
            throw new IllegalArgumentException("Please select a SCR No.");
        }
        if (!StringUtils.hasText(jcAppId)) {
            throw new IllegalArgumentException("Please select a Application Id.");
        }
        if (!StringUtils.hasText(coordinator)) {
            throw new IllegalArgumentException("Please select a Coordinator.");
        }
        if (!StringUtils.hasText(programmer)) {
            throw new IllegalArgumentException("Please select a Programmer.");
        }
        // unique check
        if (scrDao.findByScrNo(scrNo) != null) {
            throw new IllegalStateException("SCR No. : " + scrNo + " is already exist.");
        }
        // put created value
        Date now = new Date();
        dataMap.put("create_time", now);
        dataMap.put("status", Status.CREATE.ordinal());
        dataMap.put("process_time", now);
        dataMap.put("register_count", 0);
        dataMap.put("deleted", 0);
        Long id = scrDao.create(dataMap);
        Scr scr = scrDao.findById(id);
        return gson.toJson(scr);
    }

    private String update(Map dataMap) {
        if (!AccessControlUtil.authenticateCBCUser(dataMap)) {
            throw new IllegalArgumentException("Authentication fail");
        }
        String jcAppId = (String) dataMap.get("jc_app_id");
        String scrNo = (String) dataMap.get("scr_no");
        String programmer = (String) dataMap.get("jc_programmer_id");
        String coordinator = (String) dataMap.get("jc_coordinator_id");
        // check
        if (!StringUtils.hasText(scrNo)) {
            throw new IllegalArgumentException("Please select a SCR No.");
        }
        if (!StringUtils.hasText(jcAppId)) {
            throw new IllegalArgumentException("Please select a Application Id.");
        }
        if (!StringUtils.hasText(coordinator)) {
            throw new IllegalArgumentException("Please select a Coordinator.");
        }
        if (!StringUtils.hasText(programmer)) {
            throw new IllegalArgumentException("Please select a Programmer.");
        }
        // SCR Status 為 CREATE 時才能改 app。
        String id = (String) dataMap.get("id");
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("Cannot find parameter: id");
        }
        Long scrId = Long.parseLong(id);
        Scr scr = scrDao.findById(scrId);
        if (scr != null && Status.CREATE != scr.getStatus() && scr.getJcAppId() != Long.parseLong(jcAppId)) {
            throw new IllegalStateException("SCR's APP can be modified only when STATUS is CREATE.");
        }
        scrDao.updateBasicInfo(dataMap);
        return "";
    }

    private String delete(Map dataMap) {
        if (!AccessControlUtil.authenticateCBCUser(dataMap)) {
            throw new IllegalArgumentException("Authentication fail");
        }
        // check
        String id = (String) dataMap.get("id");
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("Cannot find parameter: id");
        }
        Long scrId = Long.parseLong(id);
        Scr scr = scrDao.findById(scrId);
        // SCR Status 為 CREATE or MOVE_TO_PROD 時才能刪除。
        if (scr != null && (Status.MOVE_TO_PROD == scr.getStatus() || Status.CREATE == scr.getStatus())) {
            scrDao.delete(dataMap);
        } else {
            throw new IllegalStateException("Selected SCR cannot be deleted. SCR Status is "
                    + scr.getStatus().toString() + ".");
        }
        return "";
    }

    private String init(Map dataMap) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("deleted", "0");
        List<Scr> scrList = scrDao.find(params, new String[] { "equal" }, "", "SCR_NO", 0, Long.MAX_VALUE);
        List<App> appList = appDao.findAll();
        List<Coordinator> coordinatorList = coordinatorDao.findAll();
        List<Programmer> programmerList = programmerDao.findAll();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("scrList", scrList);
        data.put("appList", appList);
        data.put("coordinatorList", coordinatorList);
        data.put("programmerList", programmerList);
        return gson.toJson(data);
    }

    private String query(Map dataMap) {
        String sId = (String) dataMap.get("id");
        if (sId == null || "".equals(sId.trim())) {
            throw new IllegalArgumentException("Please select a SCR No.");
        }
        Scr scr = scrDao.findById(Long.parseLong((String) dataMap.get("id")));
        return gson.toJson(scr);
    }

}
