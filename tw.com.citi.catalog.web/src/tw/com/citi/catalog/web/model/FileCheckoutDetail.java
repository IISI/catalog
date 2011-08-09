package tw.com.citi.catalog.web.model;

import tw.com.citi.catalog.web.annotation.Mapper;
import tw.com.citi.catalog.web.annotation.Table;
import tw.com.citi.catalog.web.mapper.FileCheckoutDetailMapper;

@Table("JC_FILE_CHECKOUT_DETAIL")
@Mapper(FileCheckoutDetailMapper.class)
public class FileCheckoutDetail implements IModel<Long> {

    private Long id;

    private Long jcFunctionLogId;

    private Long jcAppFileId;

    private String targetPath;

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

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getTargetPath() {
        return targetPath;
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
