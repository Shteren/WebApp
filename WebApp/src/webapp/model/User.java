package webapp.model;

import java.util.Collection;
import java.util.List;

public class User {
	
	private String userName;
	private String password;
	private String nickName;
	private String description;
	private double userRating;
	private String photoUrl;
	private Collection<String> experties;
			
	public User(String userName, String password, String nickName, String description, double userRating, String photoUrl, Collection<String> experties) {
		this.userName = userName;
		this.password = password;
		this.nickName = nickName;
		this.description = description;
		this.userRating = userRating;
		this.photoUrl = photoUrl;
		this.experties = experties;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getDescriptaion() {
		return description;
	}

	public void setDescriptaion(String descriptaion) {
		this.description = descriptaion;
	}
	public double getUserRating() {
		return userRating;
	}
	public void setUserRating(double userRating) {
		this.userRating = userRating;
	}
	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public Collection<String> getExperties() {
		return experties;
	}

	public void setExperties(List<String> experties) {
		this.experties = experties;
	}
}
