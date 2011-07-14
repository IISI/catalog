package tw.com.citi.catalog.web.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.web.model.Scr;

public class ScrDao extends AbstractGenericDao<Scr, Long> implements IScrDao {

    @Override
    public void updateBasicInfo(Map<String, Object> params) {
        String id = (String) params.get("id");
        if (id == null || "".equals(id.trim())) {
            throw new IllegalArgumentException("Cannot find parameter: id");
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ")
                    .append(getTableName())
                    .append(" SET SCR_NO=:scr_no, JC_APP_ID=:jc_app_id, JC_COORDINATOR_ID=:jc_coordinator_id, JC_PROGRAMMER_ID=:jc_programmer_id, DESCRIPTION=:description WHERE ID=:id");
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
            sql.append("UPDATE FROM ").append(getTableName()).append(" SET DELETED=1 WHERE ID=:id");
            super.jdbcTemplate.update(sql.toString(), params);
        }
    }

    @Override
    public Scr findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Cannot find parameter: id");
        } else {
            RowMapper<Scr> rowMapper = getRowMapper();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", id);
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE ID=:id");
            List<Scr> list = super.jdbcTemplate.query(sql.toString(), rowMapper, params);
            return (list == null || list.size() == 0) ? null : list.get(0);
        }
    }

    @Override
    public long countByProgrammerId(Long programmerId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(getTableName()).append(" WHERE JC_PROGRAMMER_ID=:programmerId");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("programmerId", programmerId);
        return jdbcTemplate.queryForLong(sql.toString(), params);
    }

    @Override
    public long countByCoordinatorId(Long coordinatorId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(getTableName()).append(" WHERE JC_COORDINATOR_ID=:coordinatorId");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("coordinatorId", coordinatorId);
        return jdbcTemplate.queryForLong(sql.toString(), params);
    }
}
