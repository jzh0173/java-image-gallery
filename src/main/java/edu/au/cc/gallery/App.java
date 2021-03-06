/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package edu.au.cc.gallery;

import spark.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import static spark.Spark.*;
import spark.Response;
import spark.Request;
import java.util.HashMap;
import java.util.Map;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;
import java.io.IOException;

//import com.amazonaws.auth.profile.ProfileCredentialsProvider;
//import com.amazonaws.regions.Regions;
//import com.amazonaws.services.lambda.AWSLambda;
//import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
//import com.amazonaws.services.lambda.model.InvokeRequest;
//import com.amazonaws.services.lambda.model.InvokeResult;
//import com.amazonaws.services.lambda.model.ServiceException;

public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    private void sendToS3(InputStream input) {
        Regions clientRegion = Regions.US_EAST_2;
        String bucketName = "jssh-image-gallery-bucket";
        String stringObjKeyName = "*** String object key name ***";
        String fileObjKeyName = "key";
        String fileName = "*** Path to file to upload ***";


	try {
            //This code expects that you have AWS credentials set up per:
            // https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentilas.html
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .build();
ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("plain/text");
            metadata.addUserMetadata("title", "someTitle");

            // Upload a file as a new object with ContentType and title specified.
            PutObjectRequest request = new PutObjectRequest(bucketName, fileObjKeyName, input, metadata);
            
	    // request.setMetadata(metadata);
            s3Client.putObject(request);
        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process 
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        }
	
	/*
	S3Client s3 = S3Client.builder().region(region).build();

        String bucket = "jssh-image-gallery-bucket";
        String key = "key";

        // Put Object
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key)
                .build(), tempf);
	*/
    }

    // methods used for logging
    private static void logInfo(Request req, Path tempFile) throws IOException, ServletException {
        System.out.println("Uploaded file '" + getFileName(req.raw().getPart("uploaded_file")) + "' saved as '" + tempFile.toAbsolutePath() + "'");
    }

    private static String getFileName(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
    /*
    private static String listUsers() {
	try {
	StringBuffer sb = new StringBuffer();
	UserDAO dao = Postgres.getUserDAO();
	for (User u : dao.getUsers()) {
	    sb.append(u.toString() + "<br/>");
	}
	return sb.toString();
	} catch (Exception e) {
	    return "Error: " + e.getMessage();
	}
	}*/

    private static String getUser(String username){
	try{
	    UserDAO dao=Postgres.getUserDAO();
	   return dao.getUserByUsername(username).toString();
	} catch (Exception e) {
	    return "Error: " + e.getMessage();
	}
    }

    private static UserDAO getUserDAO() throws Exception {
        return Postgres.getUserDAO();
    }

    
	private static String addUser(String username, String password, String fullname, Response r) {
	try {
	    UserDAO dao = Postgres.getUserDAO();
	    dao.addUser(new User(username, password, fullname));
	    r.redirect("/users");
	    return "";
	} catch (Exception e) {
	    return "Error: " + e.getMessage();
	}
	}

    private JSONObject getSecret() {
	String s = Secrets.getSecretImageGallery();
	return new JSONObject(s);
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


    private String deleteUser(Request req, Response Resp) {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Delete User");
        model.put("message", "Are you sure that you want to delete this user?");
        model.put("onYes", "/admin/deleteUserExec/" + req.params(":username"));
        model.put("onNo", "/admin/users");
        return new HandlebarsTemplateEngine().render(new ModelAndView(model, "confirm.hbs"));
        }



    private String getPassword(JSONObject secret) {
	return secret.getString("password");
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

    /*
@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {
   
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String description = request.getParameter("description"); // Retrieves <input type="text" name="description">
    Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">
    String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); // MSIE fix.
    InputStream fileContent = filePart.getInputStream();
   
}
}
    */

    public static void main(String[] args) throws Exception {
        System.out.println(new App().getGreeting());
        File uploadDir = new File("upload");
        uploadDir.mkdir(); // create the upload directory if it doesn't exist

	staticFiles.externalLocation("upload");
	//	Secrets sec = new Secrets();
	//System.out.println(sec.getSecretImageGallery());
	App app = new App();	
	//System.out.println(sec.getSecretImageGallery().get("password"));
	//	JSONObject secret = app.getSecret();
	//	System.out.println(app.getPassword(secret));
	//	db dbtest = new db();
	//dbtest.demo();

	String portString = System.getenv("JETTY_PORT");
	if (portString == null || portString.equals("")) {
	    port(5000);
	} else {
	    port(Integer.parseInt(portString));
	}
	
	get("/hello", (req, res) -> "Hello World.");
	get("/goodbye", (req, res) -> "Goodbye World.");

	get("test", (req, res) -> {
		Map<String, Object> model = new HashMap<String, Object>();
		return new HandlebarsTemplateEngine().render(
		   new ModelAndView(model, "test.hbs")); });

	get("/admin", (req, res) -> {
                Map<String, Object> model = new HashMap<String, Object>();
                return new HandlebarsTemplateEngine().render(
	        new ModelAndView(model, "dbadmin.hbs")); });

       get("/admin/users", (req, res) -> {
                Map<String, Object> model = new HashMap<String, Object>();
                return new HandlebarsTemplateEngine().render(
                new ModelAndView(model, "adminusers.hbs")); });

       get("/", (req, res) -> {
                Map<String, Object> model = new HashMap<String, Object>();
                return new HandlebarsTemplateEngine().render(
                new ModelAndView(model, "mainmenu.hbs")); });       

	get("/admin/adduser", (req, res) -> {
                Map<String, Object> model = new HashMap<String, Object>();
                return new HandlebarsTemplateEngine().render(
                new ModelAndView(model, "adduser.hbs")); });

	get("/admin/edituser", (req, res) -> {
		    Map<String, Object> model = new HashMap<String, Object>();
		    return new HandlebarsTemplateEngine().render(
		    new ModelAndView(model, "edituser.hbs")); });

get("/upload", (req, res) -> {
                    Map<String, Object> model = new HashMap<String, Object>();
                    return new HandlebarsTemplateEngine().render(
                    new ModelAndView(model, "upload.hbs")); });

get("/view", (req, res) -> {
                    Map<String, Object> model = new HashMap<String, Object>();
                    return new HandlebarsTemplateEngine().render(
                    new ModelAndView(model, "view.hbs")); });

	
	get("/users", (req, res) ->  app.listUsers() );
	get("/users/:username", (req, res) -> getUser(req.params(":username")));
	get("/addUser/:username/:password/:fullname", (req, res) -> addUser(req.params("username"), req.params(":password"), req.params("fullname"), res));
get("/admin/users", (req, res) -> app.listUsers());
        get("/admin/deleteUser/:username", (req, res) -> app.deleteUser(req, res));
        get("/admin/deleteUserExec/:username", (req, res) -> app.deleteUserExec(req, res));
        before("/admin/*", (req, res) -> app.checkAdmin(req, res));
        get("login", (req, res) -> app.login(req,res));
        post("/login", (req, res) -> app.loginPost(req, res));
	post("/upload", (req, res) -> {
		Path tempFile = Files.createTempFile(uploadDir.toPath(), "", "");
    req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
    try (InputStream input = req.raw().getPart("uploaded_file").getInputStream()) {
	Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);// Use the input stream to create a file
        
	app.sendToS3(input);
    }
    logInfo(req, tempFile);
            return "<h1>You uploaded this image:<h1><img src='" + tempFile.getFileName() + "'>";
    
});
	
    }	
}
