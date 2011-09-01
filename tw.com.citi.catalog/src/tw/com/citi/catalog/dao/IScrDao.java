package tw.com.citi.catalog.dao;

import java.util.Map;

import tw.com.citi.catalog.model.Scr;
import tw.com.citi.catalog.model.Scr.Status;

public interface IScrDao extends IGenericDao<Scr, Long> {

    void updateBasicInfo(Map<String, Object> params);

    /**
     * 將 SCR 的 DELETED 欄位設為 1
     * 
     * @param params
     */
    void delete(Map<String, Object> params);

    long countByProgrammerId(Long programmerId);

    long countByCoordinatorId(Long coordinatorId);

    Scr findByScrNo(String scrNo);

    /**
     * 更新 SCR 狀態。
     * 
     * @param jcScrId
     * @param status
     */
    void updateStatus(Long jcScrId, Status status);

    int update1100(Map<String, Object> params);

    Map<String, Object> queryScrInfo(long jcScrId);

}
