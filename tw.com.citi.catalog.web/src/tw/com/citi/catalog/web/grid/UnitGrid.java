package tw.com.citi.catalog.web.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;

import tw.com.citi.catalog.web.dao.IBuildUnitDao;
import tw.com.citi.catalog.web.dao.IBuildUnitPathDao;
import tw.com.citi.catalog.web.model.BuildUnit;
import tw.com.citi.catalog.web.model.BuildUnitPath;
import tw.com.citi.catalog.web.model.UnitGridData;

public class UnitGrid extends AbstractGridHandler {

    private IBuildUnitDao buildUnitDao;

    private IBuildUnitPathDao buildUnitPathDao;

    @Override
    List getRows(Map<String, String> params, String[] operators, String index, String order, long start, long limit) {
        List<BuildUnit> units = buildUnitDao.findUnitGridData(params);
        List<UnitGridData> result = new ArrayList<UnitGridData>();
        for (BuildUnit unit : units) {
            UnitGridData data = new UnitGridData();
            BeanUtils.copyProperties(unit, data);
            for (BuildUnitPath path : buildUnitPathDao.findByBuildUnitId(unit.getId())) {
                switch (path.getPathType()) {
                case BuildUnitPath.QA_SOURCE:
                    data.setQaSourcePath(path.getPath());
                    break;
                case BuildUnitPath.QA_EXECUTION:
                    data.setQaExecutionPath(path.getPath());
                    break;
                case BuildUnitPath.PROD_BACKUP:
                    data.setProdBackupPath(path.getPath());
                    break;
                case BuildUnitPath.PROD_SOURCE:
                    data.getProdSourcePath().add(path.getPath());
                    break;
                case BuildUnitPath.PROD_EXECUTION:
                    data.getProdExecutionPath().add(path.getPath());
                    break;
                }
            }
            result.add(data);
        }
        return result;
    }

    @Override
    long getCount(Map<String, String> params, String[] operators) {
        return buildUnitDao.count(params, operators);
    }

    public void setBuildUnitDao(IBuildUnitDao appDao) {
        this.buildUnitDao = appDao;
    }

    public void setBuildUnitPathDao(IBuildUnitPathDao buildUnitPathDao) {
        this.buildUnitPathDao = buildUnitPathDao;
    }
}
