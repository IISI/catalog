package tw.com.citi.catalog.web.dao;

import java.util.List;

import tw.com.citi.catalog.web.model.ScrFile;

public interface IScrFileDao extends IGenericDao<ScrFile, Long> {

    List<ScrFile> findSourceFilesByScrId(Long scrId);

    List<ScrFile> findSourceFilesByBuildUnitId(Long buildUnitId);
}
