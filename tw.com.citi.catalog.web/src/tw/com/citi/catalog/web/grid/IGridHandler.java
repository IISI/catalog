package tw.com.citi.catalog.web.grid;

import java.io.Serializable;

import org.apache.wicket.PageParameters;

public interface IGridHandler extends Serializable {

    String handle(PageParameters params);

}
