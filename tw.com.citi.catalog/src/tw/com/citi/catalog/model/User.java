package tw.com.citi.catalog.model;

import tw.com.citi.catalog.annotation.Table;

@Table("USERS")
public class User implements IModel<Long> {
	
	private Long id;
    private String userId;
    private String grpName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGrpName() {
        return grpName;
    }

    public void setGrpName(String grpName) {
        this.grpName = grpName;
    }

	@Override
	public Long getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public void setId(Long id) {
		// TODO Auto-generated method stub
		this.id=id;
	}
}
