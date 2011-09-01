package tw.com.citi.catalog.model;

import tw.com.citi.catalog.annotation.Mapper;
import tw.com.citi.catalog.annotation.Table;
import tw.com.citi.catalog.mapper.AppPathMapper;

@Table("JC_APP_PATH")
@Mapper(AppPathMapper.class)
public class AppPath implements IModel<Long> {

    public enum PathType {
        QA_SOURCE, QA_EXECUTION, PROD_BACKUP, PROD_SOURCE, PROD_EXECUTION, APP_BASE
    }

    private Long id;
    private Long jcAppId;
    private PathType pathType;
    private String path;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPathType(PathType pathType) {
        this.pathType = pathType;
    }

    public PathType getPathType() {
        return pathType;
    }

    public void setJcAppId(Long jcAppId) {
        this.jcAppId = jcAppId;
    }

    public Long getJcAppId() {
        return jcAppId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        AppPath other = (AppPath) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }

}
