package tw.com.citi.catalog.web.grid;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.dao.ICoordinatorDao;

public class CoordinatorGrid extends AbstractGridHandler {

    private ICoordinatorDao coordinatorDao;

    @Override
    List getRows(Map<String, String> params, String[] operators, String index, String order, long start, long limit) {
        return coordinatorDao.find(params, operators, index, order, start, limit);
    }

    @Override
    long getCount(Map<String, String> params, String[] operators) {
        return coordinatorDao.count(params, operators);
    }

    public void setCoordinatorDao(ICoordinatorDao coordinatorDao) {
        this.coordinatorDao = coordinatorDao;
    }

}
