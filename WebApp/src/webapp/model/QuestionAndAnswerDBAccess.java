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
import webapp.constants.DBConstants;
import webapp.constants.QuestionAndAnswersConstants;
import webapp.utils.Utils;

/**
 * 
 * this class implement some methods that common to answers and questions
 *
 */
public class QuestionAndAnswerDBAccess {
	
	public static int getNumberOfLeftPages(int currentPage, String state)
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		int numberOfQuestions = 0;
		try 
		{
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
			conn = ds.getConnection();
			switch (state)
			{
				case "new":
				{
					pstmt = conn.prepareStatement(QuestionAndAnswersConstants.COUNT_NEWLY_QUESTIONS_STMT);
					break;
				}
				case "all":
				{
					pstmt = conn.prepareStatement(QuestionAndAnswersConstants.COUNT_EXISTING_QUESTIONS_STMT);
				    break;
				}
			}
				
			// get number of questions
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			numberOfQuestions = rs.getInt(1);
			
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
				QuestionResults.add(new Question(questionId, submittionTime ,contentTxt ,topics, submittedUser, votes, rate, null));
				//topics.clear();
				
			}  	
			int numOfQuestions = 0;
			if (((getNumberOfLeftPages(currentPage, "new")) % 20) == 0) {
				numOfQuestions = ((getNumberOfLeftPages(currentPage, "new")) / 20) - 1;
			} else {
				numOfQuestions = ((getNumberOfLeftPages(currentPage, "new")) / 20);
			}			
			QuestionsResponse qestionsResponse = new QuestionsResponse(QuestionResults, numOfQuestions);   
			String newlyQuestionsJsonResult = gson.toJson(qestionsResponse, QuestionsResponse.class);
	
			
			PrintWriter writer = response.getWriter();
	    	writer.println(newlyQuestionsJsonResult);
	    	writer.close();
	    	
	    	Utils.closeResultAndStatment(rs, pstmt);
	    	Utils.closeResultAndStatment(rss, stmt);
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
		    	if(null != rss) {
		    		rss.close();
		    	}
		    	stmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_FIRST_ANSWER_BY_QUESTION_ID_STMT);
	    		stmt.setInt(1,questionId);
	    		rss = stmt.executeQuery();
	    		Answer answer = null;
	    		if (rss.next()) {
	    			answer = new Answer(rss.getInt(1), rss.getString(2), rss.getString(3), rss.getInt(4), rss.getInt(5), rss.getString(6));
	    		}
		    	if(null != rss) {
		    		rss.close();
		    	}
	    		String submittionTime = rs.getString(2);
				String contentTxt = rs.getString(3);
				int votes = rs.getInt(4);
				int rate = rs.getInt(5);    			
				String submittedUser =  rs.getString(6);
				QuestionResults.add(new Question(questionId, submittionTime ,contentTxt ,topics, submittedUser, votes, rate, answer));
			}  		
			int numOfQuestions = 0;
			if (((getNumberOfLeftPages(currentPage, "all"))%20) == 0) {
				numOfQuestions = ((getNumberOfLeftPages(currentPage, "all")) / 20) - 1;
			} else {
				numOfQuestions = ((getNumberOfLeftPages(currentPage, "all")) / 20);
			}
			QuestionsResponse qestionsResponse = new QuestionsResponse(QuestionResults, numOfQuestions);
			String allQuestionsJsonResult = gson.toJson(qestionsResponse, QuestionsResponse.class);
	        
			PrintWriter writer = response.getWriter();
	    	writer.println(allQuestionsJsonResult);
	    	writer.close();
	    	
	    	Utils.closeResultAndStatment(rs, pstmt);
	    	Utils.closeResultAndStatment(rss, stmt);
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
			int counter = 0;
			while( rs.next() )
			{
				counter++;
				int questionId = rs.getInt(1);
	    		stmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_TOPICS_BY_QUESTION_STMT);
	    		stmt.setInt(1,questionId);
	    		rss = stmt.executeQuery();
	    		ArrayList<String> topics = new ArrayList<>();
	    		while (rss.next())
	    		{
	    			topics.add(rss.getString(1));
	    		}
		    	if(null != rss) {
		    		rss.close();
		    	}
		    	stmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_FIRST_ANSWER_BY_QUESTION_ID_STMT);
	    		stmt.setInt(1,questionId);
	    		rss = stmt.executeQuery();
	    		Answer answer = null;
	    		if (rss.next()) {
	    			answer = new Answer(rss.getInt(1), rss.getString(2), rss.getString(3), rss.getInt(4), rss.getInt(5), rss.getString(6));
	    		}
				String submittionTime = rs.getString(2);
				String contentTxt = rs.getString(3);
				int votes = rs.getInt(4);
				int rate = rs.getInt(5);    			
				String submittedUser =  rs.getString(6);
				QuestionResults.add(new Question(questionId, submittionTime ,contentTxt ,topics, submittedUser, votes, rate, answer));
			}  
			if (counter == 0)
			{
				Utils.buildJsonResult("There is no result for this topic", response);
			} else {
				int numOfQuestions = 0;
				pstmt = conn.prepareStatement(QuestionAndAnswersConstants.COUNT_QUESTIONS_BY_TOPIC_STMT);
				pstmt.setString(1, topicName);
				rs = pstmt.executeQuery();
				if ( rs.next() )
				{
					numOfQuestions = rs.getInt(1);
				}
				if ((numOfQuestions % 20) == 0) {
					numOfQuestions = (numOfQuestions / 20) - 1;
				} else {
					numOfQuestions = (numOfQuestions / 20);
				}		
				QuestionsResponse qestionsResponse = new QuestionsResponse(QuestionResults, numOfQuestions);
				String QuestionsByTopicsJsonResult = gson.toJson(qestionsResponse, QuestionsResponse.class);
		    	//String QuestionsByTopicsJsonResult = gson.toJson(QuestionResults, QuestionAndAnswersConstants.QUESTIONS_COLLECTION);
		        
				PrintWriter writer = response.getWriter();
		    	writer.println(QuestionsByTopicsJsonResult);
		    	writer.close();
			}
			
	    	Utils.closeResultAndStatment(rs, pstmt);
	    	Utils.closeResultAndStatment(rss, stmt);
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
	
	public static void searchForQuestionAnswers(HttpServletRequest request, HttpServletResponse response,int questionId) throws IOException 
	{
		Connection conn = null;
		PreparedStatement pstmt = null;
		Collection<Answer> answersResults = new ArrayList<Answer>(); 
		try 
		{
	    	//obtain CustomerDB data source from Tomcat's context
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
			conn = ds.getConnection();    		   		
			/** prepare the statement of select all question answers **/
			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_ANSWERS_BY_QUESTION_ID_STMT);
			pstmt.setInt(1,questionId);
			
			ResultSet rs = pstmt.executeQuery();
			while( rs.next() )
			{
				int answerId = rs.getInt(1);
				String submittionTime = rs.getString(2);
				String contentTxt = rs.getString(3);
				String submittedUser =  rs.getString(6);
				int votes = rs.getInt(4);
				answersResults.add(new Answer(answerId,submittionTime ,contentTxt, votes, questionId, submittedUser));
			}  	
			
			Gson gson = new Gson();
	    	String answersJsonResult = gson.toJson(answersResults, QuestionAndAnswersConstants.ANSWERS_COLLECTION);       	
			PrintWriter writer = response.getWriter();
	    	writer.println(answersJsonResult);	    	
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
