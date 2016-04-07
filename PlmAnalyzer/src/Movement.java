import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/*
 * Movement object
 * @version 2016_04_06
 * @author Jennifer Hunter
 */
public class Movement {
	  private final Date time; // the time of the start of the event
	  private final double str; // peak g-force of the event
	  private enum eventType { 
		  P, R, I, U 	// P = PLM, R = Rejected, I = Isolated, U = User Added
	  };
	  private final eventType type; // what type of event is it
	  private final double dur; // duration of the event
	  private final double interval; // time between end of previous event and this one
	  private final boolean down; // flag that the leg is down (< 65 degrees from horizontal)
	  private final String reason; // reason for rejection
	  
	  Movement(Date time, String event, double str, double dur, double interval, boolean down, String reason) {
		  this.time=time;
		  type = eventType.valueOf(event);
		  this.str=str;
		  this.dur = dur;
		  this.interval = interval;
		  this.down = down;
		  this.reason = reason;
	  }
	  
	  /*
	   * @returns the time of the movement
	   */
	  public Date getTime() {
		  return time;
	  }
	  
	  /*
	   * @returns the strength of the movement (G-Force)
	   */
	  public double getStr() {
		  return str;
	  }
	  
	  /*
	   * @returns the type of the movement as a String (P, I, R, U))
	   */
	  public String getType() {
		  return type.toString();
	  }
	  
	  /*
	   * @returns the duration of the movement
	   */
	  public double getDur() {
		  return dur;
	  }
	  
	  /*
	   * @returns the interval between this movement
	   * and the previous one in the series
	   */
	  public double getInterval() {
		  return interval;
	  }
	  
	  /*
	   * @returns if the movement is from a leg down position
	   * (<65 degrees from horizontal)
	   */
	  public String getDown() {
		  if (down){
		  	return "t";
		  } else {
			return "f";
		  }
	  }
	  
	  /*
	   * @returns the rejection reason
	   */
	  public String getReason() {
		  return reason;
	  }
	  
	  /*
	   * Determines if two movement objects are equal
	   * (non-Javadoc)
	   * @see java.lang.Object#equals(java.lang.Object)
	   */
	  @Override
	  public boolean equals(Object obj) {
		  // Check if object being compared to is null
	      if (obj == null) {
	          return false;
	      }
	      // Check if object being compared to is a Movement object
	      if (!Movement.class.isAssignableFrom(obj.getClass())) {
	          return false;
	      }
	      // Check if any of the main variables are different
	      final Movement other = (Movement) obj;
	      if ((this.time != other.time) || (this.str != other.str)) {
	          return false;
	      }
	      if ((this.type != other.type) || (this.dur != other.dur)) {
	          return false;
	      }
	      if ((this.interval != other.interval) || (this.down != other.down)) {
	          return false;
	      }
	      // Return that the objects are equal
	      return true;
	  }
	  
	  /*
	   * Computes a hashcode for a movement object
	   * (non-Javadoc)
	   * @see java.lang.Object#hashCode()
	   */
	  @Override
	  public int hashCode(){
		  // I pretty much made this up.
		  
	      return ((11 * Objects.hashCode(time) + 13 * Objects.hashCode(str)
	      	+ 3 * Objects.hashCode(type) + 5 * Objects.hashCode(dur)
	      	+ 7 * Objects.hashCode(interval) + 23 * Objects.hashCode(down))
	    	+ 17);
	  }
	  
	  /* Returns the string representation of this Movement.
	   */
	   @Override
	   public String toString() {
		   // Format for dates on x-axis
		   SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		   String inv = (this.getInterval() == Double.POSITIVE_INFINITY)?"inf ": String.format("%.3f", this.getInterval());
		   String ret = String.format("%s,%s,%.2f,%.3f,%s,%s,%s", String.format(dateFormat.format(this.getTime())), 
				   this.getType(), this.getStr(), this.getDur(), inv, this.getDown() , this.getReason());
	       return ret;
	   }
}
