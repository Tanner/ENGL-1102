public class BusStop implements Comparable {
	private double latitude;
	private double longitude;
	
	private String stopCode;
	private String humanReadable;
	
	private double distance;
	private int timeUntilNextBus;
	
	public BusStop(double latitude, double longitude, String stopCode) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.stopCode = stopCode;
	}
	
	public BusStop(double latitude, double longitude, String stopCode, String humanReadable) {
		this(latitude, longitude, stopCode);
		
		this.humanReadable = humanReadable;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public String getStopCode() {
		return stopCode;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public void setDistance(double dist) {
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
			
			return (int)(distance - busStop.getDistance());
		}
		
		return 1;
	}
	
	public String toString() {
		return humanReadable+" ("+latitude+", "+longitude+") at distance "+distance+" with "+timeUntilNextBus+" time until the next bus.";
	}
}
