package webapp.model;

/**
 * 
 * class for saving questions and their answer for specific user
 *
 */
public class UserQuestionAndAnswer {
	
	private Question question;
	private Answer answer;
	
	/**
	 * 
	 * @param question
	 * @param answer
	 * 
	 */
	public UserQuestionAndAnswer(Question question, Answer answer) {
		super();
		this.question = question;
		this.answer = answer;
	}


	public Question getQuestion() {
		return question;
	}


	public void setQuestion(Question question) {
		this.question = question;
	}


	public Answer getAnswer() {
		return answer;
	}


	public void setAnswer(Answer answer) {
		this.answer = answer;
	}
	
	
	
	
}
