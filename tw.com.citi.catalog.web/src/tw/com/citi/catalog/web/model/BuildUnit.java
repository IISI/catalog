package tw.com.citi.catalog.web.model;

import tw.com.citi.catalog.web.annotation.Table;

@Table("JC_BUILD_UNIT")
public class BuildUnit implements IModel<Long> {

    private Long id;
    private App app;
    private String unitId;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public App getApp() {
        return app;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getUnitId() {
        return unitId;
    }
}