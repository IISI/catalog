package tw.com.citi.catalog.web.dao;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.web.model.AppFile;

public interface IAppFileDao extends IGenericDao<AppFile, Long> {

    List<AppFile> findByAppId(Long appId);

    List<AppFile> findByBuildUnitIds(List<Long> buildUnitIds);

    AppFile findByUK(long jcAppId, String filePath, String fileName);

    int update1100(Map<String, Object> params);

    int updateLastCompileTimeByJcAppId(Timestamp lastCompileTime, Long jcAppId);

    int updateFileDateTimeById(Timestamp fileDateTime, Long id);

}
