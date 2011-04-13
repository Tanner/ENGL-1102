#include "WProgram.h"
void setup();
void loop();
int left = 13;
int back = 12;
int right = 11;

// The setup() method runs once, when the sketch starts

void setup()   {                
  // initialize the digital pin as an output:
  pinMode(left, OUTPUT);
  pinMode(back, OUTPUT);    
  pinMode(right, OUTPUT);    
}

// the loop() method runs over and over again,
// as long as the Arduino has power

void loop()                     
{
  int randomNum = random(1, 4);
  int delayTime = 250;
  
  digitalWrite(randomNum + 10, HIGH);
  delay(delayTime);
  digitalWrite(randomNum + 10, LOW);
  
  delay(random(250, 300));
}

int main(void)
{
	init();

	setup();
    
	for (;;)
		loop();
        
	return 0;
}

