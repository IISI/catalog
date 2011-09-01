package tw.com.citi.catalog.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.model.AppPath;
import tw.com.citi.catalog.model.AppPath.PathType;

public class AppPathMapper implements RowMapper<AppPath> {

    @Override
    public AppPath mapRow(ResultSet rs, int rowNum) throws SQLException {
        AppPath buildUnitPath = new AppPath();
        buildUnitPath.setId(rs.getLong("ID"));
        buildUnitPath.setJcAppId(rs.getLong("JC_APP_ID"));
        buildUnitPath.setPathType(PathType.values()[rs.getInt("PATH_TYPE")]);
        buildUnitPath.setPath(rs.getString("PATH"));
        return buildUnitPath;
    }

}
