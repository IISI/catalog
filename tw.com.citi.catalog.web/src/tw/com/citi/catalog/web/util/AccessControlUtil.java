package tw.com.citi.catalog.web.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.osgi.service.exporter.OsgiServicePropertiesResolver;
import org.springframework.transaction.PlatformTransactionManager;

import tw.com.citi.catalog.web.Activator;
import tw.com.citi.catalog.web.dao.IFunctionLogDao;
import tw.com.citi.catalog.web.dao.IUserDao;
import tw.com.citi.catalog.web.dao.UserDao;

public class AccessControlUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(AccessControlUtil.class);
	
	private static IUserDao userDao;

    private static PlatformTransactionManager txManager;
	
    
	static {
        BundleContext context = Activator.getContext();
        try {
            ServiceReference[] refs = context.getServiceReferences("tw.com.citi.catalog.web.dao.IUserDao", null);
            for (ServiceReference ref : refs) {
                String beanName = (String) ref.getProperty(OsgiServicePropertiesResolver.BEAN_NAME_PROPERTY_KEY);
                if ("userDao".equals(beanName)) {
                	userDao = (IUserDao) context.getService(ref);
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
        	userDao = null;
            logger.error("Cannot find UserDao.", e);
        }
    }
   
	
	
    public static boolean authenticateCBCUser(String checkerId, String checkerPwd) {
        
    	String errMsg="";
    	boolean authed=false;
    	String id=checkerId;
    	String pass=checkerPwd;
    	
    	pass=PasswordUtil.encyptDualLoginPass(pass);
		pass=StringUtils.paddingZeroRight(64, pass);
		//--------------------------
		StringBuilder passwordArgument = new StringBuilder();
		passwordArgument.append(pass);
		
		int additional = 64 - passwordArgument.length();
        for (int i = 0; i < additional; i++) {
            passwordArgument.append("0");
        }

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("userId", id);
		errMsg+="SELECT * FROM SEC_USRBASIC WHERE USR_ID_C = "+ id ;
		
        List<Map<String, Object>> results = null;
        try {
            final LobHandler lobHandler = new DefaultLobHandler();
            
            //由id取得SEC_USRBASIC 並將result record的USR_PWD_C field 放入 password
            System.out.println("userDao="+userDao);
            
            results = userDao.query("SELECT * FROM SEC_USRBASIC WHERE USR_ID_C = :userId", new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String, Object> result = new HashMap<String, Object>();
                    result.put("password", lobHandler.getBlobAsBytes(rs, "USR_PWD_C"));
                    return result;
                }
            }, parameters);
           
            
            
            errMsg+=",results size="+results.size()+",";
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (results != null && results.size() > 0) {
            Map<String, Object> result = results.get(0);
            
            byte[] password = (byte[]) result.get("password");
            System.out.println("pass byte length="+password.length+",hex="+new String(Hex.encodeHex(password)));
            String encodedPassword ="";
            if(password!=null){
            	encodedPassword = new String(Hex.encodeHex(password));

            }
            
            errMsg+="get a id record,and pass="+result.get("password")+",and usr_pwd_c byte[] length="+password.length+",encodedPassword="+encodedPassword+",passwordArgument="+passwordArgument.toString()+",PasswordUtil.encodeUserPass ori_pass="+pass+","+"ori_pass="+checkerPwd+".";
            errMsg+="get a id record";
            System.out.println(errMsg);
            
            
            if (encodedPassword.equalsIgnoreCase(passwordArgument.toString())) {
                authed = true;
                errMsg+="[encodedPassword =passwordArgument].";
            }else{
            	errMsg+="[encodedPassword !=passwordArgument].";
            }
            
        }

        
        if (authed) {
        	errMsg+="id="+id+",and password varify success!";
        	
        	
        	/*
        	 * 不分checker  所以此處不用
        	 * 找grpName對應的 function list 是否包含UTY1530A function code
        	if(testCheckerGrp(parameters)){
        		r.setMsg("success");
                r.setAuthUserid(id);
        	}else{
        		logger.info("authed fail \n"+errMsg);
            	r.setMsg("fail");
        	}
        	*/
        	
        	return true;

        }else{
        	errMsg+="id="+id+",and password varify fail!";
        	System.out.println("authed fail"+errMsg);
        	return false;
        }

        
    }

    public static boolean authenticateCBCUser(Map<String, String> dataMap) {
        String checkerId = dataMap.get("checkerId");
        String checkerPwd = dataMap.get("checkerPwd");
        return authenticateCBCUser(checkerId, checkerPwd);
    }
    
   /*
    private boolean testCheckerGrp(Map<String, String> params){
		boolean isChecker=false;
		try {
            List<UtyUser> grpList = getDao().query("A002", UtyUser.class, params);
            if (grpList != null && grpList.size() > 0) {
                UtyUser user = grpList.get(0);
                String grpName = user.getGrpName();
                String userId=user.getUserId();
                StringTokenizer st = new StringTokenizer(grpName, ";");
                if (st.countTokens() > 0) {
                    StringBuffer sql = new StringBuffer();
                    sql.append("SELECT * FROM FUNCLIST WHERE FuncCode='NUTY1530A' and MAJORSYSCODE='NUTY' ");
                    int grpIndex = 0;
                    while (st.hasMoreElements()) {
                        if (grpIndex == 0) {
                            sql.append("AND ( ");
                        }else {
                            sql.append("OR ");
                        }
                        sql.append(st.nextToken()).append("=1 ");
                        grpIndex++;
                    }
                    sql.append(" )");
                    
                    List<AppFunction> functions = getDao().query(sql.toString(), new RowMapper<AppFunction>() {

                        @Override
                        public AppFunction mapRow(ResultSet rs, int rowNum) throws SQLException {
                            AppFunction func = new AppFunction();
                            func.setFuncCode(rs.getString("FUNCCODE"));
                            func.setFuncDesc(rs.getString("FUNCDESC"));
                            func.setMajorSysCode(rs.getString("MAJORSYSCODE"));
                            return func;
                        }
                    }, params, true);
                    errMsg+=",sql= "+sql.toString()+" ,";
                    if(functions.size()>0){
                    	errMsg+="success get one ,funcCode="+functions.get(0).getFuncCode()+",majorSysCode="+functions.get(0).getMajorSysCode();
                    	isChecker=true;
                    }else{
                    	errMsg+="funclist get none.";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return isChecker;	
	}
    */
    
    
}
