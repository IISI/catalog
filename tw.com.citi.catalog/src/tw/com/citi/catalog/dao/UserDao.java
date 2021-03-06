package tw.com.citi.catalog.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import tw.com.citi.catalog.model.User;

public class UserDao extends AbstractGenericDao<User, Long> implements IUserDao {

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
    public User findByUserId(String userId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM USERS WHERE UserID = ?");
        List<User> users = jdbcTemplate.query(sql.toString(), getRowMapper(), userId);
        return DataAccessUtils.uniqueResult(users);
    }

    @Override
    public List<byte[]> findUserBasicByUserId(String userId) {
        return jdbcTemplate.query("SELECT * FROM SEC_USRBASIC WHERE USR_ID_C = ?", new RowMapper<byte[]>() {

            private LobHandler lobHandler = new DefaultLobHandler();

            @Override
            public byte[] mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                return lobHandler.getBlobAsBytes(rs, "USR_PWD_C");
            }
        }, userId);
    }

}
