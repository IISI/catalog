package tw.com.citi.catalog.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.model.IModel;

public interface IGenericDao<T extends IModel<ID>, ID extends Serializable> {

    long count(Map<String, String> params, String[] operators);

    List<T> find(Map<String, String> params, String[] operators, String index, String order, long start, long limit);

    /**
     * order by 多個欄位
     * 
     * @param params
     * @param operators
     * @param indexs
     *            order by 的欄位
     * @param order
     * @param start
     * @param limit
     * @return
     */
    List<T> find(Map<String, String> params, String[] operators, List<String> indexes, String order, long start,
            long limit);

    Long create(Map<String, Object> params);

    List<T> findAll();

    T findById(ID id);

}
