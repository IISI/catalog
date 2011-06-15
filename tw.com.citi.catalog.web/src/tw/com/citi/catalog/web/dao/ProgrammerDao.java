package tw.com.citi.catalog.web.dao;

import tw.com.citi.catalog.web.model.Programmer;

public class ProgrammerDao extends AbstractGenericDao<Programmer, Long> implements IProgrammerDao {

    @Override
    protected String getTableName() {
        return "JC_PROGRAMMER";
    }

}
