package tw.com.citi.catalog.web.pages;

import org.apache.wicket.spring.injection.annot.SpringBean;

import tw.com.citi.catalog.web.grid.IGridHandler;

public class JCS5300 extends AbstractBasePage {

    @SpringBean(name = "programmerGrid")
    private IGridHandler programmerGrid;

    @Override
    public IGridHandler getGridHandler(String handlerName) {
        if ("programmerGrid".equals(handlerName)) {
            return programmerGrid;
        }
        return null;
    }

}
