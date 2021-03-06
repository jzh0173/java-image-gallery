package edu.au.cc.gallery.ui;

import static spark.Spark.*;
import spark.Request;
import spark.Response;
import java.util.HashMap;
import java.util.Map;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;
import edu.au.cc.gallery.UserDAO;
import edu.au.cc.gallery.Postgres;
import edu.au.cc.gallery.User;

public class Admin {
    private static UserDAO getUserDAO() throws Exception {
	return Postgres.getUserDAO();
    }

    private String deleteUser(Request req, Response Resp) {
	Map<String, Object> model = new HashMap<>();
	model.put("title", "Delete User");
	model.put("message", "Are you sure that you want to delete this user?");
	model.put("onYes", "/admin/deleteUserExec/" + req.params(":username"));
	model.put("onNo", "/admin/users");
	return new HandlebarsTemplateEngine().render(new ModelAndView(model, "confirm.hbs"));
	}

    private String deleteUserExec(Request req, Response resp) {
	try {
	    getUserDAO().deleteUser(req.params(":username"));
	    resp.redirect("/admin/users");
	} catch (Exception e) {
	    return "Error: " + e.getMessage();
	}
	return null;
    }

    private String listUsers() {
	try {
	    Map<String, Object> model = new HashMap<String, Object>();
	    model.put("users", getUserDAO().getUsers());
	    return new HandlebarsTemplateEngine().render(new ModelAndView(model, "users.hbs"));
	} catch (Exception e) {
	    return "Error: " + e.getMessage();
	}
    }

    private String login(Request req, Response resp) {
	 Map<String, Object> model = new HashMap<String, Object>();
	 return new HandlebarsTemplateEngine().render(new ModelAndView(model, "login.hbs"));
    }

    private String loginPost(Request req, Response resp) {
	try {
	String username = req.queryParams("username");
	User u = getUserDAO().getUserByUsername(username);
	if (u == null || !u.getPassword().equals(req.queryParams("password"))) {
	    resp.redirect("/login");
	    return "";
	    }
	req.session().attribute("user", username);
	} catch (Exception e) {
            return "Error: " + e.getMessage();
        }
	return "";
	
    }

    private boolean isAdmin(String username) {
	return username != null || username.equals("admin");
    }
    
    private String checkAdmin(Request req, Response resp) {
	if (!isAdmin(req.session().attribute("user"))) {
		resp.redirect("/login");
		halt();
	    }
	    
	    return "";
    }

    public void addRoutes() {
	get("/admin/users", (req, res) -> listUsers());
	get("/admin/deleteUser/:username", (req, res) -> deleteUser(req, res));
	get("/admin/deleteUserExec/:username", (req, res) -> deleteUserExec(req, res));
	before("/admin/*", (req, res) -> checkAdmin(req, res));
	get("login", (req, res) -> login(req,res));
	post("/login", (req, res) -> loginPost(req, res));
    }
}
