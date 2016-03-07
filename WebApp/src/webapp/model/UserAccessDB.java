package webapp.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import webapp.constants.UserConstants;

public class UserAccessDB {

	public static void UpdateUserRating(Connection conn, String nickname) throws SQLException{
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
    		double rating = (0.2 * (quesRes.getDouble(1)) + (0.8 * (ansRes.getInt(1))));
    		userStmt.setDouble(1, rating);
    		userStmt.setString(2, nickname);
    		userStmt.executeUpdate();
    	}
    	
    	quesStmt.close();
    	ansStmt.close();
    	userStmt.close();
    }

}
