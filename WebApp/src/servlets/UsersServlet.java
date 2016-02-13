package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import webapp.constants.DBConstants;
import webapp.constants.QuestionAndAnswersConstants;
import webapp.constants.UserConstants;
import webapp.model.Answer;
import webapp.model.Question;
import webapp.model.QuestionsResponse;
import webapp.model.User;
import webapp.model.UsersResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.sun.xml.internal.messaging.saaj.packaging.mime.util.QEncoderStream;

/**
 * Servlet implementation class RegisterServlet
 */
public class UsersServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Gson gson;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UsersServlet() {
        super();
        gson = new GsonBuilder().create();

    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			response.setContentType("application/json");
			//In case there is only query params or no params at all e.g. "/users" or "/users?userName=blabla&..."
			String requestPath = request.getPathInfo();
			if(requestPath == null) {
				//searchUsers(request, response);
				return;
			}
			String[] requestPathParts = requestPath.split("/");
			//if url path is larger than 4 it is wrong url so we have to return NOT_FOUND
			if(requestPathParts.length > 4) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			if (requestPathParts[1].equals("topRated"))
			{
				searchForTopRatedUsers(response);
			}
			String userNickName = requestPathParts[1];
			//In case the path is "/users/<someUserId>"
			if(requestPathParts.length < 3) {
				getUser(userNickName, response);
				return;			
			}
			//resource to get must be "questions" or "topics" otherwise it is wrong url so we have to return NOT_FOUND
			String resourceToGet = requestPathParts[2];
			switch(resourceToGet) {
				case "questions":
					if(requestPathParts.length < 4) {
						response.setStatus(HttpServletResponse.SC_NOT_FOUND);
						return;
					}
					if(requestPathParts[3].equals("asked")) {
						searchForUserAskedQuestions(userNickName, request.getParameter("numOfLastQuestionsAsked"), response);
						return;
					}
					if(requestPathParts[3].equals("answered")) {
						searchForUserAnsweredQuestions(userNickName, request.getParameter("numOfLastQuestionsAnswered"), response);
						return;
					}
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					return;
				case "topics":
					if(requestPathParts.length > 3) {
						response.setStatus(HttpServletResponse.SC_NOT_FOUND);
						return;
					}
					//searchForUserTopics(userNickName, request.getParameter("numOfMostUsedTopics"), response);
					return;
				default:
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					return;
			}
	       // gson.toJson(user, response.getWriter());
		} catch(Exception e) {
			//log.error("Exception in process, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private void searchForUserAskedQuestions(String userNickName, String isFiveLastAskedQuestions, HttpServletResponse response) throws IOException{
		if (isFiveLastAskedQuestions == "true")
		{
			Connection conn = null;
			PreparedStatement pstmt = null, stmt = null;
			Collection<Question> QuestionResults = new ArrayList<Question>(); 
			try 
			{
	        	//obtain CustomerDB data source from Tomcat's context
	    		Context context = new InitialContext();
	    		ResultSet rs = null, rss = null;
				BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
	    		conn = ds.getConnection();    		   		
	    		/** prepare the statement of 5 last asked questions **/
	    		pstmt = conn.prepareStatement(UserConstants.SELECT_LAST_5_ASKED_QUESTION);
	    		pstmt.setString(1,userNickName);
	    		rs = pstmt.executeQuery();
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
	    		QuestionsResponse qestionsResponse = new QuestionsResponse(QuestionResults, 0);   
	    		String fiveLastQuestionsJsonResult = gson.toJson(qestionsResponse, QuestionsResponse.class);
	    		
				PrintWriter writer = response.getWriter();
		    	writer.println(fiveLastQuestionsJsonResult);
		    	writer.close();
		    	response.setStatus(HttpServletResponse.SC_OK);
		    	
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
	
	private void searchForUserAnsweredQuestions(String userNickName, String isFiveLastAskedQuestions, HttpServletResponse response) throws IOException{
		if (isFiveLastAskedQuestions == "true")
		{
			Connection conn = null;
			PreparedStatement pstmt = null, stmt = null;
			Collection<Answer> answersResults = new ArrayList<Answer>(); 
			try 
			{
	        	//obtain CustomerDB data source from Tomcat's context
	    		Context context = new InitialContext();
	    		ResultSet rs = null, rss = null;
				BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
	    		conn = ds.getConnection();    		   		
	    		/** prepare the statement of 5 last asked questions **/
	    		pstmt = conn.prepareStatement(UserConstants.SELECT_LAST_5_ANSWERD_ANSWER);
	    		pstmt.setString(1,userNickName);
	    		rs = pstmt.executeQuery();
	    		while( rs.next() )
	    		{
	    			int answerId = rs.getInt(1);
	    			String submittionTime = rs.getString(2);
	    			String contentTxt = rs.getString(3);
	    			int votes = rs.getInt(4);
	    			int questionId = rs.getInt(5);
	    			String submittedUser =  rs.getString(6);
	    			answersResults.add(new Answer(answerId, submittionTime ,contentTxt , votes, questionId, submittedUser));
	    			
	    		}  	  
	    		String fiveLastAnswersJsonResult = gson.toJson(answersResults, QuestionAndAnswersConstants.ANSWERS_COLLECTION);
	    		
				PrintWriter writer = response.getWriter();
		    	writer.println(fiveLastAnswersJsonResult);
		    	writer.close();
		    	response.setStatus(HttpServletResponse.SC_OK);
		    	   		
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
	
	private void searchForTopRatedUsers(HttpServletResponse response) throws IOException{
		
		Connection conn = null;
		PreparedStatement pstmt = null, stmt = null;
		Collection<User> usersResults = new ArrayList<User>(); 
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		ResultSet rs = null, rss = null;
			BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();    		   		
    		/** prepare the statement of top rated users **/
    		pstmt = conn.prepareStatement(UserConstants.SELECT_TOP_RATED_USERS_STMT);
    		rs = pstmt.executeQuery();
    		while( rs.next() )
    		{
    			String userName = rs.getString(1);
    			String nickName = rs.getString(3);
    			String description = rs.getString(4);
    			String photoUrl = rs.getString(5);
    			int rating =  rs.getInt(6);
    			usersResults.add(new User(userName, null , nickName, description , rating, photoUrl));    			
    		}  
    		UsersResponse userResponse = new UsersResponse(usersResults, null);
    		String topRatedUsersJsonResult = gson.toJson(userResponse, UsersResponse.class);    		
			PrintWriter writer = response.getWriter();
	    	writer.println(topRatedUsersJsonResult);
	    	writer.close();
	    	response.setStatus(HttpServletResponse.SC_OK);
	    	   		
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
	
	private void getUser(String userNickName, HttpServletResponse response){
		
		Connection conn = null;
		PreparedStatement pstmt = null, stmt = null;
		JsonObject json = new JsonObject();
		String Answer;	
		
		try
		{
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
			conn = ds.getConnection();
			
			pstmt = conn.prepareStatement(UserConstants.SELECT_USER_BY_NAME_STMT);
			pstmt.setString(1, userNickName);
    		ResultSet rs = pstmt.executeQuery();
    		
    		User user;
    		
			String userName = rs.getString(1);
			String usernickName = rs.getString(3);
			String description = rs.getString(4);
			String photoUrl = rs.getString(5);
			double rating = rs.getDouble(6);

    		if (rs.next())
    		{
    			user = new User(userName, null, usernickName, description, rating, photoUrl);
    		}
    		
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//obtain CustomerDB data source from Tomcat's context
		BasicDataSource ds = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			Context context = new InitialContext();
			ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
			conn = ds.getConnection(); // get connection
			
			pstmt = conn.prepareStatement(UserConstants.INSERT_USER_STMT);
			// get the parameters from incoming json
			String Usename = request.getParameter("userName");
			String Password = request.getParameter("password");
			String Nickname = request.getParameter("nickName");
			String Desc = request.getParameter("description");
			// insert parameters into SQL Insert
			pstmt.setString(1,Usename);
			pstmt.setString(2,Password);
			pstmt.setString(3,Nickname);
			pstmt.setString(4,Desc);
			
			//execute insert command
			pstmt.executeUpdate();
			//commit update
			conn.commit();
			
			pstmt.close();
			conn.close();
			
			////// Success ////// 
			// Set session to be valid
			HttpSession session = request.getSession();
			session.setMaxInactiveInterval(3600); // seconds 
			session.setAttribute("Username", Usename);
			session.setAttribute("Nickname", Nickname);		
			//build Json Answer
			JsonObject json = new JsonObject();
			json.addProperty("Result", true);
			String Answer = json.toString();
			
			PrintWriter writer = response.getWriter();
        	writer.println(Answer);
        	writer.close();
			
			
		} catch (SQLException | NamingException e) {
				try {
					if(pstmt != null)
						pstmt.close();
					
					if(conn != null)
						conn.close();
					
					
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			// invalidate Session
			HttpSession session = request.getSession();
			session.setAttribute("Username", null);
			session.setAttribute("Nickname", null);
			session.invalidate();
			//build Json Answer
			JsonObject json = new JsonObject();
			json.addProperty("Result", false);
			String Answer = json.toString();
			
			PrintWriter writer = response.getWriter();
        	writer.println(Answer);
        	writer.close();
		}
	}
	


}