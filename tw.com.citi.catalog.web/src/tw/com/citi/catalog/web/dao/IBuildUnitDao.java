package tw.com.citi.catalog.web.dao;

import java.util.List;

import tw.com.citi.catalog.web.model.BuildUnit;

public interface IBuildUnitDao extends IGenericDao<BuildUnit, Long> {

    List<BuildUnit> findByAppId(Long appId);

    boolean isBuildUnitExist(String appId, String unitId);

    BuildUnit findUnique(String appId, String unitId);
    
    void delete(List<BuildUnit> buildUnits);
}
