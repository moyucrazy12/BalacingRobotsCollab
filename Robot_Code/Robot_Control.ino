#include "I2Cdev.h"
#include "MPU6050.h"
#include "Wire.h"


long int last;
long int now;


#define ENCODER_PIN_A 2
#define ENCODER_PIN_B 3
#define OUTPUT_PIN_FORWARD 4
#define OUTPUT_PIN_BACKWARD 5
#define PULSE_PER_REV 720
//#define h 10
#define h 30


volatile int encoderPos=0;
volatile unsigned long lastPulseTime=0;
float angularVelocity = 0;
bool start = false;
double angleRef,refAmp = 0,n = 2,pitchRef = 1.6,biggestTEST =0; //pitchRef = 1.64 bot 1, pitchRef = 1.6 bot 2 (blue batteries)

MPU6050 MPU;

double x1,x2,x3,x4;
float u;
int AB, ABnew,count,varannan;
float lowestPitch=0;

//h = 25
//double K11 = -17.3056;
//double K12 = -99.9978 ;
//double K13 = -1.5438 ;
//double K14 = 1.5;
//double K14 = 0;

//h = 50
//double K11 = -6.9919;
//double K12 = -39.3957 ;
//double K13 = -0.7017 ;
//double K14 = -0.1;
//double K14 = 0;

//h = 55 0.75
//double K11 = -15.3927;
//double K12 = -36.0360;
//double K13 = -0.6528 ;
//double K14 = -0.3043;
//double K14 = 0;

//h = 45 0.75
/*double K11 = -20.8016;
double K12 = -150.9357;
double K13 = -0.7679;
double K14 = 0.5577;
*/
//double K14 = 0;

        

//h = 30 0.75
double K11 = -13.1152;
double K12 = -74.4038;
double K13 = -1.2016;
double K14 = 0.5;
//double K14 = 0;



int ax, ay, az;
int gx, gy, gz;
long tiempo_prev;
float dt;
float e4=0;
float ang_x, ang_y;
float ang_x_prev, ang_y_prev;
long time1;

double R_alpha = 0.0;

char x1_str[11];
char x2_str[11];
char x3_str[11];
char x4_str[11];
char u_str[11];

int encoder(){
  int a = digitalRead(ENCODER_PIN_A);
  int b = digitalRead(ENCODER_PIN_B);

  if(a==0 & b==0){
    return 0;
  } else if (a==0 & b==1){
    return 1;
  } else if( a==1 & b==0){
    return 2;
  } else {
    return 3;
  }
}
float hast(){

  ABnew=encoder();
  int temp = encoderPos;
  
  switch(ABnew){
    case 0: if(AB==2) encoderPos++; else encoderPos--; break;
    case 1: if(AB==0) encoderPos++; else encoderPos--; break;
    case 2: if(AB==3) encoderPos++; else encoderPos--; break;
    case 3: if(AB==1) encoderPos++; else encoderPos--; break;
  }

   AB=ABnew;
   
   if(encoderPos == temp+1){
      count++;
   }else{
      count--;
   }
  
}

float limit(double value, double uppLim, double lowLim){
  if(value>uppLim){
    value=uppLim;
  } else if(value<lowLim){
    value=lowLim;
  }
  return value;
}

