package tw.com.citi.catalog.web.dao;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.web.dto.Rpt1100Dto;
import tw.com.citi.catalog.web.model.ScrFile;

public interface IScrFileDao extends IGenericDao<ScrFile, Long> {

    List<ScrFile> findSourceFilesByScrId(Long scrId);

    List<ScrFile> findSourceFilesByBuildUnitId(Long buildUnitId);

    List<ScrFile> findByBuildUnitIds(List<Long> buildUnitIds);

    List<ScrFile> findByScrId(Long scrId);

    ScrFile findByUK(long scrId, String filePath, String fileName);

    int update1100(Map<String, Object> params);

    List<Rpt1100Dto> find1100ReportData(long scrId);

}
