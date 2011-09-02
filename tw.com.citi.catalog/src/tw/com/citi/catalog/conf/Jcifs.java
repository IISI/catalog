package tw.com.citi.catalog.conf;

public class Jcifs {
    private String functionalId = "CitiTest";
    private String functionalPwd = "168;199;166;197;164;195;162;193;160;191;158;189;156;187;154;185;152;183;150;181;148;179;146;177;144;243;142;255;140;187;138;0;136;250;134;248;132;195;130;241";
    private String jcifsNetbiosWins = "";

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

    public String getDomain() {
        String domain = "";
        if (functionalId.indexOf(";") > 0) {
            domain = functionalId.substring(0, functionalId.indexOf(";"));
        }
        return domain;
    }

    public String getIdWithoutDomain() {
        String idWithoutDomain = functionalId;
        if (functionalId.indexOf(";") > 0) {
            idWithoutDomain = functionalId.substring(functionalId.indexOf(";") + 1);
        }
        return idWithoutDomain;
    }
}
