package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

import webapp.constants.DBConstants;
import webapp.constants.UserConstants;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class RegisterServlet
 */
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//obtain CustomerDB data source from Tomcat's context
		BasicDataSource ds = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			Context context = new InitialContext();
			ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
			conn = ds.getConnection(); // get connection
			
			pstmt = conn.prepareStatement(UserConstants.INSERT_USER_STMT);
			// get the parameters from incoming json
			String Usename = request.getParameter("userName");
			String Password = request.getParameter("password");
			String Nickname = request.getParameter("nickName");
			String Desc = request.getParameter("description");
			// insert parameters into SQL Insert
			pstmt.setString(1,Usename);
			pstmt.setString(2,Password);
			pstmt.setString(3,Nickname);
			pstmt.setString(4,Desc);
			
			//execute insert command
			pstmt.executeUpdate();
			//commit update
			conn.commit();
			
			pstmt.close();
			conn.close();
			
			////// Success ////// 
			// Set session to be valid
			HttpSession session = request.getSession();
			session.setMaxInactiveInterval(3600); // seconds 
			session.setAttribute("Username", Usename);
						
			//build Json Answer
			JsonObject json = new JsonObject();
			json.addProperty("Result", true);
			String Answer = json.toString();
			
			PrintWriter writer = response.getWriter();
        	writer.println(Answer);
        	writer.close();
			
			
		} catch (SQLException | NamingException e) {
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
			JsonObject json = new JsonObject();
			json.addProperty("Result", false);
			String Answer = json.toString();
			
			PrintWriter writer = response.getWriter();
        	writer.println(Answer);
        	writer.close();
		}
	}

}
