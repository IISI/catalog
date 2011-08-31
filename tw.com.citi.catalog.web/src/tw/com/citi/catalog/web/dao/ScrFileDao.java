package tw.com.citi.catalog.web.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import tw.com.citi.catalog.web.dto.Rpt1100Dto;
import tw.com.citi.catalog.web.dto.ScrFileDto;
import tw.com.citi.catalog.web.model.FileType;
import tw.com.citi.catalog.web.model.ScrFile;

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
        sb.append("SELECT * FROM ").append(getTableName())
                .append(" WHERE JC_SCR_ID = :scrId AND FILE_PATH = :filePath AND FILE_NAME = :fileName");
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

    @Override
    public int updateLastCompileTimeByJcScrId(Timestamp lastCompileTime, Long jcScrId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("LAST_COMPILE_TIME", lastCompileTime);
        params.put("JC_SCR_ID", jcScrId);
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName());
        sql.append(" SET LAST_COMPILE_TIME = :LAST_COMPILE_TIME");
        sql.append(" WHERE JC_SCR_ID = :JC_SCR_ID");
        return jdbcTemplate.update(sql.toString(), params);
    }

    @Override
    public int updateFileInfoById(Timestamp fileDateTime, Long fileSize, Long id) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("FILE_DATETIME", fileDateTime);
        params.put("FILE_SIZE", fileSize);
        params.put("ID", id);
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName());
        sql.append(" SET FILE_DATETIME = :FILE_DATETIME, ");
        sql.append(" FILE_SIZE = :FILE_SIZE");
        sql.append(" WHERE ID = :ID");
        return jdbcTemplate.update(sql.toString(), params);
    }

    @Override
    public List<Rpt1100Dto> find1100ReportData(long scrId) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT");
        sql.append("     sf.file_name fileName,");
        sql.append("     sf.file_path filePath,");
        sql.append("     CASE sf.file_type");
        sql.append("         WHEN 0 THEN 'Source'");
        sql.append("         ELSE 'Execution'");
        sql.append("     END fileType,");
        sql.append("     CASE rh.register_action");
        sql.append("         WHEN 0 THEN 'New'");
        sql.append("         WHEN 1 THEN 'Update'");
        sql.append("         ELSE 'Delete'");
        sql.append("     END action,");
        sql.append("     sf.file_datetime fileDate,");
        sql.append("     sf.file_size fileSize,");
        sql.append("     sf.last_register_time registerDate");
        sql.append(" FROM jc_scr_file sf");
        sql.append(" LEFT JOIN jc_register_history rh ON sf.id = rh.jc_scr_file_id AND rh.register_count = (SELECT MAX(REGISTER_COUNT) FROM JC_REGISTER_HISTORY WHERE SF.ID = JC_SCR_FILE_ID)");
        sql.append(" WHERE sf.jc_scr_id = ?");
        return jdbcTemplate.query(sql.toString(), BeanPropertyRowMapper.newInstance(Rpt1100Dto.class), scrId);
    }

    @Override
    public List<ScrFile> find1400ReportData(long scrId, Long buildUnitId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SF.ID, SF.JC_SCR_ID, AP.PATH + SF.FILE_PATH FILE_PATH, SF.FILE_NAME, SF.JC_BUILD_UNIT_ID, SF.FILE_TYPE, SF.CHECKOUT, SF.FILE_DATETIME, SF.FILE_SIZE, SF.FILE_MD5, SF.LAST_REGISTER_TIME, SF.LAST_COMPILE_TIME, SF.DELETED ");
        sql.append("FROM JC_SCR_FILE SF ");
        sql.append("LEFT JOIN JC_SCR S ");
        sql.append("    LEFT JOIN JC_APP A ");
        sql.append("        LEFT JOIN JC_APP_PATH AP ON A.ID = AP.JC_APP_ID AND AP.PATH_TYPE = 1 ");
        sql.append("    ON S.JC_APP_ID = A.ID ");
        sql.append("ON SF.JC_SCR_ID = S.ID ");
        sql.append("WHERE SF.file_type = 1 AND SF.jc_scr_id = :scrId");
        if (buildUnitId != null) {
            sql.append(" AND SF.jc_build_unit_id = :buildUnitId");
        }
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("scrId", scrId);
        params.put("buildUnitId", buildUnitId);
        return jdbcTemplate.query(sql.toString(), getRowMapper(), params);
    }

    @Override
    public List<ScrFileDto> findBy(long scrId, Long buildUnitId, FileType fileType) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT sf.*, rh.register_action FROM ").append(getTableName()).append(" sf");
        sql.append(" LEFT JOIN jc_register_history rh ON sf.id = rh.jc_scr_file_id");
        sql.append(" AND rh.register_count = (SELECT register_count FROM jc_scr WHERE id = sf.jc_scr_id)");
        sql.append(" WHERE sf.jc_scr_id = :scrId AND sf.file_type = :fileType");
        if (buildUnitId != null) {
            sql.append(" AND sf.jc_build_unit_id = :buildUnitId");
        }
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("scrId", scrId);
        args.put("buildUnitId", buildUnitId);
        args.put("fileType", fileType.ordinal());
        return jdbcTemplate.query(sql.toString(), BeanPropertyRowMapper.newInstance(ScrFileDto.class), args);
    }

}
