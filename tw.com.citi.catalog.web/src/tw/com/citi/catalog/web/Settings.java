package tw.com.citi.catalog.web;

public class Settings {

    private Jdbc jdbc = new Jdbc();

    public Jdbc getJdbc() {
        return jdbc;
    }

    public void setJdbc(Jdbc jdbc) {
        this.jdbc = jdbc;
    }

}
