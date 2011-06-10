package tw.com.citi.catalog.web.pages;

import org.apache.wicket.PageParameters;

import tw.com.citi.catalog.web.grid.IGridHandler;
import tw.com.iisi.aquarius.web.AbstractAquariusPage;

public abstract class AbstractBasePage extends AbstractAquariusPage {

    @Override
    public final String handleAjaxRequest(PageParameters params) {
        if (params.containsKey("_gridHandler")) {
            return getGridHandler(params.getString("_gridHandler")).handle(params);
        } else {
            return handleRequest(params);
        }
    }

    public IGridHandler getGridHandler(String handlerName) {
        return null;
    }

    public String handleRequest(PageParameters params) {
        return null;
    }

}
