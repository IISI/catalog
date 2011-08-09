package tw.com.citi.catalog.web.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.web.model.FileCheckoutDetail;
import tw.com.citi.catalog.web.model.ProcessResult;

public class FileCheckoutDetailMapper implements RowMapper<FileCheckoutDetail> {

    @Override
    public FileCheckoutDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        FileCheckoutDetail fileCheckoutDetail = new FileCheckoutDetail();
        fileCheckoutDetail.setId(rs.getLong("ID"));
        fileCheckoutDetail.setJcFunctionLogId(rs.getLong("JC_FUNCTION_LOG_ID"));
        fileCheckoutDetail.setJcAppFileId(rs.getLong("JC_APP_FILE_ID"));
        fileCheckoutDetail.setProcessResult(ProcessResult.values()[rs.getInt("PROCESS_RESULT")]);
        fileCheckoutDetail.setTargetPath(rs.getString("TARGET_PATH"));
        return fileCheckoutDetail;
    }

}
