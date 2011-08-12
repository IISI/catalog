package tw.com.citi.catalog.web.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tw.com.citi.catalog.web.model.FunctionLog;

public interface IFunctionLogDao extends IGenericDao<FunctionLog, Long> {

    void updateEndTime(Long id, Date end);

    long count4300GridData(Long jcScrId, Date startDate, Date endDate,
            Set<String> functions);

    List<Map<String, Object>> find4300GridData(Long jcScrId, Date startDate,
            Date endDate, Set<String> functions, String index, String order,
            long start, long limit);

}
