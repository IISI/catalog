package tw.com.citi.catalog.web.report;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;
import org.apache.wicket.util.resource.IResourceStream;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.service.exporter.OsgiServicePropertiesResolver;

import tw.com.citi.catalog.web.Activator;

public class PdfReportResource extends WebResource {

    private static final long serialVersionUID = 1L;

    public interface ReportServiceCallback {

        IReport getReport();

        void ungetReport();

    }

    public class PdfResourceStreamWriter extends AbstractResourceStreamWriter {

        private static final long serialVersionUID = 1L;

        private ReportServiceCallback reportService;

        public PdfResourceStreamWriter(ReportServiceCallback reportService) {
            this.reportService = reportService;
        }

        @Override
        synchronized public void write(OutputStream output) {
            try {
                // It seems jasper reports and groovy has some issues when
                // using under osgi environment, hence the following code is
                // commented out for now.
                // JasperReport report =
                // JasperCompileManager.compileReport(getReport());
                IReport report = reportService.getReport();
                JasperReport jasperReport = (JasperReport) JRLoader
                        .loadObject(report.getReportFile());
                JRDataSource reportData = convertReportData(report.getReportData());
                if (report.getExporterParameters() != null
                        && !report.getExporterParameters().isEmpty()) {
                    renderAsPdf(jasperReport,
                            report.getReportParameters(), reportData, output,
                            report.getExporterParameters());
                } else {
                    renderAsPdf(jasperReport,
                            report.getReportParameters(), reportData, output);
                }
            } catch (IllegalArgumentException e) {
                logger.error("Failed to convert report data.", e);
            } catch (JRException e) {
                logger.error("Failed to generate report.", e);
            } finally {
                reportService.ungetReport();
            }
        }

        @Override
        public String getContentType() {
            return "application/pdf";
        }

    }

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public IResourceStream getResourceStream() {
        try {
            String reportName = getParameters().getString("reportName");
            final BundleContext context = Activator.getContext();
            ServiceReference[] refs = context.getServiceReferences("tw.com.citi.catalog.web.report.IReport", null);
            for (final ServiceReference ref : refs) {
                String beanName = (String) ref.getProperty(OsgiServicePropertiesResolver.BEAN_NAME_PROPERTY_KEY);
                if (beanName != null && beanName.equalsIgnoreCase(reportName)) {
                    return new PdfResourceStreamWriter(new ReportServiceCallback() {

                        @Override
                        public IReport getReport() {
                            return (IReport) context.getService(ref);
                        }

                        @Override
                        public void ungetReport() {
                            context.ungetService(ref);
                        }
                    });
                }
            }
            throw new RuntimeException("Cannot find report.");
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static JRDataSource convertReportData(Object value)
            throws IllegalArgumentException {
        if (value instanceof JRDataSource) {
            return (JRDataSource) value;
        } else if (value instanceof Collection) {
            return new JRBeanCollectionDataSource((Collection) value);
        } else if (value instanceof Object[]) {
            return new JRBeanArrayDataSource((Object[]) value);
        } else {
            throw new IllegalArgumentException("Value [" + value
                    + "] cannot be converted to a JRDataSource");
        }
    }

    private static void render(JRExporter exporter, JasperPrint print,
            OutputStream outputStream) throws JRException {
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, outputStream);
        exporter.exportReport();
    }

    private static void renderAsPdf(JasperReport report, Map parameters,
            Object reportData, OutputStream stream) throws JRException {
        JasperPrint print = JasperFillManager.fillReport(report, parameters,
                convertReportData(reportData));
        render(new JRPdfExporter(), print, stream);
    }

    private static void renderAsPdf(JasperReport report, Map parameters,
            Object reportData, OutputStream stream, Map exporterParameters)
            throws JRException {
        JasperPrint print = JasperFillManager.fillReport(report, parameters,
                convertReportData(reportData));
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setParameters(exporterParameters);
        render(exporter, print, stream);
    }

}
