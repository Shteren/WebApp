package webapp.constants;

import java.lang.reflect.Type;
import java.util.Collection;

import com.google.gson.reflect.TypeToken;

import webapp.model.Answer;
import webapp.model.Question;

public interface QuestionAndAnswersConstants {
	
	public final Type QUESTIONS_COLLECTION = new TypeToken<Collection<Question>>(){}.getType();
	public final Type ANSWERS_COLLECTION = new TypeToken<Collection<Answer>>(){}.getType();
	public final Type TOPICS_COLLECTION = new TypeToken<Collection<String>>(){}.getType();
	
	public final String INSERT_QUESTION_STMT = "INSERT INTO TBL_QUESTION(submitiontime,contexttext,submmitedusername)"
			+ " VALUES(?,?,?)";

	public final String INSERT_ANSWER_STMT = "INSERT INTO TBL_ANSWER(submitiontime,contexttext,questionid,submmitedusername)"
			+ " VALUES(?,?,?,?)";
	
	public final String INSERT_TOPIC_STMT = "INSERT INTO TBL_TOPIC(topicName) VALUES (?)";
	public final String INSERT_QUESTION_TOPIC_REL_STMT= "INSERT INTO tbl_rel_question_topic (questionId,topicName) VALUES(?,?)";
	
	public final String SELECT_NEWLY_QUESTIONS_STMT = "SELECT * FROM TBL_QUESTION"
			+ " WHERE QUESTIONID NOT IN (SELECT tbl_question.QUESTIONID"
			+ " FROM tbl_question JOIN tbl_answer"
			+ " on tbl_question.QUESTIONID = tbl_answer.QUESTIONID)"
			+ " ORDER BY SUBMITIONTIME DESC"
			+ " OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY";
	
	public final String COUNT_NEWLY_QUESTIONS_STMT = "SELECT COUNT (*) FROM TBL_QUESTION"
			+ " WHERE QUESTIONID NOT IN (SELECT tbl_question.QUESTIONID"
			+ " FROM tbl_question JOIN tbl_answer"
			+ " on tbl_question.QUESTIONID = tbl_answer.QUESTIONID)";
	
	public final String COUNT_EXISTING_QUESTIONS_STMT = "SELECT COUNT (*) FROM TBL_QUESTION"
			+ " WHERE QUESTIONID IN (SELECT tbl_question.QUESTIONID"
			+ " FROM tbl_question JOIN tbl_answer"
			+ " on tbl_question.QUESTIONID = tbl_answer.QUESTIONID)";
	//public final String SELECT_LAST_QUESTION_STMT = "SELECT * FROM TBL_QUESTION" 
		//	+ " ORDER BY SUBMITIONTIME DESC"  
			//+ " LIMIT 1";
	public final String SELECT_LAST_QUESTION_STMT ="SELECT QUESTIONID FROM TBL_QUESTION ORDER BY submitiontime DESC FETCH FIRST 1 ROWS ONLY";
	//public final String SELECT_LAST_QUESTION_STMT ="SELECT QUESTIONID FROM TBL_QUESTION ORDER BY submitiontime DESC ";
	public final String SELECT_QUESTION_BY_ID_STMT = "SELECT * FROM TBL_QUESTION WHERE QUESTIONID=?";
	
	public final String SELECT_ANSWER_BY_ID_STMT = "SELECT * FROM TBL_ANSWER WHERE ANSWERID=?";
	
	public final String SELECT_EXISTING_QUESTIONS_STMT = "SELECT * FROM TBL_QUESTION"
			+ " WHERE QUESTIONID IN (SELECT tbl_question.QUESTIONID"
			+ " FROM tbl_question JOIN tbl_answer"
			+ " on tbl_question.QUESTIONID = tbl_answer.QUESTIONID)"
			+ " ORDER BY QUESTIONRATING DESC"
			+ " OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY"; 
	
	public final String SELECT_QUESTIONS_BY_TOPIC_STMT = "SELECT * FROM tbl_question where"
			+ " questionId IN (select questionId from tbl_rel_question_topic"
								+ " WHERE TOPICNAME=?)"
			+ " ORDER BY QUESTIONRATING DESC"
			+ " OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY";
	
	public final String SELECT_TOPICS_BY_QUESTION_STMT = "select topicName FROM tbl_rel_question_topic where questionId=?";
	
	public final String SELECT_ANSWERS_BY_QUESTION_ID_STMT = "select * from tbl_answer "
			+ " where questionId=?"
			+ " order by numberOfVotes" ;
	
	public final String SELECT_AVG_ANSWERS_BY_QUESTION_ID_STMT = "select AVG(CAST (numberOfVotes AS DOUBLE PRECISION)) from tbl_answer "
			+ " where questionId=?";
	
	public final String INSERT_VOTE_FOR_QUESTIONS_STMT = "INSERT INTO tbl_rel_user_question_vote VALUES (?,?,?)";
	public final String INSERT_VOTE_FOR_ANSWER_STMT = "INSERT INTO tbl_rel_user_answer_vote VALUES (?,?,?)";
	
	public final String UPDATE_VOTE_FOR_QUESTIONS_STMT = "UPDATE tbl_question SET numberOfVotes=?, questionRating=? WHERE QUESTIONID=?";
	public final String UPDATE_VOTE_FOR_ANSWER_STMT = "UPDATE tbl_answer SET numberOfVotes=? WHERE QUESTIONID=?";
	
	public final String SELECT_TOP_20_TOPICS_STMT = "select sum(tbl_question.questionRating), tbl_rel_question_topic.topicName"
			+ " from tbl_question join tbl_tbl_rel_question_topic "
			+ " on tbl_question.questionId = tbl_tbl_rel_question_topic.questionId"
			+ " Group by tbl_rel_question_topic.topicName"
			+ " order by sum(tbl_question.questionRating) DESC"
			+ " OFFSET ? ROWS FETCH NEXT 20 ROWS ONLY";

}

