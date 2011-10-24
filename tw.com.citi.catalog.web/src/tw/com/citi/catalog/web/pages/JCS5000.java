package tw.com.citi.catalog.web.pages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jcifs.smb.SmbAuthException;

import org.apache.commons.vfs.FileSystemException;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import tw.com.citi.catalog.conf.Jcifs;
import tw.com.citi.catalog.dao.IAppPathDao;
import tw.com.citi.catalog.dao.IParamsDao;
import tw.com.citi.catalog.model.AppPath;
import tw.com.citi.catalog.model.Params;
import tw.com.citi.catalog.web.util.SmbFileUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JCS5000 extends AbstractBasePage {

    @SpringBean(name = "paramsDao")
    private IParamsDao paramsDao;

    @SpringBean(name = "appPathDao")
    private IAppPathDao appPathDao;

    private transient Gson gson = new Gson();

    @Override
    public String handleRequest(PageParameters params) {
        String actionName = params.getString("actionName");
        String actionParams = params.getString("actionParams");
        Map dataMap = gson.fromJson(actionParams, new TypeToken<Map<String, String>>() {
        }.getType());
        String result;
        if ("Init".equals(actionName)) {
            result = init(dataMap);
        } else if ("Verify".equals(actionName)) {
            result = verify(dataMap);
        } else {
            throw new IllegalArgumentException("Cannot find actionName: " + actionName);
        }
        return result;
    }

    private String init(Map dataMap) {
        String domain = null;
        String username = null;
        Map<String, Object> data = new HashMap<String, Object>();
        List<Params> params = paramsDao.findAll();
        if (params != null) {
            for (Params param : params) {
                if ("domain".equalsIgnoreCase(param.getpKey())) {
                    domain = param.getpValue();
                } else if ("username".equalsIgnoreCase(param.getpKey())) {
                    username = param.getpValue();
                }
            }
        }
        data.put("domain", domain);
        data.put("username", username);
        return gson.toJson(data);
    }

    private String verify(Map dataMap) {
        String domain = (String) dataMap.get("domain");
        String username = (String) dataMap.get("username");
        String userpassword = (String) dataMap.get("userpassword");
        String oldDomain = Jcifs.getDomain();
        String oldUsername = Jcifs.getUsername();
        String oldUserpassword = Jcifs.getUserpasswordWithoutCheck();

        AppPath appPath = appPathDao.findLastestPath();
        Jcifs.setDomain(domain);
        Jcifs.setUsername(username);
        Jcifs.setUserpassword(userpassword);
        SmbFileUtil.init();
        boolean save = false;
        String status = "";
        try {
            if (appPath == null || SmbFileUtil.accessCheck(appPath.getPath())) {
                save = true;
                if (appPath == null) {
                    status = "There is no path to verify. JCS only saves the information.";
                } else {
                    status = "Verify OK.";
                }
            } else {
                Jcifs.setDomain(oldDomain);
                Jcifs.setUsername(oldUsername);
                Jcifs.setUserpassword(oldUserpassword);
                SmbFileUtil.init();
            }
            if (save) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("PKEY", "DOMAIN");
                params.put("PVALUE", domain);
                if (paramsDao.findByPKey("DOMAIN") != null) {
                    paramsDao.update(params);
                } else {
                    paramsDao.create(params);
                }
                params.put("PKEY", "USERNAME");
                params.put("PVALUE", username);
                if (paramsDao.findByPKey("USERNAME") != null) {
                    paramsDao.update(params);
                } else {
                    paramsDao.create(params);
                }
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
            logger.error("Failed to check share folder information.", e);
            if (e.getCause() instanceof SmbAuthException) {
                throw new RuntimeException("User name or password is invalid.");
            } else {
                throw new RuntimeException("Failed to check access.");
            }
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("status", status);
        return gson.toJson(data);
    }
}
