package webapp.utils;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

/**
 * 
 * class for some utils of system
 *
 */

public class Utils {
	
	/**
	 * this method is for closing open statements and ResultSets
	 * @param rs
	 * @param stmt
	 * @throws SQLException
	 */
	public static void closeResultAndStatment (ResultSet rs , PreparedStatement stmt) throws SQLException
	{
		if (rs != null) {
			rs.close();
		}
		
		if (stmt != null) {
			stmt.close();
		}
			
	}
	
	/**
	 * this method is for creating response to send to client
	 * @param result
	 * @param response
	 * @throws IOException
	 */
	public static void buildJsonResult (String result, HttpServletResponse response) throws IOException
	{
		//build Json Answer
		JsonObject json = new JsonObject();
		json.addProperty("Result", result);
		String Answer = json.toString();
		
		PrintWriter writer = response.getWriter();
    	writer.println(Answer);
    	writer.close();
	}
}
