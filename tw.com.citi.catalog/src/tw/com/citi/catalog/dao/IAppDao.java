package tw.com.citi.catalog.dao;

import java.util.Map;

import tw.com.citi.catalog.model.App;

public interface IAppDao extends IGenericDao<App, Long> {

    void update(Map<String, Object> params);

    void delete(Map<String, Object> params);
    
    App findByAppId(String appId);

}
