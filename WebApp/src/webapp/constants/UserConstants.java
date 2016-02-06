package webapp.constants;

public interface UserConstants {
	
	public final String USERS_TABLE_NAME = "tbl_user";
	public final String INSERT_USER_STMT = "INSERT INTO "+ USERS_TABLE_NAME+" (username,password,nickname,description,photoUrl) VALUES(?,?,?,?,'a')";
	public final String SELECT_USER_BY_NAME_STMT = "SELECT * FROM "+USERS_TABLE_NAME +" WHERE username=? ";
	
			

}
