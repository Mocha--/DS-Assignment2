import org.json.JSONObject;

/**
 * a thread which can broadcast messages in a room
 * this thread belongs to only one room
 * a room can have at most one broadcast thread
 */
public class BroadcastThread extends Thread {
	
	/**
	 * the room where messages are broadcast
	 */
	public Room room;
	
	/**
	 * constructor
	 * @param  room [the room where messages are broadcast]
	 * @return      [constructor]
	 */
	public BroadcastThread(Room room){
		this.room = room;
		
		this.start();
	}
	
	/**
	 * this thread is kind of like listening the message queue
	 * if in the queue there are messages, this thread will broadcast
	 * it in its room. After that delete the messge which is broadcast.
	 */
	public void run(){
		while(true){
			if(this.room.messages.size() > 0){
				JSONObject message = this.room.messages.getFirst();
				
				for (SessionThread thread: this.room.sessionThreads) {
					thread.socket.sendMsg(message);
				}
				
				this.room.messages.removeFirst();
			}
			
			try {
				sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
