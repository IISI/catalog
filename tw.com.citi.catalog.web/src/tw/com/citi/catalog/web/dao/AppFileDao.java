package tw.com.citi.catalog.web.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.support.DataAccessUtils;
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

    @Override
    public AppFile findByUK(long jcAppId, String filePath, String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(getTableName()).append(" WHERE JC_APP_ID = :jcAppId AND FILE_PATH = :filePath AND FILE_NAME = :fileName");
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("jcAppId", jcAppId);
        args.put("filePath", filePath);
        args.put("fileName", fileName);
        List<AppFile> results = jdbcTemplate.query(sb.toString(), getRowMapper(), args);
        return DataAccessUtils.uniqueResult(results);
    }

    @Override
    public int update1100(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName());
        sql.append(" SET JC_BUILD_UNIT_ID = :JC_BUILD_UNIT_ID,");
        sql.append(" FILE_TYPE = :FILE_TYPE, CHECKOUT = :CHECKOUT,");
        sql.append(" FILE_DATETIME = :FILE_DATETIME,");
        sql.append(" FILE_SIZE = :FILE_SIZE, FILE_MD5 = :FILE_MD5,");
        sql.append(" LAST_REGISTER_TIME = :LAST_REGISTER_TIME,");
        sql.append(" DELETED = :DELETED");
        sql.append(" WHERE ID = :ID");
        return jdbcTemplate.update(sql.toString(), params);
    }

}
