package tw.com.citi.catalog;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import tw.com.citi.catalog.dao.IPcapDao;
import tw.com.citi.catalog.dao.IUserDao;
import tw.com.citi.catalog.model.PCAP;
import tw.com.citi.catalog.util.PasswordUtil;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    final Logger logger = LoggerFactory.getLogger(getClass());

	private boolean authed = false;
    private boolean dsPrepared = false;
    
    public ApplicationWorkbenchWindowAdvisor(
            IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(
            IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    public void preWindowOpen() {
    	logger.debug("catalog prewindowopen");
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        prepareDataSource();
        Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize(); 
        configurer.setInitialSize(new Point((int)dim.getWidth(), (int)dim.getHeight())); 
        //configurer.setInitialSize(new Point(600, 400));
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(false);
        configurer.setShowPerspectiveBar(true);
        
    }
    
   
    private void prepareDataSource() {
        dsPrepared = false;
        String[] args = Platform.getApplicationArgs();
        
        //logger.debug("#### Platform.getApplicationArgs() args="+args.toString());
        for(int i=0;i<args.length;i++){
        	logger.debug("#### Platform.getApplicationArgs() arg"+i+"="+args[i]);
        }
 
        String dbName = "db_pcapp";
        String dbPort = "2431";
        String serverName = "";
        String user = "usr_app";
        String password = "";
        for (String arg : args) {
            String[] keyValue = arg.split("=", 2);
            if ("dbServerName".equalsIgnoreCase(keyValue[0])) {
                serverName = keyValue[1];
                if(serverName.startsWith("0 ")){
                	serverName=serverName.substring(2);
                }
                password = serverName;
            }
            if ("dbName".equalsIgnoreCase(keyValue[0])) {
                dbName = keyValue[1];
            }
            if ("dbPort".equalsIgnoreCase(keyValue[0])) {
                dbPort = keyValue[1];
            }
            if ("dbUser".equalsIgnoreCase(keyValue[0])) {
                user = keyValue[1];
            }
            if ("dbPassword".equalsIgnoreCase(keyValue[0])) {
                password = keyValue[1];
            }
            
        }
        
        try {
            setDataSource(user, password, serverName, dbPort, dbName, "secDataSourceProxy");
        	//logger.debug("pcapDao="+pcapDao);
        	
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Initialize Error",
                    "Initialize security datasource error.");
            return;
        }
        
        

        // select from PCAP
        try {
        	logger.debug("select from PCAP start");
        	
            Map<String, String> p = new HashMap<String, String>();
            p.put("dbName", dbName);
            logger.debug("getDao="+getPcapDao());
            List<PCAP> pcaps = getPcapDao().findAll();
            
            logger.debug("### pcaps: "+pcaps); 
            logger.debug("### select pcap records size: "+pcaps.size()); 
            if (pcaps != null && pcaps.size() > 0) {

            	
                byte[] dbId = pcaps.get(0).getDbId();
                user = pcaps.get(0).getUsrName().trim();
                serverName = pcaps.get(0).getSvrName().trim();
                StringBuffer sb = new StringBuffer();
                for (int c : dbId) {
                    if (c < 0) {
                        c += 256;
                    }
                    sb.append(String.valueOf(c)).append(";");
                }
                password = PasswordUtil.decodePwd(sb.toString());
                logger.debug("### password: "+password); 
                
                
                
            }
            
            //LoggerFactory.getLogger(ApplicationWorkbenchWindowAdvisor.class).info("## : "+user); 
            //LoggerFactory.getLogger(ApplicationWorkbenchWindowAdvisor.class).info("## : "+password); 
            //LoggerFactory.getLogger(ApplicationWorkbenchWindowAdvisor.class).info("## : "+serverName); 
            //LoggerFactory.getLogger(ApplicationWorkbenchWindowAdvisor.class).info("## : "+dbPort); 
            //LoggerFactory.getLogger(ApplicationWorkbenchWindowAdvisor.class).info("## : "+dbName); 
            
            setDataSource(user, password, serverName, dbPort, dbName, "dataSourceProxy");
            
            
        
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        dsPrepared = true;
        logger.debug(" ^^^^^ dsPrepared="+dsPrepared);
        //LoggerFactory.getLogger(ApplicationWorkbenchWindowAdvisor.class).info("## dsPrepared: "+dsPrepared); 
    }
    
    @Override
    public void postWindowCreate() {
        super.postWindowCreate();

        if (!dsPrepared) {
            MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Initialize Error",
                    "Initialize app datasource error.");
            return;
        }

        Map<String, String> parameters = new HashMap<String, String>();
        StringBuilder passwordArgument = new StringBuilder();
        String[] args = Platform.getApplicationArgs();
        for (String arg : args) {
            String[] keyValue = arg.split("=", 2);
            if ("userId".equalsIgnoreCase(keyValue[0])) {
                parameters.put("userId", keyValue[1]);
            }
            if ("userPassword".equalsIgnoreCase(keyValue[0])) {
                passwordArgument.append(keyValue[1]);
            }
        }

        int additional = 64 - passwordArgument.length();
        for (int i = 0; i < additional; i++) {
            passwordArgument.append("0");
        }

        List<byte[]> results;
        try {
            results = getUserDao().findUserBasicByUserId(parameters.get("userId"));
        } catch (Exception e) {
            throw new RuntimeException("Can not find UserDao object.", e);
        }
        
        if (results != null && results.size() > 0) {
            byte[] password = results.get(0);
            
            String encodedPassword = new String(Hex.encodeHex(password));
            if (encodedPassword.equalsIgnoreCase(passwordArgument.toString())) {
                authed = true;
            }
            
        }else{
        	LoggerFactory.getLogger("No record from SEC_USRBASIC.");
        }

        if (!authed) {
            MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Access Denied",
                    "User is not authorized to use this application.");
            LoggerFactory.getLogger("Find one record from SEC_USRBASIC, invalid password");
        }
    }
    
    @Override
    public void postWindowOpen() {
        super.postWindowOpen();
        if (!authed) {
            PlatformUI.getWorkbench().close();
        }
    }

    private IUserDao getUserDao() throws Exception {
        BundleContext bc = Platform.getBundle(Activator.PLUGIN_ID).getBundleContext();
        ServiceReference[] daoRefs = bc.getServiceReferences(IUserDao.class.getName(),
                "(org.springframework.osgi.bean.name=userDao)");
        return (IUserDao) bc.getService(daoRefs[0]);
    }

    private IPcapDao getPcapDao() throws Exception {
        BundleContext bc = Platform.getBundle(Activator.PLUGIN_ID).getBundleContext();
        ServiceReference[] daoRefs = bc.getServiceReferences(IPcapDao.class.getName(),
                "(org.springframework.osgi.bean.name=pcapDao)");
        return (IPcapDao) bc.getService(daoRefs[0]);
    }

    private DelegatingDataSource getDataSource(String beanName) throws Exception {
        BundleContext bc = Platform.getBundle(Activator.PLUGIN_ID).getBundleContext();
        
        logger.debug("beanName="+beanName);
        ServiceReference[] refs = bc.getServiceReferences(DelegatingDataSource.class.getName(),
                "(org.springframework.osgi.bean.name=" + beanName + ")");
        logger.debug("af refs="+refs);
        while (refs == null) {
            refs = bc.getServiceReferences(DelegatingDataSource.class.getName(), "(org.springframework.osgi.bean.name="
                    + beanName + ")");
            Thread.sleep(1000);
        }
        DelegatingDataSource dataSource = (DelegatingDataSource) bc.getService(refs[0]);
        logger.debug("af dataSource="+dataSource);
        return dataSource;
    }
    
    private void setDataSource(String user, String password, String serverName, String serverPort, String dbName,
            String dataSourceBeanName) throws Exception {
    	
    	DelegatingDataSource ds = null;
    	logger.debug("bf get datasource");
        ds = getDataSource(dataSourceBeanName);
        logger.debug("af get datasource");
        //BasicDataSource bds = new BasicDataSource();
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        cpds.setDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        //bds.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        
        /*
        cpds.setCheckoutTimeout(10000);
        cpds.setUser(user);
        cpds.setPassword(password);
        cpds.setJdbcUrl("jdbc:sqlserver://" + serverName + ":" + serverPort + ";databaseName=" + dbName);
        */
        cpds.setCheckoutTimeout(0);
        cpds.setUser(user);
        cpds.setPassword(password);
        cpds.setJdbcUrl("jdbc:sqlserver://" + serverName + ":" + serverPort + ";databaseName=" + dbName);
        
        logger.debug("cpds jdbc="+cpds.getJdbcUrl());
        logger.debug("cpds user="+cpds.getUser());
        logger.debug("cpds pass="+cpds.getPassword());
        cpds.setForceIgnoreUnresolvedTransactions(true);
        cpds.setIdleConnectionTestPeriod(10800);
        cpds.setMinPoolSize(0);
        
        
        /*
        bds.setUsername(user);
        bds.setPassword(password);
        bds.setUrl("jdbc:sqlserver://" + serverName + ":" + serverPort + ";databaseName=" + dbName);
        bds.setMaxActive( 50 );
        bds.setMaxIdle( 10 );
        bds.setMaxWait( 10000 ); // 10 seconds
       */
        
        ds.setTargetDataSource(cpds);
    }

}
