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
import org.apache.tomcat.jni.Time;

import com.google.gson.JsonObject;

import webapp.constants.DBConstants;
import webapp.constants.QuestionAndAnswersConstants;
import webapp.constants.UserConstants;

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
        // TODO Auto-generated constructor stub
    }
   
    public void InsertTopics(Connection connection, String[] topiclist) throws SQLException{
    	PreparedStatement pstmt = connection.prepareStatement(QuestionAndAnswersConstants.INSERT_TOPIC_STMT);
    	for (String topic : topiclist) {
    		pstmt.setString(1, topic);
    		pstmt.executeUpdate();
		}
    	connection.commit();
    	pstmt.close();
    }
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
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
			String contentTxt = request.getParameter("questionTxt");
			String topics = request.getParameter("questionTopics");
			String nickname = (String)session.getAttribute("Nickname");
			String[] topicsList = topics.split(",");
			InsertTopics(conn, topicsList); 
			// insert parameters into SQL Insert
			pstmt.setString(1,submiition.toString());
			pstmt.setString(2,contentTxt);
			pstmt.setString(3,nickname);
			
			//execute insert command
			pstmt.executeUpdate();
			//commit update
			
			conn.commit();
			
			pstmt.close();
			pstmt = conn.prepareStatement(QuestionAndAnswersConstants.SELECT_LAST_QUESTION_STMT);
			
			
			
			
			
			ResultSet rs = pstmt.executeQuery();
    		// 
    		if(!rs.next()) // Username doesn't exist
			{
    			System.out.println("here");
			}
    		else
    		{
    			System.out.println("else");
    		}
			
			
			
			
			
			
			
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
			try {
				if(pstmt != null)
					pstmt.close();
				
				if(conn != null)
					conn.close();
				
				
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
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
}
