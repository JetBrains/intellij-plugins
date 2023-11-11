#include <Arduino.h>

extern "C" {
void nested();
void nested_nested();
}
void forced_included();

void setup() {
    nested();
    nested_nested();
    forced_included();
}

void loop() {

}