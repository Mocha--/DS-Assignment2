import java.util.ArrayList;
import java.util.LinkedList;
import org.json.JSONObject;

/**
 * a class which acts as chat room
 */
public class Room {
	
	/**
	 * static variable which contains all rooms
	 */
	public static ArrayList<Room> rooms = new ArrayList<Room>();
	
	/**
	 * static function which can judge if a room id is valid
	 * @param  id
	 * @return
	 */
	public static boolean isIdValid(String id){
		if(Room.findById(id) != null){
			return false;
		}
		
		if(id.length() < 3 || id.length() > 32 || !id.matches("[A-Za-z0-9]+") || id.substring(0,1).matches("[0-9]+")){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * find the room by room id
	 * @param  id room id
	 * @return    room instance
	 */
	public static Room findById(String id){
		for(Room room: Room.rooms){
			if(room.id.equals(id)){
				return room;
			}
		}
		return null;
	}
	
	/**
	 * roomd id
	 */
	public String id;
	
	/**
	 * the owner of the room. here the owner is a thread.
	 * because no matter how the user id changes, the tread
	 * does not change.
	 */
	public SessionThread owner;
	
	/**
	 * message queue
	 */
	public LinkedList<JSONObject> messages;
	
	/**
	 * user threads in this room
	 */
	public ArrayList<SessionThread> sessionThreads;
	
	/**
	 * black list which can prevent user's joining
	 */
	public ArrayList<Black> blackList;
	
	/**
	 * a thread which can broadcast messages to clients
	 * in this room.
	 */
	public BroadcastThread broadcastThread;
	
	/**
	 * constructor
	 * @param  id            room id
	 * @param  sessionThread owner's thread
	 * @return               
	 */
	public Room(String id, SessionThread sessionThread){
		this.id = id;
		this.owner = sessionThread;
		
		this.messages = new LinkedList<JSONObject>();
		this.sessionThreads = new ArrayList<SessionThread>();
		this.broadcastThread = new BroadcastThread(this);
		this.blackList = new ArrayList<Black>();
		
		Room.rooms.add(this);
	}
	
	/**
	 * add a user into this room
	 * @param thread  the new user
	 */
	public void addThread(SessionThread thread){
		this.sessionThreads.add(thread);
		thread.room = this;
	}
	
	/**
	 * remove a user from this room
	 * @param thread the user's thread
	 */
	public void removeThread(SessionThread thread){
		this.sessionThreads.remove(thread);
		thread.room = null;
	}
	
	/**
	 * push a message into the message queue
	 * @param msg message object
	 */
	public void pushMsg(JSONObject msg){
		this.messages.addLast(msg);
	}
	
	/**
	 * is the user which want to join this room in the black list
	 * @param  sessionThread user's thread
	 * @return               true if in the black list, not if not in the black list
	 */
	public boolean isInBlackList(SessionThread sessionThread){
		for( int i = 0 ; i <= this.blackList.size() - 1 ; i++){
			Black black = this.blackList.get(i);
			if(black.sessionThread != sessionThread){
				continue;
			}
			else if(black.sessionThread == sessionThread && black.isAllowJoin()){
				this.blackList.remove(i);
			} else {
				return true;
			}
		}
		return false;
	}
	
}
