package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

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
import webapp.model.Answer;
import webapp.model.Question;

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
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		if(request.getPathInfo() != null){
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		Gson gson = new Gson();
		Answer answer;
		try {
			answer = gson.fromJson(request.getReader(), Answer.class);
		//Problem with reading json to question
		} catch(Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		BasicDataSource ds = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			Context context = new InitialContext();
			ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
			conn = ds.getConnection(); // get connection
			HttpSession session = request.getSession();
			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.INSERT_ANSWER_STMT);
			
			// get the parameters from incoming json
			Timestamp submiition =  new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
			String contentTxt = answer.getAnswerTxt();
			int questionId = answer.getQuestionId();
			String nickname = (String)session.getAttribute("Nickname");
			
			// insert parameters into SQL Insert
			pstmt.setString(1,submiition.toString());
			pstmt.setString(2,contentTxt);
			pstmt.setInt(3, questionId);
			pstmt.setString(4,nickname);
			
			//execute insert command
			pstmt.executeUpdate();
			
			conn.commit();
			pstmt.close();
			
			conn.close();
			
			////// Success //////
			
			//build Json Answer
			JsonObject json = new JsonObject();
			json.addProperty("Result", true);
			String Answer = json.toString();
			
			PrintWriter writer = response.getWriter();
        	writer.println(Answer);
        	writer.close();
			
		} catch (SQLException | NamingException e) {
			System.out.println(e.toString());
			try {
				if(pstmt != null)
					pstmt.close();
				
				if(conn != null)
					conn.close();
				
				
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			//build Json Answer
			JsonObject json = new JsonObject();
			json.addProperty("Result", false);
			String Answer = json.toString();
			
			PrintWriter writer = response.getWriter();
        	writer.println(Answer);
        	writer.close();
		}		
			
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
		String answerId = requestPathParts[1];
		Answer answer;
		int numOfVotes=0;
		try {
			answer = gson.fromJson(request.getReader(), Answer.class);
			numOfVotes = answer.getRating();
			numOfVotes += checkIfAnswerIdInDBandSendVoteNumber(Integer.parseInt(answerId));
			if(numOfVotes == -1){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		//Problem with reading json to user
		} catch(Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
	}
	
	
    private int checkIfAnswerIdInDBandSendVoteNumber(int answerId)
    {
		Connection conn = null;
		PreparedStatement pstmt = null;
		JsonObject json = new JsonObject();
		String answer;
		int votes = -1;
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();    		   		
    		/** prepare the statement of check if questions id is in DB **/
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_ANSWER_BY_ID_STMT);
    		pstmt.setInt(1,answerId);
    		
    		ResultSet rs = pstmt.executeQuery();
    		if( !rs.next() )
    		{
    			votes = -1;
    		} else
    		{
    			votes = rs.getInt(4);
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
		return votes;
    }
    
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
    		/** prepare the statement of update vote and rating in tbl_question and tbl_vote_question in DB **/
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.UPDATE_VOTE_FOR_ANSWER_STMT);
    		pstmt.setInt(1, numOfVotes);
    		pstmt.setInt(2, answerId);
    		
    		pstmt.executeUpdate();
    		pstmt.close();
    		String submittedUser = (String)request.getSession().getAttribute("nickName");
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.INSERT_VOTE_FOR_ANSWER_STMT);
    		pstmt.setInt(1, answerId);
    		pstmt.setString(2, submittedUser);
    		pstmt.setInt(3, numOfVotes);
    		
    		pstmt.executeUpdate();  
    		
    		conn.commit();			
    		conn.close();
    		
			//build Json Answer
			json = new JsonObject();
			json.addProperty("Result", true);
			answer = json.toString();
			
			PrintWriter writer = response.getWriter();
        	writer.println(answer);
        	writer.close();
    		
    		    		    		
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

}
