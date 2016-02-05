package webapp.constants;

public interface UserConstants {
	
	public final String INSERT_USER_STMT = "INSERT INTO tbl_user (username,password,nickname,description,photoUrl) VALUES(?,?,?,?,'a')";
	public final String SELECT_USER_BY_NAME_STMT = "SELECT * FROM tbl_user WHERE username=? ";
	
			

}
