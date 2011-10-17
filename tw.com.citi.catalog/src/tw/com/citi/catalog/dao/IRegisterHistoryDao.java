package tw.com.citi.catalog.dao;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.model.RegisterHistory;

public interface IRegisterHistoryDao extends IGenericDao<RegisterHistory, Long> {

    <T> List<T> query(String sqlCode, Map<String, ?> parameters);

}
