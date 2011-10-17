package tw.com.citi.catalog.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.model.RegisterHistory;

public class RegisterHistoryMapper implements RowMapper<RegisterHistory> {

    @Override
    public RegisterHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
        RegisterHistory registerHistory = new RegisterHistory();
        registerHistory.setId(rs.getLong("ID"));
        registerHistory.setRegisterCount(rs.getLong("REGISTER_COUNT"));
        registerHistory.setCheckin(rs.getBoolean("CHECK_IN"));
        registerHistory.setFileDatetime(rs.getTimestamp("FILE_DATETIME"));
        registerHistory.setFileMd5(rs.getString("FILE_MD5"));
        registerHistory.setFileSize(rs.getLong("FILE_SIZE"));
        registerHistory.setFileType(rs.getInt("FILE_TYPE"));
        registerHistory.setJcScrFileId(rs.getLong("JC_SCR_FILE_ID"));
        registerHistory.setJcScrId(rs.getLong("JC_SCR_ID"));
        registerHistory.setRegisterAction(rs.getInt("REGISTER_ACTION"));
        registerHistory.setRegisterTime(rs.getTimestamp("REGISTER_TIME"));
        return registerHistory;
    }

}
