package tw.com.citi.catalog.dao;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.model.CompileDetail;

public interface ICompileDetailDao extends IGenericDao<CompileDetail, Long> {

    List<Map<String, Object>> find1300LReportData(long functionLogId);

}
