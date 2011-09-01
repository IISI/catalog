package tw.com.citi.catalog.dao;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.model.AppFunction;
import tw.com.citi.catalog.model.User;

public interface IAppFunctionDao extends IGenericDao<AppFunction, Long> {

    void update(Map<String, Object> params);

    void delete(Map<String, Object> params);
    
    AppFunction findUnique(String name, String team);

    List<AppFunction> findUserFunctions(User user);

}
