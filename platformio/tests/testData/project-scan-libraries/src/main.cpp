#include <Arduino.h>
#include "confusing-name.h"
#include "confusing-name-no-src.h"
#include "confusing-name-nested-src.h"
void setup() {
    checkStandardLocation();
    checkNoSrc();
    checkNestedSrc();
    checkNestedSrc2();
}

void loop() {

// write your code here
}