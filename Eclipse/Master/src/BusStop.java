public class BusStop implements Comparable {
	private int latitude;
	private int longitude;
	
	private String stopCode;
	private String humanReadable;
	
	private int distance;
	private int timeUntilNextBus;
	
	public BusStop(int latitude, int longitude, String stopCode) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.stopCode = stopCode;
	}
	
	public BusStop(int latitude, int longitude, String stopCode, String humanReadable) {
		this(latitude, longitude, stopCode);
		
		this.humanReadable = humanReadable;
	}
	
	public int getLatitude() {
		return latitude;
	}
	
	public int getLongitude() {
		return longitude;
	}
	
	public String getStopCode() {
		return stopCode;
	}
	
	public int getDistance() {
		return distance;
	}
	
	public void setDistance(int dist) {
		distance = dist;
	}
	
	public int getTimeUntilNextBus() {
		return timeUntilNextBus;
	}
	
	public void setTimeUntilNextBus(int time) {
		timeUntilNextBus = time;
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof BusStop) {
			BusStop busStop = (BusStop)o;
			
			int timeDifference = timeUntilNextBus - busStop.getTimeUntilNextBus();
			if (timeDifference != 0) {
				return timeDifference;
			}
			
			return distance - busStop.getDistance();
		}
		
		return 1;
	}
	
	public String toString() {
		return humanReadable+" ("+latitude+", "+longitude+") at distance "+distance+" with "+timeUntilNextBus+" time until the next bus.";
	}
}
