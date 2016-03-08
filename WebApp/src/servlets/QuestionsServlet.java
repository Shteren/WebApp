package servlets;
/** here is Question **/
/** Try **/
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
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

import webapp.constants.DBConstants;
import webapp.constants.QuestionAndAnswersConstants;
import webapp.constants.UserConstants;
import webapp.model.Question;
import webapp.model.QuestionAndAnswerDBAccess;
import webapp.model.UserAccessDB;
import webapp.utils.DBUtils;

/**
 * Servlet implementation class Questions
 */
public class QuestionsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public QuestionsServlet() {
        super();
    }
   
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("application/json");
		//In case there is only query params or no params
		String requestPath = request.getPathInfo();
		if(requestPath == null) {
			searchQuestions(request, response);
			return;
		}
		String[] requestPathParts = requestPath.split("/");
		//if uri path is larger than 3 it is wrong uri so we have to return NOT_FOUND
		if(requestPathParts.length > 3) {
			DBUtils.buildJsonResult("Url is empty" , response);
			return;
		}
		String questionId = requestPathParts[1];
		//In case the path is "/question/<questionId>"
		if(requestPathParts.length < 3) {
			//In case the path is "/questions/<somequestionId>"
			getQuestion(Integer.parseInt(questionId), request, response);
			return;
		}
		//resource to get must be "answers" otherwise it is wrong uri so we have to return NOT_FOUND
		String resourceToGet = requestPathParts[2];
		if (resourceToGet.equals("answers"))
		{
			// search for all answers of questions - this methods called from QuestionAndAnswerDBAccess because we called 
			//  this method from Question servlet and from Answer servlet
			QuestionAndAnswerDBAccess.searchForQuestionAnswers(request, response, Integer.parseInt(questionId));
			return;
		//resource is not answers and we are not handling that
		} else
		{
			DBUtils.buildJsonResult("unexpected uri" , response);
			return;
		}
    }
						
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("application/json");
		if(request.getPathInfo() != null){
			DBUtils.buildJsonResult("Uri is empty" , response);
			return;
		}
		Gson gson = new Gson();
		Question question;
		try {
			// insert to question data from client request
			question = gson.fromJson(request.getReader(), Question.class);
		//Problem with reading json to question
		} catch(Exception e) {
			DBUtils.buildJsonResult("Problem reading incoming Json" , response);
			return;
		}
		// method that insert new question to DB
		insertNewQuestion(request, response, question);
	}		
	
