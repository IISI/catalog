package tw.com.citi.catalog.web.pages;

import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import tw.com.citi.catalog.dao.IScrDao;
import tw.com.citi.catalog.model.Scr;
import tw.com.citi.catalog.web.grid.IGridHandler;

public class JCS4300 extends AbstractBasePage {

    @SpringBean(name = "scrDao")
    private IScrDao scrDao;

    @SpringBean(name = "reportGrid")
    private IGridHandler reportGrid;

    @Override
    public IGridHandler getGridHandler(String handlerName) {
        if ("reportGrid".equals(handlerName)) {
            return reportGrid;
        }
        return super.getGridHandler(handlerName);
    }

    @Override
    public String handleRequest(PageParameters params) {
        String actionName = params.getString("actionName");
        if ("findScrNo".equalsIgnoreCase(actionName)) {
            return findScrNo();
        }
        return null;
    }

    private String findScrNo() {
        List<Scr> scrList = scrDao.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("<option value='none'>&nbsp;</option>");
        for (Scr scr : scrList) {
            sb.append("<option value='").append(scr.getId()).append("'>");
            sb.append(scr.getScrNo());
            sb.append("</option>");
        }
        return sb.toString();
    }

}
