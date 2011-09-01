package tw.com.citi.catalog.dao;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.model.RegisterHistory;

public interface IRegisterHistoryDao extends IGenericDao<RegisterHistory, Long> {

	<T> List<T> query(String sqlCode, RowMapper<T> rowMapper,
			Map<String, ?> parameters);
	
	<T> List<T> query(String sqlCode, Class<T> requiredType,
			Map<String, ?> parameters);
	
}
