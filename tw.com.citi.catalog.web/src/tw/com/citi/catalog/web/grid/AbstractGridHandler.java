package tw.com.citi.catalog.web.grid;

import java.util.List;
import java.util.Map;

import org.apache.wicket.PageParameters;

import tw.com.citi.catalog.util.DateUtil;
import tw.com.citi.catalog.web.GridModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public abstract class AbstractGridHandler implements IGridHandler {

    private Gson gson = new Gson();

    @SuppressWarnings("unchecked")
    @Override
    public String handle(PageParameters params) {
        long page = params.getLong("page");
        long limit = params.getLong("rows");
        String index = params.getString("sidx");
        String order = params.getString("sord");
        Map<String, String> queryParams = gson.fromJson(params.getString("queryParams"), new TypeToken<Map<String, String>>() {}.getType());
        String[] queryOperators = params.getStringArray("queryOperators[]");
        if (queryParams.size() != queryOperators.length) {
            throw new IllegalArgumentException(String.format("There are %d param(s), but have %d operator(s).", queryParams.size(), queryOperators.length));
        }
        long count = getCount(queryParams, queryOperators);
        
        long totalPages = 0;
        if (count > 0) {
            totalPages = Math.round(Math.ceil(Double.valueOf(count) / Double.valueOf(limit)));
        }
        
        if (page > totalPages) {
            page = totalPages;
        }
        
        long start = limit * page - limit;
        
        GridModel grid = new GridModel();
        grid.setCurrentPage(page);
        grid.setTotalPages(totalPages);
        grid.setTotalRecords(count);
        grid.setRows(getRows(queryParams, queryOperators, index, order, start, limit));
        
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat(DateUtil.FORMAT);
        return builder.create().toJson(grid);
    }

    abstract List getRows(Map<String, String> params, String[] operators, String index, String order, long start, long limit);

    abstract long getCount(Map<String, String> params, String[] operators);

}
