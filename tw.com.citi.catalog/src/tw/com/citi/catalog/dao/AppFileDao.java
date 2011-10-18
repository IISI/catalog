package tw.com.citi.catalog.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.model.AppFile;
import tw.com.citi.catalog.model.AppPath;

public class AppFileDao extends AbstractGenericDao<AppFile, Long> implements IAppFileDao {

    @Override
    public List<AppFile> findByBuildUnitIds(List<Long> buildUnitIds) {
        if (buildUnitIds == null || buildUnitIds.size() == 0) {
            throw new IllegalArgumentException("Cannot find parameter: buildUnitId");
        } else {
            RowMapper<AppFile> rowMapper = getRowMapper();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE JC_BUILD_UNIT_ID in (:buildUnitId)");
            List<AppFile> list = super.namedParameterJdbcTemplate.query(sql.toString(),
                    Collections.singletonMap("buildUnitId", buildUnitIds), rowMapper);
            return (list == null || list.size() == 0) ? new ArrayList<AppFile>() : list;
        }
    }

    @Override
    public List<AppFile> findByAppId(Long appId) {
        if (appId == null) {
            throw new IllegalArgumentException("Cannot find parameter: appId");
        } else {
            StringBuilder sql = new StringBuilder();
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("appId", appId);
            sql.append("SELECT * FROM ").append(getTableName()).append(" WHERE JC_APP_ID = :appId");
            List<AppFile> list = super.jdbcTemplate.query(sql.toString(), getRowMapper(), params);
            return (list == null || list.size() == 0) ? new ArrayList<AppFile>() : list;
        }
    }

    @Override
    public AppFile findByUK(long jcAppId, String filePath, String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(getTableName())
                .append(" WHERE JC_APP_ID = :jcAppId AND FILE_PATH = :filePath AND FILE_NAME = :fileName");
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("jcAppId", jcAppId);
        args.put("filePath", filePath);
        args.put("fileName", fileName);
        List<AppFile> results = jdbcTemplate.query(sb.toString(), getRowMapper(), args);
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
        return jdbcTemplate.update(sql.toString(), params);
    }

    @Override
    public int updateLastCompileTimeByJcAppId(Timestamp lastCompileTime, Long jcAppId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("LAST_COMPILE_TIME", lastCompileTime);
        params.put("JC_APP_ID", jcAppId);
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName());
        sql.append(" SET LAST_COMPILE_TIME = :LAST_COMPILE_TIME");
        sql.append(" WHERE JC_APP_ID = :JC_APP_ID");
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
    public List<Map<String, Object>> find1600ReportData(long functionLogId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT AF.FILE_NAME fileName, AF.FILE_PATH filePath, AF.FILE_SIZE fileSize, AF.FILE_DATETIME fileDate, AF.FILE_MD5 hash, CASE AF.FILE_TYPE WHEN 0 THEN 'SOURCE' ELSE 'EXECUTION' END fileType ");
        sql.append("FROM JC_FILE_MOVE_DETAIL FMD, JC_APP_FILE AF ");
        sql.append("WHERE FMD.JC_FUNCTION_LOG_ID = :functionLogId AND FMD.JC_APP_FILE_ID = AF.ID AND FMD.PATH_TYPE IN (:pathType)");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("functionLogId", functionLogId);
        Set<AppPath.PathType> pathTypes = new HashSet<AppPath.PathType>();
        Collections.addAll(pathTypes, AppPath.PathType.PROD_SOURCE, AppPath.PathType.PROD_EXECUTION);
        params.put("pathType", pathTypes);
        return jdbcTemplate.queryForList(sql.toString(), params);
    }

    @Override
    public List<Map<String, Object>> find1600BReportData(long functionLogId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT AF.FILE_NAME fileName, AF.FILE_PATH filePath, AF.FILE_SIZE fileSize, AF.FILE_DATETIME fileDate, AF.FILE_MD5 hash, CASE AF.FILE_TYPE WHEN 0 THEN 'SOURCE' ELSE 'EXECUTION' END fileType ");
        sql.append("FROM JC_FILE_MOVE_DETAIL FMD, JC_APP_FILE AF ");
        sql.append("WHERE FMD.JC_FUNCTION_LOG_ID = :functionLogId AND FMD.JC_APP_FILE_ID = AF.ID AND FMD.PATH_TYPE IN (:pathType)");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("functionLogId", functionLogId);
        Set<AppPath.PathType> pathTypes = new HashSet<AppPath.PathType>();
        Collections.addAll(pathTypes, AppPath.PathType.PROD_BACKUP);
        params.put("pathType", pathTypes);
        return jdbcTemplate.queryForList(sql.toString(), params);
    }

    @Override
    public int updateCheckOutFlag(Long id) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ID", id);
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(getTableName());
        sql.append(" SET CHECKOUT = 0 ");
        sql.append(" WHERE ID = :ID");
        return jdbcTemplate.update(sql.toString(), params);
    }

    @Override
    public List<Map<String, Object>> find1600PReportData(long functionLogId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT AF.FILE_NAME fileName, AF.FILE_PATH filePath, AF.FILE_SIZE fileSize, AF.FILE_DATETIME fileDate, AF.FILE_MD5 hash, CASE FMD.PROCESS_RESULT WHEN 0 THEN 'SUCCESS' ELSE 'FAILURE' END result ");
        sql.append("FROM JC_FILE_MOVE_DETAIL FMD, JC_APP_FILE AF ");
        sql.append("WHERE FMD.JC_FUNCTION_LOG_ID = :functionLogId AND FMD.JC_APP_FILE_ID = AF.ID AND FMD.PATH_TYPE IN (:pathType)");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("functionLogId", functionLogId);
        Set<AppPath.PathType> pathTypes = new HashSet<AppPath.PathType>();
        Collections.addAll(pathTypes, AppPath.PathType.PVCS);
        params.put("pathType", pathTypes);
        return jdbcTemplate.queryForList(sql.toString(), params);
    }

}
