package tw.com.citi.catalog.web.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.web.dao.IBuildUnitDao;
import tw.com.citi.catalog.web.dao.IScrFileDao;
import tw.com.citi.catalog.web.model.BuildUnit;
import tw.com.citi.catalog.web.model.ScrFile;

public class ScrFileGrid extends AbstractGridHandler {

    private IScrFileDao scrFileDao;

    private IBuildUnitDao buildUnitDao;

    @Override
    List getRows(Map<String, String> params, String[] operators, String index, String order, long start, long limit) {
        List<String> indexes = new ArrayList<String>();
        if ("fullPathName".equalsIgnoreCase(index)) {
            indexes.add("FILE_PATH");
            indexes.add("FILE_NAME");
        } else if ("unitId".equalsIgnoreCase(index)) {
            indexes.add("JC_BUILD_UNIT_ID");
        } else {
            indexes.add(index);
        }
        List<ScrFile> scrFiles = scrFileDao.find(params, operators, indexes, order, start, limit);
        for (ScrFile scrFile : scrFiles) {
            scrFile.setFullPathName(scrFile.getFilePath() + "\\" + scrFile.getFileName());
            Long jcBuildUnitId = scrFile.getJcBuildUnitId();
            BuildUnit buildUnit = buildUnitDao.findById(jcBuildUnitId);
            scrFile.setUnitId(buildUnit == null ? "" : buildUnit.getUnitId());
        }
        return scrFiles;
    }

    @Override
    long getCount(Map<String, String> params, String[] operators) {
        return scrFileDao.count(params, operators);
    }

    public void setScrFileDao(IScrFileDao scrFileDao) {
        this.scrFileDao = scrFileDao;
    }

    public void setBuildUnitDao(IBuildUnitDao buildUnitDao) {
        this.buildUnitDao = buildUnitDao;
    }

}
