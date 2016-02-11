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

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import webapp.constants.DBConstants;
import webapp.constants.QuestionAndAnswersConstants;
import webapp.model.Question;

/**
 * Servlet implementation class GetPreviouesOrNextQuestions
 */
public class GetPreviouesOrNextQuestions extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetPreviouesOrNextQuestions() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Connection conn = null;
		PreparedStatement pstmt = null;
		JsonObject json = new JsonObject();
		String Answer;
		try 
		{
        	//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(DBConstants.DB_DATASOURCE);
    		conn = ds.getConnection();
    		
    		Collection<Question> QuestionResults = new ArrayList<Question>(); 
    		// get number of questions
    		/*pstmt = conn.prepareStatement(QuestionAndAnswersConstants.COUNT_NEWLY_QUESTIONS_STMT);
    		
    		ResultSet rs = pstmt.executeQuery();
    		rs.next();
    		int numofquestions = rs.getInt(1);*/
    		
    		String currentPage = request.getParameter("currentPage");
    		int intCurrentPage;
    		if (null == currentPage)
    			intCurrentPage = 0;
    		else
    		{
    			intCurrentPage = Integer.parseInt(request.getParameter("currentPage"));
    		}
    		int fromQuestion = intCurrentPage * 20;
    		pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_NEWLY_QUESTIONS_STMT);
    		pstmt.setInt(1, fromQuestion);
    		
    		ResultSet rs = pstmt.executeQuery();
    		while( rs.next() )
    		{
    			//int rate = Integer.parseInt(rs.getString(4));
    			QuestionResults.add(new Question(rs.getString(2) ,rs.getString(3) , null,0, rs.getString(5) ));
    		}
    		Gson gson = new Gson();
    		//JsonObject numberOfQuestion= new JsonObject();
    		//numberOfQuestion.addProperty("numOfQuestions", numofquestions);
        	//convert from customers collection to json
        	String QuestionsJsonResult = gson.toJson(QuestionResults, QuestionAndAnswersConstants.QUESTIONS_COLLECTION);
        	
        	//String QuestionResultAndTheNumberOfThem =  "["+QuestionsJsonResult+","+numberOfQuestion+"]";
        	
			PrintWriter writer = response.getWriter();
	    	writer.println(QuestionsJsonResult);
	    	//writer.println(QuestionResultAndTheNumberOfThem);
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}			
    	}
	}
		

}
