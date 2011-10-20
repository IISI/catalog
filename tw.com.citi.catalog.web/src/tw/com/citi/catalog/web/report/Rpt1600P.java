package tw.com.citi.catalog.web.report;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.data.ListOfArrayDataSource;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import tw.com.citi.catalog.dao.IAppFileDao;
import tw.com.citi.catalog.dao.IFunctionLogDao;
import tw.com.citi.catalog.model.FunctionLog;
import tw.com.citi.catalog.web.util.F;

public class Rpt1600P implements IReport {

    private IFunctionLogDao functionLogDao;

    private IAppFileDao appFileDao;

    @Override
    public InputStream getReportFile() {
        return getClass().getResourceAsStream("/WEB-INF/report/Rpt1600P.jasper");
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
        Gson gson = new Gson();
        List<String[]> logs = gson.fromJson(req.getParameter("pvcsConsole"), new TypeToken<List<String[]>>() {}.getType());
        return new ListOfArrayDataSource(logs, new String[] { "log" });
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
