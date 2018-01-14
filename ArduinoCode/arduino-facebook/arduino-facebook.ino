#include<LiquidCrystal.h>
int CONTRAST           = 90;
int BACKLIGHT          = 20;
unsigned long oldTime = 0;
int strLength         = 0;
char *substr;
//defining lcd pins
LiquidCrystal lcd(12, 11, 5, 4, 3, 2);
void setup() {
  analogWrite(6, CONTRAST);
  analogWrite(9, BACKLIGHT);
  Serial.begin(9600);
  Serial.setTimeout(50);
  lcd.begin(16,2);
}
void loop() {
    String text = Serial.readString();
    //Process only if the text received on serial is not empty
    if(text.length() > 0) {
       // Convert from String Object to String, so we can use strtok
      char buf[text.length()+1];
      strncpy(buf, text.c_str(), text.length());
      buf[text.length()] = 0;
      char *p = buf;
      char *str;
      Serial.print(buf);
      while ((str = strtok_r(p, "|", &p)) != NULL){
        /*
         *Print the parts on the display
         */
        printOnDisplay(str, strlen(str));
        /*
         *We need a little delay between print and clear
         */
        delay(500);
        lcd.clear();
        delay(500);
      }
    }
}
//function that helps at printing {str} on lcd
void printOnDisplay(char *str,  int n) {
  /*
   * If string is bigger thatn the screen,
    scroll it
  */
  if(n >= 15) {
      /*
       * First, print the first 15 chars,
      then scroll whenever a nea char is printed
      */
      substr = new char [15];
      strncpy(substr, str, 15);
      substr[15] = '\0';
      lcd.print(substr);
      /*
       * Small delay so we can read the first 15 chars
       * before animating
      */
      delay(300);
      /*Enable autoscroll*/
      lcd.autoscroll();
      int i = 15;
      /*Print the rest of the chars*/
      while(i < n) {
        lcd.print(str[i]);
        delay(300);
        i++;
      }
       // turn off automatic scrolling
      lcd.noAutoscroll();
      delay(1300);
  } else {
    /*If string is smaller than lcd display,
      just print it
    */
    Serial.println("Smaller string");
    lcd.print(str);
    delay(1300);
  }
}
void clearDisplay() {
    lcd.clear();
}
