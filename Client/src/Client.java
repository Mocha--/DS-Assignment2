import java.io.IOException;
import java.net.Socket;
import org.json.JSONException;
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
	
	/**
	 * constructor
	 * @param  args          
	 * @return               
	 * @throws IOException   
	 * @throws JSONException
	 */
	public Client(String[] args) throws IOException, JSONException{
		
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
		
		this.socket = new MySocket(new Socket(this.hostname, this.port));
		this.userInputThread = new UserInputThread(this);
		this.clientRecvThread = new ClientRecvThread(this);
		this.interrupt = false;
	}
	
	public static void main(String[] args) throws IOException, JSONException{
		Client clientMain = new Client(args);
	}
}
