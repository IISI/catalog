package tw.com.citi.catalog.web.dao;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.web.model.Programmer;

public interface IProgrammerDao extends IGenericDao {

    long count(Map<String, String> params, String[] operators);

    List<Programmer> find(Map<String, String> params, String[] operators, String index, String order, long start, long limit);

}
