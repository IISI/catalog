package tw.com.citi.catalog.web.grid;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.web.dao.IAppDao;

public class AppGrid extends AbstractGridHandler {

    private IAppDao appDao;

    @Override
    List getRows(Map<String, String> params, String[] operators, String index, String order, long start, long limit) {
        return appDao.find(params, operators, index, order, start, limit);
    }

    @Override
    long getCount(Map<String, String> params, String[] operators) {
        return appDao.count(params, operators);
    }

    public void setAppDao(IAppDao appDao) {
        this.appDao = appDao;
    }

}
