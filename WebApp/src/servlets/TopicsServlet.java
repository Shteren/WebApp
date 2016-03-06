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
import webapp.model.QuestionsResponse;
import webapp.model.TopicResponse;
import webapp.utils.DBUtils;

import com.google.gson.Gson;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.QEncoderStream;


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
     * @throws IOException 
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			response.setContentType("application/json");		
			searchTopics(request, response);
			return;
			
		} catch(Exception e) {
			DBUtils.buildJsonResult("Wrong uri" , response);
			return;
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
			
			TopicResponse topicsResponse = new TopicResponse(topics, numberOfQuestions);
			String TopicsByTopicsJsonResult = gson.toJson(topicsResponse, TopicResponse.class);
	        
			PrintWriter writer = response.getWriter();
	    	writer.println(TopicsByTopicsJsonResult);
	    	writer.close();
    		
	    	DBUtils.closeResultAndStatment(rs, pstmt);
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