package tw.com.citi.catalog.web.dao;

import java.util.Date;

import tw.com.citi.catalog.web.model.FunctionLog;

public interface IFunctionLogDao extends IGenericDao<FunctionLog, Long> {
    void updateEndTime(Long id, Date end);
}
