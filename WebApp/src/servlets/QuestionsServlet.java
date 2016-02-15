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
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.Response;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.apache.tomcat.jni.Time;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.beans.editors.BooleanEditor;

import webapp.constants.DBConstants;
import webapp.constants.QuestionAndAnswersConstants;
import webapp.constants.UserConstants;
import webapp.model.Answer;
import webapp.model.Question;
import webapp.model.QuestionsResponse;

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
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String questionId = requestPathParts[1];
		//In case the path is "/question/<questionId>"
		if(requestPathParts.length < 3) {
			//In case the path is "/questions/<somequestionId>"
			getQuestion(questionId, request, response);
			return;
		}
		//resource to get must be "answers" otherwise it is wrong uri so we have to return NOT_FOUND
		String resourceToGet = requestPathParts[2];
		if (resourceToGet.equals("answers"))
		{
			searchForQuestionAnswers(request, response, questionId);
			return;
		//resource is not answers and we are not handling that
		} else
		{
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
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
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		Gson gson = new Gson();
		Question question;
		try {
			question = gson.fromJson(request.getReader(), Question.class);
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
			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.INSERT_QUESTION_STMT);
			
			// get the parameters from incoming json
			Timestamp submiition =  new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
			String contentTxt = question.getQuestionsText();
			List<String> topics = question.getQuestionsTopic();
			String nickname = (String)session.getAttribute("Nickname");
			
			InsertTopics(conn, topics); 
			// insert parameters into SQL Insert
			pstmt.setString(1,submiition.toString());
			pstmt.setString(2,contentTxt);
			pstmt.setString(3,nickname);
			
			//execute insert command
			pstmt.executeUpdate();
			//commit update
			
			//conn.commit();
			
			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_LAST_QUESTION_STMT);
			
			ResultSet rs = pstmt.executeQuery();
			String id = "";
    		if(!rs.next()) // Question doesn't exist
			{
    			System.out.println("here");
			}
    		else
    		{
    			 id = rs.getString(1);
    		}
    					
    		InsertTopicsQusrtionRel(conn, topics, id);
    		UpdateUserRating(conn, nickname);
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
				if(pstmt != null){
					pstmt.close();
				}
				
				if(conn != null){
					conn.rollback();
					conn.close();
				}
								
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
	
