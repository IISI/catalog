package tw.com.citi.catalog.web;

import java.util.List;

public class GridModel {

    private List<Object> rows;

    private long currentPage;

    private long totalPages;

    private long totalRecords;

    public List<Object> getRows() {
        return rows;
    }

    public void setRows(List<Object> rows) {
        this.rows = rows;
    }

    public long getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(long currentPage) {
        this.currentPage = currentPage;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

}
