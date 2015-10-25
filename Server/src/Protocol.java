import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * a class which contains message protocol sent by server
 */
public class Protocol {
	
	/**
	 * new identity message
	 * @param  former
	 * @param  identity
	 * @return json object
	 * @throws JSONException 
	 */
	public static JSONObject newIdentity(String former, String identity) throws JSONException{
		JSONObject newIdentity = new JSONObject();
		newIdentity.put("type", "newidentity");
		newIdentity.put("former", former);
		newIdentity.put("identity", identity);
		return newIdentity;
	}
	
	/**
	 * room list message
	 * @return json object
	 * @throws JSONException
	 */
	public static JSONObject roomList() throws JSONException{
		JSONObject roomList = new JSONObject();
		roomList.put("type", "roomlist");
		ArrayList<JSONObject> roomInfos = new ArrayList<JSONObject>();
		for(Room room: Room.rooms){
			JSONObject roomInfo = new JSONObject();
			roomInfo.put("roomid", room.id);
			roomInfo.put("count", room.connecters.size());
			roomInfos.add(roomInfo);
		}
		
		roomList.put("rooms", roomInfos);
		return roomList;
	}
	
	/**
	 * room contents message
	 * @param  room          
	 * @return json object
	 * @throws JSONException 
	 */
	public static JSONObject roomContents(Room room) throws JSONException{
		JSONObject roomContents = new JSONObject();
		roomContents.put("type", "roomcontents");
		roomContents.put("roomid", room.id);
		ArrayList<String> identities = new ArrayList<String>();
		for (Connecter connecter: room.connecters) {
			identities.add(connecter.id);
		}
		roomContents.put("identities", identities);
		if(room.owner == null){
			roomContents.put("owner", "");
		}
		else{
			roomContents.put("owner", room.owner.id);	
		}
		return roomContents;
	}
	
	/**
	 * room change message
	 * @param  identity
	 * @param  former
	 * @param  roomId
	 * @return json object
	 * @throws JSONException
	 */
	public static JSONObject roomChange(String identity, String former, String roomId) throws JSONException{
		JSONObject roomChange = new JSONObject();
		roomChange.put("type", "roomchange");
		roomChange.put("identity", identity);
		roomChange.put("former", former);
		roomChange.put("roomid", roomId);
		return roomChange;
	}
	
	/**
	 * message
	 * @param  identity
	 * @param  content
	 * @return json object
	 * @throws JSONException
	 */
	public static JSONObject message(String identity, String content) throws JSONException{
		JSONObject message = new JSONObject();
		message.put("type", "message");
		message.put("identity", identity);
		message.put("content", content);
		
		return message;
	}
	
	/**
	 * new user
	 * @param  id            identity
	 * @return               json object
	 * @throws JSONException
	 */
	public static JSONObject newUser(String id) throws JSONException{
		JSONObject newUser = new JSONObject();
		newUser.put("type", "newUser");
		newUser.put("id", id);
		
		return newUser;
	}
	
	/**
	 * authenticated
	 * @param  id            identity
	 * @param  roomId        room id
	 * @return               json object
	 * @throws JSONException
	 */
	public static JSONObject authenticated(String id, String roomId) throws JSONException{
		JSONObject authenticated = new JSONObject();
		authenticated.put("type", "authenticated");
		authenticated.put("id", id);
		authenticated.put("roomId", roomId);
		return authenticated;
	}
	
}
