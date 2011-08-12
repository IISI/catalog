package tw.com.citi.catalog.web.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tw.com.citi.catalog.web.model.FunctionLog;

public class FunctionLogDao extends AbstractGenericDao<FunctionLog, Long> implements IFunctionLogDao {

    @Override
    public void updateEndTime(Long id, Date end) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("endTime", end);
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName()).append(" SET END_TIME=:endTime WHERE ID=:id");
        super.jdbcTemplate.update(sql.toString(), params);
    }

    @Override
    public long count4300GridData(Long jcScrId, Date startDate, Date endDate,
            Set<String> functions) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM JC_FUNCTION_LOG FL ");
        sql.append("WHERE ");
        if (jcScrId != null) {
            sql.append("FL.JC_SCR_ID = :scrId AND ");
        }
        if (startDate != null) {
            sql.append("FL.START_TIME >= :startDate AND ");
        }
        if (endDate != null) {
            sql.append("FL.END_TIME <= :endDate AND ");
        }
        if (!functions.isEmpty()) {
            sql.append("FL.FUNCTION_ID IN (:functionIds) ");
        }
        if (sql.toString().trim().endsWith("AND")) {
            sql.delete(sql.lastIndexOf("AND"), sql.length());
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("scrId", jcScrId);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("functionIds", functions);
        logger.info("count sql = {}", sql.toString());
        return jdbcTemplate.queryForLong(sql.toString(), params);
    }

    @Override
    public List<Map<String, Object>> find4300GridData(Long jcScrId,
            Date startDate, Date endDate, Set<String> functions, String index,
            String order, long start, long limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (SELECT ROW_NUMBER() OVER (ORDER BY INNER_SUB.").append(index).append(" ").append(order).append(") AS ROW_INDEX, INNER_SUB.* FROM ( ");
        sql.append("SELECT FL.ID id, S.ID scrId, S.SCR_NO scrNo, FL.FUNCTION_ID functionId, A.APP_ID app, S.CREATE_TIME createDate, S.PROCESS_TIME processDate ");
        sql.append("FROM JC_FUNCTION_LOG FL ");
        sql.append("    LEFT JOIN JC_SCR S ");
        sql.append("        LEFT JOIN JC_APP A ON S.JC_APP_ID = A.ID ");
        sql.append("    ON FL.JC_SCR_ID = S.ID ");
        sql.append("WHERE ");
        if (jcScrId != null) {
            sql.append("FL.JC_SCR_ID = :scrId AND ");
        }
        if (startDate != null) {
            sql.append("FL.START_TIME >= :startDate AND ");
        }
        if (endDate != null) {
            sql.append("FL.END_TIME <= :endDate AND ");
        }
        if (!functions.isEmpty()) {
            sql.append("FL.FUNCTION_ID IN (:functionIds) ");
        }
        if (sql.toString().trim().endsWith("AND")) {
            sql.delete(sql.lastIndexOf("AND"), sql.length());
        }
        sql.append(") AS INNER_SUB) AS SUB WHERE SUB.ROW_INDEX > ").append(start).append(" AND SUB.ROW_INDEX <= ").append(start + limit);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("scrId", jcScrId);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("functionIds", functions);
        logger.info("find sql = {}", sql.toString());
        return jdbcTemplate.queryForList(sql.toString(), params);
    }

}
