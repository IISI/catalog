package tw.com.citi.catalog.web.model;

import tw.com.citi.catalog.web.annotation.Table;

@Table("JC_BUILD_UNIT")
public class BuildUnit implements IModel<Long> {

    private Long id;
    private Long jcAppId;
    private String unitId;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setJcAppId(Long jcAppId) {
        this.jcAppId = jcAppId;
    }

    public Long getJcAppId() {
        return jcAppId;
    }
}
