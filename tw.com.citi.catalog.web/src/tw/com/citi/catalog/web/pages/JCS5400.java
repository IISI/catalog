package tw.com.citi.catalog.web.pages;

import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import tw.com.citi.catalog.web.dao.ICoordinatorDao;
import tw.com.citi.catalog.web.grid.IGridHandler;
import tw.com.citi.catalog.web.utils.AccessControlUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JCS5400 extends AbstractBasePage {

    @SpringBean(name = "coordinatorGrid")
    private IGridHandler coordinatorGrid;

    @SpringBean(name = "coordinatorDao")
    private ICoordinatorDao coordinatorDao;

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
        Map dataMap = gson.fromJson(actionParams, new TypeToken<Map<String, String>>() {}.getType());
        if(AccessControlUtil.authenticateCBCUser(dataMap)) {
            if ("Create".equals(actionName)) {
                coordinatorDao.create(dataMap);
            } else if ("Modify".equals(actionName)) {
                coordinatorDao.update(dataMap);
            } else if ("Delete".equals(actionName)) {
                coordinatorDao.delete(dataMap);
            }
        } else {
            throw new IllegalArgumentException("Authentication fail");
        }
        return super.handleRequest(params);
    }

}
