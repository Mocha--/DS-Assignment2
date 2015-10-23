import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.sound.sampled.Line;

import org.json.JSONArray;
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
		
		System.setProperty("javax.net.ssl.keyStore", "../mochaServerKeyStore");
		System.setProperty("javax.net.ssl.keyStorePassword", "123456");
		
		ServerSocketFactory factory = SSLServerSocketFactory.getDefault();
		this.serverSocket = factory.createServerSocket(this.port);
		this.log = new Log();
		Room mainHall = new Room("MainHall", null);
		
		loadUsersFromFile("../users.json");
		
		while(true){
			this.log.write("waiting for connections");
			Socket socket = this.serverSocket.accept();
			Connecter connecter = new Guest();
			SessionThread sessionThread = new SessionThread(connecter, new MySocket(socket));
		}
	}
	
	public void loadUsersFromFile(String path) throws IOException, JSONException{
		File file = new File(path);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String content = "";
		String line = br.readLine();
		while(line != null){
			content += line;
			line = br.readLine();
		}
		JSONArray users = new JSONArray(content);
		for( int i = 0 ; i <= users.length() - 1 ; i++){
			JSONObject user = new JSONObject(users.get(i).toString());
			String id = user.getString("id");
			String password = user.getString("password");
			User.register(id, password);
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