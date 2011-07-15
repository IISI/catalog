package tw.com.citi.catalog.web.pages;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.com.citi.catalog.web.grid.IGridHandler;
import tw.com.iisi.aquarius.web.AbstractAquariusPage;

public abstract class AbstractBasePage extends AbstractAquariusPage {

    final Logger logger = LoggerFactory.getLogger(AbstractBasePage.class);

    @Override
    public final String handleAjaxRequest(PageParameters params) {
        try {
            if (params.containsKey("_gridHandler")) {
                return getGridHandler(params.getString("_gridHandler")).handle(params);
            } else {
                return handleRequest(params);
            }
        } catch (Exception e) {
            logger.error("Internal server error.", e);
            ((WebResponse) RequestCycle.get().getResponse()).getHttpServletResponse().setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return "{\"type\":\"" + e.getClass().getName() + "\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    public IGridHandler getGridHandler(String handlerName) {
        return null;
    }

    public String handleRequest(PageParameters params) {
        return null;
    }

}
