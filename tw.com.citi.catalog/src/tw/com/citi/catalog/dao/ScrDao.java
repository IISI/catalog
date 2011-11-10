package tw.com.citi.catalog.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.model.Scr;
import tw.com.citi.catalog.model.Scr.Status;

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
            sql.append("UPDATE ").append(getTableName()).append(" SET DELETED=1 WHERE ID=:id");
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
        List<Scr> list = jdbcTemplate.query("SELECT * FROM " + getTableName() + " WHERE SCR_NO = ?", getRowMapper(),
                scrNo);
        return (list == null || list.size() == 0) ? null : list.get(0);
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

    @Override
    public int update1100(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName());
        sql.append(" SET REGISTER_COUNT = :registerCount, STATUS = :status, PROCESS_TIME = :processTime");
        sql.append(" WHERE ID = :id");
        return jdbcTemplate.update(sql.toString(), params);
    }

    @Override
    public Map<String, Object> queryScrInfo(long jcScrId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT A.APP_ID appId, P.NAME programmer, C.NAME coordinator ");
        sql.append("FROM JC_SCR S, JC_APP A, JC_PROGRAMMER P, JC_COORDINATOR C ");
        sql.append("WHERE S.ID = ? AND A.ID = S.JC_APP_ID AND P.ID = S.JC_PROGRAMMER_ID AND C.ID = S.JC_COORDINATOR_ID");
        return jdbcTemplate.queryForMap(sql.toString(), jcScrId);
    }

    @Override
    public List<Scr> findActiveScr() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ");
        sql.append(getTableName());
        sql.append(" WHERE DELETED = 0 AND STATUS <> ").append(Scr.Status.MOVE_TO_PROD.ordinal());
        return jdbcTemplate.query(sql.toString(), getRowMapper());
    }

    @Override
    public List<Scr> findClosedScr() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ");
        sql.append(getTableName());
        sql.append(" WHERE DELETED = 0 AND STATUS = ").append(Scr.Status.MOVE_TO_PROD.ordinal());
        return jdbcTemplate.query(sql.toString(), getRowMapper());
    }

}
