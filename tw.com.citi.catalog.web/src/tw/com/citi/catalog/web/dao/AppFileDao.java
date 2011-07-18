package tw.com.citi.catalog.web.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.web.model.AppFile;

public class AppFileDao extends AbstractGenericDao<AppFile, Long> implements IAppFileDao {

    @Override
    public List<AppFile> findByBuildUnitIds(List<Long> buildUnitIds) {
        if (buildUnitIds == null || buildUnitIds.size() == 0) {
            throw new IllegalArgumentException("Cannot find parameter: buildUnitId");
        } else {
            RowMapper<AppFile> rowMapper = getRowMapper();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE JC_BUILD_UNIT_ID in (:buildUnitId)");
            List<AppFile> list = super.namedParameterJdbcTemplate.query(sql.toString(),
                    Collections.singletonMap("buildUnitId", buildUnitIds), rowMapper);
            return (list == null || list.size() == 0) ? new ArrayList<AppFile>() : list;
        }
    }

    @Override
    public List<AppFile> findByAppId(Long appId) {
        if (appId == null) {
            throw new IllegalArgumentException("Cannot find parameter: appId");
        } else {
            StringBuilder sql = new StringBuilder();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("appId", appId);
            sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE JC_APP_ID = :appId");
            List<AppFile> list = super.jdbcTemplate.query(sql.toString(), getRowMapper(), params);
            return (list == null || list.size() == 0) ? new ArrayList<AppFile>() : list;
        }
    }
}
