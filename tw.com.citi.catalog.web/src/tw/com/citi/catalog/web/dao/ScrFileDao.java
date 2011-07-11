package tw.com.citi.catalog.web.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.web.model.ScrFile;
import tw.com.citi.catalog.web.model.ScrFile.FileType;

public class ScrFileDao extends AbstractGenericDao<ScrFile, Long> implements IScrFileDao {

    @Override
    public List<ScrFile> findSourceFilesByScrId(Long scrId) {
        if (scrId == null) {
            throw new IllegalArgumentException("Cannot find parameter: scrId");
        } else {
            RowMapper<ScrFile> rowMapper = getRowMapper();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("scrId", scrId);
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append(getTableName())
                    .append(" WHERE JC_SCR_ID=:scrId AND FILE_TYPE=" + FileType.SOURCE.ordinal());
            List<ScrFile> list = super.jdbcTemplate.query(sql.toString(), rowMapper, params);
            return (list == null || list.size() == 0) ? new ArrayList<ScrFile>() : list;
        }
    }

    @Override
    public List<ScrFile> findSourceFilesByBuildUnitId(Long buildUnitId) {
        if (buildUnitId == null) {
            throw new IllegalArgumentException("Cannot find parameter: buildUnitId");
        } else {
            RowMapper<ScrFile> rowMapper = getRowMapper();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("buildUnitId", buildUnitId);
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append(getTableName())
                    .append(" WHERE JC_BUILD_UNIT_ID=:buildUnitId AND FILE_TYPE=" + FileType.SOURCE.ordinal());
            List<ScrFile> list = super.jdbcTemplate.query(sql.toString(), rowMapper, params);
            return (list == null || list.size() == 0) ? new ArrayList<ScrFile>() : list;
        }
    }
}
