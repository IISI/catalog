package tw.com.citi.catalog.web.model;

import tw.com.citi.catalog.web.annotation.Table;

@Table("JC_BUILD_UNIT_PATH")
public class BuildUnitPath implements IModel<Long> {

    public final static int QA_SOURCE = 0;
    public final static int QA_EXECUTION = 1;
    public final static int PROD_BACKUP = 2;
    public final static int PROD_SOURCE = 3;
    public final static int PROD_EXECUTION = 4;

    private Long id;
    private BuildUnit buildUnit;
    private Integer pathType;
    private String path;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setBuildUnit(BuildUnit buildUnit) {
        this.buildUnit = buildUnit;
    }

    public BuildUnit getBuildUnit() {
        return buildUnit;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPathType(Integer pathType) {
        this.pathType = pathType;
    }

    public Integer getPathType() {
        return pathType;
    }

}
