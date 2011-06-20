package tw.com.citi.catalog.web.model;

import tw.com.citi.catalog.web.annotation.Table;

@Table("JC_APP")
public class App implements IModel<Long> {

    private Long id;
    private String appId;
    private String description;
    private String appBasePath;

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

    public void setAppBasePath(String appBasePath) {
        this.appBasePath = appBasePath;
    }

    public String getAppBasePath() {
        return appBasePath;
    }
}
