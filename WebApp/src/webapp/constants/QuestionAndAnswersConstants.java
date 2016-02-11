package webapp.constants;

import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.reflect.TypeToken;

import webapp.model.Question;

public interface QuestionAndAnswersConstants {
	
	public final Type QUESTIONS_COLLECTION = new TypeToken<Collection<Question>>(){}.getType();
	
	public final String INSERT_QUESTION_STMT = "INSERT INTO TBL_QUESTION(submitiontime,contexttext,submmitedusername)"
			+ " VALUES(?,?,?)";

	public final String INSERT_ANSWER_STMT = "INSERT INTO TBL_ANSWER(submitiontime,contexttext,questionid,submmitedusername)"
			+ " VALUES(?,?,?,?)";
	
	public final String INSERT_TOPIC_STMT = "INSERT INTO TBL_TOPIC(topicName) VALUES (?)";
	
	public final String SELECT_NEWLY_QUESTIONS_STMT = "SELECT * FROM TBL_QUESTION"
			+ " WHERE QUESTIONID NOT IN (SELECT tbl_question.QUESTIONID"
			+ " FROM tbl_question JOIN tbl_answer"
			+ " on tbl_question.QUESTIONID = tbl_answer.QUESTIONID)"
			+ " ORDER BY SUBMITIONTIME DESC FETCH FIRST 20 ROWS ONLY";
	
	public final String COUNT_NEWLY_QUESTIONS_STMT = "SELECT COUNT (*) FROM TBL_QUESTION"
			+ " WHERE QUESTIONID NOT IN (SELECT tbl_question.QUESTIONID"
			+ " FROM tbl_question JOIN tbl_answer"
			+ " on tbl_question.QUESTIONID = tbl_answer.QUESTIONID)";
	//public final String SELECT_LAST_QUESTION_STMT = "SELECT * FROM TBL_QUESTION" 
		//	+ " ORDER BY SUBMITIONTIME DESC"  
			//+ " LIMIT 1";
	public final String SELECT_LAST_QUESTION_STMT ="SELECT QUESTIONID FROM TBL_QUESTION ORDER BY submitiontime DESC FETCH FIRST 1 ROWS ONLY";
	//public final String SELECT_LAST_QUESTION_STMT ="SELECT QUESTIONID FROM TBL_QUESTION ORDER BY submitiontime DESC ";
	
	public final String INSERT_QUESTION_TOPIC_REL_STMT= "INSERT INTO tbl_rel_question_topic (questionId,topicName) VALUES(?,?)";


}
