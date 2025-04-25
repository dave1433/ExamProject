package dk.easv.blsgn.intgrpbelsign.be;

public class User {
    private int user_id;
    private String user_name;
    private String password_hash;

    public User(int user_id, String user_name, String password_hash, int role_id) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.password_hash = password_hash;
        this.role_id = role_id;
    }


    public int getRole_id() {
        return role_id;
    }

    public void setRole_id(int role_id) {
        this.role_id = role_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    private int role_id;



}
