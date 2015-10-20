import java.security.Timestamp;
import java.util.Date;

/**
 * Black class
 * to record if a user is in the black list
 */
public class Black {
	
	/**
	 * the user's sesstion thread
	 */
	public SessionThread sessionThread;

	/**
	 * the time point when the user can join again
	 */
	public Date allowJoinTimePoint;
	
	/**
	 *
	 * @param  sessionThread      [the user's sesstion thread]
	 * @param  allowJoinTimePoint [the time point when the user can join again]
	 * @return                    [constructor]
	 */
	public Black(SessionThread sessionThread, Date allowJoinTimePoint){
		this.sessionThread = sessionThread;
		this.allowJoinTimePoint = allowJoinTimePoint;
	}
	
	/**
	 * public function which can judge if a user is allowed to join again
	 * @return [true if the user is permitted to join again, false if the user is not permitted]
	 */
	public boolean isAllowJoin(){
		Date now = new Date();
		if(now.after(this.allowJoinTimePoint)){
			return true;
		}else{
			return false;
		}
		
	}
}
