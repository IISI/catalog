package tw.com.citi.catalog.web.grid;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.com.citi.catalog.dao.IBuildUnitDao;

import com.google.gson.Gson;

public class CheckoutGrid extends AbstractGridHandler {

    final Logger logger = LoggerFactory.getLogger(getClass());

	private transient Gson gson = new Gson();
	
    private IBuildUnitDao buildUnitDao;

    @Override
    List getRows(Map<String, String> params, String[] operators, String index, String order, long start, long limit) {
    	
    	String checkoutFile=params.get("checkoutFile");
    	List<Map<String,String>> resultList=parseCheckoutListFile(checkoutFile);
        return resultList;
    }

    @Override
    long getCount(Map<String, String> params, String[] operators) {
    	String checkoutFile=params.get("checkoutFile");
    	List<Map<String,String>> resultList=parseCheckoutListFile(checkoutFile);
    	return resultList.size();
    }

    
    private List<Map<String,String>> parseCheckoutListFile(String filePath){
    	List<Map<String,String>> resultsList=new ArrayList<Map<String,String>>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            if (!in.ready())
                throw new IOException();

            String line;
            while ((line = in.readLine()) != null) {

                String[] lintArr = line.split("\\|");
                String colBuildUnit = lintArr[0];
                String colBilePath = lintArr[1] + lintArr[2];
                Map<String, String> results = new HashMap<String, String>();
                results.put("filePath", colBuildUnit);
                results.put("buildUnit", colBilePath);
                resultsList.add(results);
            }

            in.close();
        } catch (IOException e) {
            logger.warn("Failed to parse check out file.", e);
            return null;
        }
    	
    	logger.debug(gson.toJson(resultsList));
    	
    	return resultsList;
    }

}
