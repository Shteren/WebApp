package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.smartcardio.ResponseAPDU;
import javax.xml.ws.Response;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.JsonObject;

import webapp.constants.DBConstants;
import webapp.constants.UserConstants;

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
        // TODO Auto-generated constructor stub
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		Connection conn = null;
		PreparedStatement pstmt = null;
		JsonObject json = new JsonObject();
		String Answer;
		try {
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();
    		
    		pstmt = conn.prepareStatement(UserConstants.SELECT_USER_BY_NAME_STMT);
    		//Statement stmt = conn.createStatement();
    		String Username = request.getParameter("userName");
    		pstmt.setString(1, Username);
    		//String query = "SELECT * FROM tbl_user where username = '" + request.getParameter("userName") + "'";
    		System.out.println(Username);
    		
    		ResultSet rs = pstmt.executeQuery();
    		// 
    		if(!rs.next()) // Username doesn't exist
			{
    			//build Json Answer
    			json.addProperty("Result", false);
    			Answer = json.toString();
    			
    			PrintWriter writer = response.getWriter();
            	writer.println(Answer);
            	writer.close();
    			return;
			}
    		else
    		{
	    		String userPassword = rs.getString("password"); // get the password from Result set
	    		if (userPassword.equals(request.getParameter("password"))) // compare the password
	    		{
	    			json.addProperty("Result", true);
	    			// Set session to be valid
	    			HttpSession session = request.getSession();
	    			session.setMaxInactiveInterval(3600); // seconds 
	    			session.setAttribute("Username", Username);
	    			
	    		}
	    		else
	    		{
	    			json.addProperty("Result", false);
	    			HttpSession session = request.getSession();
	    			session.setAttribute("Username", null);
	    			session.invalidate();
	    		}
	    		
	    		Answer = json.toString();
    			
    			PrintWriter writer = response.getWriter();
            	writer.println(Answer);
            	writer.close();
    		}
			rs.close();
			pstmt.close();
    		conn.close();
		}catch (SQLException | NamingException e) {
			try {
				if(pstmt != null)
					pstmt.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// invalidate Session
			HttpSession session = request.getSession();
			session.setAttribute("Username", null);
			session.invalidate();
			
			//build Json Answer
			json = new JsonObject();
			json.addProperty("Result", false);
			Answer = json.toString();
			
			PrintWriter writer = response.getWriter();
	    	writer.println(Answer);
	    	writer.close();
    	}
		
	}

}
