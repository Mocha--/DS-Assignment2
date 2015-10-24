import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Calendar;
import org.json.JSONException;

import com.auth0.jwt.JWTVerifyException;

public class User extends Connecter {

	public static User authenticate(String id, String password) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, SignatureException, IOException, JWTVerifyException{
		return UserTable.find(id, password);
	}

	public static User register(String id, String password){
		if(UserTable.findById(id) != null){
			return null;
		}else{
			User user = UserTable.create(id, password);
			return user;
		}
	}

	public String password;
	
	public User(String id, String password){
		super();
		this.id = id;
		this.password = password;
		this.room = Room.findById("MainHall");
	}
	
	public void deleteRoom(Room room) throws JSONException{
		Room mainHall = Room.findById("MainHall");
		for(int i = 0 ; i <= room.connecters.size() - 1 ; i++){
			Connecter connecter = room.connecters.get(i);
			connecter.sendMsg(Protocol.roomChange(connecter.id, connecter.room.id, "MainHall"));
			connecter.changeRoom(mainHall);
		}
		Room.rooms.remove(room);
	}
	
	public void kickUser(Room room, Connecter connecterToBeKicked, int time){
		Room mainHall = Room.findById("MainHall");
		connecterToBeKicked.changeRoom(mainHall);
		
		try {
			mainHall.pushMsg(Protocol.roomChange(connecterToBeKicked.id, room.id, "MainHall"));
			room.pushMsg(Protocol.roomChange(connecterToBeKicked.id, room.id, "MainHall"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.SECOND, time);
		room.blackList.add(new Black(connecterToBeKicked, c.getTime()));
	}
	
	public void quit() throws JSONException, IOException{
		for(Connecter connecter: this.room.connecters){
			connecter.sendMsg(Protocol.roomChange(this.id, this.room.id, ""));
		}
		Room previousRoom = this.room;
		Connecter.connecters.remove(this);
		this.room.connecters.remove(this);
		
		if(previousRoom.id != "MainHall" && previousRoom.isToBeDeleted()){
			Room.rooms.remove(previousRoom);
		}
		
	}
	
	public void login(User user){
		this.room.connecters.remove(this);
		Connecter.connecters.remove(this);
		this.sessionThread.connecter = user;
		user.setSessionThread(this.sessionThread);
		user.room.addConnecter(user);
		Connecter.connecters.add(user);
	}

}
