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
	public Connecter owner;
	
	/**
	 * message queue
	 */
	public LinkedList<JSONObject> messages;
	
	/**
	 * user threads in this room
	 */
	public ArrayList<Connecter> connecters;
	
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
	public Room(String id, Connecter connecter){
		this.id = id;
		this.owner = connecter;
		
		this.messages = new LinkedList<JSONObject>();
		this.connecters = new ArrayList<Connecter>();
		this.broadcastThread = new BroadcastThread(this);
		this.blackList = new ArrayList<Black>();
		
		Room.rooms.add(this);
	}
	
	/**
	 * add a user into this room
	 * @param thread  the new user
	 */
	public void addConnecter(Connecter connecter){
		this.connecters.add(connecter);
		connecter.setRoom(this);
	}
	
	/**
	 * remove a user from this room
	 * @param thread the user's thread
	 */
	public void removeConnecter(Connecter connecter){
		this.connecters.remove(connecter);
		connecter.clearRoom();
	}
	
	/**
	 * push a message into the message queue
	 * @param msg message object
	 */
	public void pushMsg(JSONObject msg){
		this.messages.addLast(msg);
	}
	
	/**
	 * if meet the requirement of being deleted
	 * @return [description]
	 */
	public boolean isToBeDeleted(){
		if(this.owner == null && this.connecters.size() == 0){
			return true;
		} else {
			return false;
		}
	}
	
}
