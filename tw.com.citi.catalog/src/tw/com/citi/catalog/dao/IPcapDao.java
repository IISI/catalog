package tw.com.citi.catalog.dao;

import java.util.Map;

import tw.com.citi.catalog.model.PCAP;

public interface IPcapDao extends IGenericDao<PCAP, Long> {
	
	//<T> List<T> query(String sqlCode, RowMapper<T> rowMapper, Map<String, ?> parameters);

    void update(Map<String, Object> params);

    void delete(Map<String, Object> params);
    
    PCAP findUnique(String name, String team);

}
