package tw.com.citi.catalog.model;

import tw.com.citi.catalog.annotation.Mapper;
import tw.com.citi.catalog.annotation.Table;
import tw.com.citi.catalog.mapper.FileMoveDetailMapper;
import tw.com.citi.catalog.model.AppPath.PathType;

@Table("JC_FILE_MOVE_DETAIL")
@Mapper(FileMoveDetailMapper.class)
public class FileMoveDetail implements IModel<Long> {

    private Long id;

    private Long jcFunctionLogId;

    private Long jcAppFileId;

    private String targetPath;

    private ProcessResult processResult;
    
    private PathType pathType;

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

    public void setPathType(PathType pathType) {
        this.pathType = pathType;
    }

    public PathType getPathType() {
        return pathType;
    }

}
