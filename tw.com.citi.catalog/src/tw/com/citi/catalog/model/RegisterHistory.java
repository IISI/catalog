package tw.com.citi.catalog.model;

import java.sql.Timestamp;

import tw.com.citi.catalog.annotation.Table;

@Table("JC_REGISTER_HISTORY")
public class RegisterHistory implements IModel<Long> {

    private long id;

    private long jcScrId;

    private long registerCount;

    private long jcScrFileId;

    private int fileType;

    private boolean checkin;

    private int registerAction;

    private Timestamp fileDatetime;

    private long fileSize;

    private String fileMd5;

    private Timestamp registerTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public long getJcScrId() {
        return jcScrId;
    }

    public void setJcScrId(long jcScrId) {
        this.jcScrId = jcScrId;
    }

    public long getRegisterCount() {
        return registerCount;
    }

    public void setRegisterCount(long registerCount) {
        this.registerCount = registerCount;
    }

    public long getJcScrFileId() {
        return jcScrFileId;
    }

    public void setJcScrFileId(long jcScrFileId) {
        this.jcScrFileId = jcScrFileId;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public boolean isCheckin() {
        return checkin;
    }

    public void setCheckin(boolean checkin) {
        this.checkin = checkin;
    }

    public int getRegisterAction() {
        return registerAction;
    }

    public void setRegisterAction(int registerAction) {
        this.registerAction = registerAction;
    }

    public Timestamp getFileDatetime() {
        return fileDatetime;
    }

    public void setFileDatetime(Timestamp fileDatetime) {
        this.fileDatetime = fileDatetime;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public Timestamp getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Timestamp registerTime) {
        this.registerTime = registerTime;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (jcScrFileId ^ (jcScrFileId >>> 32));
        result = prime * result + (int) (jcScrId ^ (jcScrId >>> 32));
        result = prime * result
                + (int) (registerCount ^ (registerCount >>> 32));
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
        RegisterHistory other = (RegisterHistory) obj;
        if (jcScrFileId != other.jcScrFileId)
            return false;
        if (jcScrId != other.jcScrId)
            return false;
        if (registerCount != other.registerCount)
            return false;
        return true;
    }

}
