package tw.com.citi.catalog.web.dao;

import java.util.Map;

import tw.com.citi.catalog.web.model.Scr;

public interface IScrDao extends IGenericDao<Scr, Long> {

    void updateBasicInfo(Map<String, Object> params);

    void delete(Map<String, Object> params);
    
    Scr findById(Long id);

}
