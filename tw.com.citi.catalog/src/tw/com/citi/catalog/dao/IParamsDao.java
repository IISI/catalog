package tw.com.citi.catalog.dao;

import java.util.Map;

import tw.com.citi.catalog.model.Params;

public interface IParamsDao extends IGenericDao<Params, Long> {

    Params findByPKey(String pKey);

    int update(Map<String, Object> params);
}
