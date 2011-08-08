package tw.com.citi.catalog.web.pages;

import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.util.StringUtils;

import tw.com.citi.catalog.web.dao.ICoordinatorDao;
import tw.com.citi.catalog.web.dao.IScrDao;
import tw.com.citi.catalog.web.grid.IGridHandler;
import tw.com.citi.catalog.web.model.Coordinator;
import tw.com.citi.catalog.web.util.AccessControlUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JCS5400 extends AbstractBasePage {

    @SpringBean(name = "coordinatorGrid")
    private IGridHandler coordinatorGrid;

    @SpringBean(name = "coordinatorDao")
    private ICoordinatorDao coordinatorDao;

    @SpringBean(name = "scrDao")
    private IScrDao scrDao;

    private transient Gson gson = new Gson();

    @Override
    public IGridHandler getGridHandler(String handlerName) {
        if ("coordinatorGrid".equals(handlerName)) {
            return coordinatorGrid;
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
        Coordinator coordinator = coordinatorDao.findUnique(name, team);
        if (AccessControlUtil.authenticateCBCUser(dataMap) && doCheck(name, team)) {
            if ("Create".equals(actionName)) {
                if (coordinator != null) {
                    throw new IllegalStateException("Coordinator with given name and team is already exist.");
                }
                coordinatorDao.create(dataMap);
            } else if ("Modify".equals(actionName)) {
                String id = (String) dataMap.get("id");
                if (coordinator != null && !id.equals(coordinator.getId())) {
                    throw new IllegalStateException("Coordinator with given name and team is already exist.");
                }
                coordinatorDao.update(dataMap);
            } else if ("Delete".equals(actionName)) {
                if (scrDao.countByProgrammerId(coordinator.getId()) > 0) {
                    throw new IllegalStateException("Cannot delete Coordinator that related to exist SCR.");
                }
                coordinatorDao.delete(dataMap);
            }
        } else {
            throw new IllegalArgumentException("Authentication fail");
        }
        return null;
    }

    private boolean doCheck(String name, String team) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(team)) {
            throw new IllegalArgumentException("Cannot find Programmer Name or Team.");
        }
        return true;
    }

}
