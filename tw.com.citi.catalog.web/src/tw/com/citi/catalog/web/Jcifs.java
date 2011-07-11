package tw.com.citi.catalog.web;

public class Jcifs {
    private String functionalId = "domain;username";
    private String functionalPwd = "userpassword";
    private String jcifsNetbiosWins = "netbioswins";

    public void setFunctionalId(String functionalId) {
        this.functionalId = functionalId;
    }

    public String getFunctionalId() {
        return functionalId;
    }

    public void setFunctionalPwd(String functionalPwd) {
        this.functionalPwd = functionalPwd;
    }

    public String getFunctionalPwd() {
        return functionalPwd;
    }

    public void setJcifsNetbiosWins(String jcifsNetbiosWins) {
        this.jcifsNetbiosWins = jcifsNetbiosWins;
    }

    public String getJcifsNetbiosWins() {
        return jcifsNetbiosWins;
    }
}
