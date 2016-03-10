package webapp.model;

import java.util.Collection;


/**
 * 
 * this class is for create 20 questions and number of pages that left to send in response to client
 *
 */
public class QuestionsResponse {
	private Collection<Question> questions;
	private int numOfPages;
	//private Answer firstAnswer;
	
	public QuestionsResponse(Collection<Question> questions, int numOfPages) {
		this.questions = questions;
		this.numOfPages = numOfPages;
	}

	public Collection<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(Collection<Question> questions) {
		this.questions = questions;
	}

	public int getNumOfPages() {
		return numOfPages;
	}

	public void setNumOfPages(int numOfPages) {
		this.numOfPages = numOfPages;
	}
		
}
