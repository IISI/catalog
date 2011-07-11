package tw.com.citi.catalog.web.dao;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.web.model.AppPath;
import tw.com.citi.catalog.web.model.AppPath.PathType;

public interface IAppPathDao extends IGenericDao<AppPath, Long> {

    /**
     * 根據 ScrId 取得相關的 build unit path
     * 
     * @param scrId
     *            JC_SCR_ID
     * @param buildUnitId
     *            JC_BUILD_UNIT_ID，若為 null 代表取得全部。
     * @param pathTypes
     *            PATH_TYPE，若為 null 代表取得全部。
     * @return 符合條件的 BuildUnitPath list
     */
    List<AppPath> findByScrId(Long scrId, Long buildUnitId,  PathType... pathTypes);

    List<AppPath> findByAppId(Long appId);

    void delete(List<AppPath> appPaths);
    
    AppPath findUnique(String path);
    
    Map<PathType, String> getAppPathsByAppId(Long appId);
}
