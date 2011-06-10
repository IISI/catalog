package tw.com.citi.catalog.web.dao;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public abstract class AbstractGenericDao implements IGenericDao {

    protected SimpleJdbcTemplate jdbcTemplate;

    @Override
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new SimpleJdbcTemplate(dataSource);
    }

    protected String getQueryString(Map<String, String> params, String[] operators) {
        StringBuilder str = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.trim().length() > 0 && value.trim().replaceAll("%", "").length() > 0) {
                str.append(key);
                if ("like".equalsIgnoreCase(operators[i])) {
                    str.append(" LIKE :");
                } else {
                    str.append(" = :");
                }
                str.append(key);
                str.append(" AND ");
            }
            i++;
        }
        if (str.lastIndexOf("AND") != -1) {
            str.delete(str.lastIndexOf("AND"), str.length());
        }
        return str.toString();
    }

}
