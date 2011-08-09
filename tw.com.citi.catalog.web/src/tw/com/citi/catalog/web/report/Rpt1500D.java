package tw.com.citi.catalog.web.report;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.data.ListOfArrayDataSource;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;

import tw.com.citi.catalog.web.dao.IAppPathDao;
import tw.com.citi.catalog.web.dao.IScrFileDao;
import tw.com.citi.catalog.web.dto.Rpt1500Dto;
import tw.com.citi.catalog.web.dto.ScrFileDto;
import tw.com.citi.catalog.web.model.AppPath;
import tw.com.citi.catalog.web.model.FileType;
import tw.com.citi.catalog.web.util.impl.PvcsCmd;

public class Rpt1500D implements IReport {

    private IAppPathDao appPathDao;

    private IScrFileDao scrFileDao;

    @Override
    public InputStream getReportFile() {
        return getClass().getResourceAsStream("/WEB-INF/report/Rpt1500D.jasper");
    }

    @Override
    public Map<String, Object> getReportParameters() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", null);
        params.put("scrNo", req.getParameter("scrNo"));
        return params;
    }

    @Override
    public Object getReportData() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());
        long scrId = Long.valueOf(req.getParameter("scrId"));
        Long buildUnitId = "all".equalsIgnoreCase(req.getParameter("buildUnitId")) ? null : Long.valueOf(req.getParameter("buildUnitId"));
        PvcsCmd pvcs = new PvcsCmd();
        List<String[]> results = new ArrayList<String[]>();
        
        List<AppPath> qaSourcePaths = appPathDao.findByAppName(req.getParameter("appId"), AppPath.PathType.QA_SOURCE);
        String qaSourcePath = null;
        if (qaSourcePaths != null && !qaSourcePaths.isEmpty()) {
            qaSourcePath = qaSourcePaths.get(0).getPath();
        }
        List<AppPath> prodSourcePaths = appPathDao.findByAppName(req.getParameter("appId"), AppPath.PathType.PROD_SOURCE);
        List<ScrFileDto> scrFiles = scrFileDao.findBy(scrId, buildUnitId, FileType.SOURCE);
        List<Rpt1500Dto> data = new ArrayList<Rpt1500Dto>();
        for (AppPath prodSourcePath : prodSourcePaths) {
            for (ScrFileDto file : scrFiles) {
                String qaSourceFile = qaSourcePath + "\\" + file.getFilePath() + file.getFileName();
                String prodSourceFile = prodSourcePath.getPath() + "\\" + file.getFilePath() + file.getFileName();
                String result = pvcs.diff(qaSourceFile, prodSourceFile);
                results.add(new String[] { result });
            }
        }
        
        ListOfArrayDataSource ds = new ListOfArrayDataSource(results, new String[] { "diff" });
        return ds;
    }

    @Override
    public Map<String, Object> getExporterParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setAppPathDao(IAppPathDao appPathDao) {
        this.appPathDao = appPathDao;
    }

    public void setScrFileDao(IScrFileDao scrFileDao) {
        this.scrFileDao = scrFileDao;
    }

}
