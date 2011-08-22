package tw.com.citi.catalog.web.report;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.data.ListOfArrayDataSource;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Rpt1300 implements IReport {

    @Override
    public InputStream getReportFile() {
        return getClass().getResourceAsStream("/WEB-INF/report/Rpt1300.jasper");
    }

    @Override
    public Map<String, Object> getReportParameters() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("userId", null);
        data.put("scrNo", req.getParameter("scrNo"));
        return data;
    }

    @Override
    public Object getReportData() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());
        Gson gson = new Gson();
        List<String[]> logs = gson.fromJson(req.getParameter("consoleLogs"), new TypeToken<List<String[]>>() {}.getType());
        return new ListOfArrayDataSource(logs, new String[] { "log" });
    }

    @Override
    public Map<String, Object> getExporterParameters() {
        // TODO Auto-generated method stub
        return null;
    }

}
