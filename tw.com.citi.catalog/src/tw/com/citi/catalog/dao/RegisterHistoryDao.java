package tw.com.citi.catalog.dao;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.model.RegisterHistory;

public class RegisterHistoryDao extends
        AbstractGenericDao<RegisterHistory, Long> implements
        IRegisterHistoryDao {
	
	public static final String SORT_INDEX = "sidx";

    public static final String SORT_DIR = "sord";
    
    public static final int SORT_INDEX_LEN = SORT_INDEX.length();

    public static final int SORT_DIR_LEN = SORT_DIR.length();
    
    @Override
    public <T> List<T> query(String sqlCode, RowMapper<T> rowMapper, Map<String, ?> parameters) {
        return super.jdbcTemplate.query(sqlCode, rowMapper, parameters);
    }
    
    @Override
    public <T> List<T> query(String sqlCode, Class<T> requiredType, Map<String, ?> parameters) {
        return super.jdbcTemplate.query(sqlCode, new BeanPropertyRowMapper<T>(requiredType), parameters);
    }
    

}
