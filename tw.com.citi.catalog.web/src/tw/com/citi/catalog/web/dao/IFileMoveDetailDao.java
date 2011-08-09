package tw.com.citi.catalog.web.dao;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.web.model.FileMoveDetail;

public interface IFileMoveDetailDao extends IGenericDao<FileMoveDetail, Long> {

    List<Map<String, Object>> find1200ReportData(long functionLogId);

}
