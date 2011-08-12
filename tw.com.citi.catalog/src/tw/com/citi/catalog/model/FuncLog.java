package tw.com.citi.catalog.model;

import java.util.Date;

public class FuncLog {
    private String userId;
    private String funcCode;
    private Date inDate;
    private String inTime;
    private Date outDate;
    private String outTime;
    private boolean prtFlag;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFuncCode() {
        return funcCode;
    }

    public void setFuncCode(String funcCode) {
        this.funcCode = funcCode;
    }

    public Date getInDate() {
        return inDate;
    }

    public void setInDate(Date inDate) {
        this.inDate = inDate;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public Date getOutDate() {
        return outDate;
    }

    public void setOutDate(Date outDate) {
        this.outDate = outDate;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public boolean isPrtFlag() {
        return prtFlag;
    }

    public void setPrtFlag(boolean prtFlag) {
        this.prtFlag = prtFlag;
    }
}
