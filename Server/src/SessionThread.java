import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * thread which is created when a new client connected
 */
public class SessionThread extends Thread {
	
	/**
	 * all session threads. static variable.
	 */
	public static ArrayList<SessionThread> sessionThreads = new ArrayList<SessionThread>();
	
	/**
	 * assign a new id when a client connected
	 * @return new id
	 */
	public static String assginId(){
		for( int i = 1 ; ; i++){
			boolean flag = false;
			for(SessionThread sessionThread: SessionThread.sessionThreads){
				String id = sessionThread.id;
				if(id.substring(0, 5).equals("guest")){
					if(id.substring(5, id.length()).equals("" + i)){
						flag = true;
						break;
					}
				}
			}
			
			if(flag == false){
				return "guest" + i;
			}
		}
	}
	
	/**
	 * find a session thread by id
	 * @param  id session thread id
	 * @return
	 */
	public static SessionThread findById(String id){
		for(SessionThread sessionThread: SessionThread.sessionThreads){
			if(sessionThread.id.equals(id)){
				return sessionThread;
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
		if(SessionThread.findById(id) != null){
			return false;
		}
		
		if(id.length() < 3 || id.length() > 16 || !id.matches("[A-Za-z0-9]+") || id.substring(0,1).matches("[0-9]+")){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * customized socket instance
	 */
	public MySocket socket;
	
	/**
	 * the room this user is in
	 */
	public Room room;
	
	/**
	 * session thread id
	 */
	public String id;
	
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
	public SessionThread(MySocket socket, Room room) throws JSONException{
		this.socket = socket;
		this.room = room;
		this.id = SessionThread.assginId();
		this.log = new Log();
		//this.log.write("A new user connected");
		this.interrupt = false;
		
		SessionThread.sessionThreads.add(this);
		this.room.addThread(this);
		
		this.firstResponse();
		
		this.start();
	}
	
	public void run(){
		try{
			while(true){
				JSONObject recv = null;
				recv = this.socket.recvMsg();
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
				for(Room room: Room.rooms){
					if(room.owner == this){
						room.owner = null;
					}
				}
				
				for(SessionThread sessionThread: this.room.sessionThreads){
					sessionThread.socket.sendMsg(Protocol.roomChange(this.id, this.room.id, ""));
				}
				SessionThread.sessionThreads.remove(this);
				this.room.sessionThreads.remove(this);
				
				for( int i = 0 ; i <= Room.rooms.size() - 1 ; i ++){
					Room room = Room.rooms.get(i);
					if(room.id != "MainHall" && room.owner == null && room.sessionThreads.size() == 0){
						Room.rooms.remove(i);
					}
				}
				
				this.socket.end();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * when a new client connected, server's first response
	 * @throws JSONException
	 */
	private void firstResponse() throws JSONException{
		//System.out.println(Protocol.roomList());
		this.socket.sendMsg(Protocol.newIdentity("", this.id));
		this.socket.sendMsg(Protocol.roomChange(this.id, "", this.room.id));
		this.socket.sendMsg(Protocol.roomContents(this.room));
		this.socket.sendMsg(Protocol.roomList());
	}
	
	/**
	 * a handle to handle different kinds of messages
	 * @param  recv          message object received
	 * @throws JSONException 
	 * @throws IOException
	 */
	private void handler(JSONObject recv) throws JSONException, IOException{
		//System.out.println(recv);
		this.handleIdentityChange(recv);
		this.handleCreateRoom(recv);
		this.handleList(recv);
		this.handleWho(recv);
		this.handleJoin(recv);
		this.handleMessage(recv);
		this.handleDelete(recv);
		this.handleQuit(recv);
		this.handleKick(recv);
	}
	
	/**
	 * when the received message type is identitychange, handle this message
	 * @param  recv          received message object
	 * @throws JSONException
	 */
	private void handleIdentityChange(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("identitychange")){
			String identity = recv.getString("identity");
			if(!SessionThread.isIdValid(identity)){
				this.log.write("Invalid identity");
				this.socket.sendMsg(Protocol.newIdentity(this.id, this.id));
			}
			else{
				Server.messages.addLast(Protocol.newIdentity(this.id, identity));	
				this.id = identity;
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
				Room room = new Room(roomId, this);
			}
			this.socket.sendMsg(Protocol.roomList());
		}
	}
	
	/**
	 * when the received message type is list, handle this message
	 * @param  recv          received message object
	 * @throws JSONException
	 */
	private void handleList(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("list")){
			this.socket.sendMsg(Protocol.roomList());
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
				this.socket.sendMsg(Protocol.roomContents(room));	
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
				if(roomId.equals(this.room.id)){
					this.socket.sendMsg(Protocol.roomChange(this.id, this.room.id, this.room.id));
					return;
				}
				Room room = Room.findById(roomId);
				if(room.isInBlackList(this)){
					this.socket.sendMsg(Protocol.roomChange(this.id, this.room.id, this.room.id));
					return;
				}
				JSONObject roomChange = Protocol.roomChange(this.id, this.room.id, roomId);
				this.room.pushMsg(roomChange);
				this.room.removeThread(this);
				
				/* clear empty room without owner */
				for( int i = 0 ; i <= Room.rooms.size() - 1 ; i ++){
					Room toDelete = Room.rooms.get(i);
					if(toDelete.id != "MainHall" && toDelete.owner == null && toDelete.sessionThreads.size() == 0){
						Room.rooms.remove(i);
					}
				}
				
				this.room = room;
				this.room.addThread(this);
				this.room.pushMsg(roomChange);
				if(this.room.id.equals("MainHall")){
					this.socket.sendMsg(Protocol.roomContents(this.room));
					this.socket.sendMsg(Protocol.roomList());
				}
				
			} else {
				this.socket.sendMsg(Protocol.roomChange(this.id, this.room.id, this.room.id));
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
				if(room.owner.id.equals(this.id)){
					Room mainHall = Room.findById("MainHall");
					for(SessionThread sessionThread: room.sessionThreads){
						sessionThread.socket.sendMsg(Protocol.roomChange(sessionThread.id, sessionThread.room.id, "MainHall"));
						sessionThread.room = mainHall;
						mainHall.addThread(sessionThread);
					}
					Room.rooms.remove(room);
					this.socket.sendMsg(Protocol.roomList());
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
			if(room == null || room.owner != this){
				return;
			}
			SessionThread sessionThread = SessionThread.findById(recv.getString("identity"));
			if(sessionThread == null || sessionThread.room.id != room.id){
				return;
			}
			
			room.sessionThreads.remove(sessionThread);
			Room mainHall = Room.findById("MainHall");
			mainHall.addThread(sessionThread);
			sessionThread.room = mainHall;
			
			room.pushMsg(Protocol.roomChange(sessionThread.id, room.id, "MainHall"));
			mainHall.pushMsg(Protocol.roomChange(sessionThread.id, room.id, "MainHall"));
			
			Calendar c = Calendar.getInstance();
			c.add(Calendar.SECOND, recv.getInt("time"));
			room.blackList.add(new Black(sessionThread, c.getTime()));
		}
	}
	
	/**
	 * when the received message type is message, handle this message.
	 * @param  recv          received message object
	 * @throws JSONException
	 */
	private void handleMessage(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("message")){
			this.room.pushMsg(Protocol.message(this.id, recv.getString("content")));
		}
	}
	
	
}
