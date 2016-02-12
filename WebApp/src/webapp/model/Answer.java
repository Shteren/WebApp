package webapp.model;

public class Answer {
	
	private int answerId;
	private String submmitionTime;
	private String answerTxt;
	private int rating;
	private int questionId;
	private String nickName;

	
	public Answer(int answerId, String submmitionTime, String answerTxt, int rating, int quesionId, String nickName) {
		this.answerId = answerId;
		this.submmitionTime = submmitionTime;
		this.answerTxt = answerTxt;
		this.rating = rating;
		this.questionId = quesionId;
		this.nickName = nickName;
		
	}

	public int getAnswerId() {
		return answerId;
	}

	public void setAnswerId(int answerId) {
		this.answerId = answerId;
	}

	public String getSubmmitionTime() {
		return submmitionTime;
	}

	public void setSubmmitionTime(String submmitionTime) {
		this.submmitionTime = submmitionTime;
	}

	public String getAnswerTxt() {
		return answerTxt;
	}

	public void setAnswerTxt(String answerTxt) {
		this.answerTxt = answerTxt;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}
	
	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	
	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	
	
	
	
}
