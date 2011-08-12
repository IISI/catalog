package tw.com.citi.catalog;

public class Jdbc {

    private String driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    private String url = "jdbc:sqlserver://127.0.0.1:1433;databaseName=citi_catalog";
    
    private String username = "sa";

    private String password = "passw0rd";

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
