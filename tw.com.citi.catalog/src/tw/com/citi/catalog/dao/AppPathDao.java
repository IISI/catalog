package tw.com.citi.catalog.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;

import tw.com.citi.catalog.model.AppPath;
import tw.com.citi.catalog.model.AppPath.PathType;

public class AppPathDao extends AbstractGenericDao<AppPath, Long> implements IAppPathDao {

    public List<AppPath> findByAppId(Long appId) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("appId", appId);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE JC_APP_ID = :appId");
        return super.jdbcTemplate.query(sql.toString(), getRowMapper(), args);
    }

    @Override
    public List<AppPath> findByScrId(Long scrId, PathType... pathTypes) {
        RowMapper<AppPath> rowMapper = ParameterizedBeanPropertyRowMapper.newInstance(AppPath.class);
        Map<String, Object> args = new HashMap<String, Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(getTableName());
        args.put("scrId", scrId);
        sql.append(" WHERE JC_APP_ID = (SELECT JC_APP_ID FROM JC_SCR WHERE ID = :scrId)");
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
    public Map<PathType, Object> getAppPathsByAppId(Long appId) {
        List<AppPath> appPaths = findByAppId(appId);
        Map<PathType, Object> results = new HashMap<PathType, Object>();
        List<String> prodSourcePaths = new ArrayList<String>();
        List<String> prodExecutionPaths = new ArrayList<String>();
        for (AppPath appPath : appPaths) {
            if (PathType.PROD_SOURCE == appPath.getPathType()) {
                prodSourcePaths.add(appPath.getPath());
            } else if (PathType.PROD_EXECUTION == appPath.getPathType()) {
                prodExecutionPaths.add(appPath.getPath());
            } else {
                results.put(appPath.getPathType(), appPath.getPath());
            }
        }
        results.put(PathType.PROD_SOURCE, prodSourcePaths);
        results.put(PathType.PROD_EXECUTION, prodExecutionPaths);
        return results;
    }

    @Override
    public List<AppPath> findByAppName(String appName, AppPath.PathType pathType) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(getTableName());
        sql.append(" WHERE jc_app_id = (SELECT id FROM JC_APP WHERE app_id = ?) AND path_type = ?");
        return jdbcTemplate.query(sql.toString(), getRowMapper(), appName, pathType.ordinal());
    }

    @Override
    public AppPath findLastestPath() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TOP 1 * FROM ").append(getTableName()).append(" ORDER BY ID DESC");
        List<AppPath> appPaths = jdbcTemplate.query(sql.toString(), getRowMapper());
        if (appPaths != null) {
            return appPaths.get(0);
        } else {
            return null;
        }
    }

}
