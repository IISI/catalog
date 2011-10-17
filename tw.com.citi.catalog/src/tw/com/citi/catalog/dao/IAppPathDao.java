package tw.com.citi.catalog.dao;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.model.AppPath;
import tw.com.citi.catalog.model.AppPath.PathType;

public interface IAppPathDao extends IGenericDao<AppPath, Long> {

    /**
     * 根據 ScrId 取得相關的 build unit path
     * 
     * @param scrId
     *            JC_SCR_ID
     * @param pathTypes
     *            PATH_TYPE，若為 null 代表取得全部。
     * @return 符合條件的 BuildUnitPath list
     */
    List<AppPath> findByScrId(Long scrId, PathType... pathTypes);

    List<AppPath> findByAppId(Long appId);

    void delete(List<AppPath> appPaths);

    AppPath findUnique(String path);

    Map<PathType, Object> getAppPathsByAppId(Long appId);

    List<AppPath> findByAppName(String appName, AppPath.PathType pathType);

    /**
     * 取得 AppPath 的最後一筆資料，用以檢核 JCS5000 的資料是否正確。
     * 
     * @return
     */
    AppPath findLastestPath();

}
