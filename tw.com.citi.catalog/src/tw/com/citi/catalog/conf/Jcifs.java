package tw.com.citi.catalog.conf;

public class Jcifs {
    private static String domain;
    private static String username;
    private static String userpassword;
    private static String jcifsNetbiosWins;

    public static void setJcifsNetbiosWins(String jcifsNetbiosWins) {
        Jcifs.jcifsNetbiosWins = jcifsNetbiosWins;
    }

    public static String getJcifsNetbiosWins() {
        return jcifsNetbiosWins;
    }

    public static void setUsername(String username) {
        Jcifs.username = username;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUserpassword(String userpassword) {
        Jcifs.userpassword = userpassword;
    }

    public static String getUserpassword() {
        return userpassword;
    }

    public static void setDomain(String domain) {
        Jcifs.domain = domain;
    }

    public static String getDomain() {
        return domain;
    }

}
