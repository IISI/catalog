package tw.com.citi.catalog.dao;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.model.AppFile;

public interface IAppFileDao extends IGenericDao<AppFile, Long> {

    List<AppFile> findByAppId(Long appId);

    List<AppFile> findByBuildUnitIds(List<Long> buildUnitIds);

    AppFile findByUK(long jcAppId, String filePath, String fileName);

    int update1100(Map<String, Object> params);

    int updateLastCompileTimeByJcAppId(Timestamp lastCompileTime, Long jcAppId);

    int updateFileInfoById(Timestamp fileDateTime, Long fileSize, Long id);
    
    int updateCheckOutFlag(Long id);

    /**
     * 查詢 1600, 1700, 1800 報表的資料。
     * 
     * @param functionLogId
     * @return
     */
    List<Map<String, Object>> find1600ReportData(long functionLogId);

    List<Map<String, Object>> find1600BReportData(long functionLogId);

}
