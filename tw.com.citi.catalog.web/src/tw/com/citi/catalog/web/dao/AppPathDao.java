package tw.com.citi.catalog.web.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;

import tw.com.citi.catalog.web.model.AppPath;
import tw.com.citi.catalog.web.model.AppPath.PathType;

public class AppPathDao extends AbstractGenericDao<AppPath, Long> implements IAppPathDao {

    public List<AppPath> findByAppId(Long appId) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("appId", appId);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE JC_APP_ID = :appId");
        return super.jdbcTemplate.query(sql.toString(), getRowMapper(), args);
    }

    @Override
    public List<AppPath> findByScrId(Long scrId, Long buildUnitId, PathType... pathTypes) {
        RowMapper<AppPath> rowMapper = ParameterizedBeanPropertyRowMapper.newInstance(AppPath.class);
        Map<String, Object> args = new HashMap<String, Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(getTableName());
        if (buildUnitId == null) {
            args.put("scrId", scrId);
            sql.append(" WHERE JC_BUILD_UNIT_ID IN (SELECT ID FROM JC_BUILD_UNIT WHERE JC_APP_ID = (SELECT JC_APP_ID FROM JC_SCR WHERE ID = :scrId))");
        } else {
            args.put("buildUnitId", buildUnitId);
            sql.append(" WHERE JC_BUILD_UNIT_ID = :buildUnitId");
        }
        if (pathTypes != null) {
            for (int i = 0; i < pathTypes.length; i++) {
                if (i == 0) {
                    sql.append(" AND PATH_TYPE IN (");
                }
                sql.append(pathTypes[i].ordinal());
                if (i == pathTypes.length - 1) {
                    sql.append(")");
                } else {
                    sql.append(", ");
                }
            }
        }
        return super.jdbcTemplate.query(sql.toString(), getRowMapper(), args);
    }

    @Override
    public AppPath findUnique(String path) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE PATH=:path");
        Map<String, String> params = new HashMap<String, String>();
        params.put("path", path);
        List<AppPath> results = super.jdbcTemplate.query(sql.toString(), getRowMapper(), params);
        return (results != null && results.size() > 0) ? results.get(0) : null;
    }

    @Override
    public void delete(List<AppPath> appPaths) {
        for (AppPath appPath : appPaths) {
            StringBuilder sql = new StringBuilder();
            Map params = new HashMap();
            params.put("id", appPath.getId());
            sql.append("DELETE FROM ").append(getTableName()).append(" WHERE ID=:id");
            super.jdbcTemplate.update(sql.toString(), params);
        }
    }

    @Override
    public Map<PathType, String> getAppPathsByAppId(Long appId) {
        // TODO Auto-generated method stub
        List<AppPath> appPaths = findByAppId(appId);
        Map<PathType, String> results = new HashMap<PathType, String>();
        for (AppPath appPath : appPaths) {
            results.put(appPath.getPathType(), appPath.getPath());
        }
        return results;
    }

}
