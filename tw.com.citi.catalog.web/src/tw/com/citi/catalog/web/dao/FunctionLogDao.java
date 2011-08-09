package tw.com.citi.catalog.web.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

}
