package servlets;

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
import webapp.model.User;
import webapp.model.UserQuestionAndAnswer;
import webapp.model.UsersResponse;
import webapp.utils.DBUtils;

import com.google.gson.Gson;

/**
 * Servlet implementation class RegisterServlet
 */
public class UsersServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UsersServlet() {
        super();
    }
    
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			response.setContentType("application/json");
			//In case there is only query params or no params at all e.g. "/users" 
			String requestPath = request.getPathInfo();
			if(requestPath == null) {
				searchUsers(request, response);
				return;
			}
			String[] requestPathParts = requestPath.split("/");
			//if url path is larger than 2 it is wrong url so we have to return NOT_FOUND
			if(requestPathParts.length > 2) {
				DBUtils.buildJsonResult("Wrong uri" , response);
				return;
			}
			// get the user nickname from client request
			//String userNickName = requestPathParts[1];
			//In case the path is "/users/<someUserNickName>"
			if(requestPathParts.length < 2) {
			//	getUser(userNickName, response);
			}
		} catch(Exception e) {
			//log.error("Exception in process, e);
			DBUtils.buildJsonResult("false" , response);
		}
	}
	
    // search for 20 top rated users
    private void searchUsers (HttpServletRequest request, HttpServletResponse response) throws IOException {
    	
    	searchForTopRatedUsers(response);
    }
    
    ///////////******* methods handling with logic and DB *******///////////////
    
	private void searchForTopRatedUsers(HttpServletResponse response) throws IOException
	{	
		Connection conn = null;
		PreparedStatement pstmt = null, stmt = null;
		User userResult = null;
		Collection<UsersResponse> topRatedUser = new ArrayList<UsersResponse>();
		Collection<Question> five_last_asked_questions = null;
		Collection<UserQuestionAndAnswer> five_last_answered_questions_and_answeres = null;
		
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		ResultSet rs = null, answeredRS = null;
			BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();    		   		
    		/** prepare the statement of top rated users **/
    		pstmt = conn.prepareStatement(UserConstants.SELECT_TOP_RATED_USERS_STMT);
    		rs = pstmt.executeQuery();
    		while( rs.next() )
    		{
    			// get all parameters from result of the query
    			String userName = rs.getString(1);
    			String nickName = rs.getString(3);
    			String description = rs.getString(4);
    			String photoUrl = rs.getString(5);
    			double rating =  rs.getDouble(6);
    			// build user expertise
    			Collection<String> experties = findUserExperties(conn, nickName);
    			// build topRated user
    			userResult = new User(userName, null , nickName, description , rating, photoUrl, experties); 
    			// get the 5 last questions user asked
    			five_last_asked_questions = searchForUserAskedQuestions(conn, nickName);
    			
    			// get the 5 last questions user answered and his answer
        		pstmt = conn.prepareStatement(UserConstants.SELECT_LAST_5_QUESTION_USER_ANSWERED_STMT);
        		pstmt.setString(1, nickName);
        		answeredRS = pstmt.executeQuery();
        		five_last_answered_questions_and_answeres = new ArrayList<>();
        		while ( answeredRS.next() ) {
        			int questionId = answeredRS.getInt(1);
        			// get topics for each question
            		stmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_TOPICS_BY_QUESTION_STMT);
            		stmt.setInt(1,questionId);
            		ResultSet topicsRS = stmt.executeQuery();
            		ArrayList<String> topics = new ArrayList<>();
            		while (topicsRS.next())
            		{
            			topics.add(topicsRS.getString(1));
            		}
            		topicsRS.close();
            		// build question and user answer to this question
        			String submittionTime = answeredRS.getString(2);
        			String contentTxt = answeredRS.getString(3);
        			int votes = answeredRS.getInt(4);
        			double rate = answeredRS.getDouble(5);    			
        			String submittedUser =  rs.getString(6);
        			// build 5 last questions user answered
        			Question question = new Question(questionId, submittionTime ,contentTxt ,topics, submittedUser, votes, rate, null);
        			int answerId = answeredRS.getInt(7);
        			String answerSubmittionTime = answeredRS.getString(8);
        			String answerContentTxt = answeredRS.getString(9);
        			String answerSubmittedUser =  answeredRS.getString(12);
        			int answerVotes = answeredRS.getInt(10);
        			// build 5 last answer for those question (in same order)
        			Answer answer = new Answer(answerId,answerSubmittionTime ,answerContentTxt, answerVotes, questionId, answerSubmittedUser);
        			UserQuestionAndAnswer uQandA = new UserQuestionAndAnswer(question, answer);
        			five_last_answered_questions_and_answeres.add(uQandA);
        		}
        		// add user to the 20 top rated users
        		topRatedUser.add(new UsersResponse(userResult, five_last_asked_questions, five_last_answered_questions_and_answeres));
        		
    		}  
    		
    		// prepare collection of top rated user with all data for sending to client
			Gson gson = new Gson();
	    	String topRatedJsonResult = gson.toJson(topRatedUser, UserConstants.TOP_RATED_COLLECTION);       	
			PrintWriter writer = response.getWriter();
	    	writer.println(topRatedJsonResult);	    	
	    	writer.close();  
	    	    	   		
	    	DBUtils.closeResultAndStatment(rs, pstmt);
	    	DBUtils.closeResultAndStatment(answeredRS, stmt);
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
	 * 
	 * @param userNickName
	 * @return this method return the last 5 question user asked
	 * @throws IOException
	 */
	private Collection<Question> searchForUserAskedQuestions(Connection conn, String userNickName) throws IOException{
		PreparedStatement pstmt = null, stmt = null;
		ResultSet rs = null, rss = null;
		Collection<Question> five_last_asked_questions = new ArrayList<Question>(); 
		try 
		{
    		/** prepare the statement of 5 last asked questions **/
    		pstmt = conn.prepareStatement(UserConstants.SELECT_LAST_5_ASKED_QUESTION);
    		pstmt.setString(1, userNickName);
    		rs = pstmt.executeQuery();
    		
    		while ( rs.next() ) {
    			int questionId = rs.getInt(1);
        		stmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_TOPICS_BY_QUESTION_STMT);
        		stmt.setInt(1,questionId);
        		rss = stmt.executeQuery();
        		ArrayList<String> topics = new ArrayList<>();
        		while (rss.next())
        		{
        			topics.add(rss.getString(1));
        		}
        		rss.close();
    			String submittionTime = rs.getString(2);
    			String contentTxt = rs.getString(3);
    			int votes = rs.getInt(4);
    			int rate = rs.getInt(5);    			
    			String submittedUser =  rs.getString(6);
    			five_last_asked_questions.add(new Question(questionId, submittionTime ,contentTxt ,topics, submittedUser, votes, rate, null));
    		}
    		
    		DBUtils.closeResultAndStatment(rs, pstmt);
    		DBUtils.closeResultAndStatment(rss, stmt);
    		
		}catch (SQLException e) {
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
		return five_last_asked_questions;	
	}
	

	
