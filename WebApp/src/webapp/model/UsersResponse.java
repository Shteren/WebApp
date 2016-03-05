package webapp.model;

import java.util.Collection;

public class UsersResponse {
	private User user;
	private Collection<Question> five_last_Questions;
	private Collection<Question> five_last_answered_quetions;
	private Collection<Answer> five_last_user_answered;
			
	public UsersResponse(User user, Collection<Question> five_last_Questions, Collection<Question> five_last_answered_quetions, Collection<Answer> five_last_user_answered ) {
		super();
		this.user = user;
		this.five_last_Questions = five_last_Questions;
		this.five_last_answered_quetions = five_last_answered_quetions;
		this.five_last_user_answered = five_last_user_answered;
	}
	
	public User getUsers() {
		return user;
	}
	public void setUsers(User user) {
		this.user = user;
	}
	public Collection<Question> getLastQuestions() {
		return five_last_Questions;
	}
	public void setTopics(Collection<Question> lastQuestions) {
		this.five_last_Questions = lastQuestions;
	}

	public Collection<Question> getFive_last_answered_quetions() {
		return five_last_answered_quetions;
	}

	public void setFive_last_answered_quetions(Collection<Question> five_last_answered_quetions) {
		this.five_last_answered_quetions = five_last_answered_quetions;
	}

	public Collection<Answer> getFive_last_user_answered() {
		return five_last_user_answered;
	}

	public void setFive_last_user_answered(Collection<Answer> five_last_user_answered) {
		this.five_last_user_answered = five_last_user_answered;
	}
	
	
	
}
