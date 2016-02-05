package webapp.constants;

public interface DBConstants {
	
	public final String USERS = "users";
	//derby constants
	public final String DB_NAME = "WebAppDB";
	public final String DB_DATASOURCE = "java:comp/env/jdbc/WebAppDatasource";
	public final String PROTOCOL = "jdbc:derby:"; 
	
	//sql create tables statements
	public final String CREATE_USERS_TABLE = "CREATE TABLE tbl_user (username varchar(10) PRIMARY KEY,"
			+ "password varchar(100) NOT NULL,"
			+ "nickname varchar(100),"
			+ "description varchar(50),"
			+ "photoUrl varchar(200))";
	public final String CREATE_QUESTIONS_TABLE = "CREATE TABLE tbl_question (questionId INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
			+ "submitionTime TIMESTAMP NOT NULL,"
			+ "contextText varchar(300) NOT NULL,"
			+"numberOfVotes INT DEFAULT(0),"
			+ "submmitedUsername varchar(100) REFERENCES tbl_user(username))";
	public final String CREATE_ANSWERS_TABLE = "CREATE TABLE tbl_answer (answerId INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
			+ "submitionTime TIMESTAMP NOT NULL,"
			+ "contextText varchar(300) NOT NULL,"
			+"numberOfVotes INT DEFAULT(0),"
			+"questionId INT REFERENCES tbl_question(questionId),"
			+ "submmitedUsername varchar(100) REFERENCES tbl_user(username))";
	public final String CREATE_TOPIC_TABLE = "CREATE TABLE tbl_topic (topicId INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
			+ "topicName varchar(100) NOT NULL)";
		public final String CREATE_REL_QUESTIONS_TOPICS_TABLE = "CREATE TABLE tbl_rel_question_topic (questionId INT REFERENCES tbl_question(questionId),"
			+ "topicId INT REFERENCES tbl_topic(topicId),"
			+ "primary key(questionId,topicId))";
	public final String CREATE_REL_USER_QUESTIONS_VOTES_TABLE = "CREATE TABLE tbl_rel_user_question_vote (questionId INT REFERENCES tbl_question(questionId),"
			+ "username varchar(100) REFERENCES tbl_user(username),"
			+ "primary key(questionId,username))";	
	public final String CREATE_REL_USER_ANSWERS_VOTES_TABLE = "CREATE TABLE tbl_rel_user_answer_vote (answerId INT REFERENCES tbl_answer(answerId),"
			+ "username varchar(100) REFERENCES tbl_user(username),"
			+ "vote SMALLINT CONSTRAINT vote_ck CHECK (vote = 1 or vote = -1),"
			+ "primary key(answerId,username))";	
	public final String INSERT_USER_STMT = "INSERT INTO tbl_user (username,password,nickname,description,photoUrl) VALUES(?,?,?,?,'a')";
	//public final String SELECT_ALL_USERS_STMT = "SELECT * FROM tbl_user";


}
