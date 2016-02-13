package webapp.model;

import java.util.Collection;
import java.util.List;

public class QuestionsResponse {
	private Collection<Question> questions;
	int numOfPages;
	
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
