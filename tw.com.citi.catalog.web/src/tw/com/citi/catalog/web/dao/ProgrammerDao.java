package tw.com.citi.catalog.web.dao;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import tw.com.citi.catalog.web.model.Programmer;

@Repository
public class ProgrammerDao extends AbstractGenericDao implements IProgrammerDao {

    @Override
    public long count(Map<String, String> params, String[] operators) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT count(*) FROM programmer");
        String queryString = getQueryString(params, operators);
        if (queryString.trim().length() > 0) {
            sql.append(" WHERE ");
            sql.append(queryString);
        }
        return jdbcTemplate.queryForLong(sql.toString(), params);
    }

    @Override
    public List<Programmer> find(Map<String, String> params, String[] operators, String index, String order, long start, long limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM programmer");
        String queryString = getQueryString(params, operators);
        if (queryString.trim().length() > 0) {
            sql.append(" WHERE ");
            sql.append(queryString);
        }
        sql.append(" ORDER BY ").append(index).append(" ").append(order);
        RowMapper<Programmer> rowMapper = ParameterizedBeanPropertyRowMapper.newInstance(Programmer.class);
        return jdbcTemplate.query(sql.toString(), rowMapper, params);
    }

}
