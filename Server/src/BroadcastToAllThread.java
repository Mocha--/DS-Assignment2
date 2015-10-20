import org.json.JSONObject;

/**
 * a thread which can broadcast messages accross the whole server
 * messages are broadcast to every user connecting to the server
 * this thread belongs to only one server
 * a server can have at most one broadcast thread
 */
public class BroadcastToAllThread extends Thread {
	
	/**
	 * constructor
	 * @return [constructor]
	 */
	public BroadcastToAllThread(){
		this.start();
	}
	
	/**
	 * this thread is kind of like listening the message queue
	 * if in the queue there are messages, this thread will broadcast
	 * it accross the whole server. After that delete the messge which is broadcast.
	 */
	public void run(){
		while(true){
			
			if(Server.messages.size() > 0){
				JSONObject message = Server.messages.getFirst();
				
				for (SessionThread thread: SessionThread.sessionThreads) {
					thread.socket.sendMsg(message);
				}
				
				Server.messages.removeFirst();
			}
			
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
