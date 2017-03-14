package nl.jhnbos.letsmeet;

/**
 * Created by Johan Bos on 14-3-2017.
 */

public class Group {
    private int ID;
    private String Name;
    private String Creator;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCreator() {
        return Creator;
    }

    public void setCreator(String creator) {
        Creator = creator;
    }
}
