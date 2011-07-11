package tw.com.citi.catalog.web;

public class Settings {

    private Jdbc jdbc = new Jdbc();
    private Jcifs jcifs = new Jcifs();

    public Jdbc getJdbc() {
        return jdbc;
    }

    public void setJdbc(Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    public void setJcifs(Jcifs jcifs) {
        this.jcifs = jcifs;
    }

    public Jcifs getJcifs() {
        return jcifs;
    }

}