@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		Gson gson = new Gson();
		String requestPath = request.getPathInfo();
		// method put must get uri of questionId
		if(requestPath == null) {
			DBUtils.buildJsonResult("Uri is empty" , response);
			return;
		}
		String[] requestPathParts = requestPath.split("/");
		//if uri path is smaller than 2 it is wrong url so we have to return METHOD_NOT_ALLOWED
		if(requestPathParts.length != 2) {
			DBUtils.buildJsonResult("Wrong uri" , response);
			return;
		}
		// get the questionId from client request
		int questionId = Integer.parseInt(requestPathParts[1]);
		Question question;
		int numOfVotes=0;
		try {
			// reading question from incoming json
			question = gson.fromJson(request.getReader(), Question.class);
			numOfVotes = question.getQuestionVotes();
						// return from DB the current num of votes
			int numOfExistingVote = checkIfQuestionIdInDBandSendVoteNumber(request, response, questionId);
			if(numOfExistingVote == Integer.MIN_VALUE){
				DBUtils.buildJsonResult("Question not existing in DB" , response);
				return;
			}
			numOfVotes += numOfExistingVote;

		//Problem with reading json to question
		} catch(Exception e) {
			DBUtils.buildJsonResult("Problem with incoming Json" , response);
			return;
		}
		
		try {
			// update vote of quesion 
			updateVoteOfQuesion(request,response,questionId,numOfVotes);
			return;
			
		} catch (Exception e) {
			//log.error("Exception in process, e);
			DBUtils.buildJsonResult("false" , response);
		}
	}


	//////////////*** implementation of methods that handling with DB ***/////////////////	

	/**
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * 
	 * this method search for questions according to the page client came from
	 * according to number of questions from each type (new, all , topics) we send each time collection of questions. 
	 */
	public static void searchQuestions(HttpServletRequest request, HttpServletResponse response) throws IOException
	{		

		String currentPage = request.getParameter("currentPage");
		// get the page client came from - topics
		String topicName = request.getParameter("topicName");
		// get the page client came from - new or all qyestions
		String newOrAll = request.getParameter("newOrAll");
		if (newOrAll == null)
		{
			newOrAll = "new";
		}
		
		if (null == currentPage)
		{
			currentPage = "0";
		}
		
		if (null == topicName)
		{

			// select the suitable query to be execute
			switch(newOrAll)
			{
				case "new":
				{
					QuestionAndAnswerDBAccess.selectNewlyQuestionsByCurrentPage(Integer.parseInt(currentPage), response);
			    	break;
				}
				case "all":
				{
					QuestionAndAnswerDBAccess.selectExistingQuestionsByCurrentPage(Integer.parseInt(currentPage), response);
			    	break;
				}
			}			
			
		}else
		{
			QuestionAndAnswerDBAccess.selectQuestionsByTopic(topicName, Integer.parseInt(currentPage), response);
	    	
		}	
			
	}
	
	/**
	 * 
	 * @param questionId
	 * @param request
	 * @param response
	 * 
	 * this method returns to client the data of a specific question
	 * @throws IOException 
	 * 
	 */
	private void getQuestion(int questionId, HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		Connection conn = null;
		PreparedStatement pstmt = null, stmt = null;	
		Gson gson = new Gson();
		
		try
		{
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
			conn = ds.getConnection();
			
			// get all data of question according to specific questionId.
			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_QUESTION_BY_ID_STMT);
			pstmt.setInt(1, questionId);
    		ResultSet rs = pstmt.executeQuery();
    		
    		//get all topics of specific questionId
    		stmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_TOPICS_BY_QUESTION_STMT);
    		stmt.setInt(1, questionId);
    		ResultSet rss = stmt.executeQuery();
    		// prepare list of topics for speciific question
    		ArrayList<String> topics = new ArrayList<>();
    		while (rss.next())
    		{
    			topics.add(rss.getString(1));
    		}
    		Question question = null;
    		
			String submittionTime = rs.getString(2);
			String contentTxt = rs.getString(3);
			int votes = rs.getInt(4);
			int rate = rs.getInt(5);    			
			String submittedUser =  rs.getString(6);
    		if (rs.next())
    		{
    			question = new Question(questionId, submittionTime ,contentTxt ,topics, submittedUser, votes, rate, null);
    		}
    		//prepare Json of question to sent by response to client
			String questionJsonResult = gson.toJson(question, Question.class);
	        
			PrintWriter writer = response.getWriter();
	    	writer.println(questionJsonResult);
	    	writer.close();
    		
	    	DBUtils.closeResultAndStatment(rs, pstmt);
	    	DBUtils.closeResultAndStatment(rss, stmt);
	    	
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
	 * @param request
	 * @param response
	 * @param question
	 * @throws IOException
	 * 
	 * this method is for insert new question to DB
	 */
	private void insertNewQuestion(HttpServletRequest request, HttpServletResponse response, Question question) throws IOException
	{
		BasicDataSource ds = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			Context context = new InitialContext();
			ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
			conn = ds.getConnection(); // get connection
			HttpSession session = request.getSession();
			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.INSERT_QUESTION_STMT);
			
			// get the parameters from incoming json
			Timestamp submiition =  new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
			String contentTxt = question.getQuestionsText();
			List<String> topics = question.getQuestionsTopic();
			String nickname = (String)session.getAttribute("Nickname");
			
