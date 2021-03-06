package webapp.constants;

import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.reflect.TypeToken;

import webapp.model.UsersResponse;

public interface UserConstants {
	
	public final Type TOP_RATED_COLLECTION = new TypeToken<Collection<UsersResponse>>(){}.getType();

	
	public final String USERS_TABLE_NAME = "tbl_user";
	public final String INSERT_USER_STMT = "INSERT INTO TBL_USER(username,password,nickname,description,photoUrl) VALUES(?,?,?,?,?)";
	public final String SELECT_USER_BY_NAME_STMT = "SELECT * FROM TBL_USER WHERE username=? ";
	
	public final String SELECT_LAST_5_ASKED_QUESTION = "select * FROM tbl_question"
			+ " where submmitedUsername=?"
			+ " order by submitionTime"
			+ " FETCH First 5 ROWS ONLY";
	
	public final String SELECT_LAST_5_ANSWERD_ANSWER = "select * FROM tbl_answer"
			+ " where submmitedUsername=?"
			+ " order by submitionTime"
			+ " FETCH First 5 ROWS ONLY";
	
	public final String SELECT_LAST_5_QUESTION_USER_ANSWERED_STMT = "select * from "
			+ " tbl_question JOIN tbl_answer"
			+ " on tbl_question.questionId = tbl_answer.questionId"
			+ " where tbl_answer.submmitedUsername=?"
			+ " order by tbl_answer.submitionTime"
			+ " FETCH First 5 ROWS ONLY";
	
	public final String SELECT_AVG_QUESTION_BY_USER_NAME = "SELECT AVG(questionRating) FROM TBL_QUESTION"
			+ " WHERE submmitedUsername=?";
	
	public final String SELECT_AVG_ANSWER_BY_USER_NAME = "SELECT AVG(CAST (numberOfVotes AS DOUBLE PRECISION)) FROM TBL_ANSWER"
			+ " WHERE submmitedUsername=?";
	
	public final String SELECT_TOP_RATED_USERS_STMT = "select * from tbl_user"
			+ " order by rating desc"
			+ " fetch first 20 rows only";
	
	public final String UPDATE_USER_RATING_STMT = "update tbl_user set rating=? where nickname=?";
	
	public final String SELECT_USER_BY_VOTE_QUESTION_STMT = "select * from tbl_rel_user_question_vote where username=? and questionid=?";
	public final String SELECT_USER_BY_VOTE_ANSWER_STMT = "select * from tbl_rel_user_answer_vote where username=? and answerid=?";
	
	public final String SELECT_USER_EXPERTIES_STMT = "SELECT tbl_rel_question_topic.topicName, sum(tbl_answer.numberOfVotes)"
			+ " FROM tbl_answer JOIN tbl_rel_question_topic"
			+ " on tbl_answer.QUESTIONID = tbl_rel_question_topic.QUESTIONID"
			+ " WHERE tbl_answer.submmitedUsername=?"
			+ " GROUP BY tbl_rel_question_topic.topicName"
			+ " ORDER BY sum(tbl_answer.numberOfVotes) DESC"
			+ " fetch first 5 rows only";
	
	
	

}
