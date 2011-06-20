package tw.com.citi.catalog.web.model;

import tw.com.citi.catalog.web.annotation.Table;

@Table("JC_BUILD_UNIT_PATH")
public class BuildUnitPath implements IModel<Long> {

    public enum PathType {
        QA_SOURCE, QA_EXECUTION, PROD_BACKUP, PROD_SOURCE, PROD_EXECUTION
    }

    private Long id;
    private BuildUnit buildUnit;
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

    public void setBuildUnit(BuildUnit buildUnit) {
        this.buildUnit = buildUnit;
    }

    public BuildUnit getBuildUnit() {
        return buildUnit;
    }

    public void setPathType(PathType pathType) {
        this.pathType = pathType;
    }

    public PathType getPathType() {
        return pathType;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}
