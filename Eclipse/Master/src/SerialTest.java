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
				int available = input.available();
				byte chunk[] = new byte[available];
				input.read(chunk, 0, available);

				String serialString = new String(chunk);
				
				String[] split = serialString.split(";+");
				if (split[0].equals(START_DELIM) && split.length >= 3) {
					//We're in our string so shoot!
					double latitude = Integer.valueOf(split[1]) / 100000;
					double longitude = Integer.valueOf(split[2]) / 100000;
					System.out.println("("+latitude+", "+longitude+")");
					
					if (output != null) {
						output.write(DataScrape.getOptimumBusDirection(latitude, longitude));
					}
				}
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}
}