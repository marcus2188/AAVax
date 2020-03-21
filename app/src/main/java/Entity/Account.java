package Entity;

import java.util.ArrayList;

public class Account {
    private String email;
    private String password;
    private ArrayList<Profile> profiles;

    public Account(String email, String password, ArrayList<Profile> profiles) {
        this.email = email;
        this.password = password;
        this.profiles = profiles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(ArrayList<Profile> profiles) {
        this.profiles = profiles;
    }
}