package webapp.model;

import com.sun.jmx.snmp.Timestamp;

public class Question 
{
	String subtime;
	String QuestionsText;
	String QuestionsTopic;
	String Nickname;
	int QuestionRating;
	
	public Question(String s,String txt,String Topic,int Rate, String nick)
	{
		subtime = s; 
		QuestionsText = txt;
		QuestionsTopic = Topic;
		Nickname = nick;
		QuestionRating = Rate;
	}
}
