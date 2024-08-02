#include <Arduino.h>

#include "nothingA.h"
#include "nothingB.h"

extern "C" {
void nested();
void nested_nested();
}
void forced_included();
void doNothingC();

void setup() {
    nested();
    nested_nested();
    forced_included();
    doNothingA();
    doNothingB();
    doNothingC();
}

void loop() {

}