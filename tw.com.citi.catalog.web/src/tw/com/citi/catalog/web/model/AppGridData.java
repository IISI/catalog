package tw.com.citi.catalog.web.model;

import java.util.ArrayList;
import java.util.List;

public class AppGridData extends App {
    private String appBasePath;
    private String qaSourcePath;
    private String qaExecutionPath;
    private String prodBackupPath;
    private List<String> prodSourcePath = new ArrayList<String>();
    private List<String> prodExecutionPath = new ArrayList<String>();

    public String getQaSourcePath() {
        return qaSourcePath;
    }

    public void setQaSourcePath(String qaSourcePath) {
        this.qaSourcePath = qaSourcePath;
    }

    public String getQaExecutionPath() {
        return qaExecutionPath;
    }

    public void setQaExecutionPath(String qaExecutionPath) {
        this.qaExecutionPath = qaExecutionPath;
    }

    public String getProdBackupPath() {
        return prodBackupPath;
    }

    public void setProdBackupPath(String prodBackupPath) {
        this.prodBackupPath = prodBackupPath;
    }

    public List<String> getProdSourcePath() {
        return prodSourcePath;
    }

    public void setProdSourcePath(List<String> prodSourcePath) {
        this.prodSourcePath = prodSourcePath;
    }

    public List<String> getProdExecutionPath() {
        return prodExecutionPath;
    }

    public void setProdExecutionPath(List<String> prodExecutionPath) {
        this.prodExecutionPath = prodExecutionPath;
    }

    public void setAppBasePath(String appBasePath) {
        this.appBasePath = appBasePath;
    }

    public String getAppBasePath() {
        return appBasePath;
    }

}
