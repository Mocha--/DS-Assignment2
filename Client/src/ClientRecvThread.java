import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * thread responsible for receiving messages from the server
 */
public class ClientRecvThread extends Thread{
	
	/**
	 * client instance
	 */
	public Client client;
	
	/**
	 * log instance
	 */
	private Log log;
	
	/**
	 * constructor
	 * @param  client 
	 * @return
	 */
	public ClientRecvThread(Client client){
		this.client = client;
		this.log = new Log();
		start();
	}
	
	public void run(){
		try {
			while(true){
				JSONObject recv = null;
				recv = this.client.socket.recvMsg();
				if(recv == null){
					break;
				}
				this.handler(recv);
				if(this.client.interrupt){
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			try {
				this.client.socket.end();
				this.interrupt();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * handle received messages
	 * @param  recv          received message object
	 * @throws JSONException
	 * @throws IOException
	 */
	private void handler(JSONObject recv) throws JSONException, IOException{
		System.out.println(recv);
		
		this.handleNewIdentity(recv);
		this.handlerRoomList(recv);
		this.handleRoomChange(recv);
		this.handleMessage(recv);
		this.handleRoomContents(recv);	
		this.handleAuthenticated(recv);
		this.handleNewUser(recv);
		
		if(this.client.isGetFirstResponse < 4){
			this.client.isGetFirstResponse += 1;
		}
	}
	
	/**
	 * when the received message tye is newidentity, handle this message.
	 * @param  recv          received message object
	 * @throws JSONException 
	 */
	private void handleNewIdentity(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("newidentity")){
			if(recv.getString("former").equals("")){
				this.client.id = recv.getString("identity");
				this.log.write("connected to " + this.client.hostname  + " as " + recv.getString("identity"));
			}
			else if(recv.getString("former").equals(recv.getString("identity"))){
				this.log.write("Requested identity invalid or in use");
			}else{
				this.log.write(recv.getString("former") + " is now " + recv.getString("identity"));
				if(recv.getString("former").equals(this.client.id)){
					this.client.id = recv.getString("identity");
				}
			}
		}
	}
	
	/**
	 * when the received message type is roomlist, handle this message.
	 * @param  recv          received message object
	 * @throws JSONException 
	 */
	private void handlerRoomList(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("roomlist")){
			JSONArray rooms = recv.getJSONArray("rooms");
			for( int i = 0 ; i <= rooms.length() - 1 ; i++){
				String roomId = rooms.getJSONObject(i).getString("roomid");
				int count = rooms.getJSONObject(i).getInt("count");
				this.log.write(roomId + ": " + count + " guests");
			}
		}
	}
	
	/**
	 * when the received message type is roomchange, handle this message.
	 * @param  recv          received message object
	 * @throws JSONException 
	 * @throws IOException  
	 */
	private void handleRoomChange(JSONObject recv) throws JSONException, IOException{
		if(recv.getString("type").equals("roomchange")){
			String identity = recv.getString("identity");
			String former = recv.getString("former");
			String roomId= recv.getString("roomid");
			if(roomId.equals("")){
				this.log.write(identity + " leaves " + former);
				if(identity.equals(this.client.id)){
					this.log.write("Disconnected from " + this.client.hostname);
					this.client.interrupt = true;
				}
			} else if(former.equals("") && roomId.equals("MainHall")){
				this.log.write(identity + " moves to MainHall");
				if(identity.equals(this.client.id)){
					this.client.roomId = roomId;
				}
			}
			else if(former.equals(roomId)){
				this.log.write("The requested room is invalid or non existent.");
			} else {
				this.log.write(identity + " moved from " + former + " to " + roomId);
				if(identity.equals(this.client.id)){
					this.client.roomId = roomId;
				}
			}
		}
	}
	
	/**
	 * when the received message type is roomcontents, handle this message.
	 * @param  recv          received message object
	 * @throws JSONException
	 */
	private void handleRoomContents(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("roomcontents")){
			String roomId = recv.getString("roomid");
			String output = roomId + " contains";
			String owner = recv.getString("owner");
			
			JSONArray identities = recv.getJSONArray("identities");
			for( int i = 0 ; i <= identities.length() - 1; i++){
				String identity = identities.getString(i);
				if(identity.equals(owner)){
					identity += "*";
				}
				output += " " + identity;
			}
			this.log.write(output);
		}
	}
	
	/**
	 * when the received message type is message, handle this message.
	 * @param  recv          received message object
	 * @throws JSONException
	 */
	private void handleMessage(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("message")){
			System.out.println(recv);
			this.log.msg(recv.getString("identity"), recv.getString("content"));
		}
	}
	
	private void handleAuthenticated(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("authenticated")){
			String id = recv.getString("id");
			String roomId = recv.getString("roomId");
			if(id.equals("") && roomId.equals("")){
				this.log.write("User is currently logined.");
			} else if (id.equals("none") && roomId.equals("")){
				this.log.write("User not existing or Wrong password");
			} else {
				this.client.id = id;
				this.client.roomId = roomId;
			}
		}
	}
	
	private void handleNewUser(JSONObject recv) throws JSONException{
		if(recv.getString("type").equals("newUser")){
			String id = recv.getString("id");
			if(id.equals("")){
				this.log.write("The username has already been used.");
			} else {
				this.log.write("User " + id + " is registered successfully.");
			}
		}
	}
	
}
