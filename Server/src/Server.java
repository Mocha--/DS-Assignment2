import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Server {
	
	/**
	 * args from console
	 */
	@Option(name="-p", usage="port")
	public int port = 4444;
	
	/**
	 * server socket
	 */
	public ServerSocket serverSocket;
	
	/**
	 * a thread which can broadcast messages to every client
	 * @return
	 */
	public static BroadcastToAllThread broadcastToAllThread = new BroadcastToAllThread();
	
	/**
	 * a message queue
	 */
	public static LinkedList<JSONObject> messages = new LinkedList<JSONObject>();
	
	/**
	 * log instance
	 */
	private Log log;
	
	/**
	 * constructor
	 * @param  args          arguments from console
	 * @return               
	 * @throws IOException   
	 * @throws JSONException
	 */
	public Server(String[] args) throws IOException, JSONException{
		
		CmdLineParser parser = new CmdLineParser(this);
		
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			e.printStackTrace();
		}
		
		this.serverSocket = new ServerSocket(this.port);
		this.log = new Log();
		Room mainHall = new Room("MainHall", null);
		
		while(true){
			//this.log.write("waiting for connections");
			Socket socket = this.serverSocket.accept();
			SessionThread sessionThread = new SessionThread(new MySocket(socket), mainHall);
		}
	}
	
	/**
	 * main function
	 * @param  args          
	 * @throws IOException  
	 * @throws JSONException
	 */
	public static void main(String[] args) throws IOException, JSONException {
		Server server = new Server(args);
	}
}