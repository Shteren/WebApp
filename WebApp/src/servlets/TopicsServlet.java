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
import webapp.model.Question;
import webapp.model.QuestionsResponse;
import webapp.model.User;
import webapp.model.UsersResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Servlet implementation of topic resource
 */
public class TopicsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Gson gson;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TopicsServlet() {
        super();
        gson = new GsonBuilder().create();

    }
    
    /**
     * Should support the api
     * /topics?currentPage
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		try {
			response.setContentType("application/json");		
			searchTopics(request, response);
			return;
			
		} catch(Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
    private void searchTopics(HttpServletRequest request, HttpServletResponse response) throws NumberFormatException, IOException {
    	String currentPage = request.getParameter("currentPage");
		if(currentPage == null) {
			currentPage = "0";
		}

		selectTopicsByCurrentPage(Integer.parseInt(currentPage), response);			
	}
    
    
    private void selectTopicsByCurrentPage(int currentPage, HttpServletResponse response) throws IOException {

	}
	
}