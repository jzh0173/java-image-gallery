package edu.au.cc.gallery;

import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;

public class PostgresUserDAO implements UserDAO {

    private db connection;

    public PostgresUserDAO() throws SQLException {
	connection = new db();
	connection.connect();
    }

    public List<User> getUsers() throws SQLException {
	List<User> result = new ArrayList<>();
	ResultSet rs = connection.executeQuery("select username, password, full_name from users");
	while(rs.next()) {
	    result.add(new User(rs.getString(1), rs.getString(2), rs.getString(3)));
	}
	rs.close();
	return result;
    }

    public User getUserByUsername(String username) throws SQLException {
	ResultSet rs = connection.executeQuery("select username, password, full_name from users where username =?", new String[] {username});
        if (rs.next()) {
            return new User(rs.getString(1), rs.getString(2), rs.getString(3));
	    }
        rs.close();
        return null;
    }

    public void addUser(User u) throws SQLException {
	connection.execute("insert into users(username, password, full_name) values (?, ?, ?)", new String[] {u.getUsername(), u.getPassword(), u.getFullName()});
	
    }

    public void deleteUser(String name) throws Exception {

    }
    
}
