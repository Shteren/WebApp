package webapp.utils;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



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
