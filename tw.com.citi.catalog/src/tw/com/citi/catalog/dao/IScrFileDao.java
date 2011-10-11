package tw.com.citi.catalog.dao;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.dto.Rpt1100Dto;
import tw.com.citi.catalog.dto.ScrFileDto;
import tw.com.citi.catalog.model.FileType;
import tw.com.citi.catalog.model.ScrFile;

public interface IScrFileDao extends IGenericDao<ScrFile, Long> {

    List<ScrFile> findSourceFilesByScrId(Long scrId);

    List<ScrFile> findSourceFilesByBuildUnitId(Long buildUnitId);

    List<ScrFile> findByBuildUnitIds(List<Long> buildUnitIds);

    List<ScrFile> findByScrId(Long scrId);

    ScrFile findByUK(long scrId, String filePath, String fileName);

    int update1100(Map<String, Object> params);

    List<Rpt1100Dto> find1100ReportData(long scrId);

    List<ScrFile> find1400ReportData(long scrId, Long buildUnitId);

    List<ScrFileDto> findBy(long scrId, Long buildUnitId, FileType fileType);

    int updateLastCompileTimeByJcScrId(Timestamp lastCompileTime, Long jcScrId);

    int updateFileInfoById(Timestamp fileDateTime, Long fileSize, Long id);
    
    int updateCheckOutFlag(Long id);

}
