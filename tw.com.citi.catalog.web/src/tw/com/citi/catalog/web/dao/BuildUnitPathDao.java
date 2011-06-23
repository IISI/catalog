package tw.com.citi.catalog.web.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;

import tw.com.citi.catalog.web.model.BuildUnitPath;

public class BuildUnitPathDao extends AbstractGenericDao<BuildUnitPath, Long> implements IBuildUnitPathDao {

    public List<BuildUnitPath> findByBuildUnitId(Long buildUnitId) {
        RowMapper<BuildUnitPath> rowMapper = ParameterizedBeanPropertyRowMapper.newInstance(BuildUnitPath.class);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("buildUnitId", buildUnitId);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE JC_BUILD_UNIT_ID = :buildUnitId");
        return super.jdbcTemplate.query(sql.toString(), rowMapper, args);
    }

    @Override
    public void deleteAllPathByUnitId(Long buildUnitId) {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(getTableName()).append(" WHERE JC_BUILD_UNIT_ID = :buildUnitId");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("buildUnitId", buildUnitId);
        super.jdbcTemplate.update(sql.toString(), params);
    }
}
