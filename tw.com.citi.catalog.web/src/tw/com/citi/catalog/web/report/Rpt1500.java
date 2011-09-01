package tw.com.citi.catalog.web.report;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.com.citi.catalog.dao.IAppPathDao;
import tw.com.citi.catalog.dao.IScrFileDao;
import tw.com.citi.catalog.dto.Rpt1500Dto;
import tw.com.citi.catalog.dto.ScrFileDto;
import tw.com.citi.catalog.model.AppPath;
import tw.com.citi.catalog.model.FileType;
import tw.com.citi.catalog.util.DateUtil;
import tw.com.citi.catalog.web.util.F;
import tw.com.citi.catalog.web.util.SmbFileUtil;

public class Rpt1500 implements IReport {

    final Logger logger = LoggerFactory.getLogger(Rpt1500.class);

    private IAppPathDao appPathDao;

    private IScrFileDao scrFileDao;

    @Override
    public InputStream getReportFile() {
        return getClass().getResourceAsStream("/WEB-INF/report/Rpt1500.jasper");
    }

    @Override
    public Map<String, Object> getReportParameters() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", F.getCurrentUser());
        params.put("reportId", "JCS1500");
        params.put("scrNo", req.getParameter("scrNo"));
        params.put("printDate", DateUtil.format(new Date()));
        params.put("appId", req.getParameter("appId"));
        return params;
    }

    @Override
    public Object getReportData() {
        WebRequest req = ((WebRequest) RequestCycle.get().getRequest());
        long scrId = Long.valueOf(req.getParameter("scrId"));
        Long buildUnitId = "all".equalsIgnoreCase(req.getParameter("buildUnitId")) ? null : Long.valueOf(req.getParameter("buildUnitId"));
        
        List<AppPath> qaSourcePaths = appPathDao.findByAppName(req.getParameter("appId"), AppPath.PathType.QA_SOURCE);
        String qaSourcePath = null;
        if (qaSourcePaths != null && !qaSourcePaths.isEmpty()) {
            qaSourcePath = qaSourcePaths.get(0).getPath();
        }
        List<AppPath> prodSourcePaths = appPathDao.findByAppName(req.getParameter("appId"), AppPath.PathType.PROD_SOURCE);
        List<ScrFileDto> scrFiles = scrFileDao.findBy(scrId, buildUnitId, FileType.SOURCE);
        List<Rpt1500Dto> data = new ArrayList<Rpt1500Dto>();
        try {
            for (AppPath prodSourcePath : prodSourcePaths) {
                for (ScrFileDto file : scrFiles) {
                    FileObject qaSourceFile = SmbFileUtil.getFile(qaSourcePath + "\\" + file.getFilePath(), file.getFileName());
                    FileObject prodSourceFile = SmbFileUtil.getFile(prodSourcePath.getPath() + "\\" + file.getFilePath(), file.getFileName());
                    Rpt1500Dto dto = new Rpt1500Dto();
                    switch (file.getRegisterAction()) {
                    case 0:
                        dto.setAction("N");
                        break;
                    case 1:
                        dto.setAction("O");
                        break;
                    case 2:
                        dto.setAction("D");
                    }
                    dto.setFileDate(new Date(file.getFileDatetime().getTime()));
                    dto.setFileName(file.getFileName());
                    dto.setFileSize(file.getFileSize());
                    dto.setPathName(file.getFilePath());
                    if (qaSourceFile.getContent().getSize() != prodSourceFile.getContent().getSize() || qaSourceFile.getContent().getLastModifiedTime() != prodSourceFile.getContent().getLastModifiedTime()) {
                        dto.setDifferent(true);
                    } else {
                        dto.setDifferent(false);
                    }
                    data.add(dto);
                }
            }
        } catch (FileSystemException e) {
            logger.error("Failed to read file.", e);
        }
        return data;
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
