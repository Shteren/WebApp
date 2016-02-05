package servlets;

import java.io.IOException;
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
import javax.smartcardio.ResponseAPDU;
import javax.xml.ws.Response;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		System.out.println("Here is get servlet");
		try {
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		Connection conn = ds.getConnection();
    		
    		PreparedStatement pstmt = conn.prepareStatement(UserConstants.SELECT_USER_BY_NAME_STMT);
    		//Statement stmt = conn.createStatement();
    		pstmt.setString(1, request.getParameter("userName"));
    		//String query = "SELECT * FROM tbl_user where username = '" + request.getParameter("userName") + "'";
    		System.out.println(request.getParameter("userName"));
    		
    		ResultSet rs = pstmt.executeQuery();
    		if(!rs.next())
    			response.sendError(500);
    		
    		String userPassword = rs.getString("password");
    		if (userPassword.equals(request.getParameter("password"))){
    				System.out.println("password is valid");
    		}else{
    			System.out.println("password is wrong");
    		}
			rs.close();
			pstmt.close();
    		conn.close();
		}catch (SQLException | NamingException e) {
    		getServletContext().log("Error while closing connection", e);
    		response.sendError(500);//internal server error
    	}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		System.out.println("Here is servlet");
		try {
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		Connection conn = ds.getConnection();
    		
    		PreparedStatement stmt = conn.prepareStatement(UserConstants.SELECT_USER_BY_NAME_STMT);
    		PreparedStatement pstmt = conn.prepareStatement(UserConstants.INSERT_USER_STMT);
    		//Statement stmt = conn.createStatement();
    		String UserName = request.getParameter("userName");
    		stmt.setString(1,UserName );
    		System.out.println(request.getParameter("userName"));
    		
    		ResultSet rs = stmt.executeQuery();
    		//System.out.println(rs.getString(0));
    		if(!rs.next())
    		{	  		
    			pstmt.setString(1,request.getParameter("userName"));
    			pstmt.setString(2,request.getParameter("password"));
    			pstmt.setString(3,request.getParameter("nickName"));
    			pstmt.setString(4,request.getParameter("description"));			
    			
    			pstmt.executeUpdate();
    			
    			//commit update
    			conn.commit(); 			
    			//close statements
    			
    			System.out.println("insert to table");
    			
    		}else{
    			response.sendError(500);
    		}  			
    		//close connection
			rs.close();
			stmt.close();
			pstmt.close();
    		conn.close();
		} catch (SQLException | NamingException e) {
    		getServletContext().log("Error while closing connection", e);
    		response.sendError(500);//internal server error
    	}
	}

}
