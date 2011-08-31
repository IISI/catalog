package tw.com.citi.catalog.web.dao;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.web.model.User;

public interface IUserDao extends IGenericDao<User, Long> {
	
	User findByUserId(String userId);

	List<User> listAll();
	
    void update(Map<String, Object> params);

    void delete(Map<String, Object> params);
    
    User findUnique(String name, String team);
    
    <T> List<T> query(String sqlCode, RowMapper<T> rowMapper,
			Map<String, ?> parameters);
	
	<T> List<T> query(String sqlCode, Class<T> requiredType,
			Map<String, ?> parameters);
	
	<T> List<T> query(String sqlCode, RowMapper<T> rowMapper,
            Map<String, ?> parameters, boolean isNative);

}
