import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * thread which is created when a new client connected
 */
public class SessionThread extends Thread {
	/**
	 * connecter
	 */
	public Connecter connecter;
	
	
	public MySocket socket;
	
	/**
	 * log instance
	 */
	private Log log;
	
	/**
	 * is this thread interrupted
	 */
	private boolean interrupt;
	
	/**
	 * constructor
	 * @param  socket        
	 * @param  room          
	 * @return
	 * @throws JSONException 
	 */
	public SessionThread(Connecter connecter, MySocket socket) throws JSONException{
		this.connecter = connecter;
		this.connecter.setSessionThread(this);
		this.socket = socket;
		this.log = new Log();
		this.interrupt = false;
		
		this.firstResponse();
		
		this.start();
	}
	
	public void run(){
		try{
			while(true){
				JSONObject recv = null;
				recv = this.connecter.recvMsg();
				if(recv == null){
					break;
				}
				this.handler(recv);
				if(this.interrupt){
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			try {
				this.connecter.quit();
				this.socket.end();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * when a new client connected, server's first response
	 * @throws JSONException
	 */
	public void firstResponse() throws JSONException{
		//System.out.println(Protocol.roomList());
		this.connecter.sendMsg(Protocol.newIdentity("", this.connecter.id));
		this.connecter.sendMsg(Protocol.roomChange(this.connecter.id, "", this.connecter.room.id));
		this.connecter.sendMsg(Protocol.roomContents(this.connecter.room));
		this.connecter.sendMsg(Protocol.roomList());
	}
	
	/**
	 * a handle to handle different kinds of messages
	 * @param  recv          message object received
	 * @throws JSONException 
	 * @throws IOException
	 */
	private void handler(JSONObject recv) throws JSONException, IOException{
		System.out.println(recv);
		this.handleIdentityChange(recv);
		this.handleCreateRoom(recv);
		this.handleList(recv);
		this.handleWho(recv);
		this.handleJoin(recv);
		this.handleMessage(recv);
		this.handleDelete(recv);
		this.handleQuit(recv);
		this.handleKick(recv);
		this.handleRegiter(recv);
		this.handleLogin(recv);
	}
	
	/**
	 * when the received message type is identitychange, handle this message
	 * @param  recv          received message object
	 * @throws JSONException
	 */
	private void handleIdentityChange(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("identitychange")){
			String identity = recv.getString("identity");
			if(!Connecter.isIdValid(identity)){
				this.log.write("Invalid identity");
				this.connecter.sendMsg(Protocol.newIdentity(this.connecter.id, this.connecter.id));
			}
			else{
				Server.messages.addLast(Protocol.newIdentity(this.connecter.id, identity));	
				this.connecter.id = identity;
			}
		}
	}
	
	/**
	 * when the received message type is createroom, handle this message
	 * @param  recv          received message object
	 * @throws JSONException
	 */
	private void handleCreateRoom(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("createroom")){
			String roomId = recv.getString("roomid");
			if(!Room.isIdValid(roomId)){
				this.log.write("invalid identity");
			}else{
				Room room = this.connecter.createRoom(roomId);
			}
			this.connecter.sendMsg(Protocol.roomList());
		}
	}
	
	/**
	 * when the received message type is list, handle this message
	 * @param  recv          received message object
	 * @throws JSONException
	 */
	private void handleList(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("list")){
			this.connecter.sendMsg(Protocol.roomList());
		}
	}
	
	/**
	 * when the received message type is who, handle this message
	 * @param  recv          received message object
	 * @throws JSONException 
	 */
	private void handleWho(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("who")){
			Room room = Room.findById(recv.getString("roomid"));
			if(room != null){
				this.connecter.sendMsg(Protocol.roomContents(room));	
			}
		}
	}
	
	/**
	 * when the received message type is join, handle this message
	 * @param  recv          received message object
	 * @throws JSONException 
	 */
	private void handleJoin(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("join")){
			String roomId = recv.getString("roomid");
			if(Room.findById(roomId) != null){
				if(roomId.equals(this.connecter.room.id)){
					this.connecter.sendMsg(Protocol.roomChange(this.connecter.id, this.connecter.room.id, this.connecter.room.id));
					return;
				}
				Room newRoom = Room.findById(roomId);
				if(this.connecter.isInBlackList(newRoom)){
					this.connecter.sendMsg(Protocol.roomChange(this.connecter.id, this.connecter.room.id, this.connecter.room.id));
					return;
				}
				JSONObject roomChange = Protocol.roomChange(this.connecter.id, this.connecter.room.id, roomId);
				Room previousRoom = this.connecter.room;
				previousRoom.pushMsg(roomChange);
				this.connecter.changeRoom(newRoom);
				
				if(previousRoom.id != "MainHall" && previousRoom.isToBeDeleted()){
					Room.rooms.remove(previousRoom);
				}
				
				newRoom.pushMsg(roomChange);
				if(newRoom.id.equals("MainHall")){
					this.connecter.sendMsg(Protocol.roomContents(newRoom));
					this.connecter.sendMsg(Protocol.roomList());
				}
				
			} else {
				this.connecter.sendMsg(Protocol.roomChange(this.connecter.id, this.connecter.room.id, this.connecter.room.id));
			}
		}
	}
	
