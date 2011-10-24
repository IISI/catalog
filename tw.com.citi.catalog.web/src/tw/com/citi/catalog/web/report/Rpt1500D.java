package tw.com.citi.catalog.web.report;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.data.ListOfArrayDataSource;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;

import tw.com.citi.catalog.dao.IAppPathDao;
import tw.com.citi.catalog.model.AppPath;
import tw.com.citi.catalog.web.util.F;
import tw.com.citi.catalog.web.util.NetUseUtil;
import tw.com.citi.catalog.web.util.impl.PvcsCmd;

public class Rpt1500D implements IReport {

    private IAppPathDao appPathDao;

    @Override
    public InputStream getReportFile() {
        return getClass().getResourceAsStream("/WEB-INF/report/Rpt1500D.jasper");
    }

    @Override
    public Map<String, Object> getReportParameters() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", F.getCurrentUser());
        params.put("scrNo", req.getParameter("scrNo"));
        return params;
    }

    @Override
    public Object getReportData() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());
        PvcsCmd pvcs = new PvcsCmd();
        List<String[]> results = new ArrayList<String[]>();
        
        List<AppPath> qaSourcePaths = appPathDao.findByAppName(req.getParameter("appId"), AppPath.PathType.QA_SOURCE);
        String qaSourcePath = null;
        if (qaSourcePaths != null && !qaSourcePaths.isEmpty()) {
            qaSourcePath = qaSourcePaths.get(0).getPath();
            connectRemotePath(qaSourcePath);
        }
        List<AppPath> prodSourcePaths = appPathDao.findByAppName(req.getParameter("appId"), AppPath.PathType.PROD_SOURCE);
        for (AppPath prodSourcePath : prodSourcePaths) {
            connectRemotePath(prodSourcePath.getPath());
            String result = pvcs.diff(qaSourcePath, prodSourcePath.getPath());
            results.add(new String[] { result });
        }
        
        ListOfArrayDataSource ds = new ListOfArrayDataSource(results, new String[] { "diff" });
        return ds;
    }

    @Override
    public Map<String, Object> getExporterParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    private String connectRemotePath(String path) {
        String mappingLocalPath = NetUseUtil.mappingLocalPath(path);
        int rc = 0;
        if (!localPathWritable(mappingLocalPath)) {
            if ((rc = NetUseUtil.disconnectAllNetworkPath()) == 0) {
                rc += NetUseUtil.connectNetworkDrive(path);
            }
            if (rc == 0) {
                return NetUseUtil.mappingLocalPath(path);
            } else {
                throw new RuntimeException("Cannot map qaSourcePath to local driver.");
            }
        } else {
            return mappingLocalPath;
        }
    }

    private boolean localPathWritable(String path) {
        boolean tf = false;
        if (!"".equals(path)) {
            String tmpFileName = String.valueOf(System.currentTimeMillis()) + ".tmp";
            File f = new File(path + tmpFileName);
            try {
                tf = f.createNewFile() && f.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tf;
    }

    public void setAppPathDao(IAppPathDao appPathDao) {
        this.appPathDao = appPathDao;
    }

}
