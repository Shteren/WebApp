package webapp.model;

import java.util.Collection;

/**
 * 
 * this class create 20 topics to send in response to client
 *
 */
public class TopicResponse {
	private Collection<String> topics;
	private int numOfPages;
	
	
	
	public TopicResponse(Collection<String> topics, int numOfPages) {
		this.topics = topics;
		this.numOfPages = numOfPages;
	}
	public Collection<String> getTopics() {
		return topics;
	}
	public void setTopics(Collection<String> topics) {
		this.topics = topics;
	}
	public int getNumOfPages() {
		return numOfPages;
	}
	public void setNumOfPages(int numOfPages) {
		this.numOfPages = numOfPages;
	}
	
	
}
