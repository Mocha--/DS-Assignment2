import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class Connecter {
	
	public static ArrayList<Connecter> connecters = new ArrayList<Connecter>();
	
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
	
	
	public String id;
	
	public Room room;
	
	public ArrayList<Room> ownedRooms;
	
	public SessionThread sessionThread;
	
	public Connecter(){
		this.ownedRooms = new ArrayList<Room>();
	}
	
	public void setSessionThread(SessionThread sessionThread){
		this.sessionThread = sessionThread;
	}
	
	public void setRoom(Room room){
		this.room = room;
	}
	
	public void clearRoom(){
		this.room = null;
	}
	
	public void sendMsg(JSONObject msg){
		this.sessionThread.socket.sendMsg(msg);
	}
	
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
	
	public Room createRoom(String roomId){
		Room room = new Room(roomId, this);
		this.ownedRooms.add(room);
		return room;
	}
	
	public void changeRoom(Room room){
		this.room.removeConnecter(this);
		this.setRoom(room);
		this.room.addConnecter(this);
	}
	
	public abstract void deleteRoom(Room room) throws JSONException;
	
	public abstract void kickUser(Room room, Connecter connecter, int time);
	
	public abstract void quit() throws JSONException, IOException;
	
	public abstract void login(User user);
	
	
}
