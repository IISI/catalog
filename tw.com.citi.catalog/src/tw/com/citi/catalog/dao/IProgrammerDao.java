package tw.com.citi.catalog.dao;

import java.util.Map;

import tw.com.citi.catalog.model.Programmer;

public interface IProgrammerDao extends IGenericDao<Programmer, Long> {

    void update(Map<String, Object> params);

    void delete(Map<String, Object> params);
    
    Programmer findUnique(String name, String team);

}
