package tw.com.citi.catalog.web.pages;

import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import tw.com.citi.catalog.web.dao.IProgrammerDao;
import tw.com.citi.catalog.web.grid.IGridHandler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JCS5300 extends AbstractBasePage {

    @SpringBean(name = "programmerGrid")
    private IGridHandler programmerGrid;

    @SpringBean(name = "programmerDao")
    private IProgrammerDao programmerDao;

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
        Map dataMap = gson.fromJson(actionParams, new TypeToken<Map<String, String>>() {}.getType());
        
        if ("add".equals(actionName)) {
            programmerDao.create(dataMap);
        }
        return super.handleRequest(params);
    }

}
