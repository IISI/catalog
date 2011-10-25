package tw.com.citi.catalog.web.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        } else {
            throw new RuntimeException("Cannot get qaSourcePath.");
        }
        int rc = 0;
        String qa = null;
        String prod = null;
        List<AppPath> prodSourcePaths = appPathDao.findByAppName(req.getParameter("appId"),
                AppPath.PathType.PROD_SOURCE);
        for (AppPath prodSourcePath : prodSourcePaths) {
            if ((rc = NetUseUtil.disconnectAllNetworkPath()) == 0) {
                if ((rc += NetUseUtil.connectNetworkDrive(qaSourcePath)) == 0) {
                    qa = NetUseUtil.mappingLocalPath(qaSourcePath);
                }
                if ((rc += NetUseUtil.connectNetworkDrive(prodSourcePath.getPath())) == 0) {
                    prod = NetUseUtil.mappingLocalPath(prodSourcePath.getPath());
                }
            }
            if (rc == 0) {
                pvcs.diff(qa, prod, prodSourcePath.getPath().replace('\\', '_'));
            } else {
                throw new RuntimeException("Cannot map qaSourcePath or prodSourcePath to local driver.");
            }
        }
        File f;
        FileInputStream fis;
        BufferedReader br;
        for (AppPath prodSourcePath : prodSourcePaths) {
            results.add(new String[] { "\n====================================================================================================" });
            results.add(new String[] { "QA   Source Path: " + qaSourcePath });
            results.add(new String[] { "PROD Source Path: " + prodSourcePath.getPath() });
            results.add(new String[] { "====================================================================================================\n" });
            f = new File("C:\\Temp\\" + prodSourcePath.getPath().replace('\\', '_') + ".diff");
            try {
                fis = new FileInputStream(f);
                br = new BufferedReader(new InputStreamReader(fis));
                String line = "";
                while ((line = br.readLine()) != null) {
                    results.add(new String[] { line });
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        ListOfArrayDataSource ds = new ListOfArrayDataSource(results, new String[] { "diff" });
        return ds;
    }

    @Override
    public Map<String, Object> getExporterParameters() {
        return null;
    }

    public void setAppPathDao(IAppPathDao appPathDao) {
        this.appPathDao = appPathDao;
    }

}