	/**
	 * when the received message type is delete, handle this message.
	 * @param  recv          received message object
	 * @throws JSONException 
	 */
	private void handleDelete(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("delete")){
			String roomId = recv.getString("roomid");
			if(Room.findById(roomId) != null ){
				Room room = Room.findById(roomId);
				if(room.owner == this.connecter){
					this.connecter.deleteRoom(room);
					this.connecter.sendMsg(Protocol.roomList());
				}
			}
		}
	}
	
	/**
	 * when the received message type is quit, handle this message.
	 * @param  recv          received message object
	 * @throws JSONException 
	 * @throws IOException  
	 */
	private void handleQuit(JSONObject recv) throws JSONException, IOException{
		if(recv.getString("type").equals("quit")){
			this.interrupt = true;
		}
	}
	
	/**
	 * when the received message type is kick, handle this message.
	 * @param  recv          received message object
	 * @throws JSONException
	 * @throws IOException
	 */
	private void handleKick(JSONObject recv) throws JSONException, IOException{
		if(recv.getString("type").equals("kick")){
			Room room = Room.findById(recv.getString("roomid"));
			if(room == null || room.owner != this.connecter){
				return;
			}
			
			Connecter connecterToBeKicked = Connecter.findById(recv.getString("identity"));
			if(connecterToBeKicked == null || connecterToBeKicked.room!= room){
				return;
			}
			
			this.connecter.kickUser(room, connecterToBeKicked, recv.getInt("time"));
		}
	}
	
	/**
	 * when the received message type is message, handle this message.
	 * @param  recv          received message object
	 * @throws JSONException
	 */
	private void handleMessage(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("message")){
			this.connecter.room.pushMsg(Protocol.message(this.connecter.id, recv.getString("content")));
		}
	}
	
	private void handleLogin(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("login")){
			String id = recv.getString("id");
			String password = recv.getString("password");
			User user = User.authenticate(id, password);
			if(user == null){
				this.connecter.sendMsg(Protocol.newIdentity(this.connecter.id, this.connecter.id));
			} else{
				this.connecter.login(user);
				this.connecter.sendMsg(Protocol.authenticated(this.connecter.id, this.connecter.room.id));
			}
		}
	}
	
	private void handleRegiter(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("register")){
			String id = recv.getString("id");
			String password = recv.getString("password");
			User user = User.register(id, password);
			for(User u: UserTable.users){
				System.out.println(u.id);
			}
			if(user != null){
				this.connecter.sendMsg(Protocol.newUser(user.id));
			} else {
				this.connecter.sendMsg(Protocol.newUser(""));
			}
		}
	}
	
}
