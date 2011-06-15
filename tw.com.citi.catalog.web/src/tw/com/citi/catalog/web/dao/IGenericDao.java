package tw.com.citi.catalog.web.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.web.model.IModel;

public interface IGenericDao<T extends IModel<ID>, ID extends Serializable> {

    long count(Map<String, String> params, String[] operators);

    List<T> find(Map<String, String> params, String[] operators, String index, String order, long start, long limit);

    void create(Map<String, Object> params);

}
