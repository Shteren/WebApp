package webapp.utils;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;



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
