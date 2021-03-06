package tw.com.citi.catalog.web.pages;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import tw.com.citi.catalog.dao.IAppDao;
import tw.com.citi.catalog.dao.IAppPathDao;
import tw.com.citi.catalog.dao.IBuildUnitDao;
import tw.com.citi.catalog.dao.IScrDao;
import tw.com.citi.catalog.model.App;
import tw.com.citi.catalog.model.AppPath;
import tw.com.citi.catalog.model.BuildUnit;
import tw.com.citi.catalog.model.Scr;
import tw.com.citi.catalog.model.Scr.Status;
import tw.com.citi.catalog.web.grid.IGridHandler;
import tw.com.citi.catalog.web.util.AccessControlUtil;
import tw.com.citi.catalog.web.util.F;
import tw.com.citi.catalog.web.util.F.Func;

import com.google.gson.Gson;

public class JCS1500 extends AbstractBasePage {

    @SpringBean(name = "scrDao")
    private IScrDao scrDao;

    @SpringBean(name = "appDao")
    private IAppDao appDao;

    @SpringBean(name = "appPathDao")
    private IAppPathDao appPathDao;

    @SpringBean(name = "buildUnitDao")
    private IBuildUnitDao buildUnitDao;

    @SpringBean(name = "scrFileGrid")
    private IGridHandler scrFileGrid;

    private transient Gson gson = new Gson();

    @Override
    public IGridHandler getGridHandler(String handlerName) {
        if ("scrFileGrid".equals(handlerName)) {
            return scrFileGrid;
        }
        return super.getGridHandler(handlerName);
    }

    @Override
    public String handleRequest(PageParameters params) {
        String actionName = params.getString("actionName");
        if ("findScrNo".equals(actionName)) {
            return findScrNo();
        } else if ("getScrInfo".equals(actionName)) {
            Long scrId = params.getAsLong("actionParams[scrId]");
            return getScrInfo(scrId);
        } else if ("findBuildUnit".equals(actionName)) {
            Long scrId = params.getAsLong("actionParams[scrId]");
            return findBuildUnit(scrId);
        } else if ("compare".equals(actionName)) {
            String checkerId = params.getString("actionParams[checkerId]");
            String checkerPwd = params.getString("actionParams[checkerPwd]");
            boolean authenticated = AccessControlUtil.authenticateCBCUser(checkerId, checkerPwd);
            if (!authenticated) {
                throw new RuntimeException("ID/Password is invalid.");
            }
            long scrId = params.getAsLong("actionParams[scrId]");
            long functionLogId = F.log(scrId, Func.JCS1500, checkerId, new Date(), null);
            scrDao.updateStatus(scrId, Status.SOURCE_COMPARE);
            F.updateEndTime(functionLogId, new Date());
        }
        return null;
    }

    private String findScrNo() {
        List<Scr> scrList = scrDao.findActiveScr();
        StringBuilder sb = new StringBuilder();
        for (Scr scr : scrList) {
            sb.append("<option value='").append(scr.getId()).append("'>");
            sb.append(scr.getScrNo());
            sb.append("</option>");
        }
        return sb.toString();
    }

    private String getScrInfo(Long scrId) {
        Scr scr = scrDao.findById(scrId);
        App app = appDao.findById(scr.getJcAppId());
        Map<AppPath.PathType, Object> paths = appPathDao.getAppPathsByAppId(app.getId());
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("appId", app.getAppId());
        results.put("status", scr.getStatus().name());
        results.put("QASourcePath", paths.get(AppPath.PathType.QA_SOURCE));
        List<String> prodPaths = (List<String>) paths.get(AppPath.PathType.PROD_SOURCE);
        StringBuilder sb = new StringBuilder();
        for (String prodPath : prodPaths) {
            sb.append(prodPath).append("<br />");
        }
        results.put("prodSourcePath", sb.toString());
        return gson.toJson(results);
    }

    private String findBuildUnit(Long scrId) {
        Scr scr = scrDao.findById(scrId);
        List<BuildUnit> buildUnitList = buildUnitDao.findByAppId(scr.getJcAppId());
        StringBuilder sb = new StringBuilder();
        sb.append("<option value='all'>&nbsp;</option>");
        for (BuildUnit unit : buildUnitList) {
            sb.append("<option value='").append(unit.getId()).append("'>");
            sb.append(unit.getUnitId());
            sb.append("</option>");
        }
        return sb.toString();
    }

}
