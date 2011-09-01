package tw.com.citi.catalog.web.report;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;

import tw.com.citi.catalog.dao.ICompileDetailDao;
import tw.com.citi.catalog.dao.IFunctionLogDao;
import tw.com.citi.catalog.model.FunctionLog;
import tw.com.citi.catalog.web.util.F;

public class Rpt1300L implements IReport {

    private IFunctionLogDao functionLogDao;

    private ICompileDetailDao compileDetailDao;

    @Override
    public InputStream getReportFile() {
        return getClass().getResourceAsStream("/WEB-INF/report/Rpt1300L.jasper");
    }

    @Override
    public Map<String, Object> getReportParameters() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());
        Long functionLogId = Long.valueOf(req.getParameter("functionLogId"));
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
        Long functionLogId = Long.valueOf(req.getParameter("functionLogId"));
        return new JRMapCollectionDataSource(compileDetailDao.find1300LReportData(functionLogId));
    }

    @Override
    public Map<String, Object> getExporterParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setFunctionLogDao(IFunctionLogDao functionLogDao) {
        this.functionLogDao = functionLogDao;
    }

    public void setCompileDetailDao(ICompileDetailDao compileDetailDao) {
        this.compileDetailDao = compileDetailDao;
    }

}
