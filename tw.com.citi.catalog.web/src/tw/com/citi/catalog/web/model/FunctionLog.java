package tw.com.citi.catalog.web.model;

import java.sql.Timestamp;

import tw.com.citi.catalog.web.annotation.Table;

@Table("JC_FUNCTION_LOG")
public class FunctionLog implements IModel<Long> {

    private Long id;

    private Long jcScrId;

    private String functionId;

    private String maker;

    private String checker;

    private Timestamp startTime;

    private Timestamp endTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getJcScrId() {
        return jcScrId;
    }

    public void setJcScrId(Long jcScrId) {
        this.jcScrId = jcScrId;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getMaker() {
        return maker;
    }

    public void setMaker(String maker) {
        this.maker = maker;
    }

    public String getChecker() {
        return checker;
    }

    public void setChecker(String checker) {
        this.checker = checker;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

}
