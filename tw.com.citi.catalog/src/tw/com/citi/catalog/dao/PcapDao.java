package tw.com.citi.catalog.dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import tw.com.citi.catalog.model.PCAP;

public class PcapDao extends AbstractGenericDao<PCAP, Long> implements IPcapDao {

	private SimpleJdbcTemplate jdbcTemplate;

    private TransactionTemplate txTemplate;

    private Properties sqlProps;
	
    public PcapDao(DataSource ds) {
    	jdbcTemplate = new SimpleJdbcTemplate(ds);
        PlatformTransactionManager txManager = new DataSourceTransactionManager(ds);
        txTemplate = new TransactionTemplate(txManager);
    }


	
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
    public PCAP findUnique(String name, String team) {
        
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
        return jdbcTemplate.query(sqlCode, rowMapper, parameters);
    }
    
	@Override
	public List<PCAP> listAll() {
		// TODO Auto-generated method stub
		StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM PCAP");
        Map<String, Object> args = new HashMap<String, Object>();
        List<PCAP> list = jdbcTemplate.query(sql.toString(), getRowMapper(), args);
        return list;
	}





	@Override
	public <T> List<T> query(String sqlCode, Class<T> requiredType,
			Map<String, ?> parameters) {
		// TODO Auto-generated method stub
		return null;
	}





	@Override
	public <T> List<T> query(String sqlCode, RowMapper<T> rowMapper,
			Map<String, ?> parameters, boolean isNative) {
		// TODO Auto-generated method stub
		return null;
	}

}
