package tw.com.citi.catalog.web.grid;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.web.dao.IBuildUnitDao;

public class UnitGrid extends AbstractGridHandler {

    private IBuildUnitDao buildUnitDao;

    @Override
    List getRows(Map<String, String> params, String[] operators, String index, String order, long start, long limit) {
        return buildUnitDao.find(params, operators, "unit_id", order, start, limit);
    }

    @Override
    long getCount(Map<String, String> params, String[] operators) {
        return buildUnitDao.count(params, operators);
    }

    public void setBuildUnitDao(IBuildUnitDao appDao) {
        this.buildUnitDao = appDao;
    }

}
