package tw.com.citi.catalog.web.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;

import tw.com.citi.catalog.web.annotation.Mapper;
import tw.com.citi.catalog.web.annotation.Table;
import tw.com.citi.catalog.web.model.IModel;

@Repository
public abstract class AbstractGenericDao<T extends IModel<ID>, ID extends Serializable> implements IGenericDao<T, ID> {

    private Class<T> persistenceClass;

    protected SimpleJdbcTemplate jdbcTemplate;

    protected SimpleJdbcInsert jdbcInsert;

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @SuppressWarnings("unchecked")
    public AbstractGenericDao() {
        this.persistenceClass = (Class<T>) ((ParameterizedType) super.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    private String getQueryString(Map<String, String> params, String[] operators) {
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

    @Override
    public long count(Map<String, String> params, String[] operators) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM ").append(getTableName());
        String queryString = getQueryString(params, operators);
        if (queryString.trim().length() > 0) {
            sql.append(" WHERE ");
            sql.append(queryString);
        }
        logger.info("count sql = [{}]", sql.toString());
        return jdbcTemplate.queryForLong(sql.toString(), params);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<T> find(Map<String, String> params, String[] operators,
            String index, String order, long start, long limit) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ");
        sql.append("(SELECT Row_Number() OVER (ORDER BY InnerSub.");
        sql.append(index).append(" ").append(order);
        sql.append(") AS RowIndex, InnerSub.* FROM (SELECT * FROM ");
        sql.append(getTableName());
        String queryString = getQueryString(params, operators);
        if (queryString.trim().length() > 0) {
            sql.append(" WHERE ");
            sql.append(queryString);
        }
        sql.append(") AS InnerSub) AS Sub");
        sql.append(" WHERE Sub.RowIndex > ").append(start);
        sql.append(" AND Sub.RowIndex <= ").append(start + limit);
        RowMapper rowMapper = getRowMapper();
        logger.info("find sql = [{}]", sql.toString());
        return jdbcTemplate.query(sql.toString(), rowMapper, params);
    }

    @Override
    public Long create(Map<String, Object> params) {
        return jdbcInsert.executeAndReturnKey(params).longValue();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<T> findAll() {
        RowMapper rowMapper = getRowMapper();
        return jdbcTemplate.query("SELECT * FROM " + getTableName(), rowMapper);
    }

    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new SimpleJdbcTemplate(dataSource);
        jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName(getTableName()).usingGeneratedKeyColumns("id");
    }

    protected String getTableName() {
        Table table = this.persistenceClass.getAnnotation(Table.class);
        if (table != null) {
            return table.value();
        }
        return this.persistenceClass.getSimpleName();
    }

    @SuppressWarnings("rawtypes")
    private RowMapper getRowMapper() {
        Mapper mapper = this.persistenceClass.getAnnotation(Mapper.class);
        try {
            if (mapper != null) {
                return mapper.value().newInstance();
            }
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return ParameterizedBeanPropertyRowMapper.newInstance(this.persistenceClass);
    }

}
