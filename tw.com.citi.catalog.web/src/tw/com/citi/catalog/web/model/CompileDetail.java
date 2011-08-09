package tw.com.citi.catalog.web.model;

import tw.com.citi.catalog.web.annotation.Mapper;
import tw.com.citi.catalog.web.annotation.Table;
import tw.com.citi.catalog.web.mapper.CompileDetailMapper;

@Table("JC_COMPILE_DETAIL")
@Mapper(CompileDetailMapper.class)
public class CompileDetail implements IModel<Long> {

    private Long id;

    private Long jcFunctionLogId;

    private Long jcAppFileId;

    private ProcessResult processResult;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setJcFunctionLogId(Long jcFunctionLogId) {
        this.jcFunctionLogId = jcFunctionLogId;
    }

    public Long getJcFunctionLogId() {
        return jcFunctionLogId;
    }

    public void setProcessResult(ProcessResult processResult) {
        this.processResult = processResult;
    }

    public ProcessResult getProcessResult() {
        return processResult;
    }

    public void setJcAppFileId(Long jcAppFileId) {
        this.jcAppFileId = jcAppFileId;
    }

    public Long getJcAppFileId() {
        return jcAppFileId;
    }

}
