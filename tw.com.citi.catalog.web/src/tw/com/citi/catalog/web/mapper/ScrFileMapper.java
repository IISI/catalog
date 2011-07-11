package tw.com.citi.catalog.web.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.web.model.ScrFile;
import tw.com.citi.catalog.web.model.ScrFile.FileType;

public class ScrFileMapper implements RowMapper<ScrFile> {

    @Override
    public ScrFile mapRow(ResultSet rs, int rowNum) throws SQLException {
        ScrFile scrFile = new ScrFile();
        scrFile.setId(rs.getLong("ID"));
        scrFile.setCheckout(rs.getBoolean("CHECKOUT"));
        scrFile.setFileDatetime(rs.getTimestamp("FILE_DATETIME"));
        scrFile.setFileMd5(rs.getString("FILE_MD5"));
        scrFile.setFileName(rs.getString("FILE_NAME"));
        scrFile.setFilePath(rs.getString("FILE_PATH"));
        scrFile.setFileSize(rs.getLong("FILE_SIZE"));
        scrFile.setFileType(FileType.values()[rs.getInt("FILE_TYPE")]);
        scrFile.setLastCompileTime(rs.getTimestamp("LAST_COMPILE_TIME"));
        scrFile.setLastRegisterTime(rs.getTimestamp("LAST_REGISTER_TIME"));
        scrFile.setJcScrId(rs.getLong("JC_SCR_ID"));
        scrFile.setJcBuildUnitId(rs.getLong("JC_BUILD_UNIT_ID"));
        return scrFile;
    }

}
