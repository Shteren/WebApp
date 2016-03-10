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

import webapp.constants.DBConstants;
import webapp.constants.QuestionAndAnswersConstants;
import webapp.constants.UserConstants;
import webapp.model.Answer;
import webapp.model.UserAccessDB;
import webapp.utils.Utils;

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
	 * Should support /answers - request from client to insert new answer
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		Gson gson = new Gson();
		//there is no url from client
		if(request.getPathInfo() != null){
			Utils.buildJsonResult("Url is empty" , response);
			return;
		}

		Answer answer = null;
		try {
			// get answer from incoming json 
			answer = gson.fromJson(request.getReader(), Answer.class);
		//Problem with reading json to question
		} catch(Exception e) {
			Utils.buildJsonResult("Problem reading incoming Json" , response);
			return;
		}
		// method that insert new answer to DB
		insertNewAnswer(request, response, answer);

			
	}

	/**
	 * Should support update of votes by /answers?answerVote
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		String requestPath = request.getPathInfo();
		// method put must get uri of answerId
		if(requestPath == null) {
			Utils.buildJsonResult("Uri is empty" , response);
			return;
		}
		String[] requestPathParts = requestPath.split("/");
		//if uri path is smaller than 2 it is wrong uri so we have to return METHOD_NOT_ALLOWED
		if(requestPathParts.length != 2) {
			Utils.buildJsonResult("Wrong uri" , response);
			return;
		}
		Gson gson = new Gson();
		// get specific answer id from incoming uri
		int answerId = Integer.parseInt(requestPathParts[1]);
		Answer answer;
		// local variable to sum the new num of votes for update relevant DB tables
		int numOfVotes=0;
		try {
			// get answer from incoming Json
			answer = gson.fromJson(request.getReader(), Answer.class);
			numOfVotes = answer.getRating();
									// return from DB the current num of votes
			int numOfExistingVote = checkIfAnswerIdInDBandSendVoteNumber(request, response, answerId);	
			// 
			if(numOfExistingVote == Integer.MIN_VALUE){
				Utils.buildJsonResult("Answer not existing in DB" , response);
				return;
			}
			numOfVotes += numOfExistingVote;
		//Problem with reading json to user
		} catch(Exception e) {
			Utils.buildJsonResult("Problem with incoming Json" , response);
			return;
		}
		
		try {
			// update new votes for specific answer
			updateVoteOfAnswer(request, response, answerId, numOfVotes);
			
		} catch (Exception e) {
			//log.error("Exception in process, e);
			Utils.buildJsonResult("Update votes failed" , response);
		}
	}
	
	/**
	 * this method insert new answer to DB 
	 *
	 * @param request - request from client
	 * @param response - sending true or false indicate for insert new answer to DB
	 * @param answer
	 * @throws IOException
	 */
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
			Utils.buildJsonResult("true" , response);
        	
        	Utils.closeResultAndStatment(null, pstmt);
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
			Utils.buildJsonResult("false" , response);
		}		
		
	}
	
    /**
     * this method check if specific answer exist in DB and return the current votes for it.
     * @param request - request of answer from client
     * @param response - sending indicate from server to client if user trying to vote to is answer
     * @param answerId - the answer user want to vote for
     * @return current number of votes of answer - nubmer of votes for the answerId coming from DB
     * @throws IOException
     * 
     * 
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
    				Utils.buildJsonResult("It's your answer", response);
        	    	votes = Integer.MIN_VALUE;        	    	
    			}    				
    			else {
    				// user can vote to answer
    				votes = rs.getInt(4);
    			}
    		}
    		
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
		return votes;
    }
    
    /**
     * this method update vote of answer after client vote for it , and insert to relation table of votes and answers
     * the user that vote - for handling voting twice 
     * @param request - request from client for user answer votes 
     * @param response - sending indicate from server to client if user trying to vote twice or success to vote
     * @param answerId
     * @param numOfVotes - the number of votes should be update in DB
     * @throws IOException
     * 
     */
    private void updateVoteOfAnswer(HttpServletRequest request,HttpServletResponse response,int answerId,int numOfVotes) throws IOException
    {
		Connection conn = null;
		PreparedStatement pstmt = null;
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
    			Utils.buildJsonResult("The user already voted", response);
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
			Utils.buildJsonResult("true", response);
    		//close resultSet and statements
        	Utils.closeResultAndStatment(rs, pstmt);        	
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
     * this method updating question rating after changing the votes of answer
     * @param request
     * @param response - success to update question rating
     * @param questionId - the question should be update
     * @throws SQLException
     * @throws IOException
     * 
     * 
     */
    private void UpdateQuestionRating(Connection conn, HttpServletRequest request, HttpServletResponse response, int questionId) throws SQLException, IOException{
    	PreparedStatement  pstmt = null;
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
        		// update rating of user that asked the question S
        		UserAccessDB.UpdateUserRating(conn, userAskedQuestion);
    		}
    		
			//build Json Answer
    		Utils.buildJsonResult("true", response);
        	
        	Utils.closeResultAndStatment(rss, pstmt);

    		    		    		
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
