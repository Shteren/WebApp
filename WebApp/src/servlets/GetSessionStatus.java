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
 * Servlet implementation class GetSessionStatus
 */
public class GetSessionStatus extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetSessionStatus() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		//get the session
		HttpSession session = request.getSession();
		String SessionUsername = (String) session.getAttribute("Username");
		String SessionNickname = (String) session.getAttribute("Nickname");
		JsonObject json = new JsonObject();
		String Answer;
		if (SessionUsername == null){
			session.invalidate();
			json.addProperty("Result", false);
		}else{
			json.addProperty("Result", true);
		}
		json.addProperty("Username", SessionUsername);
		json.addProperty("Nickname", SessionNickname);
		//build Json Answer
	
		Answer = json.toString();
		
		PrintWriter writer = response.getWriter();
    	writer.println(Answer);
    	writer.close();
	}

}
