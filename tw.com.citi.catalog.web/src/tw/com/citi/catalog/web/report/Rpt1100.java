package tw.com.citi.catalog.web.report;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;

import tw.com.citi.catalog.web.dao.IScrDao;
import tw.com.citi.catalog.web.dao.IScrFileDao;

public class Rpt1100 implements IReport {

    private IScrDao scrDao;

    private IScrFileDao scrFileDao;

    @Override
    public InputStream getReportFile() {
        return getClass().getResourceAsStream("/WEB-INF/report/Rpt1100.jasper");
    }

    @Override
    public Map<String, Object> getReportParameters() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());
        Map<String, Object> params = new HashMap<String, Object>();
        Long scrId = Long.valueOf(req.getParameter("scrId"));
        Map<String, Object> data = scrDao.queryScrInfo(scrId);
        params.put("userId", "");
        params.put("scrNo", req.getParameter("scrNo"));
        params.put("status", req.getParameter("status"));
        params.put("createDate", req.getParameter("createDate"));
        params.put("appId", data.get("appId"));
        params.put("programmer", data.get("programmer"));
        params.put("coordinator", data.get("coordinator"));
        params.put("librarian", req.getParameter("librarian"));
        return params;
    }

    @Override
    public Object getReportData() {
        String scrId = ((WebRequest) RequestCycle.get().getRequest()).getParameter("scrId");
        return scrFileDao.find1100ReportData(Long.valueOf(scrId));
    }

    @Override
    public Map<String, Object> getExporterParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setScrDao(IScrDao scrDao) {
        this.scrDao = scrDao;
    }

    public void setScrFileDao(IScrFileDao scrFileDao) {
        this.scrFileDao = scrFileDao;
    }

}
