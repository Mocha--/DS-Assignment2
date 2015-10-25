import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * abstract class connecter
 * any client who connects to server is a connecter
 */
public abstract class Connecter {
	
	/**
	 * all connecters
	 */
	public static ArrayList<Connecter> connecters = new ArrayList<Connecter>();
	
	/**
	 * find a connecter by id
	 * @param  id connecter's id
	 * @return    the connecter if found
	 */
	public static Connecter findById(String id){
		for(Connecter connecter: Connecter.connecters){
			if(connecter.id.equals(id)){
				return connecter;
			}
		}
		return null;
	}
	
	/**
	 * is a new id valid
	 * @param  id session thread id
	 * @return
	 */
	public static boolean isIdValid(String id){
		if(Connecter.findById(id) != null){
			return false;
		}
		
		if(id.length() < 3 || id.length() > 16 || !id.matches("[A-Za-z0-9]+") || id.substring(0,1).matches("[0-9]+")){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * connecter's id
	 */
	public String id;
	
	/**
	 * connecter's current room
	 */
	public Room room;
	
	/**
	 * rooms owned by the connecter
	 */
	public ArrayList<Room> ownedRooms;
	
	/**
	 * the thread assigned to this connecter
	 */
	public SessionThread sessionThread;
	
	/**
	 * constructor
	 * @return [description]
	 */
	public Connecter(){
		this.ownedRooms = new ArrayList<Room>();
	}
	
	/**
	 * set session thread
	 * @param sessionThread [description]
	 */
	public void setSessionThread(SessionThread sessionThread){
		this.sessionThread = sessionThread;
	}
	
	/**
	 * set current room
	 * @param room [description]
	 */
	public void setRoom(Room room){
		this.room = room;
	}
	
	/**
	 * clear current room
	 */
	public void clearRoom(){
		this.room = null;
	}
	
	/**
	 * send a message
	 * @param msg json object
	 */
	public void sendMsg(JSONObject msg){
		this.sessionThread.socket.sendMsg(msg);
	}
	
	/**
	 * receive messages
	 * @return received message
	 * @throws IOException   
	 * @throws JSONException 
	 */
	public JSONObject recvMsg() throws IOException, JSONException{
		return this.sessionThread.socket.recvMsg();
	}
	
	
	/**
	 * is the user which want to join this room in the black list
	 * @param  the room which the connecter wants to join
	 * @return               true if in the black list, not if not in the black list
	 */
	public boolean isInBlackList(Room room){
		for( int i = 0 ; i <= room.blackList.size() - 1 ; i++){
			Black black = room.blackList.get(i);
			if(black.connecter != this){
				continue;
			}
			else if(black.connecter == this && black.isAllowJoin()){
				room.blackList.remove(i);
			} else {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * create room
	 * @param  roomId room id
	 * @return        new room
	 */
	public Room createRoom(String roomId){
		Room room = new Room(roomId, this);
		this.ownedRooms.add(room);
		return room;
	}
	
	/**
	 * change current room
	 * @param room the room which the connecter wants to change
	 */
	public void changeRoom(Room room){
		this.room.removeConnecter(this);
		this.setRoom(room);
		this.room.addConnecter(this);
	}
	
	/**
	 * delete room
	 * @param  room          the room to be deleted
	 * @throws JSONException
	 */
	public abstract void deleteRoom(Room room) throws JSONException;
	
	/**
	 * kick connecter
	 * @param room      which room
	 * @param connecter which connecter
	 * @param time      for how long
	 */
	public abstract void kickUser(Room room, Connecter connecter, int time);
	
	/**
	 * quit the chat system
	 * @throws JSONException 
	 * @throws IOException   
	 */
	public abstract void quit() throws JSONException, IOException;
	
	/**
	 * login as an authenticated user
	 * @param user the authenticated user
	 */
	public abstract void login(User user);
	
	/**
	 * first response when a conncter connects
	 * @throws JSONException
	 */
	public abstract void firstResponse() throws JSONException;
	
	
}
