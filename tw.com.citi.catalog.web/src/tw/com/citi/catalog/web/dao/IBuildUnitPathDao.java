package tw.com.citi.catalog.web.dao;

import java.util.List;

import tw.com.citi.catalog.web.model.BuildUnitPath;

public interface IBuildUnitPathDao extends IGenericDao<BuildUnitPath, Long> {

    List<BuildUnitPath> findByBuildUnitId(Long buildUnitId);
    
    void deleteAllPathByUnitId(Long buildUnitId);
}
