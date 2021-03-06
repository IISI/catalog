package tw.com.citi.catalog.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import tw.com.citi.catalog.model.AppFunction;
import tw.com.citi.catalog.model.User;

public class AppFunctionDao extends AbstractGenericDao<AppFunction, Long> implements IAppFunctionDao {

	public static final String SORT_INDEX = "sidx";

    public static final String SORT_DIR = "sord";
    
    public static final int SORT_INDEX_LEN = SORT_INDEX.length();

    public static final int SORT_DIR_LEN = SORT_DIR.length();

    private Properties sqlProps;

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
    public AppFunction findUnique(String name, String team) {
        
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
    public List<AppFunction> findUserFunctions(User user) {
        List<AppFunction> functions = new ArrayList<AppFunction>();
        StringTokenizer tokenizer = new StringTokenizer(user.getGrpName(), ";");
        if (tokenizer.countTokens() > 0) {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT FUNCCODE, FUNCDESC FROM FUNCLIST WHERE MAJORSYSCODE='JCS' AND (");
            while (tokenizer.hasMoreElements()) {
                sql.append(tokenizer.nextToken().trim()).append("=1 OR ");
            }
            sql.delete(sql.lastIndexOf(" OR "), sql.length());
            sql.append(" )");
            
            functions = jdbcTemplate.query(sql.toString(), getRowMapper());
        }
        return functions;
    }

}
