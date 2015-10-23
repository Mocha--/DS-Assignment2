import java.io.IOException;
import org.json.JSONException;

public class Guest extends Connecter {
	
	public Guest(){
		super();
		this.room = Room.findById("MainHall");
		this.room.addConnecter(this);
		for( int i = 1 ; ; i++){
			boolean flag = false;
			for(Connecter connecter: Connecter.connecters){
				String id = connecter.id;
				if(id.substring(0,5).equals("guest") && id.substring(5, id.length()).equals("" + i)){
					flag = true;
					break;
				}
			}

			if(flag == false){
				this.id = "guest" + i;
				break;
			}
		}
		Connecter.connecters.add(this);
	}
	
	public void deleteRoom(Room room){
		System.out.println(this.id + " is a guset, who can not delete rooms");
	}
	
	public void kickUser(Room room, Connecter connecter, int time){
		System.out.println(this.id + " is a guest, who can not kick users");
	}
	
	public void quit() throws JSONException, IOException{
		for(Room room: this.ownedRooms){
			room.owner = null;
			if(room.isToBeDeleted()){
				Room.rooms.remove(room);
			}
		}
		this.ownedRooms = null;
		
		/** room pushMsg cant work because the broadcast thread sleeps for a while
		 *  however the socket will close before data all sent
		 *  **/
		for(Connecter connecter: this.room.connecters){
			connecter.sendMsg(Protocol.roomChange(this.id, this.room.id, ""));
		}
		
		Room previousRoom = this.room;
		Connecter.connecters.remove(this);
		this.room.removeConnecter(this);
		
		if(previousRoom.id != "MainHall" && previousRoom.isToBeDeleted()){
			Room.rooms.remove(previousRoom);
		}
	}
	
	public void login(User user){
		this.room.removeConnecter(this);
		Connecter.connecters.remove(this);
		this.sessionThread.connecter = user;
		user.setSessionThread(this.sessionThread);
		user.room.addConnecter(user);
		Connecter.connecters.add(user);
	}
	
	
}
