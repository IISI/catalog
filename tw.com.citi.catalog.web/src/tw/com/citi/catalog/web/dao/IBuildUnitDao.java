package tw.com.citi.catalog.web.dao;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.web.model.BuildUnit;

public interface IBuildUnitDao extends IGenericDao<BuildUnit, Long> {

    List<BuildUnit> findUnitGridData(Map params);
    
    boolean isBuildUnitExist(String appId, String unitId);
    
    BuildUnit findUnique(String appId, String unitId);
}
