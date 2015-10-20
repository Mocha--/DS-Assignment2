import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class MySocket {
	/*
	 * socket
	 */
	public Socket socket;
	
	/**
	 * print write which can write messages into output stream to the server
	 */
	private PrintWriter out;
	
	/**
	 * buffer reader which can read messages from input stream from the server
	 */
	private BufferedReader in;
	
	public MySocket(Socket socket) throws IOException{
		this.socket = socket;
		this.out = new PrintWriter(this.socket.getOutputStream(), true);
		this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
	}
	
	public void end() throws IOException {
		//this.in.close();
		//this.out.close();
		this.socket.close();
		//this.socket.shutdownOutput();
	}
	
	public void sendMsg(JSONObject msg){
		
		this.out.println(msg.toString());
		this.out.flush();
	}
	
	public JSONObject recvMsg() throws IOException{
		String s = this.in.readLine();
		try {
			return new JSONObject(s);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

}
