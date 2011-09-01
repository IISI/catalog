package tw.com.citi.catalog.web.grid;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tw.com.citi.catalog.dao.IFunctionLogDao;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class ReportGrid extends AbstractGridHandler {

    private IFunctionLogDao functionLogDao;

    @Override
    List getRows(Map<String, String> params, String[] operators, String index,
            String order, long start, long limit) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Gson gson = new Gson();

            Long scrId = params.get("scrId") == null ? null : Long.valueOf(params.get("scrId"));
            String startDate = params.get("startDate");
            Date begin = null;
            Date end = null;
            if (startDate != null && !startDate.isEmpty()) {
                begin = sdf.parse(startDate);
            }
            String endDate = params.get("endDate");
            if (endDate != null && !endDate.isEmpty()) {
                end = sdf.parse(endDate);
                Calendar endCal = Calendar.getInstance();
                endCal.setTime(end);
                endCal.add(Calendar.DAY_OF_MONTH, 1);
                endCal.add(Calendar.MILLISECOND, -1);
                end = endCal.getTime();
            }
            Set<String> functions = gson.fromJson(params.get("functionIds"), new TypeToken<Set<String>>() {}.getType());
            if (functions.isEmpty()) {
                functions.add("");
            }
            
            return functionLogDao.find4300GridData(scrId, begin, end, functions, index, order, start, limit);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Failed to parsing scrId into Long value.", e);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Failed to parsing functionId into String[].", e);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parsing date.", e);
        }
    }

    @Override
    long getCount(Map<String, String> params, String[] operators) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Gson gson = new Gson();
            
            Long scrId = params.get("scrId") == null ? null : Long.valueOf(params.get("scrId"));
            String startDate = params.get("startDate");
            Date start = null;
            Date end = null;
            if (startDate != null && !startDate.isEmpty()) {
                start = sdf.parse(startDate);
            }
            String endDate = params.get("endDate");
            if (endDate != null && !endDate.isEmpty()) {
                end = sdf.parse(endDate);
            }
            Set<String> functions = gson.fromJson(params.get("functionIds"), new TypeToken<Set<String>>() {}.getType());
            if (functions.isEmpty()) {
                functions.add("");
            }
            
            return functionLogDao.count4300GridData(scrId, start, end, functions);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Failed to parsing scrId into Long value.", e);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Failed to parsing functionId into String[].", e);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parsing date.", e);
        }
    }

    public void setFunctionLogDao(IFunctionLogDao functionLogDao) {
        this.functionLogDao = functionLogDao;
    }

}
