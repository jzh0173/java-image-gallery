package edu.au.cc.gallery;

public class User {

    private String username;
    private String password;
    private String fullname;

    public User(String username, String password, String fullname) {
	this.username = username;
	this.password = password;
	this.fullname = fullname;
    }

    public String getUsername() { return username; }
    public void setUsername(String u) { username = u; }
    public String getPassword() { return password; }
    public void setPassword(String p) { password = p; }
    public String getFullName() {return fullname; }
    public void setFullName(String fn) { fullname = fn; }

    @Override
    public String toString() {
	return "username: " + username + "password: " + password + "full name: " + fullname;
    }

}
