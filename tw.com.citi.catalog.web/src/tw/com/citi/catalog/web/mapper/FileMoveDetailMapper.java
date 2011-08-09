package tw.com.citi.catalog.web.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.web.model.FileMoveDetail;
import tw.com.citi.catalog.web.model.ProcessResult;

public class FileMoveDetailMapper implements RowMapper<FileMoveDetail> {

    @Override
    public FileMoveDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        FileMoveDetail fileMoveDetail = new FileMoveDetail();
        fileMoveDetail.setId(rs.getLong("ID"));
        fileMoveDetail.setJcFunctionLogId(rs.getLong("JC_FUNCTION_LOG_ID"));
        fileMoveDetail.setProcessResult(ProcessResult.values()[rs.getInt("PROCESS_RESULT")]);
        fileMoveDetail.setTargetPath(rs.getString("TARGET_PATH"));
        return fileMoveDetail;
    }

}
