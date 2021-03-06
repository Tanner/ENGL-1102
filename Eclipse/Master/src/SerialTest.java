import java.io.InputStream;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 
import java.util.Enumeration;

/**
 * Credit to http://www.arduino.cc/playground/Interfacing/Java
 */
public class SerialTest implements SerialPortEventListener {
	
	SerialPort serialPort;
	private static final String PORT_NAMES[] = { 
			"/dev/cu.usbserial-A900adLk", // Mac OS X
			"/dev/ttyUSB0", // Linux
			"COM3", // Windows
			};
	private InputStream input;
	private OutputStream output;
	private static final int TIME_OUT = 2000;
	private static final int DATA_RATE = 115200;
	
	private static final String START_DELIM = "ENGL1102&";
	
	private static final boolean SIMULATION = true;
	private static final boolean NORTH_SIMULATION = true;
	
	private static final int SIMULATED_HEADING = 90;
	
	private static final double SIMULATED_LAT = 33.773583;
	private static final double SIMULATED_LONG = -84.396066;

	public static void main(String[] args) throws Exception {
		SerialTest main = new SerialTest();
		main.initialize();
		System.out.println("Started");
		Thread.sleep(1500);
	}	

	public void initialize() {
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		// iterate through, looking for the port
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}

		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				int optimalHeading = 0;
				
				int available = input.available();
				byte chunk[] = new byte[available];
				input.read(chunk, 0, available);

				String serialString = new String(chunk);
				System.out.println("Received: "+serialString);
				
				String[] split = serialString.split(";+");
				if (split[0].equals(START_DELIM) && split.length >= 3) {
					System.out.println("Got data!");
					//We're in our string so shoot!
					try {
						System.out.println("1: "+split[1]+" 2: "+split[2]);
						double latitude = Integer.valueOf(split[1]) / 100000;
						double longitude = Integer.valueOf(split[2]) / 100000;
						System.out.println("("+latitude+", "+longitude+")");
						
						if (SIMULATION && !NORTH_SIMULATION) {
							optimalHeading = DataScrape.getOptimumBusDirection(SIMULATED_LAT, SIMULATED_LONG);
						} else if (SIMULATION && NORTH_SIMULATION) {
							optimalHeading = SIMULATED_HEADING;
						} else {
							optimalHeading = DataScrape.getOptimumBusDirection(latitude, longitude);
						}
					} catch (Exception e) {
						e.printStackTrace();
						output.write(-1);
					}
				} else if (SIMULATION && NORTH_SIMULATION) {
					optimalHeading = SIMULATED_HEADING;
				} else if (SIMULATION && !NORTH_SIMULATION) {
					optimalHeading = DataScrape.getOptimumBusDirection(SIMULATED_LAT, SIMULATED_LONG);
				}
				
				System.out.println("Sending... "+optimalHeading);
				output.write(optimalHeading);
			} catch (Exception e) {
				System.err.println(e.toString());
			}
			System.out.println("=====================");
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}
}