package tw.com.citi.catalog.web.grid;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.web.dao.IProgrammerDao;

public class ProgrammerGrid extends AbstractGridHandler {

    private IProgrammerDao programmerDao;

    @Override
    List getRows(Map<String, String> params, String index, String order,
            long start, long limit) {
        return programmerDao.find(params, index, order, start, limit);
    }

    @Override
    long getCount(Map<String, String> params) {
        return programmerDao.count(params);
    }

    public void setProgrammerDao(IProgrammerDao programmerDao) {
        this.programmerDao = programmerDao;
    }

}
