package su.grinev.gallery.model;

public class Album {
    private int id;
    private String name;

    public Album() {
    }

    public Album(int id, String name) {
        /* todo: Zhdankin: давай попробуйем сделать идентификатором альбома UUID.randomString()? */
        this.id=id;
        this.name=name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Album {" +
                "id=" + this.id +
                ", name='" + this.name + '\''+'}';
    }
}
