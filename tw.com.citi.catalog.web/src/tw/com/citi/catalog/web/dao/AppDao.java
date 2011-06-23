package tw.com.citi.catalog.web.dao;

import java.util.Map;

import tw.com.citi.catalog.web.model.App;

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
                    .append(" SET DESCRIPTION=:description,APP_BASE_PATH=:appBasePath,PVCS_PROJ_DB=:pvcsProjDb,PVCS_PROJ_PATH=:pvcsProjPath WHERE ID=:id");
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

}