/*	private void getUser(String userNickName, HttpServletResponse response) throws IOException{
		
		Connection conn = null;
		PreparedStatement pstmt = null, stmt = null;
		User userResult = null; 
		Collection<Question> five_last_asked_questions = null;
		Collection<Question> five_last_user_answered_questions = new ArrayList<Question>();
		Collection<Answer> five_last_user_answers = new ArrayList<Answer>();
		
		try
		{
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
			conn = ds.getConnection();
			
			pstmt = conn.prepareStatement(UserConstants.SELECT_USER_BY_NAME_STMT);
			pstmt.setString(1, userNickName);
    		ResultSet rs = pstmt.executeQuery();
    		
			String userName = rs.getString(1);
			String usernickName = rs.getString(3);
			String description = rs.getString(4);
			String photoUrl = rs.getString(5);
			double rating = rs.getDouble(6);
			Collection<String> experties = findUserExperties(conn, usernickName);

    		if (rs.next())
    		{
    			userResult = new User(userName, null, usernickName, description, rating, photoUrl, experties);
    		}
    		
    		five_last_asked_questions = searchForUserAskedQuestions(userNickName);
    		
    		pstmt = conn.prepareStatement(UserConstants.SELECT_LAST_5_QUESTION_USER_ANSWERED_STMT);
    		pstmt.setString(1, userNickName);
    		rs = pstmt.executeQuery();
    		
    		while ( rs.next() ) {
    			int questionId = rs.getInt(1);
        		stmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_TOPICS_BY_QUESTION_STMT);
        		stmt.setInt(1,questionId);
        		ResultSet rss = stmt.executeQuery();
        		ArrayList<String> topics = new ArrayList<>();
        		while (rss.next())
        		{
        			topics.add(rss.getString(1));
        		}
        		rss.close();
    			String submittionTime = rs.getString(2);
    			String contentTxt = rs.getString(3);
    			int votes = rs.getInt(4);
    			int rate = rs.getInt(5);    			
    			String submittedUser =  rs.getString(6);
    			five_last_user_answered_questions.add(new Question(questionId, submittionTime ,contentTxt ,topics, submittedUser, votes, rate, null));
    			int answerId = rs.getInt(7);
    			String answerSubmittionTime = rs.getString(8);
    			String answerContentTxt = rs.getString(9);
    			String answerSubmittedUser =  rs.getString(12);
    			int answerVotes = rs.getInt(10);
    			five_last_user_answers.add(new Answer(answerId,answerSubmittionTime ,answerContentTxt, answerVotes, questionId, answerSubmittedUser));
    			
    		}
    		
    		UsersResponse userResponse = new UsersResponse(userResult, five_last_asked_questions, five_last_user_answered_questions, five_last_user_answers);
    		String UserJsonResult = gson.toJson(userResponse, UsersResponse.class);
	        
			PrintWriter writer = response.getWriter();
	    	writer.println(UserJsonResult);
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
	*/
	
	/**
	 * 
	 * @param conn
	 * @param userNickName
	 * @return this methods return the 5 topics that including in questions that user answered them and the answers were top voted
	 * @throws SQLException
	 */
	Collection<String> findUserExperties(Connection conn, String userNickName) throws SQLException {
		
		PreparedStatement pstmt = null;
		Collection<String> experties = new ArrayList<String>();
		// prepare statment for find 5 topics that are the expertise of user
		pstmt = conn.prepareStatement(UserConstants.SELECT_USER_EXPERTIES_STMT);
		pstmt.setString(1, userNickName);
		
		ResultSet rs = pstmt.executeQuery();
		while ( rs.next() ) {
			// add to list new topic
			experties.add(rs.getString(1));
		}		
		rs.close();
		
		if(experties.size() == 0)
			return null;
		return experties;
	}
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//obtain CustomerDB data source from Tomcat's context
		BasicDataSource ds = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			Context context = new InitialContext();
			ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
			conn = ds.getConnection(); // get connection
			Gson gson = new Gson();
			User user;
			try {
				user = gson.fromJson(request.getReader(), User.class);
			//Problem with reading json to question
			} catch(Exception e) {
				DBUtils.buildJsonResult("Problem reading incoming Json" , response);
				return;
			}
			pstmt = conn.prepareStatement(UserConstants.INSERT_USER_STMT);
			// get the parameters from incoming json
			String Usename = user.getUserName();
			String Password = user.getPassword();
			String nickName = user.getNickName();
			String Desc = user.getDescriptaion();
			String photoUrl = user.getPhotoUrl();
			if (photoUrl == null) {
				photoUrl = "images/defultimg.png";
			}
			// insert parameters into SQL Insert
			pstmt.setString(1,Usename);
			pstmt.setString(2,Password);
			pstmt.setString(3,nickName);
			pstmt.setString(4,Desc);
			pstmt.setString(5, photoUrl);
			
			//execute insert command
			pstmt.executeUpdate();
			//commit update
			conn.commit();
			
			////// Success ////// 
			// Set session to be valid
			HttpSession session = request.getSession();
			session.setMaxInactiveInterval(3600); // seconds 
			session.setAttribute("Username", Usename);
			session.setAttribute("Nickname", nickName);	
			
			//build Json Answer
			DBUtils.buildJsonResult("true", response);
			
			pstmt.close();
			conn.close();
			
		} catch (SQLException | NamingException e) {
				try {
					if(pstmt != null)
						pstmt.close();
					
					if(conn != null)
						conn.close();
					
					
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			
			// invalidate Session
			HttpSession session = request.getSession();
			session.setAttribute("Username", null);
			session.setAttribute("Nickname", null);
			session.invalidate();
			
			//build Json Answer
			DBUtils.buildJsonResult("false", response);
		}
	}
}