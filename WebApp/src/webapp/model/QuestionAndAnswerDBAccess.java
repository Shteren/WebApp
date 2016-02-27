package webapp.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import webapp.constants.DBConstants;
import webapp.constants.QuestionAndAnswersConstants;

public class QuestionAndAnswerDBAccess {
	
	public static void searchQuestions(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		String currentPage = request.getParameter("currentPage");
		String topicName = request.getParameter("topicName");
		String newOrAll = request.getParameter("newOrAll");
		if (newOrAll == null)
		{
			newOrAll = "new";
		}
		
		if (null == topicName)
		{
			if (null == currentPage)
			{
				// bad request
			}
			switch(newOrAll)
			{
				case "new":
				{
					if(currentPage == null)
					{
						currentPage = "0";
					}
					selectNewlyQuestionsByCurrentPage(Integer.parseInt(currentPage), response);
			    	break;
				}
				case "all":
				{
					selectExistingQuestionsByCurrentPage(Integer.parseInt(currentPage), response);
			    	break;
				}
			}			
			
		}else
		{
			selectQuestionsByTopic(topicName, Integer.parseInt(currentPage), response);
	    	
		}	
			
	}
	
	public static int getNumberOfLeftPages(int currentPage, String state)
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		JsonObject json = new JsonObject();
		String Answer;
		int numberOfQuestions = 0;
		try 
		{
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();
    		if ("new" == state)
    		{
    			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.COUNT_NEWLY_QUESTIONS_STMT);
    		} else    		
    		{
    			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.COUNT_EXISTING_QUESTIONS_STMT);
    		}    		
    		// get number of questions
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.COUNT_NEWLY_QUESTIONS_STMT);
    		
    		ResultSet rs = pstmt.executeQuery();
    		rs.next();
    		numberOfQuestions = rs.getInt(1);
    		
    		rs.close();
    		//rss.close();
    		pstmt.close();
    		//stmt.close();
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
		return numberOfQuestions;			
	}
	
	
	public static void selectNewlyQuestionsByCurrentPage(int currentPage, HttpServletResponse response) throws IOException {
		Connection conn = null;
		PreparedStatement pstmt = null, stmt = null;
		Collection<Question> QuestionResults = new ArrayList<Question>(); 
		Gson gson = new Gson();
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		ResultSet rss = null;
			BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();    		   		
    		/** prepare the statement of select 20 questions by current page **/
    		int fromQuestion = currentPage * 20;
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_NEWLY_QUESTIONS_STMT);
    		pstmt.setInt(1, fromQuestion);
    		ResultSet rs = pstmt.executeQuery();
    		while( rs.next() )
    		{
    			int questionId = rs.getInt(1);
        		stmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_TOPICS_BY_QUESTION_STMT);
        		stmt.setInt(1,questionId);
        		rss = stmt.executeQuery();
        		ArrayList<String> topics = new ArrayList<>();
        		while (rss.next())
        		{
        			topics.add(rss.getString(1));
        		}

    			String submittionTime = rs.getString(2);
    			String contentTxt = rs.getString(3);
    			int votes = rs.getInt(4);
    			int rate = rs.getInt(5);    			
    			String submittedUser =  rs.getString(6);
    			QuestionResults.add(new Question(questionId, submittionTime ,contentTxt ,topics, submittedUser, votes, rate));
    			topics.clear();
    			
    		}  	
    		
    		int numofquestions = (getNumberOfLeftPages(currentPage, "new"))/20;
    		QuestionsResponse qestionsResponse = new QuestionsResponse(QuestionResults, numofquestions);   
    		String newlyQuestionsJsonResult = gson.toJson(qestionsResponse, QuestionsResponse.class);

    		
			PrintWriter writer = response.getWriter();
	    	writer.println(newlyQuestionsJsonResult);
	    	writer.close();
	    	
    		rss.close();   		
    		rs.close();
    		pstmt.close();
    		stmt.close();
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
	
	public static void selectExistingQuestionsByCurrentPage(int currentPage, HttpServletResponse response) throws IOException {
		Connection conn = null;
		PreparedStatement pstmt = null, stmt = null;
		JsonObject json = new JsonObject();
		String Answer;
		Collection<Question> QuestionResults = new ArrayList<Question>(); 
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();    		
    		Gson gson = new Gson();
    		ResultSet rss = null;
    		/** prepare the statement of select 20 questions by current page **/
    		int fromQuestion = currentPage * 20;
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_EXISTING_QUESTIONS_STMT);
    		pstmt.setInt(1, fromQuestion);
    		
    		ResultSet rs = pstmt.executeQuery();
    		while( rs.next() )
    		{
    			int questionId = rs.getInt(1);
        		stmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_TOPICS_BY_QUESTION_STMT);
        		stmt.setInt(1,questionId);
        		rss = stmt.executeQuery();
        		ArrayList<String> topics = new ArrayList<>();
        		while (rss.next())
        		{
        			topics.add(rss.getString(1));
        		}
    			String submittionTime = rs.getString(2);
    			String contentTxt = rs.getString(3);
    			int votes = rs.getInt(4);
    			int rate = rs.getInt(5);    			
    			String submittedUser =  rs.getString(6);
    			QuestionResults.add(new Question(questionId, submittionTime ,contentTxt ,topics, submittedUser, votes, rate));
    		}  		
    		       	
    		int numofquestions = (getNumberOfLeftPages(currentPage, "all"))/20;
    		QuestionsResponse qestionsResponse = new QuestionsResponse(QuestionResults, numofquestions);
    		String allQuestionsJsonResult = gson.toJson(qestionsResponse, QuestionsResponse.class);
	        
			PrintWriter writer = response.getWriter();
	    	writer.println(allQuestionsJsonResult);
	    	writer.close();
	    	
    		rss.close();
    		stmt.close();
			rs.close();
			pstmt.close();
			stmt.close();
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
	
	public static void selectQuestionsByTopic(String topicName, int currentPage, HttpServletResponse response) throws IOException {
		Connection conn = null;
		PreparedStatement pstmt = null, stmt = null;
		JsonObject json = new JsonObject();
		String Answer;
		Collection<Question> QuestionResults = new ArrayList<Question>(); 
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();  
    		Gson gson = new Gson();
    		ResultSet rss = null;
    		/** prepare the statement of select 20 questions by current page **/
    		int fromQuestion = currentPage * 20;
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_QUESTIONS_BY_TOPIC_STMT);
    		pstmt.setString(1, topicName);
    		pstmt.setInt(2, fromQuestion);    		
    		ResultSet rs = pstmt.executeQuery();
    		while( rs.next() )
    		{
    			int questionId = rs.getInt(1);
        		stmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_TOPICS_BY_QUESTION_STMT);
        		stmt.setInt(1,questionId);
        		rss = stmt.executeQuery();
        		ArrayList<String> topics = new ArrayList<>();
        		while (rss.next())
        		{
        			topics.add(rss.getString(1));
        		}
        		
    			String submittionTime = rs.getString(2);
    			String contentTxt = rs.getString(3);
    			int votes = rs.getInt(4);
    			int rate = rs.getInt(5);    			
    			String submittedUser =  rs.getString(6);
    			QuestionResults.add(new Question(questionId, submittionTime ,contentTxt ,topics, submittedUser, votes, rate));
    		}  
    		
        	String QuestionsByTopicsJsonResult = gson.toJson(QuestionResults, QuestionAndAnswersConstants.QUESTIONS_COLLECTION);
	        
			PrintWriter writer = response.getWriter();
	    	writer.println(QuestionsByTopicsJsonResult);
	    	writer.close();
    		
    		rss.close();
			rs.close();
			pstmt.close();
			stmt.close();
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
