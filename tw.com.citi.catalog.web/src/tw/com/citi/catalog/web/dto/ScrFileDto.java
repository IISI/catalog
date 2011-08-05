package tw.com.citi.catalog.web.dto;

import java.sql.Timestamp;

import tw.com.citi.catalog.web.model.ScrFile.FileType;

public class ScrFileDto {

    private Long id;
    private Long jcScrId;
    private String filePath;
    private String fileName;
    private Long jcBuildUnitId;
    private Boolean checkout;
    private Timestamp fileDatetime;
    private Long fileSize;
    private String fileMd5;
    private Timestamp lastRegisterTime;
    private Timestamp lastCompileTime;
    private Boolean deleted;
    private int registerAction;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void setJcScrId(Long jcScrId) {
        this.jcScrId = jcScrId;
    }

    public Long getJcScrId() {
        return jcScrId;
    }

    public void setJcBuildUnitId(Long jcBuildUnitId) {
        this.jcBuildUnitId = jcBuildUnitId;
    }

    public Long getJcBuildUnitId() {
        return jcBuildUnitId;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public int getRegisterAction() {
        return registerAction;
    }

    public void setRegisterAction(int registerAction) {
        this.registerAction = registerAction;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
        result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
        result = prime * result + ((jcScrId == null) ? 0 : jcScrId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScrFileDto other = (ScrFileDto) obj;
        if (fileName == null) {
            if (other.fileName != null)
                return false;
        } else if (!fileName.equals(other.fileName))
            return false;
        if (filePath == null) {
            if (other.filePath != null)
                return false;
        } else if (!filePath.equals(other.filePath))
            return false;
        if (jcScrId == null) {
            if (other.jcScrId != null)
                return false;
        } else if (!jcScrId.equals(other.jcScrId))
            return false;
        return true;
    }

}
