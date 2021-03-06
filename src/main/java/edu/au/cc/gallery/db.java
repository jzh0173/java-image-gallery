package edu.au.cc.gallery;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class db {

    private static final String dbUrl = "jdbc:postgresql://image-gallery.cyms7joammd7.us-east-2.rds.amazonaws.com/";

    private Connection conn;

    private JSONObject getSecret() {
	String s = Secrets.getSecretImageGallery();
	return new JSONObject(s);
    }

    private String getPassword(JSONObject secret) {
	return secret.getString("password");
    }

    /*
    private String getPassword() {
        try (BufferedReader br = new BufferedReader(new FileReader("/home/ec2-user/.sql-passwd"))) {
	    String result = br.readLine();
	    br.close();
	    return result;
	} catch (IOException ex) {
	    System.err.println("Password File Error.");
	    System.exit(1);
	}
	return null;
	}*/

    public void connect() throws SQLException {
        try
            {
                Class.forName("org.postgresql.Driver");
		//	JSONObject secret = getSecret();
		//                conn = DriverManager.getConnection(dbUrl, "image_gallery", getPassword(secret));
		conn = DriverManager.getConnection(dbUrl, "image_gallery", "aaAA11!!55");
            }
	
        catch (ClassNotFoundException ex) {
	    ex.printStackTrace();
	    System.exit(1);

        }
    }

    public void readUsers() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("select * from users");
        ResultSet rs = stmt.executeQuery();
        while(rs.next()) {
            System.out.println(rs.getString(1) + ", " + rs.getString(2) + ", " + rs.getString(3));
        }
        rs.close();
    }

    public ResultSet executeQuery(String query) throws SQLException {
	PreparedStatement stmt = conn.prepareStatement(query);
	ResultSet rs = stmt.executeQuery();
	return rs;
    }

    public ResultSet executeQuery(String query, String[] values) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(query);
        for (int i = 0; i < values.length; i++) {
            stmt.setString(i+1, values[i]);
	}
	return stmt.executeQuery();
    }


    public void execute(String query, String[] values) throws SQLException {
	PreparedStatement stmt = conn.prepareStatement(query);
	for (int i = 0; i < values.length; i++) {
	    stmt.setString(i+1, values[i]);
	}
	stmt.execute();
    }


    public void addUser() throws SQLException {
        String name = "", password = "", fullname = "";
        Scanner scan = new Scanner(System.in);
	System.out.println("Enter name:");
	if(scan.hasNextLine()){
	    name = scan.nextLine();}
        scan = new Scanner(System.in);
        System.out.println("Enter password:");
        if(scan.hasNextLine()){
            password = scan.nextLine();}
        System.out.println("Enter full name:");
        scan = new Scanner(System.in);
        if(scan.hasNextLine()){
            fullname = scan.nextLine();}


        String sql = "INSERT INTO users(username, password, full_name) VALUES (?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);
	ps.setString(1, name);
	ps.setString(2, password);
	ps.setString(3, fullname);
        ps.executeUpdate();
        ps.close();
    }

    public void deleteUser() throws SQLException {
        String name = "";
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter name:");
	if(scan.hasNextLine()){
	    name = scan.nextLine();}
        String sql = "DELETE FROM users WHERE username ='" + name + "';";
        PreparedStatement ps = conn.prepareStatement(sql);
	ps.executeUpdate();
	ps.close();
    }

    public void editUser() throws SQLException {
        String name = "", password = "", fullname = "";
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter name:");
	if(scan.hasNextLine()){
	    name = scan.nextLine();}
        System.out.println("Enter password:");
        scan = new Scanner(System.in);
        if(scan.hasNextLine()){
            password = scan.nextLine();}
        System.out.println("Enter full name:");
	scan = new Scanner(System.in);
	if(scan.hasNextLine()){
	    fullname = scan.nextLine();}
String sql = "UPDATE users set password = '" + password + "', full_name = '" + fullname + "' WHERE username = '" + name + "';";
PreparedStatement ps = conn.prepareStatement(sql);
ps.executeUpdate();
ps.close();
    }


    public static void demo() throws Exception {
	db db = new db();
	db.connect();
	String option = "";
	Scanner s = new Scanner(System.in);
	boolean exit = false;
        while (exit==false) {
	    System.out.println("Welcome to the database.  Please choose an option:");
            System.out.println("    1: View users");
            System.out.println("    2: Add user");
            System.out.println("    3: Delete user");
            System.out.println("    4: Edit user");
	    System.out.println("    5: Quit");

	    s = new Scanner(System.in);
            // if(s.hasNextLine()){                                                              
	    option = s.next();

            switch (option) {
	    case "1" :
                db.readUsers();
                break;
            case "2" :
                db.addUser();
                break;

            case "3" :
                db.deleteUser();
                break;

            case "4" :
                db.editUser();
                break;

            case "5" :
                exit = true;
                break;

            default :
                System.out.println("Invalid Selection.  Please try again.");
	    }
	}

	db.readUsers();
    }
}
