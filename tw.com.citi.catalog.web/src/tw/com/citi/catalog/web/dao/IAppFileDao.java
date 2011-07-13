package tw.com.citi.catalog.web.dao;

import java.util.List;

import tw.com.citi.catalog.web.model.AppFile;

public interface IAppFileDao extends IGenericDao<AppFile, Long> {
    List<AppFile> findByBuildUnitIds(List<Long> buildUnitIds);
}
