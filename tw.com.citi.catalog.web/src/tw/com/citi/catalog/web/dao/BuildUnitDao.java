package tw.com.citi.catalog.web.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;

import tw.com.citi.catalog.web.model.BuildUnit;

public class BuildUnitDao extends AbstractGenericDao<BuildUnit, Long> implements IBuildUnitDao {

    @Override
    public List<BuildUnit> findUnitGridData(Map params) {
        RowMapper<BuildUnit> rowMapper = ParameterizedBeanPropertyRowMapper.newInstance(BuildUnit.class);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE JC_APP_ID = :jc_app_id");
        return super.jdbcTemplate.query(sql.toString(), rowMapper, params);
    }

    @Override
    public boolean isBuildUnitExist(String appId, String unitId) {
        return (findUnique(appId, unitId) != null);
    }

    @Override
    public BuildUnit findUnique(String appId, String unitId) {
        RowMapper<BuildUnit> rowMapper = ParameterizedBeanPropertyRowMapper.newInstance(BuildUnit.class);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT BU.* FROM ").append(getTableName())
                .append(" BU INNER JOIN JC_APP APP ON APP.APP_ID = :appId WHERE UNIT_ID = :unitId");
        Map<String, String> params = new HashMap<String, String>();
        params.put("appId", appId);
        params.put("unitId", unitId);
        List<BuildUnit> results = super.jdbcTemplate.query(sql.toString(), rowMapper, params);
        return (results != null && results.size() > 0) ? results.get(0) : null;
    }
}
