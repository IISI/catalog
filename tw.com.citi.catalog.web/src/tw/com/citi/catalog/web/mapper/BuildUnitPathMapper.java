package tw.com.citi.catalog.web.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.web.model.BuildUnitPath;
import tw.com.citi.catalog.web.model.BuildUnitPath.PathType;

public class BuildUnitPathMapper implements RowMapper<BuildUnitPath> {

    @Override
    public BuildUnitPath mapRow(ResultSet rs, int rowNum) throws SQLException {
        BuildUnitPath buildUnitPath = new BuildUnitPath();
        buildUnitPath.setId(rs.getLong("ID"));
        buildUnitPath.setBuildUnit(rs.getLong("JC_BUILD_UNIT_ID"));
        buildUnitPath.setPathType(PathType.values()[rs.getInt("PATH_TYPE")]);
        buildUnitPath.setPath(rs.getString("PATH"));
        return buildUnitPath;
    }

}
