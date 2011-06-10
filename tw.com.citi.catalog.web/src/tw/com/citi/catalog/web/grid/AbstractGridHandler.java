package tw.com.citi.catalog.web.grid;

import java.util.List;
import java.util.Map;

import org.apache.wicket.PageParameters;

import tw.com.citi.catalog.web.GridModel;

import com.google.gson.Gson;
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
        long count = getCount(queryParams);
        
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
        grid.setRows(getRows(queryParams, index, order, start, limit));
        return new Gson().toJson(grid);
    }

    abstract List getRows(Map<String, String> params, String index, String order, long start, long limit);

    abstract long getCount(Map<String, String> params);

}
