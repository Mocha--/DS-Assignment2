import org.json.JSONException;
import org.json.JSONObject;

/**
 * client messages protocol
 */
public class Protocol {
	/**
	 * identitychange message
	 * @param  identity      new identity
	 * @return message object which will be sent
	 * @throws JSONException
	 */
	public static JSONObject identityChange(String identity) throws JSONException{
		JSONObject identityChange = new JSONObject();
		identityChange.put("type", "identitychange");
		identityChange.put("identity", identity);
		return identityChange;
	}
	
	/**
	 * createroom message
	 * @param  roomId        new room id
	 * @return               message object which will be sent
	 * @throws JSONException
	 */
	public static JSONObject createRoom(String roomId) throws JSONException{
		JSONObject createRoom = new JSONObject();
		createRoom.put("type", "createroom");
		createRoom.put("roomid", roomId);
		return createRoom;
	}
	
	/**
	 * who message
	 * @param  roomId        room id
	 * @return               message object which will be sent
	 * @throws JSONException
	 */
	public static JSONObject who(String roomId) throws JSONException{
		JSONObject who = new JSONObject();
		who.put("type", "who");
		who.put("roomid", roomId);
		return who;
	}
	
	/**
	 * list message
	 * @return message object which will be sent
	 * @throws JSONException
	 */
	public static JSONObject list() throws JSONException{
		JSONObject list = new JSONObject();
		list.put("type", "list");
		return list;
	}
	
	/**
	 * join message
	 * @param  roomId        room to join
	 * @return               message object which will be sent
	 * @throws JSONException 
	 */
	public static JSONObject join(String roomId) throws JSONException{
		JSONObject join = new JSONObject();
		join.put("type", "join");
		join.put("roomid", roomId);
		return join;
	}
	
	/**
	 * delete message
	 * @param  roomId        room id
	 * @return               message object which will be sent
	 * @throws JSONException 
	 */
	public static JSONObject delete(String roomId) throws JSONException{
		JSONObject delete = new JSONObject();
		delete.put("type", "delete");
		delete.put("roomid", roomId);
		return delete;
	}
	
	/**
	 * chat message
	 * @param  content       message content
	 * @return               message object which will be sent
	 * @throws JSONException
	 */
	public static JSONObject message(String content) throws JSONException{
		JSONObject message = new JSONObject();
		message.put("type", "message");
		message.put("content", content);
		return message;
	}
	
	/**
	 * quit message
	 * @return message object which will be sent]
	 * @throws JSONException
	 */
	public static JSONObject quit() throws JSONException{
		JSONObject quit = new JSONObject();
		quit.put("type", "quit");
		return quit;
	}
	
	/**
	 * kick message
	 * @param  roomId        room id
	 * @param  time          time to ban joining
	 * @param  identity      user's id
	 * @return               message object which will be sent
	 * @throws JSONException
	 */
	public static JSONObject kick(String roomId, int time, String identity ) throws JSONException{
		JSONObject kick = new JSONObject();
		kick.put("type", "kick");
		kick.put("roomid", roomId);
		kick.put("time", time);
		kick.put("identity", identity);
		return kick;
	}
	
	public static JSONObject register(String id, String password) throws JSONException{
		JSONObject register = new JSONObject();
		register.put("type", "register");
		register.put("id", id);
		register.put("password", password);
		return register;
	}
	
	public static JSONObject login(String id , String password) throws JSONException{
		JSONObject login = new JSONObject();
		login.put("type", "login");
		login.put("id", id);
		login.put("password", password);
		return login;
	}
}
