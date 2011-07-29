package tw.com.citi.catalog.web.report;

import java.io.InputStream;
import java.util.Map;

public interface IReport {

    InputStream getReportFile();

    Map<String, Object> getReportParameters();

    Object getReportData();

    Map<String, Object> getExporterParameters();

}
