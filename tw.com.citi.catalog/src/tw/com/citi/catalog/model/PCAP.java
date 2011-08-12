package tw.com.citi.catalog.model;

import tw.com.citi.catalog.annotation.Table;
import tw.com.citi.catalog.model.IModel;

@Table("PCAP")
public class PCAP implements IModel<Long> {
	
	private Long id;
    private int seqNo;
    private String svrName;
    private String dbName;
    private String usrName;
    private byte[] dbId;
    private String oleProvider;
    private boolean passync;

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public String getSvrName() {
        return svrName;
    }

    public void setSvrName(String svrName) {
        this.svrName = svrName;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getUsrName() {
        return usrName;
    }

    public void setUsrName(String usrName) {
        this.usrName = usrName;
    }

    public byte[] getDbId() {
        return dbId;
    }

    public void setDbId(byte[] dbId) {
        this.dbId = dbId;
    }

    public String getOleProvider() {
        return oleProvider;
    }

    public void setOleProvider(String oleProvider) {
        this.oleProvider = oleProvider;
    }

    public void setPassync(boolean passync) {
        this.passync = passync;
    }

    public boolean isPassync() {
        return passync;
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
