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
	
	public static void closeResultAndStatment (ResultSet rs , PreparedStatement stmt) throws SQLException
	{
		if (rs != null) {
			rs.close();
		}
		
		if (stmt != null) {
			stmt.close();
		}
			
	}
}
