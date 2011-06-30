package tw.com.citi.catalog.web.model;

import java.sql.Timestamp;

import tw.com.citi.catalog.web.annotation.Table;

@Table("JC_SCR_FILE")
public class ScrFile implements IModel<Long> {

    public enum FileType {
        SOURCE, EXECUTION
    }

    private Long id;
    private Scr scr;
    private String filePath;
    private String fileName;
    private BuildUnit buildUnit;
    private FileType fileType;
    private Boolean checkout;
    private Timestamp fileDatetime;
    private Long fileSize;
    private String fileMd5;
    private Timestamp lastRegisterTime;
    private Timestamp lastCompileTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setScr(Scr scr) {
        this.scr = scr;
    }

    public Scr getScr() {
        return scr;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setBuildUnit(BuildUnit buildUnit) {
        this.buildUnit = buildUnit;
    }

    public BuildUnit getBuildUnit() {
        return buildUnit;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setCheckout(Boolean checkout) {
        this.checkout = checkout;
    }

    public Boolean getCheckout() {
        return checkout;
    }

    public void setFileDatetime(Timestamp fileDatetime) {
        this.fileDatetime = fileDatetime;
    }

    public Timestamp getFileDatetime() {
        return fileDatetime;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setLastRegisterTime(Timestamp lastRegisterTime) {
        this.lastRegisterTime = lastRegisterTime;
    }

    public Timestamp getLastRegisterTime() {
        return lastRegisterTime;
    }

    public void setLastCompileTime(Timestamp lastCompileTime) {
        this.lastCompileTime = lastCompileTime;
    }

    public Timestamp getLastCompileTime() {
        return lastCompileTime;
    }
}
