package tw.com.citi.catalog.web.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import tw.com.citi.catalog.web.model.Scr;
import tw.com.citi.catalog.web.model.Scr.Status;

public class ScrMapper implements RowMapper<Scr> {

    @Override
    public Scr mapRow(ResultSet rs, int rowNum) throws SQLException {
        Scr scr = new Scr();
        scr.setId(rs.getLong("ID"));
        scr.setScrNo(rs.getNString("SCR_NO"));
        scr.setJcAppId(rs.getLong("JC_APP_ID"));
        scr.setCreateTime(rs.getTimestamp("CREATE_TIME"));
        scr.setStatus(Status.values()[rs.getInt("STATUS")]);
        scr.setProcessTime(rs.getTimestamp("PROCESS_TIME"));
        scr.setJcCoordinatorId(rs.getLong("JC_COORDINATOR_ID"));
        scr.setJcProgrammerId(rs.getLong("JC_PROGRAMMER_ID"));
        scr.setDescription(rs.getNString("DESCRIPTION"));
        scr.setRegisterCount(rs.getLong("REGISTER_COUNT"));
        return scr;
    }

}