//			InsertTopics(conn, topics); 
			// insert parameters into SQL Insert
			pstmt.setString(1,submiition.toString());
			pstmt.setString(2,contentTxt);
			pstmt.setString(3,nickname);
			
			//execute insert command
			pstmt.executeUpdate();
			//commit update			
			conn.commit();
			
			// get the last question for insert it with here topics to the relation table of questions and topics
			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_LAST_QUESTION_STMT);
			
			ResultSet rs = pstmt.executeQuery();
			String id = "";
    		if(!rs.next()) // Question doesn't exist
			{
    			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
    		else
    		{
    			 id = rs.getString(1);
    		}
    		
    		// insert all topics of question to relation table
    		InsertTopicsQusrtionRel(conn, topics, id);
    		// update user rating after adding new question to DB
    		UserAccessDB.UpdateUserRating(conn, nickname);
			
			////// Success //////
			
			//build Json Answer
    		DBUtils.buildJsonResult("true", response);
        	
			DBUtils.closeResultAndStatment(rs, pstmt);
			conn.close();
			
		} catch (SQLException | NamingException e) {
			
			System.out.println(e.toString());
			try {
				if(pstmt != null){
					//build Json Answer
					DBUtils.buildJsonResult("false", response);
					pstmt.close();
				}
				
				if(conn != null){
					//build Json Answer
					DBUtils.buildJsonResult("false", response);
					conn.rollback();
					conn.close();
				}
								
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			

		}		
	}
	
	/**
	 * 
	 * @param connection
	 * @param topiclist
	 * @param id
	 * @throws SQLException
	 * 
	 * this method insert to DB - relation table of questions and topics all topics of specific question
	 */
    public void InsertTopicsQusrtionRel(Connection connection, List<String> topiclist, String id) throws SQLException {
    	PreparedStatement pstmt = null;
    	// prepare statment of insert topic and question for each topic
    	pstmt = connection.prepareStatement(QuestionAndAnswersConstants.INSERT_QUESTION_TOPIC_REL_STMT);
    	if (topiclist != null) {
	    	for (String topic : topiclist) 
	    	{
	    		try {
					pstmt.setInt(1, Integer.parseInt(id));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
	    		try {
					pstmt.setString(2, topic );
				} catch (SQLException e) {
					e.printStackTrace();
				}
	    		try {
					pstmt.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}
	    	}
    	}
    	connection.commit();
    	pstmt.close();
  
    }
    
    /**
     * 
     * @param request
     * @param response
     * @param questionId
     * @return current number of votes of question
     * @throws IOException
     * 
     * this method check if specific question exist in DB and return the current votes for it.
     */
    private int checkIfQuestionIdInDBandSendVoteNumber(HttpServletRequest request, HttpServletResponse response, int questionId) throws IOException
    {
		Connection conn = null;
		PreparedStatement pstmt = null;
		// legal votes can't be MIN_VALUE of integer 
		int votes = Integer.MIN_VALUE;
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();    		   		
    		/** prepare the statement of check if questions id is in DB **/
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_QUESTION_BY_ID_STMT);
    		pstmt.setInt(1,questionId);
    		
    		ResultSet rs = pstmt.executeQuery();
    		if( !rs.next() )
    		{
    			votes = Integer.MIN_VALUE;
    		} else
    		{
				String nickName = (request.getSession().getAttribute("Nickname")).toString();
    			if (rs.getString(6).equals(nickName)) {
    				DBUtils.buildJsonResult("It's your question", response);
        	    	votes = Integer.MIN_VALUE;
    			} else {
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
     * @param questionId
     * @param numOfVotes
     * @throws IOException
     * 
     * this method update vote of question after client vote for it , and insert to relation table of votes and questions
     * the user that vote - for handling voting twice 
     */
    private void updateVoteOfQuesion(HttpServletRequest request,HttpServletResponse response,int questionId,int numOfVotes) throws IOException
    {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();    		   		
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_QUESTION_BY_ID_STMT);
    		pstmt.setInt(1, questionId);
    		String votedUserNickName = null;
    		ResultSet rs = pstmt.executeQuery();
    		if ( rs.next() ) {
    			 votedUserNickName = rs.getString(6);
    		}
    		/** prepare the statement of update vote and rating in tbl_question and tbl_vote_question in DB **/
    		pstmt = conn.prepareStatement(UserConstants.SELECT_USER_BY_VOTE_QUESTION_STMT);
    		String userNickName = (String)(request.getSession().getAttribute("Nickname"));
    		pstmt.setString(1, userNickName);
    		pstmt.setInt(2, questionId);
    		rs = pstmt.executeQuery();
    		
    		if ( rs.next() )
    		{
    			// The user already voted
    			DBUtils.buildJsonResult("The user already vote", response);
    			return;
    		}
    		else
    		{
    			// user didnt vote yet
    								//return the avg votes of answers for this question
        		int answersRating = scoreOfAnswer(conn, questionId, numOfVotes);
        		double questionRating = 0.2 * numOfVotes + 0.8 * answersRating;
        		// prepare statement for updating voting score of question
        		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.UPDATE_VOTE_FOR_QUESTIONS_STMT);
        		pstmt.setInt(1, numOfVotes);
        		pstmt.setDouble(2, questionRating);
        		pstmt.setInt(3, questionId);
        		
        		pstmt.executeUpdate();
        		
        		// collect data for insert new user to relation table of votes and questions
        		String submittedUser = (String)request.getSession().getAttribute("Nickname");
        		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.INSERT_VOTE_FOR_QUESTIONS_STMT);
        		
        		pstmt.setInt(1, questionId);
        		pstmt.setString(2, submittedUser);
        		pstmt.setInt(3, numOfVotes);
        		
        		pstmt.executeUpdate(); 
        		
        		conn.commit();			
        		
        		// update user rating after update votes of question
        		UserAccessDB.UpdateUserRating(conn, votedUserNickName);

    			//build Json Answer
        		DBUtils.buildJsonResult("true", response);
            	
            	DBUtils.closeResultAndStatment(rs, pstmt);
        		conn.close();
    		}
    		
		}catch (SQLException | NamingException e) {
			System.out.println(e.toString());
			try {
				if(pstmt != null) {
					pstmt.close();	
				}
				if(conn != null) {
					conn.rollback();
					conn.close();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
    	}
    }
    
    /**
     * 
     * @param conn
     * @param questionId
     * @param numOfVotes
     * @return avg of answers votes for specific question
     */
    public static int scoreOfAnswer(Connection conn, int questionId, int numOfVotes)
    {
		PreparedStatement pstmt = null;
		int answersAvgVotes = 0;
		try 
		{  		
    		/** prepare the statement of select all question answers **/
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_AVG_ANSWERS_BY_QUESTION_ID_STMT);
    		pstmt.setInt(1, questionId);
    		
    		ResultSet rs = pstmt.executeQuery();
    		if ( rs.next() ) {
    			answersAvgVotes = rs.getInt(1);
    		}
    			
		}catch (SQLException e) {
			System.out.println(e.toString());
			try {
				if(pstmt != null) {
					pstmt.close();	
				}
				if(conn != null) {
					conn.close();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}			
    	}
		return answersAvgVotes;
    }
            	
}