void setup() {
  Serial.begin(9600); //Iniciando puerto serial
  Wire.begin(); //Iniciando I2C
  MPU.initialize(); //Iniciando el sensor
  if (MPU.testConnection()) Serial.println("Sensor iniciado   correctamente");
  else Serial.println("Error al iniciar el sensor");

  time1=millis();
  pinMode(ENCODER_PIN_A,INPUT_PULLUP);
  pinMode(ENCODER_PIN_B,INPUT_PULLUP);

  attachInterrupt(digitalPinToInterrupt(ENCODER_PIN_A), hast, CHANGE);
  attachInterrupt(digitalPinToInterrupt(ENCODER_PIN_B), hast, CHANGE);
  lastPulseTime = micros();
  AB=encoder();

  delay(1000);
  last = millis();
}
void loop() {
  long time = millis(); //Loopen verkar bara köra var 80ms trots h=10ms
  //Serial.println(time);
  now = time;
  
  
  float angle = 2 * 3.142 * encoderPos/PULSE_PER_REV; 
  float anglespeed = ((count * 2 * 3.142*1000)/h)/PULSE_PER_REV;
  count=0;
  
  MPU.getAcceleration(&ax, &ay, &az);
  MPU.getRotation(&gx, &gy, &gz);
  dt = (millis()-tiempo_prev)/1000.0;
  tiempo_prev=millis();
  
//  float accel_ang_x=atan(ay/sqrt(pow(ax,2) + pow(az,2)))*(180.0/3.14);
 // float accel_ang_x=atan(ay/sqrt(pow(ax,2) + pow(az,2)));
  float accel_ang_x=atan(ay);
  ang_x = 0.98*(ang_x_prev+(gx*(250.0/32768.0*3.14/180.0))*dt) + 0.02*accel_ang_x;
  ang_x_prev=ang_x;
  

  float pitch = ang_x+pitchRef; //works  1.63 (drift towrad battery)

  if (pitch < lowestPitch){
    lowestPitch = pitch;
  }
  float pitchspeed = (gx*(250.0/32768.0*3.14/180.0))+0.05;

  if(millis()>time1+5000){
//    Serial.print("x1/pitchspeed: ");
//    Serial.print(pitchspeed,4);  
//    Serial.print("\tx2/pitch: ");   //Pitch error at values less than -0.05
//    Serial.print(pitch,4);
//    Serial.print("\tx3/anglespeed: ");
//    Serial.print(anglespeed,4); 
//    Serial.print("\tx4/angle: ");
//    Serial.print((angleRef-angle),4);



    x1=pitchspeed;
    x2=pitch;
    x3=anglespeed;
    x4=angle;
   
    angleRef = 2*3.14*refAmp*(sin(n*(2*3.14/60)*(float)(time-time1-5000)/1000)); //reference generator with omega 1 lap per 10 seconds. Amplitude (-RefAmp)<->RefAmp
//    float e4 = angleRef-x4;
    e4=R_alpha;
    u = -K11*x1 -K12*x2 -K13*x3 -K14*limit(e4,1,-1); 

//    Serial.print("\tAngle ref: ");
//    Serial.print(angleRef);

//    Serial.print("\tAngle diff: ");
//    Serial.print(e4);


    
    //Serial.print("\tu before: ");
    //Serial.print(u);
  //  u = limit(u, 4.9, -4.9); 
  //  u = limit(u, 255, -250);
    
    //Output
//    u = (float)(map(u,-5.0,5.0,-255.0,255.0));
    u = (int)(map(u,-5,5,-255,255));
    //u = (int)(u-(-5.0))*(255.0-(-255.0))/(5.0-(-5.0))+(-255.0);
    u = limit(u, 255, -255);

    //Serial.print("\tu before: ");
    //Serial.print(u);
    if(u >= 0 ){
      analogWrite(OUTPUT_PIN_FORWARD,0);
      analogWrite(OUTPUT_PIN_BACKWARD,u);
    } else if (u < 0){
      analogWrite(OUTPUT_PIN_FORWARD,-u);
      analogWrite(OUTPUT_PIN_BACKWARD,0);
    } else {
      analogWrite(OUTPUT_PIN_FORWARD,0);
      analogWrite(OUTPUT_PIN_BACKWARD,0);
    }
//    Serial.print("\tu new: ");
//    Serial.println(u); 

//Bluetooth: Convert float to string
    dtostrf(x1*180/3.1416, -5, 1, x1_str);
    dtostrf(x2*180/3.1416, -5, 1, x2_str);
    //dtostrf(x3, -3, 1, x3_str);
    dtostrf(x4, -6, 1, x4_str);
    dtostrf(u, -4, 0, u_str);
    
    
    
//Bluetooth: Send data
    Serial.print("+");
    Serial.print(x1_str);
    Serial.print("/");
    Serial.print(x2_str);
//    Serial.write("\n");
    //Serial.print("/");
    //Serial.print(x3_str);
    Serial.print("/");
    Serial.print(x4_str);
    Serial.print("/");
    Serial.println(u_str);
  }

//Bluetooth: Receive data
  if (Serial.available() > 0) {
    String ReceivedByte = Serial.readString();
    R_alpha = ReceivedByte.toFloat();
    Serial.println(R_alpha);
  }
  //Serial.println(lowestPitch);
  //Serial.print(now - last);
  
//  if((now-last) > biggestTEST){
//    biggestTEST = (now - last);
//  }
//  Serial.println(biggestTEST);
  
  last = now;
  

  //delay(20);
  
}
