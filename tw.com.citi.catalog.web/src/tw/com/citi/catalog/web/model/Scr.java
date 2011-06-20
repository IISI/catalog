package tw.com.citi.catalog.web.model;

import java.sql.Timestamp;

import tw.com.citi.catalog.web.annotation.Table;

@Table("JC_SCR")
public class Scr implements IModel<Long> {

    public enum Status {
        CREATE, REGISTER, MOVE_TO_QA, COMPILE, LAST_COMPILE, SOURCE_COMPARE, MOVE_TO_PROD, CHECKOUT
    }

    private Long id;
    private String scrNo;
    private App app;
    private Timestamp createTime;
    private Status status;
    private Timestamp processTime;
    private Coordinator coordinator;
    private Programmer programmer;
    private String description;
    private Long registerCount;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setScrNo(String scrNo) {
        this.scrNo = scrNo;
    }

    public String getScrNo() {
        return scrNo;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public App getApp() {
        return app;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void setProcessTime(Timestamp processTime) {
        this.processTime = processTime;
    }

    public Timestamp getProcessTime() {
        return processTime;
    }

    public void setCoordinator(Coordinator coordinator) {
        this.coordinator = coordinator;
    }

    public Coordinator getCoordinator() {
        return coordinator;
    }

    public void setProgrammer(Programmer programmer) {
        this.programmer = programmer;
    }

    public Programmer getProgrammer() {
        return programmer;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setRegisterCount(Long registerCount) {
        this.registerCount = registerCount;
    }

    public Long getRegisterCount() {
        return registerCount;
    }

}
