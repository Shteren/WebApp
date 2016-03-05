package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import webapp.constants.DBConstants;
import webapp.constants.QuestionAndAnswersConstants;
import webapp.constants.UserConstants;
import webapp.model.Answer;
import webapp.model.UserAccessDB;
import webapp.utils.DBUtils;

/**
 * Servlet implementation class AnswersServlet
 */
public class AnswersServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AnswersServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		Gson gson = new Gson();
		if(request.getPathInfo() != null){
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}

		Answer answer = null;
		try {
			// get answer from incoming json 
			answer = gson.fromJson(request.getReader(), Answer.class);
		//Problem with reading json to question
		} catch(Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		// method that insert new answer to DB
		insertNewAnswer(request, response, answer);

			
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		String requestPath = request.getPathInfo();
		// method put must get uri of answerId
		if(requestPath == null) {
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		String[] requestPathParts = requestPath.split("/");
		//if uri path is smaller than 2 it is wrong uri so we have to return METHOD_NOT_ALLOWED
		if(requestPathParts.length != 2) {
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		Gson gson = new Gson();
		int answerId = Integer.parseInt(requestPathParts[1]);
		Answer answer;
		int numOfVotes=0;
		try {
			answer = gson.fromJson(request.getReader(), Answer.class);
			numOfVotes = answer.getRating();
			int numOfExistingVote = checkIfAnswerIdInDBandSendVoteNumber(request, response, answerId);	
			if(numOfExistingVote == Integer.MIN_VALUE){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			numOfVotes += numOfExistingVote;
		//Problem with reading json to user
		} catch(Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		try {
			updateVoteOfAnswer(request, response, answerId, numOfVotes);
			
		} catch (Exception e) {
			//log.error("Exception in process, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private void insertNewAnswer(HttpServletRequest request, HttpServletResponse response, Answer answer) throws IOException
	{
		BasicDataSource ds = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			Context context = new InitialContext();
			ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
			conn = ds.getConnection(); // get connection
			HttpSession session = request.getSession();
			// prepare statement for insert new answer to DB 
			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.INSERT_ANSWER_STMT);
			
			// get the parameters from incoming json
			Timestamp submiition =  new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
			String contentTxt = answer.getAnswerTxt();
			int questionId = answer.getQuestionId();
			String nickname = (String)session.getAttribute("Nickname");
			
			// insert parameters into statement
			pstmt.setString(1,submiition.toString());
			pstmt.setString(2,contentTxt);
			pstmt.setInt(3, questionId);
			pstmt.setString(4,nickname);
			
			//execute insert command
			pstmt.executeUpdate();
			// update user rating after adding new answer to DB
			UserAccessDB.UpdateUserRating(conn, nickname);
			// update rating of question after adding new answer to DB
			UpdateQuestionRating(conn, request, response, questionId);
			
			conn.commit();		
			
			////// Success //////
			JsonObject json = new JsonObject();
			json.addProperty("Result", false);
			String Answer = json.toString();
			
			PrintWriter writer = response.getWriter();
        	writer.println(Answer);
        	writer.close();
        	
        	DBUtils.closeResultAndStatment(null, pstmt);
        	conn.close();
        	
		} catch (SQLException | NamingException e) {
			System.out.println(e.toString());
			try {
				if(pstmt != null)
					pstmt.close();
				
				if(conn != null){
					conn.rollback();
					conn.close();
				}
				
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			//build Json Answer of error
			JsonObject json = new JsonObject();
			json.addProperty("Result", false);
			String Answer = json.toString();
			
			PrintWriter writer = response.getWriter();
        	writer.println(Answer);
        	writer.close();
		}		
		
	}
	
    /**
     * 
     * @param request
     * @param response
     * @param answerId
     * @return current number of votes of answer
     * @throws IOException
     * 
     * this method check if specific answer exist in DB and return the current votes for it.
     */
    private int checkIfAnswerIdInDBandSendVoteNumber(HttpServletRequest request, HttpServletResponse response, int answerId) throws IOException
    {
		Connection conn = null;
		PreparedStatement pstmt = null;
		int votes = Integer.MIN_VALUE;
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();    		   		
    		/** prepare the statement of check if answer id is in DB **/
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_ANSWER_BY_ID_STMT);
    		pstmt.setInt(1,answerId);
    		
    		ResultSet rs = pstmt.executeQuery();
    		if( !rs.next() )
    		{
    			votes = Integer.MIN_VALUE;
    		} else
    		{
    			// user tring to vote to his answer
				String nickName = (request.getSession().getAttribute("Nickname")).toString();
    			if (rs.getString(6).equals(nickName)) {
        			PrintWriter writer = response.getWriter();
        	    	writer.println("It's your answer");
        	    	writer.close();
        	    	
    			}    				
    			else {
    				// user can vote to answer
    				votes = rs.getInt(4);
    			}
    		}
    		
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
		return votes;
    }
    
    /**
     * 
     * @param request
     * @param response
     * @param answerId
     * @param numOfVotes
     * @throws IOException
     * 
     * this method update vote of answer after client vote for it , and insert to relation table of votes and answers
     * the user that vote - for handling voting twice 
     */
    private void updateVoteOfAnswer(HttpServletRequest request,HttpServletResponse response,int answerId,int numOfVotes) throws IOException
    {
		Connection conn = null;
		PreparedStatement pstmt = null;
		JsonObject json = new JsonObject();
		String answer;
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();    		   		
    		/** prepare the statement of update vote and rating in tbl_answer and tbl_vote_answer in DB **/
       		pstmt = conn.prepareStatement(UserConstants.SELECT_USER_BY_VOTE_ANSWER_STMT);
    		String userNickName = (String)(request.getSession().getAttribute("Nickname"));
    		pstmt.setString(1, userNickName);
    		pstmt.setInt(2, answerId);
    		ResultSet rs = pstmt.executeQuery();
    		if ( rs.next() )
    		{
    			// The user already voted
    			PrintWriter writer = response.getWriter();
    	    	writer.println("The user already vote");
    	    	writer.close();
    	    	return;
    		}
    		// update votes of answer
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.UPDATE_VOTE_FOR_ANSWER_STMT);
    		pstmt.setInt(1, numOfVotes);
    		pstmt.setInt(2, answerId);
   		    		
    		pstmt.executeUpdate();
    		conn.commit();			

    		// insert user to relation table of votes and answers
    		String submittedUser = (String)request.getSession().getAttribute("Nickname");
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.INSERT_VOTE_FOR_ANSWER_STMT);
    		pstmt.setInt(1, answerId);
    		pstmt.setString(2, submittedUser);
    		pstmt.setInt(3, numOfVotes);
    		
    		pstmt.executeUpdate();  
    		
    		conn.commit();	
    		
			// update rating of question after update votes of answer
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_QUESTION_BY_ANSWER_ID_STMT);
    		pstmt.setInt(1, answerId);
    		rs = pstmt.executeQuery();
    		if ( rs.next() ) {
    			UpdateQuestionRating(conn, request, response, rs.getInt(1));
    		}
    		
			// update user rating after changing votes answer to DB
			UserAccessDB.UpdateUserRating(conn, submittedUser);
			// success
			json = new JsonObject();
			json.addProperty("Result", true);
			answer = json.toString();
			
			PrintWriter writer = response.getWriter();
			response.setStatus(HttpServletResponse.SC_OK);
        	writer.println(answer);
        	writer.close();
    		
        	DBUtils.closeResultAndStatment(rs, pstmt);        	
    		conn.close();
    		    		    		
		}catch (SQLException | NamingException e) {
			System.out.println(e.toString());
			try {
				if(pstmt != null)
					pstmt.close();
				
				if(conn != null)
					conn.rollback();
					conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
    	}
    }
    
    /**
     * 
     * @param request
     * @param response
     * @param questionId
     * @throws SQLException
     * @throws IOException
     * 
     * this method updating question rating after changing the votes of answer
     */
    private void UpdateQuestionRating(Connection conn, HttpServletRequest request, HttpServletResponse response, int questionId) throws SQLException, IOException{
    	PreparedStatement  pstmt = null;
    	JsonObject json = new JsonObject();
    	try {   		   		        	
    		// get data of the question that was answered
			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_QUESTION_BY_ID_STMT);
    		pstmt.setInt(1,questionId);
    		ResultSet rss = pstmt.executeQuery();
    		
    		if ( rss.next() )
    		{
    			int questionVotes = rss.getInt(4);
    			int answersRating = QuestionsServlet.scoreOfAnswer(conn, questionId, questionVotes);
    			double questionRating = 0.2 * questionVotes + 0.8 * answersRating;
    			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.UPDATE_VOTE_FOR_QUESTIONS_STMT);
    			pstmt.setInt(1, questionVotes);
    			pstmt.setDouble(2, questionRating);
    			pstmt.setInt(3, questionId); 
    			
    			pstmt.executeUpdate(); 
        		conn.commit();	
        		
        		String userAskedQuestion = rss.getString(6);
        		UserAccessDB.UpdateUserRating(conn, userAskedQuestion);
    		}
    		
			//build Json Answer
			json = new JsonObject();
			json.addProperty("Result", true);
			String answer = json.toString();
			
			PrintWriter writer = response.getWriter();
        	writer.println(answer);        	
        	writer.close();
        	
        	DBUtils.closeResultAndStatment(rss, pstmt);

    		    		    		
		}catch (SQLException e) {
			try {
				if(pstmt != null)
					pstmt.close();
				
				if(conn != null)
					conn.rollback();
					conn.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
    	}
    }

}
