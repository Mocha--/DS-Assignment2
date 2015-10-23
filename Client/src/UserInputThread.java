import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * thread responsible for user's input
 */
public class UserInputThread extends Thread {
	
	/**
	 * client
	 */
	public Client client;
	
	/**
	 * user's input
	 */
	public BufferedReader userIn;
	
	/**
	 * log instance
	 */
	private Log log;
	
	/**
	 * constructor
	 * @param  client
	 * @return
	 */
	public UserInputThread(Client client){
		this.client = client;
		this.log = new Log();
		this.start();
	}
	
	public void run(){
		while(true){
			if(this.client.id != null && this.client.roomId != null && this.client.isGetFirstResponse == 4){
				if(this.client.interrupt){
					break;
				}
				System.out.print("[" + this.client.roomId + "] " + this.client.id + "> ");
				this.userIn = new BufferedReader(new InputStreamReader(System.in));
				try {
					String input = userIn.readLine();
					
					JSONObject jsonObject = this.inputHandler(input);
					if(jsonObject == null){
						this.log.write("invalid input");
						continue;
					}
					
					this.client.socket.sendMsg(jsonObject);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			try {
				sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		this.interrupt();
	}
	
	/**
	 * handle user's input
	 * @param  input user's input
	 * @return               
	 * @throws JSONException
	 */
	private JSONObject inputHandler(String input) throws JSONException{
		if(input.charAt(0) == '#'){
			input = input.substring(1, input.length());
			String[] parts = input.split(" ");
			
			if(parts[0].equals("identitychange") ){
				if(parts.length != 2){
					return null;
				}
				return Protocol.identityChange(parts[1]); 
			}else if(parts[0].equals("createroom")){
				if(parts.length != 2){
					return null;
				}
				return Protocol.createRoom(parts[1]);
			} else if(parts[0].equals("list")){
				return Protocol.list();
			} else if(parts[0].equals("who")){
				if(parts.length != 2){
					return null;
				}
				return Protocol.who(parts[1]);
			} else if(parts[0].equals("join")){
				if(parts.length != 2){
					return null;
				}
				return Protocol.join(parts[1]);
			} else if (parts[0].equals("quit")){
				return Protocol.quit();
			} else if (parts[0].equals("delete")) {
				if(parts.length != 2){
					return null;
				}
				return Protocol.delete(parts[1]);
			} else if(parts[0].equals("kick")){
				if(parts.length != 4){
					return null;
				}
				return Protocol.kick(parts[1], Integer.parseInt(parts[2]), parts[3]);
			} else if(parts[0].equals("register")){
				if(parts.length != 3){
					return null;
				}
				return Protocol.register(parts[1], parts[2]);
			} else if(parts[0].equals("login")){
				if(parts.length != 3){
					return null;
				}
				return Protocol.login(parts[1], parts[2]);
			}
			else{
				return null;
			}
		}else{
			return Protocol.message(input);
		}
	}
	
}