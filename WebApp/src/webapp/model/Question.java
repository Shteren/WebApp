package webapp.model;

import java.util.ArrayList;
import java.util.List;

import com.sun.jmx.snmp.Timestamp;
//import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

public class Question 
{
	private String subtime;
	private String questionTxt;
	private List<String> questionTopics;
	private String Nickname;
	private int QuestionRating;
	
	public Question(String s,String txt,List<String> topicsList, int Rate, String nick)
	{
		subtime = s; 
		questionTxt = txt;
		questionTopics = topicsList;
		Nickname = nick;
		QuestionRating = Rate;
	}
	
	public String getSubtime() {
		return subtime;
	}

	public void setSubtime(String subtime) {
		this.subtime = subtime;
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
		return Nickname;
	}

	public void setNickname(String nickname) {
		Nickname = nickname;
	}

	public int getQuestionRating() {
		return QuestionRating;
	}

	public void setQuestionRating(int questionRating) {
		QuestionRating = questionRating;
	}


	

	

		
}
