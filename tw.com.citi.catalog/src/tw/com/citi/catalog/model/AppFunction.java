package tw.com.citi.catalog.model;

import tw.com.citi.catalog.annotation.Table;

@Table("FUNCLIST")
public class AppFunction implements IModel<Long>  {
	
	private Long id;
    private String funcCode;
    private String funcDesc;

    public String getFuncCode() {
        return funcCode;
    }

    public void setFuncCode(String funcCode) {
        this.funcCode = funcCode;
    }

    public String getFuncDesc() {
        return funcDesc;
    }

    public void setFuncDesc(String funcDesc) {
        this.funcDesc = funcDesc;
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
