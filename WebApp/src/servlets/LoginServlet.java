package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.JsonObject;

import webapp.constants.DBConstants;
import webapp.constants.UserConstants;
import webapp.utils.Utils;

/**
 * Servlet implementation class LoginServlet
 */
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
    }


	/**
	 * should support the api 
	 * /login or /session 
	 * for login - we sent the password in header and not in params for safety issues
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		String uri = request.getRequestURI();
		String answer = null;
		if (uri.contains("login"))
		{
			String userName = request.getParameter("userName");
			String pass = request.getHeader("password");			
			answer = login(userName, pass, request.getSession());
		} else if (uri.contains("session")) //case where the uri is session
		{
			answer = getSessionStatus(request.getSession());
		}
				
		PrintWriter writer = response.getWriter();
		writer.println(answer);
		writer.close();
		
	}
	
	/**
	 * should support the api 
	 * /logout
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		String uri = request.getRequestURI();
		String answer = null;
		if (uri.contains("logout"))
		{
			answer = logOut(request.getSession());
		
	    }
		PrintWriter writer = response.getWriter();
		writer.println(answer);
		writer.close();
	}
	
	/**
	 * 
	 * @param userName - username trying to login
	 * @param password - password of username trying to login
	 * @param session - for update the user now loged-in
	 * @return Strign of answer for sending to client - response of success of insert to system
	 */
	private String login(String userName, String password, HttpSession session)
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		JsonObject json = new JsonObject();
		String answer;
		try {
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();
    		// prepare statment of search for user with specific user name 
    		pstmt = conn.prepareStatement(UserConstants.SELECT_USER_BY_NAME_STMT);

    		pstmt.setString(1, userName);
    		
    		ResultSet rs = pstmt.executeQuery();
    		// 
    		if(!rs.next()) // Username doesn't exist
			{
    			//build Json Answer
    			json.addProperty("Result", false);
    			answer = json.toString();
			}
    		else
    		{
	    		String userPassword = rs.getString("password"); // get the password from Result set
	    		String nickname = rs.getString("nickname");
	    		if (userPassword.equals(password)) // compare the password
	    		{
	    			json.addProperty("Result", true);
	    			// Set session to be valid
	    			session.setMaxInactiveInterval(3600); // seconds 
	    			session.setAttribute("Username", userName);
	    			session.setAttribute("Nickname", nickname);
	    		}
	    		else
	    		{
	    			json.addProperty("Result", false);
	    			session.setAttribute("Username", null);
	    			session.setAttribute("Nickname", null);
	    			session.invalidate();
	    		}
	    		
	    		answer = json.toString();    			
	    		
    		}
    		
    		Utils.closeResultAndStatment(rs, pstmt);
    		conn.close();
    		
		} catch (SQLException | NamingException e) {
			try {
				if(pstmt != null)
					pstmt.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e1) {
				// Conitnue with the flow altough there is exception
			}
			
			// invalidate Session
			session.setAttribute("Username", null);
			session.invalidate();
			
			//build Json Answer
			json = new JsonObject();
			json.addProperty("Result", false);
			answer = json.toString();
		}
		return answer;
	}
	
	/**
	 * 
	 * @param session - update user now loged-in
	 * @return response true or false for creating session 
	 */
	private String getSessionStatus(HttpSession session)
	{	
		
		String SessionUsername = (String) session.getAttribute("Username");
		String SessionNickname = (String) session.getAttribute("Nickname");
		JsonObject json = new JsonObject();
		String Answer;
		if (SessionUsername == null){
			session.invalidate();
			json.addProperty("Result", false);
		}else{
			json.addProperty("Result", true);
		}
		json.addProperty("Username", SessionUsername);
		json.addProperty("Nickname", SessionNickname);
		//build Json Answer
	
		Answer = json.toString();
		return Answer;
		
	}
	
	/**
	 * 
	 * @param session
	 * @return response true or false of logout from system
	 */
	private String logOut(HttpSession session)
	{
		String answer;

		try{
			session.setAttribute("Username", null);
			session.setAttribute("Nickname", null);
			session.invalidate();
			JsonObject json = new JsonObject();
			json.addProperty("Result", true);
			answer = json.toString();
		}catch(Exception e){
			JsonObject json = new JsonObject();
			json.addProperty("Result", false);
			answer = json.toString();
		}
		
		return answer;
	}
}



