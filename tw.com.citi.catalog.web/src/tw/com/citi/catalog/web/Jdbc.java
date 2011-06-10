package tw.com.citi.catalog.web;

public class Jdbc {

    private String driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    private String url = "jdbc:sqlserver://192.168.0.1:2360;databaseName=testdb";

    private String username = "sa";

    private String password = "p@ssw0rd";

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
