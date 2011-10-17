package tw.com.citi.catalog.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.model.App;

public class AppDao extends AbstractGenericDao<App, Long> implements IAppDao {

    @Override
    public void update(Map<String, Object> params) {
        Long id = (Long) params.get("id");
        if (id == null) {
            throw new IllegalArgumentException("Cannot find parameter: id");
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ")
                    .append(getTableName())
                    .append(" SET DESCRIPTION=:description,PVCS_PROJ_DB=:pvcsProjDb,PVCS_PROJ_PATH=:pvcsProjPath WHERE ID=:id");
            super.jdbcTemplate.update(sql.toString(), params);
        }
    }

    @Override
    public void delete(Map<String, Object> params) {
        String id = (String) params.get("id");
        if (id == null || "".equals(id.trim())) {
            throw new IllegalArgumentException("Cannot find parameter: id");
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM ").append(getTableName()).append(" WHERE ID=:id");
            super.jdbcTemplate.update(sql.toString(), params);
        }
    }

    @Override
    public App findByAppId(String appId) {
        Map<String, Object> args = new HashMap<String, Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM JC_APP WHERE APP_ID=:appId");
        args.put("appId", appId);
        List<App> results = jdbcTemplate.query(sql.toString(), getRowMapper(), args);
        if (results.size() > 0) {
            return results.get(0);
        } else {
            return null;
        }

    }

}
