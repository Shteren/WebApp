package webapp.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import webapp.constants.DBConstants;


public class DBUtils {
	
	public static ResultSet excuteQuery(final String query) throws NamingException, SQLException{
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
    	//obtain CustomerDB data source from Tomcat's context
		Context context = new InitialContext();
		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
		conn = ds.getConnection();    		   		
		/** prepare the statement  **/
		pstmt = conn.prepareStatement(query);
		rs = pstmt.executeQuery();
		
		return rs;

			
	}
}
