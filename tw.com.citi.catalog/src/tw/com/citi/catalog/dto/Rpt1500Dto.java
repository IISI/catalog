package tw.com.citi.catalog.dto;

import java.util.Date;

public class Rpt1500Dto {

    private String action;

    private String pathName;

    private String fileName;

    private Date fileDate;

    private long fileSize;

    private boolean different;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getFileDate() {
        return fileDate;
    }

    public void setFileDate(Date fileDate) {
        this.fileDate = fileDate;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isDifferent() {
        return different;
    }

    public void setDifferent(boolean different) {
        this.different = different;
    }

}
