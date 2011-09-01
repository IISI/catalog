package tw.com.citi.catalog.web.grid;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.dao.IProgrammerDao;

public class ProgrammerGrid extends AbstractGridHandler {

    private IProgrammerDao programmerDao;

    @Override
    List getRows(Map<String, String> params, String[] operators, String index,
            String order, long start, long limit) {
        return programmerDao.find(params, operators, index, order, start, limit);
    }

    @Override
    long getCount(Map<String, String> params, String[] operators) {
        return programmerDao.count(params, operators);
    }

    public void setProgrammerDao(IProgrammerDao programmerDao) {
        this.programmerDao = programmerDao;
    }

}
