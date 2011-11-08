package tw.com.citi.catalog.web.report;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.data.ListOfArrayDataSource;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;

import tw.com.citi.catalog.web.util.F;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Rpt1600P implements IReport {

    @Override
    public InputStream getReportFile() {
        return getClass().getResourceAsStream("/WEB-INF/report/Rpt1600P.jasper");
    }

    @Override
    public Map<String, Object> getReportParameters() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", F.getCurrentUser());
        params.put("scrNo", req.getParameter("scrNo"));
        params.put("processDate", req.getParameter("startTime"));
        params.put("finishDate", req.getParameter("endTime"));
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

}
