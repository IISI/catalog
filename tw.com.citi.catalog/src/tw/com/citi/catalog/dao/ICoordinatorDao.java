package tw.com.citi.catalog.dao;

import java.util.Map;

import tw.com.citi.catalog.model.Coordinator;

public interface ICoordinatorDao extends IGenericDao<Coordinator, Long> {

    void update(Map<String, Object> params);

    void delete(Map<String, Object> params);
    
    Coordinator findUnique(String name, String team);

}
