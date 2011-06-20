package tw.com.citi.catalog.web.dao;

import java.util.Map;

import tw.com.citi.catalog.web.model.Coordinator;

public interface ICoordinatorDao extends IGenericDao<Coordinator, Long> {

    void update(Map<String, Object> params);

    void delete(Map<String, Object> params);

}
