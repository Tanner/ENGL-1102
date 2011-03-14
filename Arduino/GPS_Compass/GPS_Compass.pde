#include <Wire.h>
#include <NewSoftSerial.h>
#include <TinyGPS.h>

int HMC6352Address = 0x42;
int slaveAddress, headingValue;
byte headingData[2];

#define RXPIN 2
#define TXPIN 3
#define GPSBAUD 4800

TinyGPS gps;
NewSoftSerial uart_gps(RXPIN, TXPIN);

//VCC = +5
//GND = GND
//SCL = Analog 5
//SDA = Analog 4

void getgps(TinyGPS &gps);

//Compass code from
//http://forum.sparkfun.com/viewtopic.php?t=6236
//http://bildr.org/2011/01/hmc6352/
//http://www.recombine.net/blog/article/49/hmc6352-sparkfun-compass-and-arduino
void setup()
{
  // Shift the device's documented slave address (0x42) 1 bit right
  // This compensates for how the TWI library only wants the
  // 7 most significant bits (with the high bit padded with 0)
  slaveAddress = HMC6352Address >> 1;
  
  Serial.begin(115200);
  
  uart_gps.begin(GPSBAUD);
  Wire.begin();
}

void loop()
{ 
  // Send a "A" command to the HMC6352
  // This requests the current heading data
  Wire.beginTransmission(slaveAddress);
  Wire.send("A");              // The "Get Data" command
  Wire.endTransmission(); 
  delay(10);                   // The HMC6352 needs at least a 70us (microsecond) delay after this command.  Using 10ms just makes it safe
  
  // Read the 2 heading bytes, MSB first
  // The resulting 16bit word is the compass heading in 10th's of a degree
  Wire.requestFrom(slaveAddress, 2);        // Request the 2 byte heading (MSB comes first)
  int i = 0;
  while(Wire.available() && i < 2) 
  {  
    headingData[i] = Wire.receive();
    i++;
  }
  
  while(uart_gps.available())     // While there is data on the RX pin...
  {
      int c = uart_gps.read();    // load the data into a variable...
      if(gps.encode(c))      // if there is a new valid sentence...
      {
        Serial.println(c);
        getgps(gps);         // then grab the data.
      }
  }

  headingValue = headingData[0]*256 + headingData[1];  // Put the MSB and LSB together
  Serial.print("Current heading: ");
  Serial.print(int (headingValue / 10));     // The whole number part of the heading
  Serial.print(".");
  Serial.print(int (headingValue % 10));     // The fractional part of the heading
  Serial.println(" degrees");
  
  delay(500);
}

// The getgps function will get and print the values we want.
void getgps(TinyGPS &gps)
{
  // To get all of the data into varialbes that you can use in your code, 
  // all you need to do is define variables and query the object for the 
  // data. To see the complete list of functions see keywords.txt file in 
  // the TinyGPS and NewSoftSerial libs.
  
  // Define the variables that will be used
  long latitude, longitude;
  // Then call this function
  gps.get_position(&latitude, &longitude);
  // You can now print variables latitude and longitude
  Serial.print("Lat/Long: "); 
  Serial.print(latitude); 
  Serial.print(", "); 
  Serial.println(longitude);
}
