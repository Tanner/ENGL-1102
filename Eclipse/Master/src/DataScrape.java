import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;
import java.io.BufferedReader;

import org.jsoup.Jsoup;

/**
 * Class for Data Scrapping the bus stop data.
 * @author Andrew Dehn
 * @author Tanner Smith
 */
public class DataScrape {
	private static final String BASE_URL = "http://www.nextbus.com/predictor/adaPrediction.jsp?a=georgia-tech&r=red&d=Clockwise&s=";
	private static BusStop[] busStops = {
		new BusStop(0, 0, "fitten", "Fitten Hall"),
		new BusStop(0, 0, "mcm8th", "McMillian St & 8th St"),
		new BusStop(0, 0, "8thhemp", "8th St & Hemphill Ave"),
		new BusStop(0, 0, "fershemrt", "Ferst Dr & Hemphill Ave"),
		new BusStop(0, 0, "fersstmrt", "Ferst Dr & State St"),
		new BusStop(0, 0, "fersatmrt", "Ferst Dr & Atlantic Dr"),
		new BusStop(0, 0, "ferschmrt", "Ferst Dr & Cherry St"),
		new BusStop(0, 0, "5thfowl", "Ferst Dr & Fowler St"),
		new BusStop(0, 0, "tech5th", "Techwood Dr & 5th St"),
		new BusStop(0, 0, "tech4th", "Techwood Dr & 4th St"),
		new BusStop(0, 0, "techbob", "Techwood Dr & Bobby Dodd Way"),
		new BusStop(0, 0, "technorth", "Techwood Dr & North Ave"),
		new BusStop(0, 0, "ferstcher", "Cherry St & Ferst Dr"),
		new BusStop(0, 0, "centrstud", "Student Center"),
		new BusStop(0, 0, "765femrt", "CRC")
	};
	
	public static int getOptimumBusDirection(int x, int y) {
		for (BusStop stop : busStops) {
			stop.setTimeUntilNextBus(getBusUrgency(stop));
		}
		
		Arrays.sort(busStops);
		
		BusStop closestStop = busStops[0];
	
		return getBusDirection(x, y, closestStop);
	}

	public static int getBusDirection(int gpsX, int gpsY) {
		BusStop closestStop = getClosestStop(gpsX, gpsY);
		
		return getBusDirection(gpsX, gpsY, closestStop);
	}
	
	public static int getBusDirection(int gpsX, int gpsY, BusStop closestStop) {
		if (closestStop != null) {
			return (int)Math.atan(Math.abs(gpsY - closestStop.getLongitude()) / Math.abs(gpsX - closestStop.getLatitude()));
		}
		
		return -1;
	}

	private static BusStop getClosestStop(int gpsX, int gpsY) {
		BusStop closestStop = null;
		int closestStopDistance = Integer.MAX_VALUE;

		for(int i = 1; i < busStops.length; i++) {
			int testDistance = calculateDistance(gpsX, busStops[i].getLatitude(), gpsY, busStops[i].getLongitude());
			if (testDistance < closestStopDistance) {
				closestStop = busStops[i];
				closestStopDistance = testDistance;
			}
		}
		
		return closestStop;
	}

	public static int getBusUrgency(BusStop stop) {
		if (stop == null) {
			return -1;
		}
		
		URL web = null;
		try {
			web = new URL(BASE_URL+stop.getStopCode());
		} catch (MalformedURLException e) {
//			e.printStackTrace();
		}
		
		BufferedReader in = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(web.openStream()));
		} catch (IOException e) {
//			e.printStackTrace();
		}
		
		Scanner scan = new Scanner(in);
		String webpage = "";
		
		while (scan.hasNext()) {
			webpage = webpage + scan.nextLine() + "\n";
		}

		if (webpage.equals("")) {
			return -1;
		}

		String cleanWebpage = Jsoup.parse(webpage).text();

		if (cleanWebpage != null) {	
			if (!(cleanWebpage.contains("No current prediction"))) {
				int index = cleanWebpage.indexOf("minute");
				index = index - 3;
				
				int urgency = Integer.parseInt(Character.toString(cleanWebpage.charAt(index)));
				
				try {
					urgency += 10 * Integer.parseInt(Character.toString(cleanWebpage.charAt(index - 1)));
				} catch (Exception e) {
					//Number is not a number.
				}
				
				return urgency;
			} else if (!(cleanWebpage.contains("No current prediction"))) {
				return 0;
			}
		}
		
		return -1;
	}

	public static int calculateDistance(int x1, int x2, int y1, int y2) {
		return ((int) Math.sqrt(((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2))));
	}
}