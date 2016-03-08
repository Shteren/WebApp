package webapp.model;

import java.util.Collection;

public class UsersResponse {
	private User user;
	private Collection<Question> five_last_Questions;
	private Collection<UserQuestionAndAnswer> five_last_answered_questions_and_answeres;
			
	public UsersResponse(User user, Collection<Question> five_last_Questions, Collection<UserQuestionAndAnswer> five_last_answered_questions_and_answeres ) {
		super();
		this.user = user;
		this.five_last_Questions = five_last_Questions;
		this.five_last_answered_questions_and_answeres = five_last_answered_questions_and_answeres;
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

	public Collection<UserQuestionAndAnswer> getFive_last_answered_questions_and_answeres() {
		return five_last_answered_questions_and_answeres;
	}

	public void setFive_last_answered_questions_and_answeres(
			Collection<UserQuestionAndAnswer> five_last_answered_questions_and_answeres) {
		this.five_last_answered_questions_and_answeres = five_last_answered_questions_and_answeres;
	}


	
	
	
}
