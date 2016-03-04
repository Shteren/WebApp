package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import webapp.constants.DBConstants;
import webapp.constants.QuestionAndAnswersConstants;


import com.google.gson.Gson;


/**
 * Servlet implementation of topic resource
 */
public class TopicsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TopicsServlet() {
        super();
    }
    
    /**
     * Should support the api
     * /topics?currentPage
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.setContentType("application/json");		
			searchTopics(request, response);
			return;
			
		} catch(Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
    private void searchTopics(HttpServletRequest request, HttpServletResponse response) throws NumberFormatException, IOException {
    	String currentPage = request.getParameter("currentPage");
		if(currentPage == null) {
			currentPage = "0";
		}

		selectTopicsByCurrentPage(Integer.parseInt(currentPage), response);			
	}
    
    
    private void selectTopicsByCurrentPage(int currentPage, HttpServletResponse response) throws IOException {
    	
		Connection conn = null;
		PreparedStatement pstmt = null;
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();  
    		Gson gson = new Gson();

    		/** prepare the statement of select 20 topics by current page **/
    		int fromQuestion = currentPage * 20;
    		ArrayList<String> topics = new ArrayList<>();
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_TOP_20_TOPICS_STMT);
    		pstmt.setInt(1, fromQuestion);    		
    		ResultSet rs = pstmt.executeQuery();
    		while( rs.next() )
    		{
    			String topicName = rs.getString(2);
        		topics.add(topicName);        		
    		}  
    		
        	String topicsJsonResult = gson.toJson(topics, QuestionAndAnswersConstants.TOPICS_COLLECTION);
	        
			PrintWriter writer = response.getWriter();
	    	writer.println(topicsJsonResult);
	    	writer.close();
    		
			rs.close();
			pstmt.close();
    		conn.close();
    		    		    		
		}catch (SQLException | NamingException e) {
			System.out.println(e.toString());
			try {
				if(pstmt != null)
					pstmt.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}			
    	}   	
	}
	
}