package tw.com.citi.catalog.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.model.AppFile;
import tw.com.citi.catalog.model.FileType;

public class AppFileMapper implements RowMapper<AppFile> {

    @Override
    public AppFile mapRow(ResultSet rs, int rowNum) throws SQLException {
        AppFile appFile = new AppFile();
        appFile.setId(rs.getLong("ID"));
        appFile.setCheckout(rs.getBoolean("CHECKOUT"));
        appFile.setFileDatetime(rs.getTimestamp("FILE_DATETIME"));
        appFile.setFileMd5(rs.getString("FILE_MD5"));
        appFile.setFileName(rs.getString("FILE_NAME"));
        appFile.setFilePath(rs.getString("FILE_PATH"));
        appFile.setFileSize(rs.getLong("FILE_SIZE"));
        appFile.setFileType(FileType.values()[rs.getInt("FILE_TYPE")]);
        appFile.setLastCompileTime(rs.getTimestamp("LAST_COMPILE_TIME"));
        appFile.setLastRegisterTime(rs.getTimestamp("LAST_REGISTER_TIME"));
        appFile.setJcAppId(rs.getLong("JC_APP_ID"));
        appFile.setJcBuildUnitId(rs.getLong("JC_BUILD_UNIT_ID"));
        appFile.setDeleted(rs.getBoolean("DELETED"));
        return appFile;
    }

}
