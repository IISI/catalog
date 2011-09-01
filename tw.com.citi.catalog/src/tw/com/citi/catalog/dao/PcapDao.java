package tw.com.citi.catalog.dao;

import java.util.Map;

import tw.com.citi.catalog.model.PCAP;

public class PcapDao extends AbstractGenericDao<PCAP, Long> implements IPcapDao {

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

}
