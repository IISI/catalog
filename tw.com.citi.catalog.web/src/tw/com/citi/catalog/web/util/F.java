package tw.com.citi.catalog.web.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.service.exporter.OsgiServicePropertiesResolver;

import tw.com.citi.catalog.web.Activator;
import tw.com.citi.catalog.web.dao.IFunctionLogDao;

public class F {

    public enum Func {

        JCS5100, JCS5300, JCS5400, JCS1000, JCS1100, JCS1200, JCS1300, JCS1400, JCS1500, JCS1600, JCS1700, JCS1800, JCS4300

    }

    private static final Logger logger = LoggerFactory.getLogger(F.class);

    private static IFunctionLogDao functionLogDao;

    static {
        BundleContext context = Activator.getContext();
        try {
            ServiceReference[] refs = context.getServiceReferences("tw.com.citi.catalog.web.dao.IFunctionLogDao", null);
            for (ServiceReference ref : refs) {
                String beanName = (String) ref.getProperty(OsgiServicePropertiesResolver.BEAN_NAME_PROPERTY_KEY);
                if ("functionLogDao".equals(beanName)) {
                    functionLogDao = (IFunctionLogDao) context.getService(ref);
                }
            }
        } catch (InvalidSyntaxException e) {
            functionLogDao = null;
            logger.error("Cannot find FunctionLogDao.", e);
        }
    }

    public static void log(long scrId, Func function, String maker, String checker, Date start, Date end) {
        if (functionLogDao != null) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("JC_SCR_ID", scrId);
            params.put("FUNCTION_ID", function.name());
            params.put("MAKER", maker);
            params.put("CHECKER", checker);
            params.put("START_TIME", start);
            params.put("END_TIME", end);
            functionLogDao.create(params);
        }
    }

}
