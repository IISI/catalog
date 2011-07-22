package tw.com.citi.catalog.web.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.support.DataAccessUtils;

import tw.com.citi.catalog.web.model.ScrFile;
import tw.com.citi.catalog.web.model.ScrFile.FileType;

public class ScrFileDao extends AbstractGenericDao<ScrFile, Long> implements IScrFileDao {

    @Override
    public List<ScrFile> findSourceFilesByScrId(Long scrId) {
        if (scrId == null) {
            throw new IllegalArgumentException("Cannot find parameter: scrId");
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("scrId", scrId);
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append(getTableName())
                    .append(" WHERE JC_SCR_ID=:scrId AND FILE_TYPE=" + FileType.SOURCE.ordinal());
            List<ScrFile> list = super.jdbcTemplate.query(sql.toString(), getRowMapper(), params);
            return (list == null || list.size() == 0) ? new ArrayList<ScrFile>() : list;
        }
    }

    @Override
    public List<ScrFile> findSourceFilesByBuildUnitId(Long buildUnitId) {
        if (buildUnitId == null) {
            throw new IllegalArgumentException("Cannot find parameter: buildUnitId");
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("buildUnitId", buildUnitId);
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append(getTableName())
                    .append(" WHERE JC_BUILD_UNIT_ID=:buildUnitId AND FILE_TYPE=" + FileType.SOURCE.ordinal());
            List<ScrFile> list = super.jdbcTemplate.query(sql.toString(), getRowMapper(), params);
            return (list == null || list.size() == 0) ? new ArrayList<ScrFile>() : list;
        }
    }

    @Override
    public List<ScrFile> findByBuildUnitIds(List<Long> buildUnitIds) {
        if (buildUnitIds == null || buildUnitIds.size() == 0) {
            throw new IllegalArgumentException("Cannot find parameter: buildUnitId");
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE JC_BUILD_UNIT_ID in (:buildUnitId)");
            List<ScrFile> list = super.namedParameterJdbcTemplate.query(sql.toString(),
                    Collections.singletonMap("buildUnitId", buildUnitIds), getRowMapper());
            return (list == null || list.size() == 0) ? new ArrayList<ScrFile>() : list;
        }
    }

    @Override
    public List<ScrFile> findByScrId(Long scrId) {
        if (scrId == null) {
            throw new IllegalArgumentException("Cannot find parameter: scrId");
        } else {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("scrId", scrId);
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE JC_SCR_ID=:scrId");
            List<ScrFile> list = super.jdbcTemplate.query(sql.toString(), getRowMapper(), params);
            return (list == null || list.size() == 0) ? new ArrayList<ScrFile>() : list;
        }
    }

    @Override
    public ScrFile findByUK(long scrId, String filePath, String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(getTableName()).append(" WHERE JC_SCR_ID = :scrId AND FILE_PATH = :filePath AND FILE_NAME = :fileName");
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("scrId", scrId);
        args.put("filePath", filePath);
        args.put("fileName", fileName);
        List<ScrFile> results = jdbcTemplate.query(sb.toString(), getRowMapper(), args);
        return DataAccessUtils.uniqueResult(results);
    }

    @Override
    public int update1100(Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName());
        sql.append(" SET JC_BUILD_UNIT_ID = :JC_BUILD_UNIT_ID,");
        sql.append(" FILE_TYPE = :FILE_TYPE, CHECKOUT = :CHECKOUT,");
        sql.append(" FILE_DATETIME = :FILE_DATETIME,");
        sql.append(" FILE_SIZE = :FILE_SIZE, FILE_MD5 = :FILE_MD5,");
        sql.append(" LAST_REGISTER_TIME = :LAST_REGISTER_TIME,");
        sql.append(" DELETED = :DELETED");
        sql.append(" WHERE ID = :ID");
        return 0;
    }

}
