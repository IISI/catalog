package tw.com.citi.catalog.web.pages;

import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.StringUtils;

import tw.com.citi.catalog.web.dao.IProgrammerDao;
import tw.com.citi.catalog.web.dao.IScrDao;
import tw.com.citi.catalog.web.grid.IGridHandler;
import tw.com.citi.catalog.web.model.Programmer;
import tw.com.citi.catalog.web.utils.AccessControlUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JCS5300 extends AbstractBasePage {

    @SpringBean(name = "programmerGrid")
    private IGridHandler programmerGrid;

    @SpringBean(name = "programmerDao")
    private IProgrammerDao programmerDao;

    @SpringBean(name = "scrDao")
    private IScrDao scrDao;

    private transient Gson gson = new Gson();

    @Override
    public IGridHandler getGridHandler(String handlerName) {
        if ("programmerGrid".equals(handlerName)) {
            return programmerGrid;
        }
        return null;
    }

    @Override
    public String handleRequest(PageParameters params) {
        String actionName = params.getString("actionName");
        String actionParams = params.getString("actionParams");
        Map dataMap = gson.fromJson(actionParams, new TypeToken<Map<String, String>>() {
        }.getType());
        String name = (String) dataMap.get("name");
        String team = (String) dataMap.get("team");
        Programmer programmer = programmerDao.findUnique(name, team);
        if (AccessControlUtil.authenticateCBCUser(dataMap) && doCheck(name, team)) {
            if ("Create".equals(actionName)) {
                if (programmer != null) {
                    throw new IllegalStateException("Programmer with given name and team is already exist.");
                }
                programmerDao.create(dataMap);
            } else if ("Modify".equals(actionName)) {
                if (programmer != null) {
                    throw new IllegalStateException("Programmer with given name and team is already exist.");
                }
                programmerDao.update(dataMap);
            } else if ("Delete".equals(actionName)) {
                if (scrDao.countByProgrammerId(programmer.getId()) > 0) {
                    throw new IllegalStateException("Cannot delete Programmer that related to exist SCR.");
                }
                programmerDao.delete(dataMap);
            }
        } else {
            throw new IllegalArgumentException("Authentication fail");
        }
        return super.handleRequest(params);
    }

    private boolean doCheck(String name, String team) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(team)) {
            throw new IllegalArgumentException("Cannot find Programmer Name or Team.");
        }
        return true;
    }

}
