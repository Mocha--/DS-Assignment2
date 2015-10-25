/**
 * log class which can print into console
 */
public class Log {
	
	public Log(){
		
	}
	
	public void write(String s){
		System.out.println("[System] " + s);
	}
	
	public void msg(String id, String msg){
		System.out.println(id + " : " + msg);
	}
}
