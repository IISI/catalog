package tw.com.citi.catalog.web.pages;

import java.util.List;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import tw.com.citi.catalog.web.dao.IAppDao;
import tw.com.citi.catalog.web.model.App;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JCS5100 extends AbstractBasePage {

    @SpringBean(name = "appDao")
    private IAppDao appDao;

    @Override
    public String handleRequest(PageParameters params) {
        String results;
        String actionName = params.getString("actionName");
        String actionParams = params.getString("actionParams");
        if (actionName == null || "".equals(actionName.trim())) {
            throw new IllegalArgumentException("Cannot find parameter: actionName");
        }
        if ("query".equalsIgnoreCase(actionName)) {
            results = query(actionParams);
        } else if ("addPathCheck".equalsIgnoreCase(actionName)) {
            results = addPathCheck(actionParams);
        } else if ("add".equalsIgnoreCase(actionName)) {
            results = add(actionParams);
        } else if ("modify".equalsIgnoreCase(actionName)) {
            results = modify(actionParams);
        } else if ("delete".equalsIgnoreCase(actionName)) {
            results = delete(actionParams);
        } else {
            throw new IllegalArgumentException("Cannot find actionName: " + actionName);
        }
        return results;
    }

    private String query(String actionParams) {
        Gson gson = new Gson();
        Map<String, String> params = gson.fromJson(actionParams, new TypeToken<Map<String, String>>() {
        }.getType());

        List<App> apps = appDao.find(params, new String[] { "equal", "like" }, "", "APP_ID", 0, 0);
        return gson.toJson(apps, new TypeToken<List<App>>(){}.getType());
    }

    private String addPathCheck(String actionParams) {
        Gson gson = new Gson();
        Map<String, String> actionParam = gson.fromJson(actionParams, new TypeToken<Map<String, String>>() {
        }.getType());
        String unitId = actionParam.get("unitId");
        String basePath = actionParam.get("basePath");
        if (unitId == null || "".equals(unitId.trim())) {
            throw new IllegalArgumentException("unitId");
        }
        if (basePath == null || "".equals(basePath.trim())) {
            throw new IllegalArgumentException("basePath");
        }
        // TODO check if path is accessible.
        return "";
    }

    private String add(String actionParams) {
        return "";
    }

    private String modify(String actionParams) {
        return "";
    }

    private String delete(String actionParams) {
        return "";
    }
}
