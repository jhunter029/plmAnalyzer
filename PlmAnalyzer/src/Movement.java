import java.util.Date;
import java.util.Objects;

/*
 * Movement object
 * @version 2016_03_02
 * @author Jennifer Hunter
 */
public class Movement {
	  private Date time; // the time of the start of the event
	  private double str; // peak g-force of the event
	  private enum eventType { 
		  P, R, I, U 	// P = PLM, R = Rejected, I = Isolated, U = User Added
	  };
	  private eventType type; // what type of event is it
	  private double dur; // duration of the event
	  private double interval; // time between end of previous event and this one
	  private boolean down; // flag that the leg is down (< 65 degrees from horizontal)
	  
	  Movement(Date time, double str) {
		  this.time=time;
		  this.str=str;
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
	  public boolean getDown() {
		  return down;
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
}