@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		String requestPath = request.getPathInfo();
		// method put must get uri of questionId
		if(requestPath == null) {
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		String[] requestPathParts = requestPath.split("/");
		//if uri path is smaller than 2 it is wrong url so we have to return METHOD_NOT_ALLOWED
		if(requestPathParts.length != 2) {
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return;
		}
		Gson gson = new Gson();
		int questionId = Integer.parseInt(requestPathParts[1]);
		Question question;
		int numOfVotes=0;
		try {
			question = gson.fromJson(request.getReader(), Question.class);
			numOfVotes = question.getQuestionVotes();
			numOfVotes += checkIfQuestionIdInDBandSendVoteNumber(request, questionId);
			if(numOfVotes == -1){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		//Problem with reading json to user
		} catch(Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		try {
			updateVoteOfQuesion(request,response,questionId,numOfVotes);
	        gson.toJson(question, response.getWriter());
	        response.setStatus(HttpServletResponse.SC_OK);
			
		} catch (Exception e) {
			//log.error("Exception in process, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		
		try {
			/***********************/
			/**NOW UPDATE THE USER IN THE DB**/
			//db.update(user)...
			//bla
			//bla
			/***********************/
			
	        gson.toJson(question, response.getWriter());
	        response.setStatus(HttpServletResponse.SC_OK);
			
		} catch (Exception e) {
			//log.error("Exception in process, e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
		
	

	//////////////*** implementation of methods that handling with DB ***/////////////////	
	private void searchQuestions(HttpServletRequest request, HttpServletResponse response) throws IOException
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
    		Gson gson = new Gson();
    		int numofquestions = getNumberOfLeftPages(Integer.parseInt(currentPage), "existing");
    		JsonObject numberOfQuestion= new JsonObject();
    		numberOfQuestion.addProperty("numOfQuestions", numofquestions);
	    	
		}	
			
	}
	
	private void getQuestion(String questionId, HttpServletRequest request, HttpServletResponse response)
	{
		Connection conn = null;
		PreparedStatement pstmt = null, stmt = null;
		JsonObject json = new JsonObject();
		String Answer;	
		
		try
		{
			Context context = new InitialContext();
			BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
			conn = ds.getConnection();
			
			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_QUESTION_BY_ID_STMT);
			pstmt.setInt(1, Integer.parseInt(questionId));
    		ResultSet rs = pstmt.executeQuery();
    		stmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_TOPICS_BY_QUESTION_STMT);
    		stmt.setInt(1,Integer.parseInt(questionId));
    		ResultSet rss = pstmt.executeQuery();
    		ArrayList<String> topics = new ArrayList<>();
    		while (rss.next())
    		{
    			topics.add(rss.getString(1));
    		}
    		Question question;
    		
			String submittionTime = rs.getString(2);
			String contentTxt = rs.getString(3);
			int votes = rs.getInt(4);
			int rate = rs.getInt(5);    			
			String submittedUser =  rs.getString(6);
    		if (rs.next())
    		{
    			question = new Question(Integer.parseInt(questionId), submittionTime ,contentTxt ,topics, submittedUser, votes, rate);
    		}
    		
    		rs.close();
    		rss.close();
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
	
	private int getNumberOfLeftPages(int currentPage, String state)
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
	
	
	private void selectNewlyQuestionsByCurrentPage(int currentPage, HttpServletResponse response) throws IOException {
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


        	//String newlyQuestionsJsonResult = gson.toJson(QuestionResults, QuestionAndAnswersConstants.QUESTIONS_COLLECTION);
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
	
	private void selectExistingQuestionsByCurrentPage(int currentPage, HttpServletResponse response) throws IOException {
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
	
	private void selectQuestionsByTopic(String topicName, int currentPage, HttpServletResponse response) throws IOException {
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
	
	
	public void InsertTopics(Connection connection, List<String> topiclist) throws SQLException{//Try catch and no throw so we can deal with each topic individually and not with the man catch 
    	PreparedStatement pstmt;
		pstmt = connection.prepareStatement(QuestionAndAnswersConstants.INSERT_TOPIC_STMT);
	
    	if (topiclist==null){
    	
    	}else{
	    	for (String topic : topiclist) {
	    		try {
					pstmt.setString(1, topic);
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
	
    public void InsertTopicsQusrtionRel(Connection connection, List<String> topiclist, String id) throws SQLException {
    	PreparedStatement pstmt;
    	pstmt = connection.prepareStatement(QuestionAndAnswersConstants.INSERT_QUESTION_TOPIC_REL_STMT);
    	if (topiclist!=null){
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
    
    private int checkIfQuestionIdInDBandSendVoteNumber(HttpServletRequest request, int questionId)
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
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_QUESTION_BY_ID_STMT);
    		pstmt.setInt(1,questionId);
    		
    		ResultSet rs = pstmt.executeQuery();
    		if( !rs.next() )
    		{
    			votes = -1;
    		} else
    		{
				String nickName = (request.getSession().getAttribute("Nickname")).toString();
    			if (rs.getString(6) == nickName)
    				votes = rs.getInt(4);
    			else
    				votes = -1;
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
    
    private void updateVoteOfQuesion(HttpServletRequest request,HttpServletResponse response,int questionId,int numOfVotes) throws IOException
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
    		pstmt = conn.prepareStatement(UserConstants.SELECT_USER_BY_VOTE_QUESTION_STMT);
    		String userNickName = (String)(request.getSession().getAttribute("Nickname"));
    		pstmt.setString(1, userNickName);
    		ResultSet rs = pstmt.executeQuery();
    		if ( rs.next() )
    		{
    			// The user already voted
    			rs.close();
    			PrintWriter writer = response.getWriter();
    	    	writer.println("The user already vote");
    	    	writer.close();
    			return;
    		}
			
    		int answersRating = calculateRatingScoreOfQuestion(questionId, numOfVotes);
    		double questionRating = 0.2 * numOfVotes + 0.8 * answersRating;
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.UPDATE_VOTE_FOR_QUESTIONS_STMT);
    		pstmt.setInt(1, numOfVotes);
    		pstmt.setDouble(2, questionRating);
    		pstmt.setInt(3, questionId);
    		
    		pstmt.executeUpdate();
    		pstmt.close();
    		String submittedUser = (String)request.getSession().getAttribute("nickName");
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.INSERT_VOTE_FOR_QUESTIONS_STMT);
    		pstmt.setInt(1, questionId);
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
    
    private void searchForQuestionAnswers(HttpServletRequest request, HttpServletResponse response,String questionId) throws IOException 
    {
		Connection conn = null;
		PreparedStatement pstmt = null;
		JsonObject json = new JsonObject();
		String answer;
		Collection<Answer> answersResults = new ArrayList<Answer>(); 
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();    		   		
    		/** prepare the statement of select all question answers **/
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_ANSWERS_BY_QUESTION_ID_STMT);
    		pstmt.setInt(1, Integer.parseInt(questionId));
    		
    		ResultSet rs = pstmt.executeQuery();
    		while( rs.next() )
    		{
    			int answerId = rs.getInt(1);
    			String submittionTime = rs.getString(2);
    			String contentTxt = rs.getString(3);
    			String submittedUser =  rs.getString(6);
    			int votes = rs.getInt(4);
    			answersResults.add(new Answer(answerId,submittionTime ,contentTxt, votes, Integer.parseInt(questionId), submittedUser));
    		}  	
    		
    		Gson gson = new Gson();
        	String answersJsonResult = gson.toJson(answersResults, QuestionAndAnswersConstants.ANSWERS_COLLECTION);       	
			PrintWriter writer = response.getWriter();
	    	writer.println(answersJsonResult);
	    	
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
    
    private int calculateRatingScoreOfQuestion(int questionId, int numOfVotes)
    {
		Connection conn = null;
		PreparedStatement pstmt = null;
		int answersAvgVotes = 0;
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();    		   		
    		/** prepare the statement of select all question answers **/
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_AVG_ANSWERS_BY_QUESTION_ID_STMT);
    		pstmt.setInt(1, questionId);
    		
    		ResultSet rs = pstmt.executeQuery();
    		if ( rs.next() )
    		{
    			answersAvgVotes = rs.getInt(1);
    		} else
    		{
    			// do something
    		}
    			
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
		return answersAvgVotes;
    }
            	
    private void UpdateUserRating(Connection conn, String nickname) throws SQLException{
    	PreparedStatement userStmt = null, quesStmt = null, ansStmt = null;
    	ResultSet quesRes = null, ansRes = null;
    	
    	quesStmt = conn.prepareStatement(UserConstants.SELECT_AVG_QUESTION_BY_USER_NAME);
    	quesStmt.setString(1, nickname);
    	ansStmt = conn.prepareStatement(UserConstants.SELECT_AVG_ANSWER_BY_USER_NAME);
    	ansStmt.setString(1, nickname);
    	userStmt = conn.prepareStatement(UserConstants.UPDATE_USER_RATING_STMT);
    	
    	quesRes = quesStmt.executeQuery();
    	ansRes = ansStmt.executeQuery();
    	
    	if (quesRes.next() && ansRes.next()) {
    		double rating = (0.2 * (quesRes.getInt(1)) + (0.8 * (ansRes.getInt(1))));
    		userStmt.setDouble(1, rating);
    		userStmt.setString(2, nickname);
    		userStmt.executeUpdate();
    	}
    	
    	quesStmt.close();
    	ansStmt.close();
    	userStmt.close();
    }
}
