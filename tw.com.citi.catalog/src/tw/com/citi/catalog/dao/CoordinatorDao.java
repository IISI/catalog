package tw.com.citi.catalog.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tw.com.citi.catalog.model.Coordinator;

public class CoordinatorDao extends AbstractGenericDao<Coordinator, Long> implements ICoordinatorDao {

    @Override
    public void update(Map<String, Object> params) {
        String id = (String) params.get("id");
        if (id == null || "".equals(id.trim())) {
            throw new IllegalArgumentException("Cannot find parameter: id");
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(getTableName())
                    .append(" SET NAME=:name, TEAM=:team, PHONE=:phone, MOBILE=:mobile WHERE ID=:id");
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
    public Coordinator findUnique(String name, String team) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(team)) {
            throw new IllegalArgumentException("Cannot find parameter: name or team");
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE NAME=:name AND TEAM=:team");
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("name", name);
            args.put("team", team);
            List<Coordinator> list = super.jdbcTemplate.query(sql.toString(), getRowMapper(), args);
            return (list == null || list.size() == 0) ? null : list.get(0);
        }
    }

}
