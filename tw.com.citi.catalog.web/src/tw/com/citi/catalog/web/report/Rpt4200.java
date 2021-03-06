package tw.com.citi.catalog.web.report;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import tw.com.citi.catalog.dao.IFunctionLogDao;
import tw.com.citi.catalog.web.util.F;

public class Rpt4200 implements IReport {

    private IFunctionLogDao functionLogDao;

    @Override
    public InputStream getReportFile() {
        return getClass().getResourceAsStream("/WEB-INF/report/Rpt4200.jasper");
    }

    @Override
    public Map<String, Object> getReportParameters() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", F.getCurrentUser());
        return params;
    }

    @Override
    public Object getReportData() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        Gson gson = new Gson();
        
        try {
            Long jcScrId = gson.fromJson(req.getParameter("scrId"), Long.class);
            String startDate = req.getParameter("startDate");
            String endDate = req.getParameter("endDate");
            Date begin = null;
            Date end = null;
            if (startDate != null && !startDate.isEmpty()) {
                begin = sdf.parse(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = sdf.parse(endDate);
                Calendar endCal = Calendar.getInstance();
                endCal.setTime(end);
                endCal.add(Calendar.DAY_OF_MONTH, 1);
                endCal.add(Calendar.MILLISECOND, -1);
                end = endCal.getTime();
            }
            String functionIdsString = gson.fromJson(req.getParameter("functionIds"), String.class);
            Set<String> functions = gson.fromJson(functionIdsString, new TypeToken<Set<String>>() {}.getType());
            return new JRMapCollectionDataSource(functionLogDao.find4300SCRReportData(jcScrId, begin, end, functions));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Failed to parsing scrId into Long value.", e);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Failed to parsing functionId into String[].", e);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parsing date.", e);
        }
    }

    @Override
    public Map<String, Object> getExporterParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setFunctionLogDao(IFunctionLogDao functionLogDao) {
        this.functionLogDao = functionLogDao;
    }

}
