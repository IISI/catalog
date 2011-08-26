package tw.com.citi.catalog.web.report;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;

import tw.com.citi.catalog.web.dao.IAppFileDao;
import tw.com.citi.catalog.web.dao.IFunctionLogDao;
import tw.com.citi.catalog.web.model.FunctionLog;
import tw.com.citi.catalog.web.util.F;

public class Rpt1700 implements IReport {

    private IFunctionLogDao functionLogDao;

    private IAppFileDao appFileDao;

    @Override
    public InputStream getReportFile() {
        return getClass().getResourceAsStream("/WEB-INF/report/Rpt1700.jasper");
    }

    @Override
    public Map<String, Object> getReportParameters() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());
        long functionLogId = Long.valueOf(req.getParameter("functionLogId"));
        FunctionLog functionLog = functionLogDao.findById(functionLogId);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", F.getCurrentUser());
        params.put("scrNo", req.getParameter("scrNo"));
        params.put("processDate", functionLog.getStartTime());
        params.put("finishDate", functionLog.getEndTime());
        params.put("appId", req.getParameter("appId"));
        return params;
    }

    @Override
    public Object getReportData() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());
        long functionLogId = Long.valueOf(req.getParameter("functionLogId"));
        return new JRMapCollectionDataSource(appFileDao.find1600ReportData(functionLogId));
    }

    @Override
    public Map<String, Object> getExporterParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setFunctionLogDao(IFunctionLogDao functionLogDao) {
        this.functionLogDao = functionLogDao;
    }

    public void setAppFileDao(IAppFileDao appFileDao) {
        this.appFileDao = appFileDao;
    }

}
