package tw.com.citi.catalog.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.model.CompileDetail;
import tw.com.citi.catalog.model.ProcessResult;

public class CompileDetailMapper implements RowMapper<CompileDetail> {

    @Override
    public CompileDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        CompileDetail compileDetail = new CompileDetail();
        compileDetail.setId(rs.getLong("ID"));
        compileDetail.setJcFunctionLogId(rs.getLong("JC_FUNCTION_LOG_ID"));
        compileDetail.setJcAppFileId(rs.getLong("JC_APP_FILE_ID"));
        compileDetail.setProcessResult(ProcessResult.values()[rs.getInt("PROCESS_RESULT")]);
        return compileDetail;
    }

}
