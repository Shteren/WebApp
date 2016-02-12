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

import webapp.constants.DBConstants;
import webapp.constants.QuestionAndAnswersConstants;
import webapp.constants.UserConstants;
import webapp.model.Question;

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
		//if url path is larger than 4 it is wrong url so we have to return NOT_FOUND
		if(requestPathParts.length > 2) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String questionId = requestPathParts[1];
		//In case the path is "/questions/<somequestionId>"
		getQuestion(questionId, request, response);
		return;
    }


		/*HttpSession session = request.getSession();
		String requestPath = request.getPathInfo();
		Gson gson = new Gson();
        	//convert from customers collection to json
        	String QuestionsJsonResult = gson.toJson(QuestionResults, QuestionAndAnswersConstants.QUESTIONS_COLLECTION);
			PrintWriter writer = response.getWriter();
	    	writer.println(QuestionsJsonResult);
	    	writer.close();
	    	
    		//JsonObject numberOfQuestion= new JsonObject();
    		//numberOfQuestion.addProperty("numOfQuestions", numofquestions);
		}*/
						
		







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
	
	private void searchQuestions(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		String currentPage = request.getParameter("currentPage");
		String topicName = request.getParameter("topicName");
		String newOrExisting = request.getParameter("newOrExisting");
		if (newOrExisting == null)
		{
			newOrExisting = "new";
		}
		
		if (null == topicName)
		{
			if (null == currentPage)
			{
				// bad request
			}
			switch(newOrExisting)
			{
				case "new":
				{
					if(currentPage == null)
					{
						currentPage = "0";
					}
					Collection<Question> newlyQuestionResult = selectNewlyQuestionsByCurrentPage(Integer.parseInt(currentPage));
		    		Gson gson = new Gson();
		    		int numofquestions = getNumberOfLeftPages(Integer.parseInt(currentPage), "new");
		    		JsonObject numberOfQuestion= new JsonObject();
		    		numberOfQuestion.addProperty("numOfQuestions", numofquestions);
		        	//convert from customers collection to json
		        	String newlyQuestionsJsonResult = gson.toJson(newlyQuestionResult, QuestionAndAnswersConstants.QUESTIONS_COLLECTION);
		        	
		        	//String QuestionResultAndTheNumberOfThem =  "["+QuestionsJsonResult+","+numberOfQuestion+"]";
		        	
					PrintWriter writer = response.getWriter();
			    	writer.println(newlyQuestionsJsonResult);
			    	//writer.println(QuestionResultAndTheNumberOfThem);
			    	writer.close();
			    	break;
				}
				case "existing":
				{
					Collection<Question> newlyQuestionResult = selectExistingQuestionsByCurrentPage(Integer.parseInt(currentPage));
		    		Gson gson = new Gson();
		    		int numofquestions = getNumberOfLeftPages(Integer.parseInt(currentPage), "existing");
		    		JsonObject numberOfQuestion= new JsonObject();
		    		numberOfQuestion.addProperty("numOfQuestions", numofquestions);
		        	//convert from customers collection to json
		        	String newlyQuestionsJsonResult = gson.toJson(newlyQuestionResult, QuestionAndAnswersConstants.QUESTIONS_COLLECTION);
		        	
		        	//String QuestionResultAndTheNumberOfThem =  "["+QuestionsJsonResult+","+numberOfQuestion+"]";
		        	
					PrintWriter writer = response.getWriter();
			    	writer.println(newlyQuestionsJsonResult);
			    	//writer.println(QuestionResultAndTheNumberOfThem);
			    	writer.close();
			    	break;
				}
			}			
			
		}else
		{
			Collection<Question> newlyQuestionResult = selectQuestionsByTopic(topicName, Integer.parseInt(currentPage));
    		Gson gson = new Gson();
    		int numofquestions = getNumberOfLeftPages(Integer.parseInt(currentPage), "existing");
    		JsonObject numberOfQuestion= new JsonObject();
    		numberOfQuestion.addProperty("numOfQuestions", numofquestions);
        	//convert from customers collection to json
        	String newlyQuestionsJsonResult = gson.toJson(newlyQuestionResult, QuestionAndAnswersConstants.QUESTIONS_COLLECTION);
        	
        	//String QuestionResultAndTheNumberOfThem =  "["+QuestionsJsonResult+","+numberOfQuestion+"]";
        	
			PrintWriter writer = response.getWriter();
	    	writer.println(newlyQuestionsJsonResult);
	    	//writer.println(QuestionResultAndTheNumberOfThem);
	    	writer.close();
	    	
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
    		ResultSet rs = pstmt.executeQuery();
    		String nickName = (String)request.getSession().getAttribute("nickName");
    		stmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_TOPICS_BY_QUESTION_STMT);
    		stmt.setString(1, rs.getString(1));
    		ResultSet rss = pstmt.executeQuery();
    		ArrayList<String> topics = new ArrayList<>();
    		while (rs.next())
    		{
    			topics.add(rss.getString(1));
    		}
    		Question question;
    		if (rs.next())
    		{
    			question = new Question(rs.getString(1), rs.getString(2), topics, rs.getInt(4), nickName);
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
	
	
	private Collection<Question> selectNewlyQuestionsByCurrentPage(int currentPage) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		JsonObject json = new JsonObject();
		String Answer;
		Collection<Question> QuestionResults = new ArrayList<Question>(); 
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();    		   		
    		/** prepare the statement of select 20 questions by current page **/
    		int fromQuestion = currentPage * 20;
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_NEWLY_QUESTIONS_STMT);
    		pstmt.setInt(1, fromQuestion);
    		
    		ResultSet rs = pstmt.executeQuery();
    		while( rs.next() )
    		{
    			String submittionTime = rs.getString(2);
    			String contentTxt = rs.getString(3);
    			String submittedUser =  rs.getString(5);
    			//int rate = Integer.parseInt(rs.getString(4));
    			QuestionResults.add(new Question(submittionTime ,contentTxt ,null,0, submittedUser));
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
		return QuestionResults;
	}
	
	private Collection<Question> selectExistingQuestionsByCurrentPage(int currentPage) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		JsonObject json = new JsonObject();
		String Answer;
		Collection<Question> QuestionResults = new ArrayList<Question>(); 
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();    		   		
    		/** prepare the statement of select 20 questions by current page **/
    		int fromQuestion = currentPage * 20;
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_EXISTING_QUESTIONS_STMT);
    		pstmt.setInt(1, fromQuestion);
    		
    		ResultSet rs = pstmt.executeQuery();
    		while( rs.next() )
    		{
    			String submittionTime = rs.getString(2);
    			String contentTxt = rs.getString(3);
    			String submittedUser =  rs.getString(6);
    			//int rate = Integer.parseInt(rs.getString(4));
    			QuestionResults.add(new Question(submittionTime ,contentTxt ,null,0, submittedUser));
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
		return QuestionResults;
	}
	
	private Collection<Question> selectQuestionsByTopic(String topicName, int currentPage) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		JsonObject json = new JsonObject();
		String Answer;
		Collection<Question> QuestionResults = new ArrayList<Question>(); 
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();    		   		
    		/** prepare the statement of select 20 questions by current page **/
    		int fromQuestion = currentPage * 20;
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_QUESTIONS_BY_TOPIC_STMT);
    		pstmt.setString(1, topicName);
    		pstmt.setInt(2, fromQuestion);
    		
    		ResultSet rs = pstmt.executeQuery();
    		while( rs.next() )
    		{
    			String submittionTime = rs.getString(2);
    			String contentTxt = rs.getString(3);
    			String submittedUser =  rs.getString(5);
    			//int rate = Integer.parseInt(rs.getString(4));
    			QuestionResults.add(new Question(submittionTime ,contentTxt ,null,0, submittedUser));
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
		return QuestionResults;
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

}
