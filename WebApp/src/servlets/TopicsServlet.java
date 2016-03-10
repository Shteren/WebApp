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
import webapp.model.TopicResponse;
import webapp.utils.Utils;

import com.google.gson.Gson;


/**
 * Servlet implementation of topic resource
 */
public class TopicsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       

    public TopicsServlet() {
    }
    
    /**
     * Should support 
     * /topics?currentPage - search for 20 topics according to currentPage
     * @throws IOException 
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			response.setContentType("application/json");		
			searchTopics(request, response);
			return;
			
		} catch(Exception e) {
			Utils.buildJsonResult("Wrong uri" , response);
			return;
		}
	}
	
    /**
     * this method is wrapper for search 20 topics each time
     * @param request
     * @param response
     * @throws NumberFormatException
     * @throws IOException
     * 
     */
    private void searchTopics(HttpServletRequest request, HttpServletResponse response) throws NumberFormatException, IOException {
    	String currentPage = request.getParameter("currentPage");
		if(currentPage == null) {
			currentPage = "0";
		}

		selectTopicsByCurrentPage(Integer.parseInt(currentPage), response);			
	}
    
    /**
     * this method set all topics in DB according to their popularity 
     * @param currentPage
     * @param response
     * @throws IOException
     */
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
    		/* check number of questions to how much pages we need for all topics */
    		int numberOfQuestions = 0;
			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.COUNT_TOPICS_STMT);
			// get number of questions
			rs = pstmt.executeQuery();
			if ( rs.next() ) {
					numberOfQuestions = rs.getInt(1);
			}
			if ((numberOfQuestions % 20) == 0) {
				numberOfQuestions = (numberOfQuestions / 20) - 1;
			} else {
				numberOfQuestions = (numberOfQuestions / 20);
			}	
			
			/* prepare the collection of topic to be send to client */
			TopicResponse topicsResponse = new TopicResponse(topics, numberOfQuestions);
			String TopicsJsonResult = gson.toJson(topicsResponse, TopicResponse.class);
	        
			PrintWriter writer = response.getWriter();
	    	writer.println(TopicsJsonResult);
	    	writer.close();
    		
	    	Utils.closeResultAndStatment(rs, pstmt);
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