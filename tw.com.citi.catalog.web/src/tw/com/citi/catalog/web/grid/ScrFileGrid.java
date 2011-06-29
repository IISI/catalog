package tw.com.citi.catalog.web.grid;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.web.dao.IScrFileDao;

public class ScrFileGrid extends AbstractGridHandler {

    private IScrFileDao scrFileDao;

    @Override
    List getRows(Map<String, String> params, String[] operators, String index, String order, long start, long limit) {
        return scrFileDao.find(params, operators, index, order, start, limit);
    }

    @Override
    long getCount(Map<String, String> params, String[] operators) {
        return scrFileDao.count(params, operators);
    }

    public void setScrFileDao(IScrFileDao scrFileDao) {
        this.scrFileDao = scrFileDao;
    }

}
