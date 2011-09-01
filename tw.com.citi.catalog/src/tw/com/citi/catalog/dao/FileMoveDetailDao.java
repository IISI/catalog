package tw.com.citi.catalog.dao;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.model.FileMoveDetail;

public class FileMoveDetailDao extends AbstractGenericDao<FileMoveDetail, Long> implements IFileMoveDetailDao {

    @Override
    public List<Map<String, Object>> find1200ReportData(long functionLogId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT AF.FILE_NAME fileName, FMD.TARGET_PATH + AF.FILE_PATH pathName, AF.FILE_SIZE fileSize, AF.FILE_DATETIME fileDate, CASE FMD.PROCESS_RESULT WHEN 0 THEN 'SUCCESS' ELSE 'FAILURE' END status ");
        sql.append("FROM JC_FILE_MOVE_DETAIL FMD LEFT JOIN JC_APP_FILE AF ON FMD.JC_APP_FILE_ID = AF.ID ");
        sql.append("WHERE FMD.JC_FUNCTION_LOG_ID = ? ORDER BY AF.FILE_PATH, AF.FILE_NAME");
        return jdbcTemplate.queryForList(sql.toString(), functionLogId);
    }

}
