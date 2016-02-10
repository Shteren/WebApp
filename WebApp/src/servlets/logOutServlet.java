package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.JsonObject;

/**
 * Servlet implementation class logOutServlet
 */
public class logOutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public logOutServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
			HttpSession session = request.getSession();
			session.setAttribute("Username", null);
			session.setAttribute("Nickname", null);
			session.invalidate();
			String Answer;
			JsonObject json = new JsonObject();
			json.addProperty("Result", true);
			Answer = json.toString();
			PrintWriter writer = response.getWriter();
	    	writer.println(Answer);
	    	writer.close();
		}catch(Exception e){
			String Answer;
			JsonObject json = new JsonObject();
			json.addProperty("Result", false);
			Answer = json.toString();
			PrintWriter writer = response.getWriter();
	    	writer.println(Answer);
	    	writer.close();
		
		}
		
	}

}
