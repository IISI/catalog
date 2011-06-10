package tw.com.citi.catalog.web.dao;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;

import tw.com.citi.catalog.web.model.Programmer;

@Repository
public class ProgrammerDao implements IProgrammerDao {

    private SimpleJdbcTemplate jdbcTemplate;

    @Override
    public long count(Map<String, String> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT count(*) FROM programmer");
        String queryString = getQueryString(params);
        if (queryString.trim().length() > 0) {
            sql.append(" WHERE ");
            sql.append(queryString);
        }
        return jdbcTemplate.queryForLong(sql.toString(), params);
    }

    @Override
    public List<Programmer> find(Map<String, String> params, String index, String order, long start, long limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM programmer");
        String queryString = getQueryString(params);
        if (queryString.trim().length() > 0) {
            sql.append(" WHERE ");
            sql.append(queryString);
        }
        sql.append(" ORDER BY ").append(index).append(" ").append(order);
        RowMapper<Programmer> rowMapper = ParameterizedBeanPropertyRowMapper.newInstance(Programmer.class);
        return jdbcTemplate.query(sql.toString(), rowMapper, params);
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    private String getQueryString(Map<String, String> params) {
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.trim().length() > 0 && value.trim().length() > 0) {
                str.append(key);
                str.append(" = :");
                str.append(key);
                str.append(" AND ");
            }
        }
        if (str.lastIndexOf("AND") != -1) {
            str.delete(str.lastIndexOf("AND"), str.length());
        }
        return str.toString();
    }

}
