import java.util.ArrayList;

public class UserTable {

	public static ArrayList<User> users = new ArrayList<User>();

	public static User find(String id, String password){
		for(User user: UserTable.users){
			if(user.id.equals(id) && user.password.equals(password)){
				return user;
			}
		}
		return null;
	}

	public static User findById(String id){
		for( User user: UserTable.users){
			if(user.id.equals(id)){
				return user;
			}
		}
		return null;
	}

	public static User create(String id, String password){
		User user = new User(id, password);
		UserTable.users.add(user);
		return user;
	}

}
