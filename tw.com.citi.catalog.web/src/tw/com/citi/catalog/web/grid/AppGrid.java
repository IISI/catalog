package tw.com.citi.catalog.web.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;

import tw.com.citi.catalog.web.dao.IAppDao;
import tw.com.citi.catalog.web.dao.IAppPathDao;
import tw.com.citi.catalog.web.model.App;
import tw.com.citi.catalog.web.model.AppGridData;
import tw.com.citi.catalog.web.model.AppPath;

public class AppGrid extends AbstractGridHandler {

    private IAppDao appDao;

    private IAppPathDao appPathDao;

    @Override
    List getRows(Map<String, String> params, String[] operators, String index, String order, long start, long limit) {
        List<App> apps = appDao.find(params, operators, index, order, start, limit);
        List<AppGridData> result = new ArrayList<AppGridData>();
        for (App app : apps) {
            AppGridData data = new AppGridData();
            BeanUtils.copyProperties(app, data);
            for (AppPath path : appPathDao.findByAppId(app.getId())) {
                switch (path.getPathType()) {
                case APP_BASE:
                    data.setAppBasePath(path.getPath());
                    break;
                case QA_SOURCE:
                    data.setQaSourcePath(path.getPath());
                    break;
                case QA_EXECUTION:
                    data.setQaExecutionPath(path.getPath());
                    break;
                case PROD_BACKUP:
                    data.setProdBackupPath(path.getPath());
                    break;
                case PROD_SOURCE:
                    data.getProdSourcePath().add(path.getPath());
                    break;
                case PROD_EXECUTION:
                    data.getProdExecutionPath().add(path.getPath());
                    break;
                }
            }
            result.add(data);
        }
        return result;
    }

    @Override
    long getCount(Map<String, String> params, String[] operators) {
        return appDao.count(params, operators);
    }

    public void setAppDao(IAppDao appDao) {
        this.appDao = appDao;
    }

    public void setAppPathDao(IAppPathDao appPathDao) {
        this.appPathDao = appPathDao;
    }

}
