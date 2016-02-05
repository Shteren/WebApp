package servlets;

import java.io.IOException;
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

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import webapp.constants.DBConstants;
import webapp.constants.UserConstants;

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
		Context context;
		System.out.print("a");
		try {
			System.out.print("b");
			context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
			Connection conn = ds.getConnection(); // get connection
			
			PreparedStatement pstmt = conn.prepareStatement(UserConstants.INSERT_USER_STMT);
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
			
			pstmt.executeUpdate();
			System.out.print("c");
			//commit update
			conn.commit(); 
			System.out.print("d");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.print("here");
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			System.out.print("there");
			e.printStackTrace();
		}
	}

}
