/*     ARBAROEN - Arduino with Rotary Encoders For Car playback
 *      
 *  by juanitomaille
 *  
 */
 
 
 /*First rotary encoder in #GPIO 3 & 4*/
 #define inputA_1 3
 #define inputB_1 4
 
 
  /*second rotary encoder in #GPIO 6 & 7*/
 #define inputA_2 6
 #define inputB_2 7
 
  /*the switches button of the 2 rotary encoders*/
 #define inputSwitchMute 5
 #define inputSwitchHome 2
 
 
 int counter_1 = 0; 
 int state_1;
 int lastState_1;
 int counter_2 = 0; 
 int state_2;
 int lastState_2;
 int homeState;
 int muteState;
 
 void setup() { 
   pinMode (inputA_1,INPUT);
   pinMode (inputB_1,INPUT);
   pinMode (inputA_2,INPUT);
   pinMode (inputB_2,INPUT);
   pinMode (inputSwitchHome,INPUT);
   pinMode (inputSwitchMute,INPUT);
   digitalWrite(inputSwitchHome, HIGH);
   digitalWrite(inputSwitchMute, HIGH);
   
   Serial.begin (9600);
   // Reads the initial state of the inputA
   lastState_1 = digitalRead(inputA_1);
   lastState_2 = digitalRead(inputA_2);   
 } 
 void loop() { 
   
      homeState = digitalRead(inputSwitchHome); // Reads the "current" state of the inputSwitchHome
      muteState = digitalRead(inputSwitchMute); // Reads the "current" state of the inputSwitchMute
      state_1 = digitalRead(inputA_1); // Reads the "current" state of the inputA_1
      state_2 = digitalRead(inputA_2); // Reads the "current" state of the inputA_2
   
   //On press switch send "HOME" String   
   if (homeState == LOW) {
     Serial.print("HOME");
     delay(200);
   }
   
   // on Press other Switch sen "MUTE" string
   if (muteState == LOW) {
     Serial.print("MUTE");
     delay(300);
   }
   
   // If the previous and the current state of the inputA are different, that means a Pulse has occured
   if (state_1 != lastState_1  && lastState_1 == LOW){     
     // If the inputB state is different to the inputA state, that means the encoder is rotating clockwise
     if (digitalRead(inputB_1) != state_1) { 
       counter_1 ++;
       Serial.print("PLUS");
     } else {
       counter_1 --;
       Serial.print("MOINS");
     }
   delay(100);
   } 
   lastState_1 = state_1; // Updates the previous state of the inputA with the current state
   
   
    // same for 2nd rotary encoder
   if (state_2 != lastState_2  && lastState_2 == LOW){     
     // If the inputB state is different to the inputA state, that means the encoder is rotating clockwise
     if (digitalRead(inputB_2) != state_2) { 
       counter_2 ++;
       Serial.print("PLUS");
     } else {
       counter_2 --;
       Serial.print("MOINS");
     }
   delay(100);
   } 
   lastState_2 = state_2; // Updates the previous state of the inputA with the current state
 }
