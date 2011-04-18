#include <Wire.h>
#include <NewSoftSerial.h>
#include <TinyGPS.h>

int HMC6352Address = 0x42;
int slaveAddress, headingValue;
byte headingData[2];

#define RXPIN 2
#define TXPIN 3
#define GPSBAUD 4800

#define HUMAN_READABLE 0

int TOLERANCE = 10;

int leftMotor = 5;
int centerMotor = 6;
int rightMotor = 7;

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
  slaveAddress = HMC6352Address >> 1;
  
  pinMode(leftMotor, OUTPUT);
  pinMode(centerMotor, OUTPUT);
  pinMode(rightMotor, OUTPUT);
  
  //Self Test
  
  digitalWrite(leftMotor, 255);
  digitalWrite(centerMotor, 0);
  digitalWrite(rightMotor, 0);
  
  delay(1000);
  
  digitalWrite(leftMotor, 0);
  digitalWrite(centerMotor, 255);
  digitalWrite(rightMotor, 0);
  
  delay(1000);
  
  digitalWrite(leftMotor, 0);
  digitalWrite(centerMotor, 0);
  digitalWrite(rightMotor, 255);
  
  delay(1000);
  
  digitalWrite(leftMotor, 255);
  digitalWrite(centerMotor, 255);
  digitalWrite(rightMotor, 255);
  
  delay(1000);
  
  digitalWrite(leftMotor, 0);
  digitalWrite(centerMotor, 0);
  digitalWrite(rightMotor, 0);
  
  Serial.begin(115200);
  
  uart_gps.begin(GPSBAUD);
  Wire.begin();
}

void loop()
{
  // This requests the current heading data
  Wire.beginTransmission(slaveAddress);
  Wire.send("A"); //Get data
  Wire.endTransmission(); 
  delay(10);
  
  Wire.requestFrom(slaveAddress, 2);
  int i = 0;
  while(Wire.available() && i < 2) 
  {  
    headingData[i] = Wire.receive();
    i++;
  }
  
  while(uart_gps.available())
  {
      int c = uart_gps.read();
      if(gps.encode(c))
      {
        gps.get_position(&latitude, &longitude);
      }
  }

  headingValue = headingData[0]*256 + headingData[1];
  
  if (HUMAN_READABLE == 1) {
    Serial.print("Lat/Long: "); 
    Serial.print(latitude); 
    Serial.print(", "); 
    Serial.println(longitude);
    
    Serial.print("Current heading: ");
    Serial.print(int (headingValue / 10));     // The whole number part of the heading
    Serial.print(".");
    Serial.print(int (headingValue % 10));     // The fractional part of the heading
    Serial.println(" degrees");
  } else {
    Serial.print("ENGL1102&;");
    Serial.print(latitude);
    Serial.print(";");
    Serial.print(longitude);
    Serial.println(";;");
  }
  
  if (Serial.available()) {
    int desiredHeading = (byte)Serial.read();
    
    int difference = desiredHeading - headingValue;
    if (difference > TOLERANCE) {
      if (difference < 0) {
        digitalWrite(leftMotor, 255);
        digitalWrite(centerMotor, 0);
        digitalWrite(rightMotor, 0);
      } else {
        digitalWrite(leftMotor, 0);
        digitalWrite(centerMotor, 0);
        digitalWrite(rightMotor, 255);
      }
    }
  } else {
    digitalWrite(leftMotor, 0);
    digitalWrite(centerMotor, 255);
    digitalWrite(rightMotor, 0);
  }
  
  delay(500);
}
