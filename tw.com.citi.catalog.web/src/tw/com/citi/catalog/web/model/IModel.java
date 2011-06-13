package tw.com.citi.catalog.web.model;

import java.io.Serializable;

public interface IModel<ID extends Serializable> extends Serializable {

    ID getId();

    void setId(ID id);

}
