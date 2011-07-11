package tw.com.citi.catalog.web.model;

import tw.com.citi.catalog.web.annotation.Table;

@Table("JC_APP")
public class App implements IModel<Long> {

    private Long id;
    private String appId;
    private String description;
    private String pvcsProjDb;
    private String pvcsProjPath;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setPvcsProjDb(String pvcsProjDb) {
        this.pvcsProjDb = pvcsProjDb;
    }

    public String getPvcsProjDb() {
        return pvcsProjDb;
    }

    public void setPvcsProjPath(String pvcsProjPath) {
        this.pvcsProjPath = pvcsProjPath;
    }

    public String getPvcsProjPath() {
        return pvcsProjPath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appId == null) ? 0 : appId.hashCode());
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
        App other = (App) obj;
        if (appId == null) {
            if (other.appId != null)
                return false;
        } else if (!appId.equals(other.appId))
            return false;
        return true;
    }

}
