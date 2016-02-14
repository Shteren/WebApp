package webapp.constants;

public interface UserConstants {
	
	public final String USERS_TABLE_NAME = "tbl_user";
	public final String INSERT_USER_STMT = "INSERT INTO "+ USERS_TABLE_NAME+" (username,password,nickname,description,photoUrl) VALUES(?,?,?,?,'a')";
	public final String SELECT_USER_BY_NAME_STMT = "SELECT * FROM "+USERS_TABLE_NAME +" WHERE username=? ";
	
	public final String SELECT_LAST_5_ASKED_QUESTION = "select * FROM tbl_question"
			+ " where submmitedUsername=?"
			+ " order by submitionTime"
			+ " FETCH First 5 ROWS ONLY";
	
	public final String SELECT_LAST_5_ANSWERD_ANSWER = "select * FROM tbl_anwer"
			+ " where submmitedUsername=?"
			+ " order by submitionTime"
			+ " FETCH First 5 ROWS ONLY";
	
	public final String SELECT_AVG_QUESTION_BY_USER_NAME = "SELECT AVG(questionRating) FROM TBL_QUESTION"
			+ "WHERE submmitedUsername =?";
	
	public final String SELECT_AVG_ANSWER_BY_USER_NAME = "SELECT AVG(numberOfVotes) FROM TBL_ANSWER"
			+ "WHERE submmitedUsername =?";
	
	public final String SELECT_TOP_RATED_USERS_STMT = "select * from tbl_user"
			+ " order by rating desc"
			+ " fetch first 20 rows only";
	
	public final String UPDATE_USER_RATING_STMT = "update tbl_user set rating=? where nickname=?";
	
	public final String SELECT_USER_BY_VOTE_QUESTION_STMT = "select username from tbl_rel_user_question_vote where username=?";
	public final String SELECT_USER_BY_VOTE_ANSWER_STMT = "select username from tbl_rel_user_answer_vote where username=?";
	

}
