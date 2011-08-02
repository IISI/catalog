package tw.com.citi.catalog.web.report;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;

import tw.com.citi.catalog.util.DateUtil;
import tw.com.citi.catalog.web.dao.IScrFileDao;

public class Rpt1400 implements IReport {

    private IScrFileDao scrFileDao;

    @Override
    public InputStream getReportFile() {
        return getClass().getResourceAsStream("/WEB-INF/report/Rpt1400.jasper");
    }

    @Override
    public Map<String, Object> getReportParameters() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", req.getParameter("userId"));
        params.put("reportId", "JCS1400");
        params.put("scrNo", req.getParameter("scrNo"));
        params.put("printDate", DateUtil.format(new Date()));
        params.put("appId", req.getParameter("appId"));
        return params;
    }

    @Override
    public Object getReportData() {
        String scrId = ((WebRequest) RequestCycle.get().getRequest()).getParameter("scrId");
        String buildUnitId = ((WebRequest) RequestCycle.get().getRequest()).getParameter("buildUnitId");
        if (buildUnitId != null && !"all".equalsIgnoreCase(buildUnitId)) {
            return scrFileDao.find1400ReportData(Long.valueOf(scrId), Long.valueOf(buildUnitId));
        } else {
            return scrFileDao.find1400ReportData(Long.valueOf(scrId), null);
        }
    }

    @Override
    public Map<String, Object> getExporterParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setScrFileDao(IScrFileDao scrFileDao) {
        this.scrFileDao = scrFileDao;
    }

}
