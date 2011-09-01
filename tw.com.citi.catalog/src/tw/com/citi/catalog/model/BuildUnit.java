package tw.com.citi.catalog.model;

import tw.com.citi.catalog.annotation.Table;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((jcAppId == null) ? 0 : jcAppId.hashCode());
        result = prime * result + ((unitId == null) ? 0 : unitId.hashCode());
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
        BuildUnit other = (BuildUnit) obj;
        if (jcAppId == null) {
            if (other.jcAppId != null)
                return false;
        } else if (!jcAppId.equals(other.jcAppId))
            return false;
        if (unitId == null) {
            if (other.unitId != null)
                return false;
        } else if (!unitId.equals(other.unitId))
            return false;
        return true;
    }

}
