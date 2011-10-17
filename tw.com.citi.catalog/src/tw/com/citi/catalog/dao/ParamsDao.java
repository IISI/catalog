package tw.com.citi.catalog.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;

import tw.com.citi.catalog.model.Params;

public class ParamsDao extends AbstractGenericDao<Params, Long> implements IParamsDao {

    @Override
    public Params findByPKey(String pKey) {
        RowMapper<Params> rowMapper = ParameterizedBeanPropertyRowMapper.newInstance(Params.class);
        Map<String, Object> args = new HashMap<String, Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE PKEY=:pKey");
        args.put("pKey", pKey);
        List<Params> results = jdbcTemplate.query(sql.toString(), rowMapper, args);
        if (results.size() > 0) {
            return results.get(0);
        } else {
            return null;
        }
    }

    @Override
    public int update(Map<String, Object> params) {
        String pKey = (String) params.get("PKEY");
        String pValue = (String) params.get("PVALUE");
        if (pKey == null || "".equals(pKey.trim())) {
            throw new IllegalArgumentException("Cannot find parameter: pKey");
        }
        if (pValue == null) {
            throw new IllegalArgumentException("Cannot find parameter: pValue");
        }
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName()).append(" SET PVALUE=:PVALUE WHERE PKEY=:PKEY");
        return super.jdbcTemplate.update(sql.toString(), params);
    }

}
