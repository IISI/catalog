package tw.com.citi.catalog.web.model;

import java.sql.Timestamp;

import tw.com.citi.catalog.web.annotation.Mapper;
import tw.com.citi.catalog.web.annotation.Table;
import tw.com.citi.catalog.web.mapper.ScrMapper;

@Table("JC_SCR")
@Mapper(ScrMapper.class)
public class Scr implements IModel<Long> {

    public enum Status {
        CREATE, REGISTER, MOVE_TO_QA, COMPILE, LAST_COMPILE, SOURCE_COMPARE, MOVE_TO_PROD, CHECKOUT
    }

    private Long id;
    private String scrNo;
    private Long jcAppId;
    private Timestamp createTime;
    private Status status;
    private Timestamp processTime;
    private Long jcCoordinatorId;
    private Long jcProgrammerId;
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

    public void setJcAppId(Long id) {
        this.jcAppId = id;
    }

    public Long getJcAppId() {
        return this.jcAppId;
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

    public void setJcCoordinatorId(Long id) {
        this.jcCoordinatorId = id;
    }

    public Long getJcCoordinatorId() {
        return this.jcCoordinatorId;
    }

    public void setJcProgrammerId(Long id) {
        this.jcProgrammerId = id;
    }

    public Long getJcProgrammerId() {
        return this.jcProgrammerId;
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
