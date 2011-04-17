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
		new BusStop(33.778327, -84.404215, "fitten", "Fitten Hall"),
		new BusStop(33.779629, -84.404107, "mcm8th", "McMillian St & 8th St"),
		new BusStop(33.779584, -84.402637, "8thhemp", "8th St & Hemphill Ave"),
		new BusStop(33.778452, -84.400781, "fershemrt", "Ferst Dr & Hemphill Ave"),
		new BusStop(33.778247, -84.399505, "fersstmrt", "Ferst Dr & State St"),
		new BusStop(33.778202, -84.397477, "fersatmrt", "Ferst Dr & Atlantic Dr"),
		new BusStop(33.777114, -84.395503, "ferschmrt", "Ferst Dr & Cherry St"),
		new BusStop(33.776864, -84.393797, "5thfowl", "Ferst Dr & Fowler St"),
		new BusStop(33.776668, -84.392166, "tech5th", "Techwood Dr & 5th St"),
		new BusStop(33.774902, -84.391992, "tech4th", "Techwood Dr & 4th St"),
		new BusStop(33.773707, -84.391994, "techbob", "Techwood Dr & Bobby Dodd Way"),
		new BusStop(33.771386, -84.392064, "technorth", "Techwood Dr & North Ave"),
		new BusStop(33.769984, -84.391547, "nortavea_a", "North Ave. Apartments"),
		new BusStop(33.772352, -84.395503, "ferstcher", "Cherry St & Ferst Dr"),
		new BusStop(33.773453, -84.399194, "centrstud", "Student Center"),
		new BusStop(33.775125, -84.402530, "765femrt", "CRC")
	};
	
	public static int getOptimumBusDirection(double gpsLatitude, double gpsLongitude) {
		for (BusStop stop : busStops) {
			stop.setTimeUntilNextBus(getBusUrgency(stop));
		}
		
		Arrays.sort(busStops);
		
		BusStop closestStop = busStops[0];
	
		return getBusDirection(gpsLatitude, gpsLongitude, closestStop);
	}

	public static int getBusDirection(double gpsLatitude, double gpsLongitude) {
		BusStop closestStop = getClosestStop(gpsLatitude, gpsLongitude);
		
		return getBusDirection(gpsLatitude, gpsLongitude, closestStop);
	}
	
	public static int getBusDirection(double gpsLatitude, double gpsLongitude, BusStop closestStop) {
		if (closestStop != null) {
			return (int)Math.atan(Math.abs(gpsLongitude - closestStop.getLongitude()) / Math.abs(gpsLatitude - closestStop.getLatitude()));
		}
		
		return -1;
	}

	private static BusStop getClosestStop(double gpsLatitude, double gpsLongitude) {
		BusStop closestStop = null;
		double closestStopDistance = Double.MAX_VALUE;

		for(int i = 1; i < busStops.length; i++) {
			double testDistance = calculateDistance(gpsLatitude, busStops[i].getLatitude(), gpsLongitude, busStops[i].getLongitude());
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

	public static double calculateDistance(double x1, double x2, double y1, double y2) {
		return Math.sqrt(((x1-x2)*(x1-x2)) + ((y1-y2)*(y1-y2)));
	}
}