package webapp.model;

import java.util.Collection;

public class UsersResponse {
	Collection<User> users;
	Collection<String> topics;
			
	public UsersResponse(Collection<User> users, Collection<String> topics) {
		super();
		this.users = users;
		this.topics = topics;
	}
	
	public Collection<User> getUsers() {
		return users;
	}
	public void setUsers(Collection<User> users) {
		this.users = users;
	}
	public Collection<String> getTopics() {
		return topics;
	}
	public void setTopics(Collection<String> topics) {
		this.topics = topics;
	}
	
	
}
