package tw.com.citi.catalog.dao;

import java.util.List;
import java.util.Map;

import tw.com.citi.catalog.model.User;

public interface IUserDao extends IGenericDao<User, Long> {

    User findByUserId(String userId);

    void update(Map<String, Object> params);

    void delete(Map<String, Object> params);

    User findUnique(String name, String team);

    List<byte[]> findUserBasicByUserId(String userId);

}
