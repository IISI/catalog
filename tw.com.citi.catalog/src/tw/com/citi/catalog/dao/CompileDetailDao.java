package tw.com.citi.catalog.dao;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.model.CompileDetail;

public class CompileDetailDao extends AbstractGenericDao<CompileDetail, Long> implements ICompileDetailDao {

    @Override
    public List<Map<String, Object>> find1300LReportData(long functionLogId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT AF.FILE_NAME fileName, AP.PATH + AF.FILE_PATH pathName, AF.FILE_SIZE fileSize, AF.FILE_DATETIME fileDate ");
        sql.append("FROM JC_COMPILE_DETAIL CD ");
        sql.append("    LEFT JOIN JC_APP_FILE AF ");
        sql.append("        LEFT JOIN JC_APP_PATH AP ON AF.JC_APP_ID = AP.JC_APP_ID AND AP.PATH_TYPE = 1 ");
        sql.append("    ON CD.JC_APP_FILE_ID = AF.ID ");
        sql.append("WHERE CD.JC_FUNCTION_LOG_ID = ?");
        return jdbcTemplate.queryForList(sql.toString(), functionLogId);
    }

}
