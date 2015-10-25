import java.io.IOException;
import java.net.Socket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Client {
	/**
	 * args
	 */
	@Option(name="-p", usage="port")
	public int port = 4444;
	
	@Argument(required=true)
	public String hostname;
	
	@Option(name = "-password", usage="password")
	public String loginPassword;
	
	@Option(name = "-id", usage="id")
	public String loginId;
	
	/**
	 * the room's id where the client stay in
	 */
	public String roomId;
	
	/**
	 * client's id
	 */
	public String id;
	
	/**
	 * whether the client get the first response
	 */
	public int isGetFirstResponse;
	
	/**
	 * thread responsible for user input 
	 */
	public UserInputThread userInputThread;
	
	/**
	 * thread responsible for receiving messages from server
	 */
	public ClientRecvThread clientRecvThread;
	
	/**
	 * whether the client is interrupted
	 */
	public boolean interrupt;
	
	/**
	 * socket
	 */
	public MySocket socket;
	
	private Log log;
	
	/**
	 * constructor
	 * @param  args          
	 * @return               
	 * @throws IOException   
	 * @throws JSONException
	 */
	public Client(String[] args) throws IOException, JSONException{
		
		this.log = new Log();
		
		CmdLineParser parser = new CmdLineParser(this);
		
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.out.println("please input hostname!");
			System.exit(0);
			e.printStackTrace();
		}
		
		this.roomId = null;
		this.id = null;
		this.isGetFirstResponse = 0;
		this.interrupt = false;
		
		System.setProperty("javax.net.ssl.trustStore", "../mochaServerKeyStore");
		System.setProperty("javax.net.ssl.trustStorePassword","123456");
		
		SocketFactory factory= SSLSocketFactory.getDefault();
		
		this.socket = new MySocket(factory.createSocket(this.hostname, this.port));
		this.login(this.loginId, this.loginPassword);
		this.userInputThread = new UserInputThread(this);
		this.clientRecvThread = new ClientRecvThread(this);
	}
	
	public void login(String loginId, String password) throws JSONException, IOException{
		if(loginId == null){
			loginId = "";
		}
		if(password == null){
			password = "";
		}
		
		this.socket.sendMsg(Protocol.login(loginId, password));
		if( !loginId.equals("") && !password.equals("")){
			while(true){
				JSONObject recv = null;
				recv = this.socket.recvMsg();
				if(recv.getString("type").equals("authenticated")){
					String id = recv.getString("id");
					String roomId = recv.getString("roomId");
					if(id.equals("") && roomId.equals("")){
						this.log.write("User is currently logined.");
						this.interrupt = true;
						System.exit(0);
					} else if (id.equals("none") && roomId.equals("")){
						this.log.write("User not existing or Wrong password");
						this.interrupt = true;
						System.exit(0);
					} else {
						this.id = id;
						this.roomId = roomId;
						this.isGetFirstResponse += 1;
					}
					return;
				}
			}
		}
		
	}
	
	public static void main(String[] args) throws IOException, JSONException{
		Client clientMain = new Client(args);
	}
}
