package tw.com.citi.catalog.web.dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import tw.com.citi.catalog.web.model.User;

public class UserDao extends AbstractGenericDao<User, Long> implements IUserDao {

	public static final String SORT_INDEX = "sidx";

    public static final String SORT_DIR = "sord";
    
    public static final int SORT_INDEX_LEN = SORT_INDEX.length();

    public static final int SORT_DIR_LEN = SORT_DIR.length();
	
	private SimpleJdbcTemplate jdbcTemplate;

    private TransactionTemplate txTemplate;

    private Properties sqlProps;
	
    /*
    public UserDao(DataSource ds) {
    	jdbcTemplate = new SimpleJdbcTemplate(ds);
        PlatformTransactionManager txManager = new DataSourceTransactionManager(ds);
        txTemplate = new TransactionTemplate(txManager);
    }
    */


	
    @Override
    public void update(Map<String, Object> params) {
        
    	/*
    	String id = (String) params.get("id");
        if (id == null || "".equals(id.trim())) {
            throw new IllegalArgumentException("Cannot find parameter: id");
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(getTableName())
                    .append(" SET NAME=:name, TEAM=:team, PHONE=:phone, MOBILE=:mobile WHERE ID=:id");
            super.jdbcTemplate.update(sql.toString(), params);
        }
        */
    }

    @Override
    public void delete(Map<String, Object> params) {
        
    	/*
    	String id = (String) params.get("id");
        if (id == null || "".equals(id.trim())) {
            throw new IllegalArgumentException("Cannot find parameter: id");
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("DELETE FROM ").append(getTableName()).append(" WHERE ID=:id");
            super.jdbcTemplate.update(sql.toString(), params);
        }
        */
    }

    @Override
    public User findUnique(String name, String team) {
        
    	/*
    	if (!StringUtils.hasText(name) || !StringUtils.hasText(team)) {
            throw new IllegalArgumentException("Cannot find parameter: name or team");
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE NAME=:name AND TEAM=:team");
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("name", name);
            args.put("team", team);
            List<PCAP> list = super.jdbcTemplate.query(sql.toString(), getRowMapper(), args);
            return (list == null || list.size() == 0) ? null : list.get(0);
        }
        */
    	return null;
    }

    @Override
    public <T> List<T> query(String sqlCode, RowMapper<T> rowMapper, Map<String, ?> parameters) {
        return super.jdbcTemplate.query(sqlCode, rowMapper, parameters);
    }
    
    @Override
    public <T> List<T> query(String sqlCode, Class<T> requiredType, Map<String, ?> parameters) {
        return super.jdbcTemplate.query(sqlCode, new BeanPropertyRowMapper<T>(requiredType), parameters);
    }
    
    private String makeSql(String sqlCode, Object sortIndex, Object sortDir) {
        StringBuilder sql = new StringBuilder(sqlProps.getProperty(sqlCode));

        int sortIndexPos = sql.indexOf(":" + SORT_INDEX);
        if (sortIndexPos != -1 && sortIndex != null) {
            sql.replace(sortIndexPos, sortIndexPos + SORT_INDEX_LEN + 1, (String) sortIndex);
        }

        int sortDirPos = sql.indexOf(":" + SORT_DIR);
        if (sortDirPos != -1 && sortDir != null) {
            sql.replace(sortDirPos, sortDirPos + SORT_DIR_LEN + 1, (String) sortDir);
        }

        return sql.toString();
    }
    
	@Override
	public List<User> listAll() {
		// TODO Auto-generated method stub
		StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM USERS");
        Map<String, Object> args = new HashMap<String, Object>();
        List<User> list = super.jdbcTemplate.query(sql.toString(), getRowMapper(), args);
        return list;
	}






	@Override
	public <T> List<T> query(String sqlCode, RowMapper<T> rowMapper,
			Map<String, ?> parameters, boolean isNative) {
		// TODO Auto-generated method stub
		return null;
	}

}
