package tw.com.citi.catalog.web.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import tw.com.citi.catalog.web.model.Scr;
import tw.com.citi.catalog.web.model.Scr.Status;

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

    @Override
    public Scr findByScrNo(String scrNo) {
        return (Scr) jdbcTemplate.queryForObject("SELECT * FROM " + getTableName() + " WHERE SCR_NO = ?",
                getRowMapper(), scrNo);
    }

    @Override
    public void updateStatus(Long jcScrId, Status status) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("jcScrId", jcScrId);
        params.put("status", status.ordinal());
        params.put("processTime", new Date());
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName())
                .append(" SET STATUS=:status, PROCESS_TIME=:processTime WHERE ID=:jcScrId");
        super.jdbcTemplate.update(sql.toString(), params);
    }
}
