package tw.com.citi.catalog.conf;

public class Settings {

    private Jdbc jdbc = new Jdbc();

    public Jdbc getJdbc() {
        return jdbc;
    }

    public void setJdbc(Jdbc jdbc) {
        this.jdbc = jdbc;
    }

}
