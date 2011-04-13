#include <Wire.h>
#include <NewSoftSerial.h>
#include <TinyGPS.h>

#include "WProgram.h"
void setup();
void loop();
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

long latitude, longitude;

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
        gps.get_position(&latitude, &longitude);
      }
  }

  headingValue = headingData[0]*256 + headingData[1];  // Put the MSB and LSB together
  Serial.print("Lat/Long: "); 
  Serial.print(latitude); 
  Serial.print(", "); 
  Serial.println(longitude);
  
  Serial.print("Current heading: ");
  Serial.print(int (headingValue / 10));     // The whole number part of the heading
  Serial.print(".");
  Serial.print(int (headingValue % 10));     // The fractional part of the heading
  Serial.println(" degrees");
  
  delay(500);
}

int main(void)
{
	init();

	setup();
    
	for (;;)
		loop();
        
	return 0;
}

