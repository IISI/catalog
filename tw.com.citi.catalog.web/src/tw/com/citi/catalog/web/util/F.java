package tw.com.citi.catalog.web.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.service.exporter.OsgiServicePropertiesResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import tw.com.citi.catalog.web.Activator;
import tw.com.citi.catalog.web.dao.IFunctionLogDao;

public class F {

    public enum Func {

        JCS5100, JCS5300, JCS5400, JCS1000, JCS1100, JCS1200, JCS1300, JCS1400, JCS1500, JCS1600, JCS1700, JCS1800, JCS4300

    }

    private static final Logger logger = LoggerFactory.getLogger(F.class);

    private static IFunctionLogDao functionLogDao;

    private static PlatformTransactionManager txManager;

    static {
        BundleContext context = Activator.getContext();
        try {
            ServiceReference[] refs = context.getServiceReferences("tw.com.citi.catalog.web.dao.IFunctionLogDao", null);
            for (ServiceReference ref : refs) {
                String beanName = (String) ref.getProperty(OsgiServicePropertiesResolver.BEAN_NAME_PROPERTY_KEY);
                if ("functionLogDao".equals(beanName)) {
                    functionLogDao = (IFunctionLogDao) context.getService(ref);
                    break;
                }
            }
            refs = context.getServiceReferences("org.springframework.transaction.PlatformTransactionManager", null);
            for (ServiceReference ref : refs) {
                String beanName = (String) ref.getProperty(OsgiServicePropertiesResolver.BEAN_NAME_PROPERTY_KEY);
                if ("txManager".equals(beanName)) {
                    txManager = (PlatformTransactionManager) context.getService(ref);
                    break;
                }
            }
        } catch (InvalidSyntaxException e) {
            functionLogDao = null;
            logger.error("Cannot find FunctionLogDao.", e);
        }
    }

    public static Long log(final long scrId, final Func function, final String checker, final Date start, final Date end) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(txManager, new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        return transactionTemplate.execute(new TransactionCallback<Long>() {

            @Override
            public Long doInTransaction(TransactionStatus status) {
                Long functionLogId = null;
                if (functionLogDao != null) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("JC_SCR_ID", scrId);
                    params.put("FUNCTION_ID", function.name());
                    params.put("MAKER", F.getCurrentUser());
                    params.put("CHECKER", checker);
                    params.put("START_TIME", start);
                    params.put("END_TIME", end);
                    functionLogId = functionLogDao.create(params);
                }
                return functionLogId;
            }
        });
    }

    public static void updateEndTime(final Long functionLogId, final Date end) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(txManager, new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        transactionTemplate.execute(new TransactionCallback<Void>() {

            @Override
            public Void doInTransaction(TransactionStatus arg0) {
                if (functionLogDao != null) {
                    functionLogDao.updateEndTime(functionLogId, end);
                }
                return null;
            }
        });
    }

    public static String getCurrentUser() {
        String[] args = Platform.getApplicationArgs();
        for (String arg : args) {
            if (arg.startsWith("userId")) {
                String[] userId = arg.split("=");
                return userId[1];
            }
        }
        throw new RuntimeException("Can not find logged in user.");
    }

}
