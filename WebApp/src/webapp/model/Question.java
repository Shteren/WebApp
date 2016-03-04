package webapp.model;


import java.util.List;

//import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

public class Question 
{
	private int questionId;
	private String submmitionTime;
	private String questionTxt;
	private List<String> questionTopics;
	private String nickName;
	private int questionVote;
	private int QuestionRating;
	private Answer firstAnswer;
	
	public Question(int Id, String subtime,String txt,List<String> topicsList, String nick, int vote, int Rate, Answer answer)
	{
		questionId = Id;
		submmitionTime = subtime; 
		questionTxt = txt;
		questionTopics = topicsList;
		nickName = nick;
		questionVote = vote;
		QuestionRating = Rate;
		firstAnswer = answer;
	}
	
	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public String getSubtime() {
		return submmitionTime;
	}

	public void setSubtime(String subtime) {
		this.submmitionTime = subtime;
	}

	public String getQuestionsText() {
		return questionTxt;
	}

	public void setQuestionsText(String questionsText) {
		questionTxt = questionsText;
	}

	public List<String> getQuestionsTopic() {
		return questionTopics;
	}

	public void setQuestionsTopic(List<String> questionsTopic) {
		questionTopics = questionsTopic;
	}

	public String getNickname() {
		return nickName;
	}

	public void setNickname(String nickname) {
		nickName = nickname;
	}
	
	public int getQuestionVotes() {
		return questionVote;
	}

	public void setQuestionVotes(int questionVotes) {
		this.questionVote = questionVotes;
	}

	public int getQuestionRating() {
		return QuestionRating;
	}

	public void setQuestionRating(int questionRating) {
		QuestionRating = questionRating;
	}

	public Answer getFirstAnswer() {
		return firstAnswer;
	}

	public void setFirstAnswer(Answer firstAnswer) {
		this.firstAnswer = firstAnswer;
	}


	

	

		
}
