package tw.com.citi.catalog.model;

import tw.com.citi.catalog.annotation.Table;

@Table("JC_PARAMS")
public class Params implements IModel<Long> {

    private Long id;
    private String pKey;
    private String pValue;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setpKey(String pKey) {
        this.pKey = pKey;
    }

    public String getpKey() {
        return pKey;
    }

    public void setpValue(String pValue) {
        this.pValue = pValue;
    }

    public String getpValue() {
        return pValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pKey == null) ? 0 : pKey.hashCode());
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
        Params other = (Params) obj;
        if (pKey == null) {
            if (other.pKey != null)
                return false;
        } else if (!pKey.equals(other.pKey))
            return false;
        return true;
    }
}
